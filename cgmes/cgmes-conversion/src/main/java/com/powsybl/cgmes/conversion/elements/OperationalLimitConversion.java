/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.*;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.powsybl.cgmes.conversion.Conversion.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class OperationalLimitConversion extends AbstractIdentifiedObjectConversion {

    private static final String LIMIT_TYPE = "limitType";
    private static final String OPERATIONAL_LIMIT = "Operational limit";
    private static final String OPERATIONAL_LIMIT_TYPE_NAME = "operationalLimitTypeName";
    private static final String OPERATIONAL_LIMIT_SUBCLASS = "OperationalLimitSubclass";
    private static final String OPERATIONAL_LIMIT_SET_ID = "OperationalLimitSet";
    private static final String OPERATIONAL_LIMIT_SET_NAME = "OperationalLimitSetName";
    private static final String PERMANENT_LIMIT = "Permanent Limit";
    private static final String TEMPORARY_LIMIT = "Temporary Limit";

    public OperationalLimitConversion(PropertyBag l, Context context) {
        super(CgmesNames.OPERATIONAL_LIMIT, l, context);
        limitSubclass = Objects.requireNonNull(p.getLocal(OPERATIONAL_LIMIT_SUBCLASS));

        // Limit can be associated to a Terminal or to an Equipment
        terminalId = l.getId("Terminal");
        equipmentId = l.getId("Equipment");

        Terminal terminal = null;
        if (isLoadingLimits()) {
            if (terminalId != null) {
                terminal = context.terminalMapping().findForFlowLimits(terminalId);
                if (terminal == null) {
                    // If it is a switch Terminal we are not able to convert it
                    CgmesTerminal cgmesTerminal = context.cgmes().terminal(terminalId);
                    if (cgmesTerminal != null && CgmesNames.SWITCH_TYPES.contains(cgmesTerminal.conductingEquipmentType())) {
                        // note that in bus-breaker import, the Switch may not exist in IIDM if it has same from/to buses.
                        notAssigned("Switch", cgmesTerminal.conductingEquipment());
                        return;
                    }
                    // If it is a boundary Terminal we are not able to convert it
                    Boundary boundary = context.terminalMapping().findBoundary(terminalId);
                    if (boundary != null) {
                        notAssigned(boundary.getBoundaryLine());
                        return;
                    }
                }
            }
            if (terminal != null) {
                checkAndCreateLimitsAdder(context.terminalMapping().number(terminalId), terminal.getConnectable());
            } else if (equipmentId != null) {
                // The equipment may be a Branch, a Boundary line, a Switch ...
                Identifiable<?> identifiable = context.network().getIdentifiable(equipmentId);
                checkAndCreateLimitsAdder(-1, identifiable);
            }
        } else if (isVoltageLimits()) {
            if (terminalId != null) {
                terminal = context.terminalMapping().findForVoltageLimits(terminalId);
            }
            setVoltageLevelForVoltageLimit(terminal);
        } else {
            notAssigned();
        }
    }

    private boolean isLoadingLimits() {
        return CgmesNames.ACTIVE_POWER_LIMIT.equals(limitSubclass)
                || CgmesNames.APPARENT_POWER_LIMIT.equals(limitSubclass)
                || CgmesNames.CURRENT_LIMIT.equals(limitSubclass);
    }

    private boolean isVoltageLimits() {
        return "VoltageLimit".equals(limitSubclass);
    }

    private void setVoltageLevelForVoltageLimit(Terminal terminal) {
        if (terminal != null) {
            vl = terminal.getVoltageLevel();
        } else if (equipmentId != null) {
            Identifiable<?> i = context.network().getIdentifiable(equipmentId);
            if (i == null) {
                // Happens in BusBranch when the voltage limit is linked to a busbarSection
                vl = context.network().getVoltageLevel(p.getId("EquipmentContainer"));
            } else if (i instanceof Injection<?> injection) {
                vl = injection.getTerminal().getVoltageLevel();
            }
        }
    }

    private void addProperties(OperationalLimitsGroup limitsGroup, String limitSetId, String limitSetName) {
        limitsGroup.setProperty(PROPERTY_OPERATIONAL_LIMIT_SET_RDFID, limitSetId);
        limitsGroup.setProperty(PROPERTY_OPERATIONAL_LIMIT_SET_NAME, limitSetName);
    }

    /**
     * Create the LoadingLimitsAdder for the given branch + side and the given limit set + subclass.
     * @param terminalNumber The side of the branch to which the OperationalLimit applies.
     * @param limitSubclass The subclass of the OperationalLimit.
     * @param limitSetId The id of the set containing the OperationalLimit.
     * @param limitSetName The name of the set containing the OperationalLimit.
     * @param b The branch to which the OperationalLimit applies.
     */
    private void createLimitsAdder(int terminalNumber, String limitSubclass, String limitSetId, String limitSetName, Branch<?> b, boolean addProperties) {
        if (terminalNumber == 1) {
            OperationalLimitsGroup limitsGroup = b.getOperationalLimitsGroup1(limitSetId).orElseGet(() -> {
                OperationalLimitsGroup newLimitsGroup = b.newOperationalLimitsGroup1(limitSetId);
                if (addProperties) {
                    addProperties(newLimitsGroup, limitSetId, limitSetName);
                }
                return newLimitsGroup;
            });
            olga1 = new OLGA(limitsGroup, context.limitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubclass));
        } else if (terminalNumber == 2) {
            OperationalLimitsGroup limitsGroup = b.getOperationalLimitsGroup2(limitSetId).orElseGet(() -> {
                OperationalLimitsGroup newLimitsGroup = b.newOperationalLimitsGroup2(limitSetId);
                if (addProperties) {
                    addProperties(newLimitsGroup, limitSetId, limitSetName);
                }
                return newLimitsGroup;
            });
            olga2 = new OLGA(limitsGroup, context.limitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubclass));
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Create the LoadingLimitsAdder for the given boundary line and the given limit set + subclass.
     * @param limitSubclass The subclass of the OperationalLimit.
     * @param limitSetId The set containing the OperationalLimit.
     * @param limitSetName The name of the set containing the OperationalLimit.
     * @param bl The boundary line to which the OperationalLimit applies.
     */
    private void createLimitsAdder(String limitSubclass, String limitSetId, String limitSetName, BoundaryLine bl) {
        OperationalLimitsGroup limitsGroup = bl.getOperationalLimitsGroup(limitSetId).orElseGet(() -> {
            OperationalLimitsGroup newLimitsGroup = bl.newOperationalLimitsGroup(limitSetId);
            addProperties(newLimitsGroup, limitSetId, limitSetName);
            return newLimitsGroup;
        });
        olga = new OLGA(limitsGroup, context.limitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubclass));
    }

    /**
     * Create the LoadingLimitsAdder for the given 3w-transformer + side and the given limit set + subclass.
     * @param terminalNumber The side of the transformer to which the OperationalLimit applies.
     * @param limitSubclass The subclass of the OperationalLimit.
     * @param limitSetId The set containing the OperationalLimit.
     * @param limitSetName The name of the set containing the OperationalLimit.
     * @param twt The 3w-transformer to which the OperationalLimit applies.
     */
    private void createLimitsAdder(int terminalNumber, String limitSubclass, String limitSetId, String limitSetName, ThreeWindingsTransformer twt) {
        if (terminalNumber == 1) {
            OperationalLimitsGroup limitsGroup = twt.getLeg1().getOperationalLimitsGroup(limitSetId).orElseGet(() -> {
                OperationalLimitsGroup newLimitsGroup = twt.getLeg1().newOperationalLimitsGroup(limitSetId);
                addProperties(newLimitsGroup, limitSetId, limitSetName);
                return newLimitsGroup;
            });
            olga1 = new OLGA(limitsGroup, context.limitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubclass));
        } else if (terminalNumber == 2) {
            OperationalLimitsGroup limitsGroup = twt.getLeg2().getOperationalLimitsGroup(limitSetId).orElseGet(() -> {
                OperationalLimitsGroup newLimitsGroup = twt.getLeg2().newOperationalLimitsGroup(limitSetId);
                addProperties(newLimitsGroup, limitSetId, limitSetName);
                return newLimitsGroup;
            });
            olga2 = new OLGA(limitsGroup, context.limitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubclass));
        } else if (terminalNumber == 3) {
            OperationalLimitsGroup limitsGroup = twt.getLeg3().getOperationalLimitsGroup(limitSetId).orElseGet(() -> {
                OperationalLimitsGroup newLimitsGroup = twt.getLeg3().newOperationalLimitsGroup(limitSetId);
                addProperties(newLimitsGroup, limitSetId, limitSetName);
                return newLimitsGroup;
            });
            olga3 = new OLGA(limitsGroup, context.limitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubclass));
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Create LoadingLimitsAdder(s) as per the given inputs.
     * If the inputs are inconsistent, no limit adder is created.
     * @param terminalNumber The side of the equipment to which the OperationalLimit applies.
     * @param identifiable The equipment to which the OperationalLimit applies.
     */
    private void checkAndCreateLimitsAdder(int terminalNumber, Identifiable<?> identifiable) {
        String limitSetId = p.getId(OPERATIONAL_LIMIT_SET_ID);
        String limitSetName = p.getLocal(OPERATIONAL_LIMIT_SET_NAME);
        limitSetName = limitSetName != null ? limitSetName : limitSetId;

        if (identifiable instanceof Branch) {
            checkAndCreateLimitsAdderBranch((Branch<?>) identifiable, terminalNumber, limitSetId, limitSetName);
        } else if (identifiable instanceof BoundaryLine bl) {
            createLimitsAdder(limitSubclass, limitSetId, limitSetName, bl);
        } else if (identifiable instanceof ThreeWindingsTransformer t3w) {
            checkAndCreateLimitsAdderThreeWindingsTransformers(t3w, terminalNumber, limitSetId, limitSetName);
        } else if (identifiable instanceof Switch) {
            Switch aswitch = context.network().getSwitch(equipmentId);
            notAssigned(aswitch);
        } else {
            notAssigned(identifiable);
        }
    }

    private void checkAndCreateLimitsAdderBranch(Branch<?> b, int terminalNumber, String limitSetId, String limitSetName) {
        if (terminalNumber == 1 || terminalNumber == 2) {
            createLimitsAdder(terminalNumber, limitSubclass, limitSetId, limitSetName, b, true);
        } else if (terminalNumber == -1 && b instanceof Line) {
            // Limits applied to the whole equipment == to both sides. Properties are stored only on side 1 limit set.
            createLimitsAdder(1, limitSubclass, limitSetId, limitSetName, b, true);
            createLimitsAdder(2, limitSubclass, limitSetId + "-1", limitSetName, b, false);
        } else {
            if (terminalNumber == -1 && b instanceof TwoWindingsTransformer) {
                context.ignored(limitSubclass, "Defined for Equipment TwoWindingsTransformer. Should be defined for one Terminal of Two");
            }
            notAssigned(b);
        }
    }

    private void checkAndCreateLimitsAdderThreeWindingsTransformers(ThreeWindingsTransformer t3w, int terminalNumber, String limitSetId, String limitSetName) {
        if (terminalNumber == 1 || terminalNumber == 2 || terminalNumber == 3) {
            createLimitsAdder(terminalNumber, limitSubclass, limitSetId, limitSetName, t3w);
        } else {
            if (terminalNumber == -1) {
                context.ignored(limitSubclass, "Defined for Equipment ThreeWindingsTransformer. Should be defined for one Terminal of Three");
            }
            notAssigned(t3w);
        }
    }

    @Override
    public boolean valid() {
        if (notAssigned) {
            // a "not assigned" log was already issued
            return false;
        }
        if (vl == null && olga == null
                && olga1 == null
                && olga2 == null
                && olga3 == null) {
            missing(String.format("Terminal %s or Equipment %s", terminalId, equipmentId));
            return false;
        }
        return true;
    }

    @Override
    public void convert() {
        double value = getNormalValueFromEQ(p);
        if (Double.isNaN(value)) {
            context.ignored(OPERATIONAL_LIMIT, "value is not defined");
            return;
        } else if (value <= 0) {
            context.ignored(OPERATIONAL_LIMIT, "value is <= 0");
            return;
        }
        if (vl != null) {
            convertVoltageLimit(value);
        } else {
            if (isPatl()) { // Permanent Admissible Transmission Loading
                convertPatl(value);
            } else if (isTatl()) { // Temporary Admissible Transmission Loading
                convertTatl(value);
            }
        }
    }

    // Cgmes 2.4.15 value is defined in EQ file
    // Cgmes 3.0 normalValue is defined in EQ file and value in SSH
    private static double getNormalValueFromEQ(PropertyBag p) {
        return p.asOptionalDouble("normalValue").orElse(p.asOptionalDouble("value").orElse(Double.NaN));
    }

    private void convertVoltageLimit(double value) {
        String operationalLimitId = p.getId(CgmesNames.OPERATIONAL_LIMIT);
        String limitTypeName = p.getLocal(OPERATIONAL_LIMIT_TYPE_NAME);
        String limitType = p.getLocal(LIMIT_TYPE);
        if (limitTypeName.equalsIgnoreCase("highvoltage") || "LimitTypeKind.highVoltage".equals(limitType)) {
            convertHighVoltageLimit(operationalLimitId, value);
        } else if (limitTypeName.equalsIgnoreCase("lowvoltage") || "LimitTypeKind.lowVoltage".equals(limitType)) {
            convertLowVoltageLimit(operationalLimitId, value);
        } else {
            notAssigned(vl);
        }
    }

    private void convertHighVoltageLimit(String operationalLimitId, double value) {
        addVoltageLimitIdProperty(vl, PROPERTY_OPERATIONAL_LIMIT_HIGH_VOLTAGE_LIMIT, operationalLimitId);
        if (value < vl.getLowVoltageLimit()) {
            context.ignored("HighVoltageLimit", "Inconsistent with low voltage limit (" + vl.getLowVoltageLimit() + "kV)");
        } else if (value < Stream.of(vl.getHighVoltageLimit(), getNormalVoltageLimitValue(vl, PROPERTY_NORMAL_VALUE_HIGH_VOLTAGE_LIMIT), Double.MAX_VALUE)
            .filter(v -> v != null && !Double.isNaN(v))
            .min(Comparator.naturalOrder())
            .orElseThrow()) {
            vl.setProperty(PROPERTY_NORMAL_VALUE_HIGH_VOLTAGE_LIMIT, String.valueOf(value));
        }
    }

    private void convertLowVoltageLimit(String operationalLimitId, double value) {
        addVoltageLimitIdProperty(vl, PROPERTY_OPERATIONAL_LIMIT_LOW_VOLTAGE_LIMIT, operationalLimitId);
        if (value > vl.getHighVoltageLimit()) {
            context.ignored("LowVoltageLimit", "Inconsistent with high voltage limit (" + vl.getHighVoltageLimit() + "kV)");
        } else if (value > Stream.of(vl.getLowVoltageLimit(), getNormalVoltageLimitValue(vl, PROPERTY_NORMAL_VALUE_LOW_VOLTAGE_LIMIT), Double.MIN_VALUE)
            .filter(v -> v != null && !Double.isNaN(v))
            .max(Comparator.naturalOrder())
            .orElseThrow()) {
            vl.setProperty(PROPERTY_NORMAL_VALUE_LOW_VOLTAGE_LIMIT, String.valueOf(value));
        }
    }

    public static void setNormalVoltageLimit(VoltageLevel voltageLevel, Context context) {
        Double highNormalLimit = getNormalVoltageLimitValue(voltageLevel, PROPERTY_NORMAL_VALUE_HIGH_VOLTAGE_LIMIT);
        Double lowNormalLimit = getNormalVoltageLimitValue(voltageLevel, PROPERTY_NORMAL_VALUE_LOW_VOLTAGE_LIMIT);
        if (highNormalLimit != null && lowNormalLimit != null && highNormalLimit < lowNormalLimit) {
            // Normal limits are inconsistent. Don't apply them and remove the properties.
            voltageLevel.removeProperty(PROPERTY_NORMAL_VALUE_HIGH_VOLTAGE_LIMIT);
            voltageLevel.removeProperty(PROPERTY_NORMAL_VALUE_LOW_VOLTAGE_LIMIT);
            context.ignored("high/low VoltageLimit",
                () -> String.format("low VoltageLimit (%f) is greater than high VoltageLimit (%f) in VoltageLevel %s.",
                    lowNormalLimit, highNormalLimit, voltageLevel.getId()));
        } else {
            if (highNormalLimit != null) {
                voltageLevel.setHighVoltageLimit(highNormalLimit);
            }
            if (lowNormalLimit != null) {
                voltageLevel.setLowVoltageLimit(lowNormalLimit);
            }
        }
    }

    private boolean isPatl() {
        String limitTypeName = p.getLocal(OPERATIONAL_LIMIT_TYPE_NAME);
        String limitType = p.getLocal(LIMIT_TYPE);
        return limitTypeName.equals("PATL") || "LimitTypeKind.patl".equals(limitType) || "LimitKind.patl".equals(limitType);
    }

    private boolean addPatl(double value, LoadingLimitsAdder<?, ?> adder) {
        if (Double.isNaN(adder.getPermanentLimit())) {
            adder.setPermanentLimit(value);
            return true;
        } else {
            if (terminalId != null) {
                context.fixed(PERMANENT_LIMIT, () -> String.format("Several permanent limits defined for Terminal %s. Only the lowest is kept.", terminalId));
            } else {
                context.fixed(PERMANENT_LIMIT, () -> String.format("Several permanent limits defined for Equipment %s. Only the lowest is kept.", equipmentId));
            }
            if (adder.getPermanentLimit() > value) {
                adder.setPermanentLimit(value);
                return true;
            }
        }
        return false;
    }

    private void convertPatl(double value) {
        String operationalLimitId = p.getId(CgmesNames.OPERATIONAL_LIMIT);

        if (olga != null && addPatl(value, olga.loadingLimitsAdder)) {
            addPermanentOperationalLimitProperties(olga.operationalLimitsGroup, operationalLimitId, value);
        } else {
            if (olga1 != null && addPatl(value, olga1.loadingLimitsAdder)) {
                addPermanentOperationalLimitProperties(olga1.operationalLimitsGroup, operationalLimitId, value);
            }
            if (olga2 != null && addPatl(value, olga2.loadingLimitsAdder)) {
                addPermanentOperationalLimitProperties(olga2.operationalLimitsGroup, operationalLimitId, value);
            }
            if (olga3 != null && addPatl(value, olga3.loadingLimitsAdder)) {
                addPermanentOperationalLimitProperties(olga3.operationalLimitsGroup, operationalLimitId, value);
            }
        }
    }

    private boolean isTatl() {
        String limitTypeName = p.getLocal(OPERATIONAL_LIMIT_TYPE_NAME);
        String limitType = p.getLocal(LIMIT_TYPE);
        return limitTypeName.equals("TATL") || "LimitTypeKind.tatl".equals(limitType) || "LimitKind.tatl".equals(limitType);
    }

    private boolean addTatl(String name, double value, int acceptableDuration, LoadingLimitsAdder<?, ?> adder) {
        if (Double.isNaN(value)) {
            context.ignored(TEMPORARY_LIMIT, "Temporary limit value is undefined");
            return false;
        }

        if (Double.isNaN(adder.getTemporaryLimitValue(acceptableDuration))) {
            adder.beginTemporaryLimit()
                    .setAcceptableDuration(acceptableDuration)
                    .setName(name)
                    .setValue(value)
                    .ensureNameUnicity()
                    .endTemporaryLimit();
            return true;
        } else {
            if (terminalId != null) {
                context.fixed(TEMPORARY_LIMIT, () -> String.format("Several temporary limits defined for same acceptable duration (%d s) for Terminal %s. Only the lowest is kept.", acceptableDuration, terminalId));
            } else {
                context.fixed(TEMPORARY_LIMIT, () -> String.format("Several temporary limits defined for same acceptable duration (%d s) for Equipment %s. Only the lowest is kept.", acceptableDuration, equipmentId));
            }
            if (adder.getTemporaryLimitValue(acceptableDuration) > value) {
                adder.beginTemporaryLimit()
                        .setAcceptableDuration(acceptableDuration)
                        .setName(name)
                        .setValue(value)
                        .ensureNameUnicity()
                        .endTemporaryLimit();
                return true;
            }
        }
        return false;
    }

    private void convertTatl(double value) {
        int acceptableDuration = p.containsKey("acceptableDuration") ? (int) p.asDouble("acceptableDuration") : Integer.MAX_VALUE;
        // We only accept high or absoluteValue (considered as high when
        // current from the conducting equipment to the terminal) limits
        String direction = p.getId("direction");

        // if there is no direction, the limit is considered as absoluteValue (cf. CGMES specification)
        if (direction == null || direction.endsWith("high") || direction.endsWith("absoluteValue")) {
            String operationalLimitId = p.getId(CgmesNames.OPERATIONAL_LIMIT);
            String name = Optional.ofNullable(p.getId("shortName")).orElse(p.getId("name"));

            addTemporaryLoadingLimits(name, value, acceptableDuration, operationalLimitId);
        } else if (direction.endsWith("low")) {
            context.invalid(TEMPORARY_LIMIT, () -> String.format("TATL %s is a low limit", id));
        } else {
            context.invalid(TEMPORARY_LIMIT, () -> String.format("TATL %s does not have a valid direction", id));
        }
    }

    private void addTemporaryLoadingLimits(String name, double value, int acceptableDuration, String operationalLimitId) {
        if (olga != null && addTatl(name, value, acceptableDuration, olga.loadingLimitsAdder)) {
            addTemporaryOperationalLimitProperties(olga.operationalLimitsGroup, acceptableDuration, operationalLimitId, value);
        } else {
            if (olga1 != null && addTatl(name, value, acceptableDuration, olga1.loadingLimitsAdder)) {
                addTemporaryOperationalLimitProperties(olga1.operationalLimitsGroup, acceptableDuration, operationalLimitId, value);
            }
            if (olga2 != null && addTatl(name, value, acceptableDuration, olga2.loadingLimitsAdder)) {
                addTemporaryOperationalLimitProperties(olga2.operationalLimitsGroup, acceptableDuration, operationalLimitId, value);
            }
            if (olga3 != null && addTatl(name, value, acceptableDuration, olga3.loadingLimitsAdder)) {
                addTemporaryOperationalLimitProperties(olga3.operationalLimitsGroup, acceptableDuration, operationalLimitId, value);
            }
        }
    }

    private void addTemporaryOperationalLimitProperties(OperationalLimitsGroup operationalLimitsGroup, int duration, String operationalLimitId, double value) {
        operationalLimitsGroup.setProperty(getOperationalLimitPropertyName(limitSubclass, false, duration, CgmesNames.OPERATIONAL_LIMIT), operationalLimitId);
        operationalLimitsGroup.setProperty(getOperationalLimitPropertyName(limitSubclass, false, duration, CgmesNames.NORMAL_VALUE), String.valueOf(value));
    }

    private void addPermanentOperationalLimitProperties(OperationalLimitsGroup operationalLimitsGroup, String operationalLimitId, double value) {
        operationalLimitsGroup.setProperty(getOperationalLimitPropertyName(limitSubclass, true, 0, CgmesNames.OPERATIONAL_LIMIT), operationalLimitId);
        operationalLimitsGroup.setProperty(getOperationalLimitPropertyName(limitSubclass, true, 0, CgmesNames.NORMAL_VALUE), String.valueOf(value));
    }

    private static void addVoltageLimitIdProperty(VoltageLevel voltageLevel, String propertyName, String operationalLimitId) {
        String operationalLimitIds = voltageLevel.hasProperty(propertyName)
                ? String.join(";", voltageLevel.getProperty(propertyName), operationalLimitId)
                : operationalLimitId;
        voltageLevel.setProperty(propertyName, operationalLimitIds);
    }

    private void notAssigned() {
        notAssigned(null);
    }

    private void notAssigned(Identifiable<?> eq) {
        notAssigned(eq != null ? className(eq) : "", eq != null ? eq.getId() : "");
    }

    private void notAssigned(String className, String id) {
        this.notAssigned = true;
        if (context.config().isLogUnassignedOperationalLimits()) {
            String type = p.getLocal(LIMIT_TYPE);
            String typeName = p.getLocal(OPERATIONAL_LIMIT_TYPE_NAME);
            String subclass = p.getLocal(OPERATIONAL_LIMIT_SUBCLASS);
            Supplier<String> reason = () -> String.format(
                    "Not assigned for %s %s. Limit id, type, typeName, subClass, terminal : %s, %s, %s, %s, %s",
                    className != null ? className : "",
                    id != null ? id : "",
                    id,
                    type,
                    typeName,
                    subclass,
                    terminalId);
            context.pending(OPERATIONAL_LIMIT, reason);
        }
    }

    private static String className(Identifiable<?> o) {
        String s = o.getClass().getName();
        int dot = s.lastIndexOf('.');
        if (dot >= 0) {
            s = s.substring(dot + 1);
        }
        s = s.replace("Impl", "");
        s = s.replace("DanglingLine", "DanglingLine at boundary side");
        return s;
    }

    protected static void update(VoltageLevel voltageLevel, Context context) {
        String highLimitPropertyName = voltageLevel.hasProperty(PROPERTY_NORMAL_VALUE_HIGH_VOLTAGE_LIMIT) ?
            PROPERTY_NORMAL_VALUE_HIGH_VOLTAGE_LIMIT : PROPERTY_HIGH_VOLTAGE_LIMIT;
        Double normalHighLimitValue = getNormalVoltageLimitValue(voltageLevel, highLimitPropertyName);
        double defaultHighLimitValue = getDefaultValue(normalHighLimitValue, voltageLevel.getHighVoltageLimit(), Double.NaN, Double.NaN, context);
        double highLimitValue = getUpdatedVoltageLimitValue(voltageLevel, PROPERTY_OPERATIONAL_LIMIT_HIGH_VOLTAGE_LIMIT, Double::min, defaultHighLimitValue, context);

        String lowLimitPropertyName = voltageLevel.hasProperty(PROPERTY_NORMAL_VALUE_LOW_VOLTAGE_LIMIT) ?
            PROPERTY_NORMAL_VALUE_LOW_VOLTAGE_LIMIT : PROPERTY_LOW_VOLTAGE_LIMIT;
        Double normalLowLimitValue = getNormalVoltageLimitValue(voltageLevel, lowLimitPropertyName);
        double defaultLowLimitValue = getDefaultValue(normalLowLimitValue, voltageLevel.getLowVoltageLimit(), Double.NaN, Double.NaN, context);
        double lowLimitValue = getUpdatedVoltageLimitValue(voltageLevel, PROPERTY_OPERATIONAL_LIMIT_LOW_VOLTAGE_LIMIT, Double::max, defaultLowLimitValue, context);

        if (highLimitValue < lowLimitValue) {
            // Values are inconsistent. Just use default values.
            context.ignored("high/low VoltageLimit",
                () -> String.format("low VoltageLimit (%f) is greater than high VoltageLimit (%f) in VoltageLevel %s.",
                    lowLimitValue, highLimitValue, voltageLevel.getId()));
            voltageLevel.setHighVoltageLimit(defaultHighLimitValue);
            voltageLevel.setLowVoltageLimit(defaultLowLimitValue);
        } else {
            // Values are consistent, but make sure to apply them in the correct order.
            if (highLimitValue < voltageLevel.getLowVoltageLimit()) {
                voltageLevel.setLowVoltageLimit(lowLimitValue);
                voltageLevel.setHighVoltageLimit(highLimitValue);
            } else {
                voltageLevel.setHighVoltageLimit(highLimitValue);
                voltageLevel.setLowVoltageLimit(lowLimitValue);
            }
        }
    }

    public static void update(OperationalLimitsGroup operationalLimitsGroup, Context context) {
        operationalLimitsGroup.getActivePowerLimits().ifPresent(activePowerLimits
                -> updateLoadingLimits(CgmesNames.ACTIVE_POWER_LIMIT, activePowerLimits, operationalLimitsGroup, context));
        operationalLimitsGroup.getApparentPowerLimits().ifPresent(apparentPowerLimits
                -> updateLoadingLimits(CgmesNames.APPARENT_POWER_LIMIT, apparentPowerLimits, operationalLimitsGroup, context));
        operationalLimitsGroup.getCurrentLimits().ifPresent(currentLimits
                -> updateLoadingLimits(CgmesNames.CURRENT_LIMIT, currentLimits, operationalLimitsGroup, context));
    }

    private static void updateLoadingLimits(String limitSubclass, LoadingLimits loadingLimits, OperationalLimitsGroup operationalLimitsGroup, Context context) {
        loadingLimits.setPermanentLimit(getValue(limitSubclass, true, 0, operationalLimitsGroup, loadingLimits.getPermanentLimit(), context));
        loadingLimits.getTemporaryLimits().forEach(temporaryLimit -> {
            int duration = temporaryLimit.getAcceptableDuration();
            loadingLimits.setTemporaryLimitValue(duration, getValue(limitSubclass, false, duration, operationalLimitsGroup, temporaryLimit.getValue(), context));
        });
    }

    private static double getValue(String limitSubclass, boolean isInfiniteDuration, int duration, OperationalLimitsGroup operationalLimitsGroup, double previousValue, Context context) {
        String operationalLimitId = getOperationalLimitId(getOperationalLimitPropertyName(limitSubclass, isInfiniteDuration, duration, CgmesNames.OPERATIONAL_LIMIT), operationalLimitsGroup);
        String propertyName = getOperationalLimitPropertyName(limitSubclass, isInfiniteDuration, duration, CgmesNames.NORMAL_VALUE);
        Double normalValue = operationalLimitsGroup.getProperty(propertyName) != null ? Double.parseDouble(operationalLimitsGroup.getProperty(propertyName)) : null;
        double defaultLimitValue = getDefaultValue(normalValue, previousValue, normalValue, normalValue != null ? normalValue : previousValue, context);
        return updatedValue(operationalLimitId, context).orElse(defaultLimitValue);
    }

    private static OptionalDouble updatedValue(String operationalLimitId, Context context) {
        PropertyBag p = context.operationalLimit(operationalLimitId);
        return (p == null) ? OptionalDouble.empty() : p.asOptionalDouble("value");
    }

    private static List<String> getOperationalLimitIds(VoltageLevel voltageLevel, String propertyName) {
        String ids = voltageLevel.getProperty(propertyName);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(ids.split(";")).toList();
    }

    private static String getOperationalLimitId(String propertyName, OperationalLimitsGroup operationalLimitsGroup) {
        return operationalLimitsGroup.getProperty(propertyName);
    }

    private static double getUpdatedVoltageLimitValue(VoltageLevel voltageLevel, String operationalLimitPropertyName,
                                                      DoubleBinaryOperator operator, double defaultValue, Context context) {
        // Retrieve the most restrictive updated limit inside the VoltageLevel high/lowVoltageLimit range.
        List<String> operationalLimitIds = getOperationalLimitIds(voltageLevel, operationalLimitPropertyName);
        Double voltageLevelHighLimit = getNormalVoltageLimitValue(voltageLevel, PROPERTY_HIGH_VOLTAGE_LIMIT);
        Double voltageLevelLowLimit = getNormalVoltageLimitValue(voltageLevel, PROPERTY_LOW_VOLTAGE_LIMIT);
        return operationalLimitIds.stream()
            .map(operationalLimitId -> updatedValue(operationalLimitId, context))
            .filter(OptionalDouble::isPresent)
            .mapToDouble(OptionalDouble::getAsDouble)
            .filter(value -> (voltageLevelHighLimit == null || value < voltageLevelHighLimit)
                && (voltageLevelLowLimit == null || value > voltageLevelLowLimit))
            .reduce(operator)
            .orElse(defaultValue);
    }

    public static Double getNormalVoltageLimitValue(VoltageLevel voltageLevel, String propertyName) {
        return voltageLevel.getProperty(propertyName) != null ? Double.parseDouble(voltageLevel.getProperty(propertyName)) : null;
    }

    private record OLGA(OperationalLimitsGroup operationalLimitsGroup, LoadingLimitsAdder<?, ?> loadingLimitsAdder) {
    }

    private final String terminalId;
    private final String equipmentId;
    private final String limitSubclass;

    private boolean notAssigned = false;

    private OLGA olga;
    private OLGA olga1;
    private OLGA olga2;
    private OLGA olga3;

    private VoltageLevel vl;
}
