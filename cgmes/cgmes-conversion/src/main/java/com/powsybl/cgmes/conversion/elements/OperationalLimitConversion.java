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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.powsybl.cgmes.conversion.LoadingLimitsMapping.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class OperationalLimitConversion extends AbstractIdentifiedObjectConversion {

    private static final String ACTIVE_POWER_LIMIT = "ActivePowerLimit";
    private static final String APPARENT_POWER_LIMIT = "ApparentPowerLimit";
    private static final String CURRENT_LIMIT = "CurrentLimit";
    private static final String VOLTAGE_LIMIT = "VoltageLimit";
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
        // Limit can be associated with a Terminal or to an Equipment

        String limitSetId = p.getId(OPERATIONAL_LIMIT_SET_ID);
        String limitSetName = p.getLocal(OPERATIONAL_LIMIT_SET_NAME);
        limitSetName = limitSetName != null ? limitSetName : limitSetId;

        terminalId = l.getId("Terminal");
        equipmentId = l.getId("Equipment");
        Terminal terminal = null;
        if (limitSubclass == null || limitSubclass.equals(CgmesNames.ACTIVE_POWER_LIMIT) || limitSubclass.equals(CgmesNames.APPARENT_POWER_LIMIT) || limitSubclass.equals(CgmesNames.CURRENT_LIMIT)) {
            if (limitSubclass == null) {
                // Support for CIM14, all limits are assumed to be current
                limitSubclass = CgmesNames.CURRENT_LIMIT;
            }
            if (terminalId != null) {
                terminal = context.terminalMapping().findForFlowLimits(terminalId);
            }
            if (terminal != null) {
                checkAndCreateLimitsAdder(context.terminalMapping().number(terminalId), limitSubclass, limitSetId, limitSetName, terminal.getConnectable());
            } else if (equipmentId != null) {
                // The equipment may be a Branch, a Dangling line, a Switch ...
                Identifiable<?> identifiable = context.network().getIdentifiable(equipmentId);
                checkAndCreateLimitsAdder(-1, limitSubclass, limitSetId, limitSetName, identifiable);
            }
        } else if (limitSubclass.equals(VOLTAGE_LIMIT)) {
            if (terminalId != null) {
                terminal = context.terminalMapping().findForVoltageLimits(terminalId);
            }
            setVoltageLevelForVoltageLimit(terminal);
        } else {
            notAssigned();
        }
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

    private static Supplier<LoadingLimitsAdder<?, ?>> getLoadingLimitAdderSupplier(String limitSubClass, FlowsLimitsHolder holder) {
        if (limitSubClass == null) {
            return holder::newCurrentLimits;
        }
        return switch (limitSubClass) {
            case ACTIVE_POWER_LIMIT -> holder::newActivePowerLimits;
            case APPARENT_POWER_LIMIT -> holder::newApparentPowerLimits;
            case CURRENT_LIMIT -> holder::newCurrentLimits;
            default -> throw new IllegalStateException();
        };
    }

    private static Supplier<LoadingLimitsAdder<?, ?>> getLoadingLimitAdder1Supplier(String limitSubClass, Branch<?> b) {
        if (limitSubClass == null) {
            return b::newCurrentLimits1;
        }
        return switch (limitSubClass) {
            case ACTIVE_POWER_LIMIT -> b::newActivePowerLimits1;
            case APPARENT_POWER_LIMIT -> b::newApparentPowerLimits1;
            case CURRENT_LIMIT -> b::newCurrentLimits1;
            default -> throw new IllegalStateException();
        };
    }

    private static Supplier<LoadingLimitsAdder<?, ?>> getLoadingLimitAdder2Supplier(String limitSubClass, Branch<?> b) {
        if (limitSubClass == null) {
            return b::newCurrentLimits2;
        }
        return switch (limitSubClass) {
            case ACTIVE_POWER_LIMIT -> b::newActivePowerLimits2;
            case APPARENT_POWER_LIMIT -> b::newApparentPowerLimits2;
            case CURRENT_LIMIT -> b::newCurrentLimits2;
            default -> throw new IllegalStateException();
        };
    }

    /**
     * Create the LoadingLimitsAdder for the given branch + side and the given limit set + subclass.
     * @param terminalNumber The side of the branch to which the OperationalLimit applies.
     * @param limitSubClass The subclass of the OperationalLimit.
     * @param limitSetId The id of the set containing the OperationalLimit.
     * @param limitSetName The name of the set containing the OperationalLimit.
     * @param b The branch to which the OperationalLimit applies.
     */
    private void createLimitsAdder(int terminalNumber, String limitSubClass, String limitSetId, String limitSetName, Branch<?> b) {
        if (terminalNumber == 1) {
            OperationalLimitsGroup limitsGroup = b.getOperationalLimitsGroup1(limitSetId).orElseGet(() -> {
                b.setProperty(PROPERTY_PREFIX + limitSetId, limitSetName);
                return b.newOperationalLimitsGroup1(limitSetId); });
            loadingLimitsAdder1 = context.loadingLimitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubClass);
        } else if (terminalNumber == 2) {
            OperationalLimitsGroup limitsGroup = b.getOperationalLimitsGroup2(limitSetId).orElseGet(() -> {
                b.setProperty(PROPERTY_PREFIX + limitSetId, limitSetName);
                return b.newOperationalLimitsGroup2(limitSetId); });
            loadingLimitsAdder2 = context.loadingLimitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubClass);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Create the LoadingLimitsAdder for the given dangling line and the given limit set + subclass.
     * @param limitSubClass The subclass of the OperationalLimit.
     * @param limitSetId The set containing the OperationalLimit.
     * @param limitSetName The name of the set containing the OperationalLimit.
     * @param dl The branch to which the OperationalLimit applies.
     */
    private void createLimitsAdder(String limitSubClass, String limitSetId, String limitSetName, DanglingLine dl) {
        OperationalLimitsGroup limitsGroup = dl.getOperationalLimitsGroup(limitSetId).orElseGet(() -> {
            dl.setProperty(PROPERTY_PREFIX + limitSetId, limitSetName);
            return dl.newOperationalLimitsGroup(limitSetId); });
        loadingLimitsAdder = context.loadingLimitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubClass);
    }

    /**
     * Create the LoadingLimitsAdder for the given 3w-transformer + side and the given limit set + subclass.
     * @param terminalNumber The side of the transformer to which the OperationalLimit applies.
     * @param limitSubClass The subclass of the OperationalLimit.
     * @param limitSetId The set containing the OperationalLimit.
     * @param limitSetName The name of the set containing the OperationalLimit.
     * @param twt The 3w-transformer to which the OperationalLimit applies.
     */
    private void createLimitsAdder(int terminalNumber, String limitSubClass, String limitSetId, String limitSetName, ThreeWindingsTransformer twt) {
        if (terminalNumber == 1) {
/*** TODO JAM update
            loadingLimitsAdder1 = context.loadingLimitsMapping().computeIfAbsentLoadingLimitsAdder(twt.getId() + "_1_" + limitSubClass,
                    getLoadingLimitAdderSupplier(limitSubClass, twt.getLeg1()));
        } else if (terminalNumber == 2) {
            loadingLimitsAdder2 = context.loadingLimitsMapping().computeIfAbsentLoadingLimitsAdder(twt.getId() + "_2_" + limitSubClass,
                    getLoadingLimitAdderSupplier(limitSubClass, twt.getLeg2()));
        } else if (terminalNumber == 3) {
            loadingLimitsAdder3 = context.loadingLimitsMapping().computeIfAbsentLoadingLimitsAdder(twt.getId() + "_3_" + limitSubClass,
                    getLoadingLimitAdderSupplier(limitSubClass, twt.getLeg3()));
***/
            OperationalLimitsGroup limitsGroup = twt.getLeg1().getOperationalLimitsGroup(limitSetId).orElseGet(() -> {
                twt.setProperty(PROPERTY_PREFIX + limitSetId, limitSetName);
                return twt.getLeg1().newOperationalLimitsGroup(limitSetId); });
            loadingLimitsAdder = context.loadingLimitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubClass);
        } else if (terminalNumber == 2) {
            OperationalLimitsGroup limitsGroup = twt.getLeg2().getOperationalLimitsGroup(limitSetId).orElseGet(() -> {
                twt.setProperty(PROPERTY_PREFIX + limitSetId, limitSetName);
                return twt.getLeg2().newOperationalLimitsGroup(limitSetId); });
            loadingLimitsAdder = context.loadingLimitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubClass);
        } else if (terminalNumber == 3) {
            OperationalLimitsGroup limitsGroup = twt.getLeg3().getOperationalLimitsGroup(limitSetId).orElseGet(() -> {
                twt.setProperty(PROPERTY_PREFIX + limitSetId, limitSetName);
                return twt.getLeg3().newOperationalLimitsGroup(limitSetId); });
            loadingLimitsAdder = context.loadingLimitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubClass);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Create LoadingLimitsAdder(s) as per the given inputs.
     * If the inputs are inconsistent, no limit adder is created.
     * @param terminalNumber The side of the equipment to which the OperationalLimit applies.
     * @param limitSubClass The subclass of the OperationalLimit.
     * @param limitSetId The set containing the OperationalLimit.
     * @param limitSetName The name of the set containing the OperationalLimit.
     * @param identifiable The equipment to which the OperationalLimit applies.
     */
    private void checkAndCreateLimitsAdder(int terminalNumber, String limitSubClass, String limitSetId, String limitSetName, Identifiable<?> identifiable) {
        if (identifiable instanceof Line) {
            Branch<?> b = (Branch<?>) identifiable;
            if (terminalNumber == -1) {
                // Limits appliy to the whole equipment == to both sides
                createLimitsAdder(1, limitSubClass, limitSetId, limitSetName, b);
                createLimitsAdder(2, limitSubClass, limitSetId, limitSetName, b);
            } else if (terminalNumber == 1 || terminalNumber == 2) {
                createLimitsAdder(terminalNumber, limitSubClass, limitSetId, limitSetName, b);
            } else {
                notAssigned(b);
            }
        } else if (identifiable instanceof TwoWindingsTransformer) {
            Branch<?> b = (Branch<?>) identifiable;
            if (terminalNumber == 1 || terminalNumber == 2) {
                createLimitsAdder(terminalNumber, limitSubClass, limitSetId, limitSetName, b);
            } else {
                if (terminalNumber == -1) {
                    context.ignored(limitSubClass, "Defined for Equipment TwoWindingsTransformer. Should be defined for one Terminal of Two");
                }
                notAssigned(b);
            }
        } else if (identifiable instanceof DanglingLine dl) {
            createLimitsAdder(limitSubClass, limitSetId, limitSetName, dl);
        } else if (identifiable instanceof ThreeWindingsTransformer twt) {
            if (terminalNumber == 1 || terminalNumber == 2 || terminalNumber == 3) {
                createLimitsAdder(terminalNumber, limitSubClass, limitSetId, limitSetName, twt);
            } else {
                if (terminalNumber == -1) {
                    context.ignored(limitSubClass, "Defined for Equipment ThreeWindingsTransformer. Should be defined for one Terminal of Three");
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
        double value = getValueFromEQ(p);
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
    private static double getValueFromEQ(PropertyBag p) {
        return p.getDouble("normalValue").orElse(p.getDouble("value").orElse(Double.NaN));
    }

    private void convertVoltageLimit(double value) {
        String limitTypeName = p.getLocal(OPERATIONAL_LIMIT_TYPE_NAME);
        String limitType = p.getLocal(LIMIT_TYPE);
        if (limitTypeName.equalsIgnoreCase("highvoltage") || "LimitTypeKind.highVoltage".equals(limitType)) {
            context.loadingLimitsMapping().addOperationalLimit(id, vl.getId(), "", VOLTAGE_LIMIT, "highVoltage", 0, value);
            if (value < vl.getLowVoltageLimit()) {
                context.ignored("HighVoltageLimit", "Inconsistent with low voltage limit (" + vl.getLowVoltageLimit() + "kV)");
            } else if (value < vl.getHighVoltageLimit() || Double.isNaN(vl.getHighVoltageLimit())) {
                vl.setHighVoltageLimit(value);
            }
        } else if (limitTypeName.equalsIgnoreCase("lowvoltage") || "LimitTypeKind.lowVoltage".equals(limitType)) {
            context.loadingLimitsMapping().addOperationalLimit(id, vl.getId(), "", VOLTAGE_LIMIT, "lowVoltage", 0, value);
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

    private void addPatl(double value, LoadingLimitsAdder<?, ?> adder) {
        if (Double.isNaN(adder.getPermanentLimit())) {
            adder.setPermanentLimit(value);
        } else {
            if (terminalId != null) {
                context.fixed(PERMANENT_LIMIT, () -> String.format("Several permanent limits defined for Terminal %s. Only the lowest is kept.", terminalId));
            } else {
                context.fixed(PERMANENT_LIMIT, () -> String.format("Several permanent limits defined for Equipment %s. Only the lowest is kept.", equipmentId));
            }
            if (adder.getPermanentLimit() > value) {
                adder.setPermanentLimit(value);
            }
        }
    }

    private void convertPatl(double value) {
        if (loadingLimitsAdder != null) {
            addPatl(value, loadingLimitsAdder);
            context.loadingLimitsMapping().addOperationalLimit(id, identifiableId, "", limitSubclass, "patl", 0, value);
        } else {
            if (loadingLimitsAdder1 != null) {
                addPatl(value, loadingLimitsAdder1);
                context.loadingLimitsMapping().addOperationalLimit(id, identifiableId, "1", limitSubclass, "patl", 0, value);
            }
            if (loadingLimitsAdder2 != null) {
                addPatl(value, loadingLimitsAdder2);
                context.loadingLimitsMapping().addOperationalLimit(id, identifiableId, "2", limitSubclass, "patl", 0, value);
            }
            if (loadingLimitsAdder3 != null) {
                addPatl(value, loadingLimitsAdder3);
                context.loadingLimitsMapping().addOperationalLimit(id, identifiableId, "3", limitSubclass, "patl", 0, value);
            }
        }
    }

    private boolean isTatl() {
        String limitTypeName = p.getLocal(OPERATIONAL_LIMIT_TYPE_NAME);
        String limitType = p.getLocal(LIMIT_TYPE);
        return limitTypeName.equals("TATL") || "LimitTypeKind.tatl".equals(limitType) || "LimitKind.tatl".equals(limitType);
    }

    private void addTatl(String name, double value, int acceptableDuration, String end, LoadingLimitsAdder<?, ?> adder) {
        if (Double.isNaN(value)) {
            return;
        }

        context.loadingLimitsMapping().addOperationalLimit(id, identifiableId, end, limitSubclass, "tatl", acceptableDuration, value);
        if (Double.isNaN(adder.getTemporaryLimitValue(acceptableDuration))) {
            adder.beginTemporaryLimit()
                    .setAcceptableDuration(acceptableDuration)
                    .setName(name)
                    .setValue(value)
                    .ensureNameUnicity()
                    .endTemporaryLimit();
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
            }
        }
    }

    private void convertTatl(double value) {
        int acceptableDuration = p.containsKey("acceptableDuration") ? (int) p.asDouble("acceptableDuration") : Integer.MAX_VALUE;
        // We only accept high or absoluteValue (considered as high when
        // current from the conducting equipment to the terminal) limits
        String direction = p.getId("direction");

        // if there is no direction, the limit is considered as absoluteValue (cf. CGMES specification)
        if (direction == null || direction.endsWith("high") || direction.endsWith("absoluteValue")) {
            String name = Optional.ofNullable(p.getId("shortName")).orElse(p.getId("name"));
            if (loadingLimitsAdder != null) {
                addTatl(name, value, acceptableDuration, "", loadingLimitsAdder);
            } else {
                if (loadingLimitsAdder1 != null) {
                    addTatl(name, value, acceptableDuration, "1", loadingLimitsAdder1);
                }
                if (loadingLimitsAdder2 != null) {
                    addTatl(name, value, acceptableDuration, "2", loadingLimitsAdder2);
                }
                if (loadingLimitsAdder3 != null) {
                    addTatl(name, value, acceptableDuration, "3", loadingLimitsAdder3);
                }
            }
        } else if (direction.endsWith("low")) {
            context.invalid(TEMPORARY_LIMIT, () -> String.format("TATL %s is a low limit", id));
        } else {
            context.invalid(TEMPORARY_LIMIT, () -> String.format("TATL %s does not have a valid direction", id));
        }
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

    public static void updateOperationalLimitsOfVoltageLevel(VoltageLevel voltageLevel, Context context) {
        String lowOperationalLimitIdsString = voltageLevel.getProperty(getOperationalLimitKey("", VOLTAGE_LIMIT, "lowLimit"));
        String highOperationalLimitIdsString = voltageLevel.getProperty(getOperationalLimitKey("", VOLTAGE_LIMIT, "highLimit"));
        if (lowOperationalLimitIdsString != null && highOperationalLimitIdsString != null) {
            double maxLowLimit = getMaxLimit(voltageLevel.getParentNetwork(), context, lowOperationalLimitIdsString);
            double minHighLimit = getMinLimit(voltageLevel.getParentNetwork(), context, highOperationalLimitIdsString);
            if (!Double.isNaN(maxLowLimit) && !Double.isNaN(minHighLimit)) {
                if (maxLowLimit <= minHighLimit) {
                    voltageLevel.setLowVoltageLimit(maxLowLimit);
                    voltageLevel.setHighVoltageLimit(minHighLimit);
                } else {
                    context.ignored("VoltageLimits", "Inconsistent lowVoltageLimit (" + maxLowLimit + " kV) and highVoltageLimit (" + minHighLimit + "kV)");
                }
            }
        } else if (lowOperationalLimitIdsString != null) {
            double maxLowLimit = getMaxLimit(voltageLevel.getParentNetwork(), context, lowOperationalLimitIdsString);
            if (!Double.isNaN(maxLowLimit)) {
                voltageLevel.setLowVoltageLimit(maxLowLimit);
            }
        } else if (highOperationalLimitIdsString != null) {
            double minHighLimit = getMinLimit(voltageLevel.getParentNetwork(), context, highOperationalLimitIdsString);
            if (!Double.isNaN(minHighLimit)) {
                voltageLevel.setHighVoltageLimit(minHighLimit);
            }
        }
    }

    private static double getValue(Network network, Context context, String operationalLimitId) {
        double normalValue = fixNormalValue(network.getProperty(getOperationalLimitNormalValueKey(operationalLimitId)));
        return context.operationalLimitUpdate().getValue(operationalLimitId).orElse(normalValue);
    }

    private static double fixNormalValue(String normalValue) {
        return normalValue == null ? Double.NaN : Double.parseDouble(normalValue);
    }

    private static double getMaxLimit(Network network, Context context, String operationalLimitIdsString) {
        List<String> operationalLimitsIds = splitOperationalLimitsIdsString(operationalLimitIdsString);
        return getMaxLimit(network, context, operationalLimitsIds);
    }

    private static double getMaxLimit(Network network, Context context, List<String> operationalLimitsIds) {
        return operationalLimitsIds.stream()
                .map(operationalLimitsId -> getValue(network, context, operationalLimitsId))
                .max(Comparator.naturalOrder()).orElseThrow();
    }

    private static double getMinLimit(Network network, Context context, String operationalLimitIdsString) {
        List<String> operationalLimitsIds = splitOperationalLimitsIdsString(operationalLimitIdsString);
        return getMinLimit(network, context, operationalLimitsIds);
    }

    private static double getMinLimit(Network network, Context context, List<String> operationalLimitsIds) {
        return operationalLimitsIds.stream()
                .map(operationalLimitsId -> getValue(network, context, operationalLimitsId))
                .min(Comparator.naturalOrder()).orElseThrow();
    }

    public static void updateOperationalLimitsOfConnectable(Connectable<?> connectable, Context context) {
        if (connectable.getType().equals(IdentifiableType.DANGLING_LINE)) {
            DanglingLine danglingLine = (DanglingLine) connectable;
            danglingLine.getActivePowerLimits().ifPresent(activePowerLimits -> updateLoadingLimits(context, connectable, activePowerLimits, danglingLine.newActivePowerLimits(), "", ACTIVE_POWER_LIMIT));
            danglingLine.getApparentPowerLimits().ifPresent(apparentPowerLimits -> updateLoadingLimits(context, connectable, apparentPowerLimits, danglingLine.newApparentPowerLimits(), "", APPARENT_POWER_LIMIT));
            danglingLine.getCurrentLimits().ifPresent(currentLimits -> updateLoadingLimits(context, connectable, currentLimits, danglingLine.newCurrentLimits(), "", CURRENT_LIMIT));
        } else if (connectable.getType().equals(IdentifiableType.LINE)) {
            Line line = (Line) connectable;
            line.getActivePowerLimits1().ifPresent(activePowerLimits -> updateLoadingLimits(context, connectable, activePowerLimits, line.newActivePowerLimits1(), "1", ACTIVE_POWER_LIMIT));
            line.getApparentPowerLimits1().ifPresent(apparentPowerLimits -> updateLoadingLimits(context, connectable, apparentPowerLimits, line.newApparentPowerLimits1(), "1", APPARENT_POWER_LIMIT));
            line.getCurrentLimits1().ifPresent(currentLimits -> updateLoadingLimits(context, connectable, currentLimits, line.newCurrentLimits1(), "1", CURRENT_LIMIT));

            line.getActivePowerLimits2().ifPresent(activePowerLimits -> updateLoadingLimits(context, connectable, activePowerLimits, line.newActivePowerLimits2(), "2", ACTIVE_POWER_LIMIT));
            line.getApparentPowerLimits2().ifPresent(apparentPowerLimits -> updateLoadingLimits(context, connectable, apparentPowerLimits, line.newApparentPowerLimits2(), "2", APPARENT_POWER_LIMIT));
            line.getCurrentLimits2().ifPresent(currentLimits -> updateLoadingLimits(context, connectable, currentLimits, line.newCurrentLimits2(), "2", CURRENT_LIMIT));
        } else if (connectable.getType().equals(IdentifiableType.TWO_WINDINGS_TRANSFORMER)) {
            TwoWindingsTransformer t2wt = (TwoWindingsTransformer) connectable;
            t2wt.getActivePowerLimits1().ifPresent(activePowerLimits -> updateLoadingLimits(context, connectable, activePowerLimits, t2wt.newActivePowerLimits1(), "1", ACTIVE_POWER_LIMIT));
            t2wt.getApparentPowerLimits1().ifPresent(apparentPowerLimits -> updateLoadingLimits(context, connectable, apparentPowerLimits, t2wt.newApparentPowerLimits1(), "1", APPARENT_POWER_LIMIT));
            t2wt.getCurrentLimits1().ifPresent(currentLimits -> updateLoadingLimits(context, connectable, currentLimits, t2wt.newCurrentLimits1(), "1", CURRENT_LIMIT));

            t2wt.getActivePowerLimits2().ifPresent(activePowerLimits -> updateLoadingLimits(context, connectable, activePowerLimits, t2wt.newActivePowerLimits2(), "2", ACTIVE_POWER_LIMIT));
            t2wt.getApparentPowerLimits2().ifPresent(apparentPowerLimits -> updateLoadingLimits(context, connectable, apparentPowerLimits, t2wt.newApparentPowerLimits2(), "2", APPARENT_POWER_LIMIT));
            t2wt.getCurrentLimits2().ifPresent(currentLimits -> updateLoadingLimits(context, connectable, currentLimits, t2wt.newCurrentLimits2(), "2", CURRENT_LIMIT));
        } else if (connectable.getType().equals(IdentifiableType.THREE_WINDINGS_TRANSFORMER)) {
            ThreeWindingsTransformer t3wt = (ThreeWindingsTransformer) connectable;
            t3wt.getLeg1().getActivePowerLimits().ifPresent(activePowerLimits -> updateLoadingLimits(context, connectable, activePowerLimits, t3wt.getLeg1().newActivePowerLimits(), "1", ACTIVE_POWER_LIMIT));
            t3wt.getLeg1().getApparentPowerLimits().ifPresent(apparentPowerLimits -> updateLoadingLimits(context, connectable, apparentPowerLimits, t3wt.getLeg1().newApparentPowerLimits(), "1", APPARENT_POWER_LIMIT));
            t3wt.getLeg1().getCurrentLimits().ifPresent(currentLimits -> updateLoadingLimits(context, connectable, currentLimits, t3wt.getLeg1().newCurrentLimits(), "1", CURRENT_LIMIT));

            t3wt.getLeg2().getActivePowerLimits().ifPresent(activePowerLimits -> updateLoadingLimits(context, connectable, activePowerLimits, t3wt.getLeg2().newActivePowerLimits(), "2", ACTIVE_POWER_LIMIT));
            t3wt.getLeg2().getApparentPowerLimits().ifPresent(apparentPowerLimits -> updateLoadingLimits(context, connectable, apparentPowerLimits, t3wt.getLeg2().newApparentPowerLimits(), "2", APPARENT_POWER_LIMIT));
            t3wt.getLeg2().getCurrentLimits().ifPresent(currentLimits -> updateLoadingLimits(context, connectable, currentLimits, t3wt.getLeg2().newCurrentLimits(), "2", CURRENT_LIMIT));

            t3wt.getLeg3().getActivePowerLimits().ifPresent(activePowerLimits -> updateLoadingLimits(context, connectable, activePowerLimits, t3wt.getLeg3().newActivePowerLimits(), "3", ACTIVE_POWER_LIMIT));
            t3wt.getLeg3().getApparentPowerLimits().ifPresent(apparentPowerLimits -> updateLoadingLimits(context, connectable, apparentPowerLimits, t3wt.getLeg3().newApparentPowerLimits(), "3", APPARENT_POWER_LIMIT));
            t3wt.getLeg3().getCurrentLimits().ifPresent(currentLimits -> updateLoadingLimits(context, connectable, currentLimits, t3wt.getLeg3().newCurrentLimits(), "3", CURRENT_LIMIT));
        }
    }

    private static void updateLoadingLimits(Context context, Connectable<?> connectable, LoadingLimits loadingLimits, LoadingLimitsAdder<?, ?> loadingLimitsAdder, String end, String limitSubclass) {
        boolean okPermanent = updatePermanentLoadingLimits(context, connectable, loadingLimits, loadingLimitsAdder, end, limitSubclass);
        boolean okTemporary = updateTemporaryLoadingLimits(context, connectable, loadingLimits, loadingLimitsAdder, end, limitSubclass);
        if (okPermanent || okTemporary) {
            loadingLimitsAdder.add();
        }
    }

    private static boolean updatePermanentLoadingLimits(Context context, Connectable<?> connectable, LoadingLimits loadingLimits, LoadingLimitsAdder<?, ?> loadingLimitsAdder, String end, String limitSubclass) {
        String operationalLimitIdsString = connectable.getProperty(getOperationalLimitKey(end, limitSubclass, "patl"));
        if (operationalLimitIdsString == null) {
            loadingLimitsAdder.setPermanentLimit(loadingLimits.getPermanentLimit());
            return false;
        }
        List<String> operationalLimitsIds = splitOperationalLimitsIdsString(operationalLimitIdsString);
        double minValue = getMinLimit(connectable.getParentNetwork(), context, operationalLimitsIds);
        if (Double.isNaN(minValue)) {
            loadingLimitsAdder.setPermanentLimit(loadingLimits.getPermanentLimit());
            return false;
        }
        loadingLimitsAdder.setPermanentLimit(minValue);
        if (operationalLimitsIds.size() > 1) {
            context.fixed(PERMANENT_LIMIT, () -> String.format("Several permanent limits defined for Connectable %s. Only the lowest is kept.", connectable.getId()));
        }
        return true;
    }

    private static boolean updateTemporaryLoadingLimits(Context context, Connectable<?> connectable, LoadingLimits loadingLimits, LoadingLimitsAdder<?, ?> loadingLimitsAdder, String end, String limitSubclass) {
        String operationalLimitIdsString = connectable.getProperty(getOperationalLimitKey(end, limitSubclass, "tatl"));
        if (operationalLimitIdsString == null) {
            copyTemporaryLimitsToAdder(loadingLimits, loadingLimitsAdder);
            return false;
        }
        List<String> operationalLimitsIds = splitOperationalLimitsIdsString(operationalLimitIdsString);
        for (LoadingLimits.TemporaryLimit temporaryLimit : loadingLimits.getTemporaryLimits()) {
            int duration = temporaryLimit.getAcceptableDuration();
            List<String> operationalLimitsIdsForThisDuration = operationalLimitsIds.stream().filter(operationalLimitsId -> isSameDuration(connectable.getParentNetwork(), operationalLimitsId, duration)).toList();
            if (operationalLimitsIdsForThisDuration.size() > 1) {
                context.fixed(TEMPORARY_LIMIT, () -> String.format("Several temporary limits defined for same acceptable duration (%d s) for Connectable %s. Only the lowest is kept.", duration, connectable.getId()));
            }
            double minValue = getMinLimit(connectable.getParentNetwork(), context, operationalLimitsIdsForThisDuration);
            if (Double.isNaN(minValue)) {
                copyTemporaryLimitsToAdder(loadingLimits, loadingLimitsAdder);
                return false;
            }
            loadingLimitsAdder.beginTemporaryLimit()
                    .setAcceptableDuration(duration)
                    .setValue(minValue)
                    .setName(temporaryLimit.getName())
                    .setFictitious(temporaryLimit.isFictitious())
                    .endTemporaryLimit();
        }
        return true;
    }

    private static void copyTemporaryLimitsToAdder(LoadingLimits loadingLimits, LoadingLimitsAdder<?, ?> loadingLimitsAdder) {
        List<String> names = loadingLimitsAdder.getTemporaryLimitNames().stream().toList();
        names.forEach(loadingLimitsAdder::removeTemporaryLimit);

        for (LoadingLimits.TemporaryLimit temporaryLimit : loadingLimits.getTemporaryLimits()) {
            loadingLimitsAdder.beginTemporaryLimit()
                    .setAcceptableDuration(temporaryLimit.getAcceptableDuration())
                    .setValue(temporaryLimit.getValue())
                    .setName(temporaryLimit.getName())
                    .setFictitious(temporaryLimit.isFictitious())
                    .endTemporaryLimit();
        }
    }

    private static boolean isSameDuration(Network network, String operationalLimitsId, int durationRef) {
        int duration = fixDuration(network.getProperty(getOperationalLimitDurationKey(operationalLimitsId)));
        return duration == durationRef;
    }

    private static int fixDuration(String duration) {
        return duration == null ? 0 : Integer.parseInt(duration);
    }

    private final String terminalId;
    private final String equipmentId;

    private LoadingLimitsAdder<?, ?> loadingLimitsAdder;
    private LoadingLimitsAdder<?, ?> loadingLimitsAdder1;
    private LoadingLimitsAdder<?, ?> loadingLimitsAdder2;
    private LoadingLimitsAdder<?, ?> loadingLimitsAdder3;
    private VoltageLevel vl;

    private String identifiableId;
    private String limitSubclass;
}
