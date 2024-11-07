/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;

import static com.powsybl.cgmes.conversion.Conversion.Config.DefaultValue.*;

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
    private static final String PROPERTY_PREFIX = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.OPERATIONAL_LIMIT_SET + "_";
    private static final String PERMANENT_LIMIT = "Permanent Limit";
    private static final String TEMPORARY_LIMIT = "Temporary Limit";

    public OperationalLimitConversion(PropertyBag l, Context context) {
        super(CgmesNames.OPERATIONAL_LIMIT, l, context);
        String limitSubclass = p.getLocal(OPERATIONAL_LIMIT_SUBCLASS);
        String limitSetId = p.getId(OPERATIONAL_LIMIT_SET_ID);
        String limitSetName = p.getLocal(OPERATIONAL_LIMIT_SET_NAME);
        limitSetName = limitSetName != null ? limitSetName : limitSetId;
        // Limit can be associated to a Terminal or to an Equipment
        terminalId = l.getId("Terminal");
        equipmentId = l.getId("Equipment");

        this.limitSubclassType = fixCim14LimitSubclass(limitSubclass);

        Terminal terminal = null;
        if (isLoadingLimits()) {
            if (terminalId != null) {
                terminal = context.terminalMapping().findForFlowLimits(terminalId);
            }
            if (terminal != null) {
                checkAndCreateLimitsAdder(context.terminalMapping().number(terminalId), limitSetId, limitSetName, terminal.getConnectable());
                this.loadingLimitsIdentifiable = terminal.getConnectable();
            } else if (equipmentId != null) {
                // The equipment may be a Branch, a Dangling line, a Switch ...
                Identifiable<?> identifiable = context.network().getIdentifiable(equipmentId);
                checkAndCreateLimitsAdder(-1, limitSetId, limitSetName, identifiable);
                this.loadingLimitsIdentifiable = identifiable;
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

    // Support for CIM14, all limits are assumed to be current
    private static String fixCim14LimitSubclass(String limitSubclass) {
        return limitSubclass == null ? CgmesNames.CURRENT_LIMIT : limitSubclass;
    }

    private boolean isLoadingLimits() {
        Objects.requireNonNull(limitSubclassType);
        return limitSubclassType.equals(CgmesNames.ACTIVE_POWER_LIMIT)
                || limitSubclassType.equals(CgmesNames.APPARENT_POWER_LIMIT)
                || limitSubclassType.equals(CgmesNames.CURRENT_LIMIT);
    }

    private boolean isVoltageLimits() {
        Objects.requireNonNull(limitSubclassType);
        return limitSubclassType.equals("VoltageLimit");
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

    /**
     * Create the LoadingLimitsAdder for the given branch + side and the given limit set + subclass.
     * @param terminalNumber The side of the branch to which the OperationalLimit applies.
     * @param limitSubclass The subclass of the OperationalLimit.
     * @param limitSetId The id of the set containing the OperationalLimit.
     * @param limitSetName The name of the set containing the OperationalLimit.
     * @param b The branch to which the OperationalLimit applies.
     */
    private void createLimitsAdder(int terminalNumber, String limitSubclass, String limitSetId, String limitSetName, Branch<?> b) {
        if (terminalNumber == 1) {
            OperationalLimitsGroup limitsGroup = b.getOperationalLimitsGroup1(limitSetId).orElseGet(() -> {
                b.setProperty(PROPERTY_PREFIX + limitSetId, limitSetName);
                return b.newOperationalLimitsGroup1(limitSetId); });
            loadingLimitsAdder1 = context.loadingLimitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubclass);
        } else if (terminalNumber == 2) {
            OperationalLimitsGroup limitsGroup = b.getOperationalLimitsGroup2(limitSetId).orElseGet(() -> {
                b.setProperty(PROPERTY_PREFIX + limitSetId, limitSetName);
                return b.newOperationalLimitsGroup2(limitSetId); });
            loadingLimitsAdder2 = context.loadingLimitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubclass);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Create the LoadingLimitsAdder for the given dangling line and the given limit set + subclass.
     * @param limitSubclass The subclass of the OperationalLimit.
     * @param limitSetId The set containing the OperationalLimit.
     * @param limitSetName The name of the set containing the OperationalLimit.
     * @param dl The branch to which the OperationalLimit applies.
     */
    private void createLimitsAdder(String limitSubclass, String limitSetId, String limitSetName, DanglingLine dl) {
        OperationalLimitsGroup limitsGroup = dl.getOperationalLimitsGroup(limitSetId).orElseGet(() -> {
            dl.setProperty(PROPERTY_PREFIX + limitSetId, limitSetName);
            return dl.newOperationalLimitsGroup(limitSetId); });
        loadingLimitsAdder = context.loadingLimitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubclass);
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
                twt.setProperty(PROPERTY_PREFIX + limitSetId, limitSetName);
                return twt.getLeg1().newOperationalLimitsGroup(limitSetId); });
            loadingLimitsAdder1 = context.loadingLimitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubclass);
        } else if (terminalNumber == 2) {
            OperationalLimitsGroup limitsGroup = twt.getLeg2().getOperationalLimitsGroup(limitSetId).orElseGet(() -> {
                twt.setProperty(PROPERTY_PREFIX + limitSetId, limitSetName);
                return twt.getLeg2().newOperationalLimitsGroup(limitSetId); });
            loadingLimitsAdder2 = context.loadingLimitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubclass);
        } else if (terminalNumber == 3) {
            OperationalLimitsGroup limitsGroup = twt.getLeg3().getOperationalLimitsGroup(limitSetId).orElseGet(() -> {
                twt.setProperty(PROPERTY_PREFIX + limitSetId, limitSetName);
                return twt.getLeg3().newOperationalLimitsGroup(limitSetId); });
            loadingLimitsAdder3 = context.loadingLimitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubclass);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Create LoadingLimitsAdder(s) as per the given inputs.
     * If the inputs are inconsistent, no limit adder is created.
     * @param terminalNumber The side of the equipment to which the OperationalLimit applies.
     * @param limitSetId The set containing the OperationalLimit.
     * @param limitSetName The name of the set containing the OperationalLimit.
     * @param identifiable The equipment to which the OperationalLimit applies.
     */
    private void checkAndCreateLimitsAdder(int terminalNumber, String limitSetId, String limitSetName, Identifiable<?> identifiable) {
        if (identifiable instanceof Line) {
            Branch<?> b = (Branch<?>) identifiable;
            if (terminalNumber == -1) {
                // Limits applied to the whole equipment == to both sides
                createLimitsAdder(1, limitSubclassType, limitSetId, limitSetName, b);
                createLimitsAdder(2, limitSubclassType, limitSetId, limitSetName, b);
            } else if (terminalNumber == 1 || terminalNumber == 2) {
                createLimitsAdder(terminalNumber, limitSubclassType, limitSetId, limitSetName, b);
            } else {
                notAssigned(b);
            }
        } else if (identifiable instanceof TwoWindingsTransformer) {
            Branch<?> b = (Branch<?>) identifiable;
            if (terminalNumber == 1 || terminalNumber == 2) {
                createLimitsAdder(terminalNumber, limitSubclassType, limitSetId, limitSetName, b);
            } else {
                if (terminalNumber == -1) {
                    context.ignored(limitSubclassType, "Defined for Equipment TwoWindingsTransformer. Should be defined for one Terminal of Two");
                }
                notAssigned(b);
            }
        } else if (identifiable instanceof DanglingLine dl) {
            createLimitsAdder(limitSubclassType, limitSetId, limitSetName, dl);
        } else if (identifiable instanceof ThreeWindingsTransformer twt) {
            if (terminalNumber == 1 || terminalNumber == 2 || terminalNumber == 3) {
                createLimitsAdder(terminalNumber, limitSubclassType, limitSetId, limitSetName, twt);
            } else {
                if (terminalNumber == -1) {
                    context.ignored(limitSubclassType, "Defined for Equipment ThreeWindingsTransformer. Should be defined for one Terminal of Three");
                }
                notAssigned(twt);
            }
        } else if (identifiable instanceof Switch) {
            Switch aswitch = context.network().getSwitch(equipmentId);
            notAssigned(aswitch);
        } else {
            notAssigned(identifiable);
        }
    }

    @Override
    public boolean valid() {
        if (vl == null && loadingLimitsAdder == null && loadingLimitsAdder1 == null && loadingLimitsAdder2 == null && loadingLimitsAdder3 == null) {
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

    // Cgmes 2.6 value is defined in EQ file
    // Cgmes 3.0 normalValue is defined in EQ file and value in SSH
    private static double getNormalValueFromEQ(PropertyBag p) {
        return p.getDouble("normalValue").orElse(p.getDouble("value").orElse(Double.NaN));
    }

    private void convertVoltageLimit(double value) {
        String limitTypeName = p.getLocal(OPERATIONAL_LIMIT_TYPE_NAME);
        String limitType = p.getLocal(LIMIT_TYPE);
        if (limitTypeName.equalsIgnoreCase("highvoltage") || "LimitTypeKind.highVoltage".equals(limitType)) {
            if (value < vl.getLowVoltageLimit()) {
                context.ignored("HighVoltageLimit", "Inconsistent with low voltage limit (" + vl.getLowVoltageLimit() + "kV)");
            } else if (value < vl.getHighVoltageLimit() || Double.isNaN(vl.getHighVoltageLimit())) {
                vl.setHighVoltageLimit(value);
            }
        } else if (limitTypeName.equalsIgnoreCase("lowvoltage") || "LimitTypeKind.lowVoltage".equals(limitType)) {
            if (value > vl.getHighVoltageLimit()) {
                context.ignored("LowVoltageLimit", "Inconsistent with high voltage limit (" + vl.getHighVoltageLimit() + "kV)");
            } else if (value > vl.getLowVoltageLimit() || Double.isNaN(vl.getLowVoltageLimit())) {
                vl.setLowVoltageLimit(value);
            }
        } else {
            notAssigned(vl);
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

        if (loadingLimitsAdder != null) {
            boolean added = addPatl(value, loadingLimitsAdder);
            addPermanentOperationalLimitPropertiesIfLimitHasBeenConsidered(added, operationalLimitId, "", value);
        } else {
            if (loadingLimitsAdder1 != null) {
                boolean added = addPatl(value, loadingLimitsAdder1);
                addPermanentOperationalLimitPropertiesIfLimitHasBeenConsidered(added, operationalLimitId, "1", value);
            }
            if (loadingLimitsAdder2 != null) {
                boolean added = addPatl(value, loadingLimitsAdder2);
                addPermanentOperationalLimitPropertiesIfLimitHasBeenConsidered(added, operationalLimitId, "2", value);
            }
            if (loadingLimitsAdder3 != null) {
                boolean added = addPatl(value, loadingLimitsAdder3);
                addPermanentOperationalLimitPropertiesIfLimitHasBeenConsidered(added, operationalLimitId, "3", value);
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
            if (loadingLimitsAdder != null) {
                boolean added = addTatl(name, value, acceptableDuration, loadingLimitsAdder);
                addTemporaryOperationalLimitPropertiesIfLimitHasBeenConsidered(added, operationalLimitId, "", acceptableDuration, value);
            } else {
                if (loadingLimitsAdder1 != null) {
                    boolean added = addTatl(name, value, acceptableDuration, loadingLimitsAdder1);
                    addTemporaryOperationalLimitPropertiesIfLimitHasBeenConsidered(added, operationalLimitId, "1", acceptableDuration, value);
                }
                if (loadingLimitsAdder2 != null) {
                    boolean added = addTatl(name, value, acceptableDuration, loadingLimitsAdder2);
                    addTemporaryOperationalLimitPropertiesIfLimitHasBeenConsidered(added, operationalLimitId, "2", acceptableDuration, value);
                }
                if (loadingLimitsAdder3 != null) {
                    boolean added = addTatl(name, value, acceptableDuration, loadingLimitsAdder3);
                    addTemporaryOperationalLimitPropertiesIfLimitHasBeenConsidered(added, operationalLimitId, "3", acceptableDuration, value);
                }
            }
        } else if (direction.endsWith("low")) {
            context.invalid(TEMPORARY_LIMIT, () -> String.format("TATL %s is a low limit", id));
        } else {
            context.invalid(TEMPORARY_LIMIT, () -> String.format("TATL %s does not have a valid direction", id));
        }
    }

    private void addTemporaryOperationalLimitPropertiesIfLimitHasBeenConsidered(boolean included, String operationalLimitId, String end, int duration, double value) {
        if (included) {
            addTemporaryOperationalLimitProperties(operationalLimitId, end, duration, value);
        }
    }

    private void addTemporaryOperationalLimitProperties(String operationalLimitId, String end, int duration, double value) {
        Objects.requireNonNull(limitSubclassType);
        Objects.requireNonNull(loadingLimitsIdentifiable);
        loadingLimitsIdentifiable.setProperty(getPropertyName(end, limitSubclassType, duration), operationalLimitId);
        loadingLimitsIdentifiable.setProperty(getPropertyName(operationalLimitId), String.valueOf(value));
    }

    private void addPermanentOperationalLimitPropertiesIfLimitHasBeenConsidered(boolean included, String operationalLimitId, String end, double value) {
        if (included) {
            addPermanentOperationalLimitProperties(operationalLimitId, end, value);
        }
    }

    private void addPermanentOperationalLimitProperties(String operationalLimitId, String end, double value) {
        Objects.requireNonNull(limitSubclassType);
        Objects.requireNonNull(loadingLimitsIdentifiable);
        loadingLimitsIdentifiable.setProperty(getPropertyName(end, limitSubclassType), operationalLimitId);
        loadingLimitsIdentifiable.setProperty(getPropertyName(operationalLimitId), String.valueOf(value));
    }

    private void notAssigned() {
        notAssigned(null);
    }

    private void notAssigned(Identifiable<?> eq) {
        String type = p.getLocal(LIMIT_TYPE);
        String typeName = p.getLocal(OPERATIONAL_LIMIT_TYPE_NAME);
        String subclass = p.getLocal(OPERATIONAL_LIMIT_SUBCLASS);
        Supplier<String> reason = () -> String.format(
                "Not assigned for %s %s. Limit id, type, typeName, subClass, terminal : %s, %s, %s, %s, %s",
                eq != null ? className(eq) : "",
                eq != null ? eq.getId() : "",
                id,
                type,
                typeName,
                subclass,
                terminalId);
        context.pending(OPERATIONAL_LIMIT, reason);
    }

    private static String className(Identifiable<?> o) {
        String s = o.getClass().getName();
        int dot = s.lastIndexOf('.');
        if (dot >= 0) {
            s = s.substring(dot + 1);
        }
        s = s.replace("Impl", "");
        return s;
    }

    public static void update(OperationalLimitsGroup operationalLimitsGroup, String end, Identifiable<?> identifiable, Context context) {
        operationalLimitsGroup.getActivePowerLimits().ifPresent(activePowerLimits -> updateLoadingLimits(activePowerLimits, end, CgmesNames.ACTIVE_POWER_LIMIT, identifiable, context));
        operationalLimitsGroup.getApparentPowerLimits().ifPresent(apparentPowerLimits -> updateLoadingLimits(apparentPowerLimits, end, CgmesNames.APPARENT_POWER_LIMIT, identifiable, context));
        operationalLimitsGroup.getCurrentLimits().ifPresent(currentLimits -> updateLoadingLimits(currentLimits, end, CgmesNames.CURRENT_LIMIT, identifiable, context));
    }

    private static void updateLoadingLimits(LoadingLimits loadingLimits, String end, String limitSubclass, Identifiable<?> identifiable, Context context) {
        loadingLimits.setPermanentLimit(getValue(end, limitSubclass, identifiable, loadingLimits.getPermanentLimit(), context));
        loadingLimits.getTemporaryLimits().forEach(temporaryLimit -> {
            int duration = temporaryLimit.getAcceptableDuration();
            loadingLimits.setTemporaryLimitValue(duration, getValue(end, limitSubclass, duration, identifiable, temporaryLimit.getValue(), context));
        });
    }

    private static double getValue(String end, String limitSubclass, Identifiable<?> identifiable, double previousValue, Context context) {
        String operationalLimitId = getOperationalLimitId(getPropertyName(end, limitSubclass), identifiable);
        return updatedValue(operationalLimitId, context)
                .orElse(defaultValue(getNormalValue(getPropertyName(operationalLimitId), identifiable), previousValue, getDefaultValue(context)));
    }

    private static double getValue(String end, String limitSubclass, int duration, Identifiable<?> identifiable, double previousValue, Context context) {
        String operationalLimitId = getOperationalLimitId(getPropertyName(end, limitSubclass, duration), identifiable);
        return updatedValue(operationalLimitId, context)
                .orElse(defaultValue(getNormalValue(getPropertyName(operationalLimitId), identifiable), previousValue, getDefaultValue(context)));
    }

    private static OptionalDouble updatedValue(String operationalLimitId, Context context) {
        PropertyBag p = context.operationalLimit(operationalLimitId);
        if (p == null) {
            return OptionalDouble.empty();
        }
        return p.getDouble("value");
    }

    private static String getOperationalLimitId(String propertyName, Identifiable<?> identifiable) {
        return identifiable.getProperty(propertyName);
    }

    private static double getNormalValue(String propertyName, Identifiable<?> identifiable) {
        return Double.parseDouble(identifiable.getProperty(propertyName));
    }

    private static String getPropertyName(String end, String limitSubclass) {
        return Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.OPERATIONAL_LIMIT + "_" + end + "_" + limitSubclass + "_" + "patl";
    }

    private static String getPropertyName(String end, String limitSubclass, int duration) {
        return Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.OPERATIONAL_LIMIT + "_" + end + "_" + limitSubclass + "_" + "tatl" + "_" + duration;
    }

    private static String getPropertyName(String operationalLimitId) {
        return Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "normalValue" + "_" + operationalLimitId;
    }

    protected static double defaultValue(double normalValue, double previousValue, Conversion.Config.DefaultValue defaultValue) {
        return switch (defaultValue) {
            case EQ, DEFAULT, EMPTY -> normalValue;
            case PREVIOUS -> previousValue;
        };
    }

    private static Conversion.Config.DefaultValue getDefaultValue(Context context) {
        return selectDefaultValue(List.of(EQ, PREVIOUS), context);
    }

    private final String terminalId;
    private final String equipmentId;
    private final String limitSubclassType;
    private Identifiable<?> loadingLimitsIdentifiable;

    private LoadingLimitsAdder<?, ?> loadingLimitsAdder;
    private LoadingLimitsAdder<?, ?> loadingLimitsAdder1;
    private LoadingLimitsAdder<?, ?> loadingLimitsAdder2;
    private LoadingLimitsAdder<?, ?> loadingLimitsAdder3;

    private VoltageLevel vl;
}
