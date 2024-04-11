/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class OperationalLimitConversion extends AbstractIdentifiedObjectConversion {

    private static final String ACTIVE_POWER_LIMIT = "ActivePowerLimit";
    private static final String APPARENT_POWER_LIMIT = "ApparentPowerLimit";
    private static final String CURRENT_LIMIT = "CurrentLimit";
    private static final String LIMIT_TYPE = "limitType";
    private static final String OPERATIONAL_LIMIT = "Operational limit";
    private static final String OPERATIONAL_LIMIT_TYPE_NAME = "operationalLimitTypeName";
    private static final String OPERATIONAL_LIMIT_SUBCLASS = "OperationalLimitSubclass";

    private static final String PERMANENT_LIMIT = "Permanent Limit";
    private static final String TEMPORARY_LIMIT = "Temporary Limit";

    public OperationalLimitConversion(PropertyBag l, Context context) {
        super("OperationalLimit", l, context);
        String limitSubclass = p.getLocal(OPERATIONAL_LIMIT_SUBCLASS);
        // Limit can associated to a Terminal or to an Equipment
        terminalId = l.getId("Terminal");
        equipmentId = l.getId("Equipment");
        Terminal terminal = null;
        if (limitSubclass == null || limitSubclass.equals(ACTIVE_POWER_LIMIT) || limitSubclass.equals(APPARENT_POWER_LIMIT) || limitSubclass.equals(CURRENT_LIMIT)) {
            if (terminalId != null) {
                terminal = context.terminalMapping().findForFlowLimits(terminalId);
            }
            if (terminal != null) {
                createLimitsAdder(context.terminalMapping().number(terminalId), limitSubclass, terminal.getConnectable());
            } else if (equipmentId != null) {
                // The equipment may be a Branch, a Dangling line, a Switch ...
                Identifiable<?> identifiable = context.network().getIdentifiable(equipmentId);
                createLimitsAdder(-1, limitSubclass, identifiable);
            }
        } else if (limitSubclass.equals("VoltageLimit")) {
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
        switch (limitSubClass) {
            case ACTIVE_POWER_LIMIT:
                return holder::newActivePowerLimits;
            case APPARENT_POWER_LIMIT:
                return holder::newApparentPowerLimits;
            case CURRENT_LIMIT:
                return holder::newCurrentLimits;
            default:
                throw new IllegalStateException();
        }
    }

    private static Supplier<LoadingLimitsAdder<?, ?>> getLoadingLimitAdder1Supplier(String limitSubClass, Branch<?> b) {
        if (limitSubClass == null) {
            return b::newCurrentLimits1;
        }
        switch (limitSubClass) {
            case ACTIVE_POWER_LIMIT:
                return b::newActivePowerLimits1;
            case APPARENT_POWER_LIMIT:
                return b::newApparentPowerLimits1;
            case CURRENT_LIMIT:
                return b::newCurrentLimits1;
            default:
                throw new IllegalStateException();
        }
    }

    private static Supplier<LoadingLimitsAdder<?, ?>> getLoadingLimitAdder2Supplier(String limitSubClass, Branch<?> b) {
        if (limitSubClass == null) {
            return b::newCurrentLimits2;
        }
        switch (limitSubClass) {
            case ACTIVE_POWER_LIMIT:
                return b::newActivePowerLimits2;
            case APPARENT_POWER_LIMIT:
                return b::newApparentPowerLimits2;
            case CURRENT_LIMIT:
                return b::newCurrentLimits2;
            default:
                throw new IllegalStateException();
        }
    }

    private void createLimitsAdder(int terminalNumber, String limitSubClass, Branch<?> b) {
        if (terminalNumber == 1) {
            Supplier<LoadingLimitsAdder<?, ?>> loadingLimitAdder1Supplier = getLoadingLimitAdder1Supplier(limitSubClass, b);
            loadingLimitsAdder1 = context.loadingLimitsMapping().computeIfAbsentLoadingLimitsAdder(b.getId() + "_1_" + limitSubClass, loadingLimitAdder1Supplier);
        } else if (terminalNumber == 2) {
            Supplier<LoadingLimitsAdder<?, ?>> loadingLimitAdder2Supplier = getLoadingLimitAdder2Supplier(limitSubClass, b);
            loadingLimitsAdder2 = context.loadingLimitsMapping().computeIfAbsentLoadingLimitsAdder(b.getId() + "_2_" + limitSubClass, loadingLimitAdder2Supplier);
        } else {
            notAssigned(b);
        }
    }

    private void createLimitsAdder(int terminalNumber, String limitSubClass, ThreeWindingsTransformer twt) {
        if (terminalNumber == 1) {
            loadingLimitsAdder = context.loadingLimitsMapping().computeIfAbsentLoadingLimitsAdder(twt.getId() + "_1_" + limitSubClass,
                    getLoadingLimitAdderSupplier(limitSubClass, twt.getLeg1()));
        } else if (terminalNumber == 2) {
            loadingLimitsAdder = context.loadingLimitsMapping().computeIfAbsentLoadingLimitsAdder(twt.getId() + "_2_" + limitSubClass,
                    getLoadingLimitAdderSupplier(limitSubClass, twt.getLeg2()));
        } else if (terminalNumber == 3) {
            loadingLimitsAdder = context.loadingLimitsMapping().computeIfAbsentLoadingLimitsAdder(twt.getId() + "_3_" + limitSubClass,
                    getLoadingLimitAdderSupplier(limitSubClass, twt.getLeg3()));
        } else {
            notAssigned(twt);
        }
    }

    private void createLimitsAdder(int terminalNumber, String limitSubClass, Identifiable<?> identifiable) {
        if (identifiable instanceof Line) {
            Branch<?> b = (Branch<?>) identifiable;
            if (terminalNumber == -1) {
                loadingLimitsAdder1 = context.loadingLimitsMapping().computeIfAbsentLoadingLimitsAdder(b.getId() + "_1", getLoadingLimitAdder1Supplier(limitSubClass, b));
                loadingLimitsAdder2 = context.loadingLimitsMapping().computeIfAbsentLoadingLimitsAdder(b.getId() + "_2", getLoadingLimitAdder2Supplier(limitSubClass, b));
            } else {
                createLimitsAdder(terminalNumber, limitSubClass, b);
            }
        } else if (identifiable instanceof TwoWindingsTransformer) {
            Branch<?> b = (Branch<?>) identifiable;
            if (terminalNumber == -1) {
                context.ignored(limitSubClass, "Defined for Equipment TwoWindingsTransformer. Should be defined for one Terminal of Two");
                notAssigned(b);
            } else {
                createLimitsAdder(terminalNumber, limitSubClass, b);
            }
        } else if (identifiable instanceof DanglingLine danglingLine) {
            loadingLimitsAdder = context.loadingLimitsMapping().computeIfAbsentLoadingLimitsAdder(danglingLine.getId() + "_" + limitSubClass,
                    getLoadingLimitAdderSupplier(limitSubClass, danglingLine));
        } else if (identifiable instanceof ThreeWindingsTransformer twt) {
            if (terminalNumber == -1) {
                context.ignored(limitSubClass, "Defined for Equipment ThreeWindingsTransformer. Should be defined for one Terminal of Three");
                notAssigned(twt);
            } else {
                createLimitsAdder(terminalNumber, limitSubClass, twt);
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
        if (vl == null && loadingLimitsAdder == null && loadingLimitsAdder1 == null && loadingLimitsAdder2 == null) {
            missing(String.format("Terminal %s or Equipment %s", terminalId, equipmentId));
            return false;
        }
        return true;
    }

    @Override
    public void convert() {
        double normalValue = p.asDouble("normalValue");
        double value = p.asDouble("value", normalValue);
        if (value <= 0) {
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

    private void convertVoltageLimit(double value) {
        String limitTypeName = p.getLocal(OPERATIONAL_LIMIT_TYPE_NAME);
        String limitType = p.getLocal(LIMIT_TYPE);
        if (limitTypeName.equalsIgnoreCase("highvoltage") || "LimitTypeKind.highVoltage".equals(limitType)) {
            if (value < vl.getHighVoltageLimit() || Double.isNaN(vl.getHighVoltageLimit())) {
                if (value < vl.getLowVoltageLimit()) {
                    context.ignored("HighVoltageLimit", "Inconsistent with low voltage limit (" + vl.getLowVoltageLimit() + "kV)");
                    return;
                }
                vl.setHighVoltageLimit(value);
            }
        } else if (limitTypeName.equalsIgnoreCase("lowvoltage") || "LimitTypeKind.lowVoltage".equals(limitType)) {
            if (value > vl.getLowVoltageLimit() || Double.isNaN(vl.getLowVoltageLimit())) {
                if (value > vl.getHighVoltageLimit()) {
                    context.ignored("LowVoltageLimit", "Inconsistent with high voltage limit (" + vl.getHighVoltageLimit() + "kV)");
                    return;
                }
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
        } else {
            if (loadingLimitsAdder1 != null) {
                addPatl(value, loadingLimitsAdder1);
            }
            if (loadingLimitsAdder2 != null) {
                addPatl(value, loadingLimitsAdder2);
            }
        }
    }

    private boolean isTatl() {
        String limitTypeName = p.getLocal(OPERATIONAL_LIMIT_TYPE_NAME);
        String limitType = p.getLocal(LIMIT_TYPE);
        return limitTypeName.equals("TATL") || "LimitTypeKind.tatl".equals(limitType) || "LimitKind.tatl".equals(limitType);
    }

    private void addTatl(String name, double value, int acceptableDuration, LoadingLimitsAdder<?, ?> adder) {
        if (Double.isNaN(value)) {
            context.ignored(TEMPORARY_LIMIT, "Temporary limit value is undefined");
            return;
        }

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
                addTatl(name, value, acceptableDuration, loadingLimitsAdder);
            } else {
                if (loadingLimitsAdder1 != null) {
                    addTatl(name, value, acceptableDuration, loadingLimitsAdder1);
                }
                if (loadingLimitsAdder2 != null) {
                    addTatl(name, value, acceptableDuration, loadingLimitsAdder2);
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

    private final String terminalId;
    private final String equipmentId;

    private LoadingLimitsAdder<?, ?> loadingLimitsAdder;
    private LoadingLimitsAdder<?, ?> loadingLimitsAdder1;
    private LoadingLimitsAdder<?, ?> loadingLimitsAdder2;

    private VoltageLevel vl;
}
