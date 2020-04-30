/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.iidm.network.*;
import com.powsybl.cgmes.conversion.Context;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.function.Supplier;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class OperationalLimitConversion extends AbstractIdentifiedObjectConversion {

    private static final String CURRENT_LIMIT = "CurrentLimit";
    private static final String LIMIT_TYPE = "limitType";
    private static final String OPERATIONAL_LIMIT = "Operational limit";
    private static final String OPERATIONAL_LIMIT_TYPE_NAME = "operationalLimitTypeName";
    private static final String OPERATIONAL_LIMIT_SUBCLASS = "OperationalLimitSubclass";

    private static final String PERMANENT_CURRENT_LIMIT = "Permanent Current Limit";
    private static final String TEMPORARY_CURRENT_LIMIT = "Temporary Current Limit";

    public OperationalLimitConversion(PropertyBag l, Context context) {
        super("OperationalLimit", l, context);
        // Limit can associated to a Terminal or to an Equipment
        terminalId = l.getId("Terminal");
        equipmentId = l.getId("Equipment");
        Terminal terminal = null;
        if (terminalId != null) {
            terminal = context.terminalMapping().find(terminalId);
            if (terminal != null) {
                createCurrentLimitsAdder(context.terminalMapping().number(terminalId), terminal.getConnectable());
            }
        }
        if (terminal == null && equipmentId != null) {
            // The equipment may be a Branch, a Dangling line, a Switch ...
            Identifiable identifiable = context.network().getIdentifiable(equipmentId);
            createCurrentLimitsAdder(-1, identifiable);
        }
    }

    private void createCurrentLimitsAdder(int terminalNumber, Branch<?> b) {
        if (terminalNumber == 1) {
            currentLimitsAdder1 = context.currentLimitsMapping().getCurrentLimitsAdder(b.getId() + "_1", b::newCurrentLimits1);
        } else if (terminalNumber == 2) {
            currentLimitsAdder2 = context.currentLimitsMapping().getCurrentLimitsAdder(b.getId() + "_2", b::newCurrentLimits2);
        } else {
            notAssigned(b);
        }
    }

    private void createCurrentLimitsAdder(int terminalNumber, Identifiable<?> identifiable) {
        if (identifiable instanceof Line) {
            Branch<?> b = (Branch<?>) identifiable;
            if (terminalNumber == -1) {
                currentLimitsAdder1 = context.currentLimitsMapping().getCurrentLimitsAdder(b.getId() + "_1", b::newCurrentLimits1);
                currentLimitsAdder2 = context.currentLimitsMapping().getCurrentLimitsAdder(b.getId() + "_2", b::newCurrentLimits2);
            } else {
                createCurrentLimitsAdder(terminalNumber, b);
            }
        } else if (identifiable instanceof TwoWindingsTransformer) {
            Branch<?> b = (Branch<?>) identifiable;
            if (terminalNumber == -1) {
                context.ignored(CURRENT_LIMIT, "Defined for Equipment TwoWindingsTransformer. Should be defined for one Terminal of Two");
                notAssigned(b);
            } else {
                createCurrentLimitsAdder(terminalNumber, b);
            }
        } else if (identifiable instanceof DanglingLine) {
            DanglingLine danglingLine = (DanglingLine) identifiable;
            currentLimitsAdder = context.currentLimitsMapping().getCurrentLimitsAdder(danglingLine.getId(), danglingLine::newCurrentLimits);
        } else if (identifiable instanceof Switch) {
            Switch aswitch = context.network().getSwitch(equipmentId);
            notAssigned(aswitch);
        } else {
            notAssigned(identifiable);
        }
    }

    @Override
    public boolean valid() {
        if (currentLimitsAdder == null && currentLimitsAdder1 == null && currentLimitsAdder2 == null) {
            missing(String.format("Terminal %s or Equipment %s", terminalId, equipmentId));
            return false;
        }
        return true;
    }

    @Override
    public void convert() {
        double value = p.asDouble("value");
        if (value <= 0) {
            context.ignored(OPERATIONAL_LIMIT, "value is <= 0");
            return;
        }
        String limitSubclass = p.getLocal(OPERATIONAL_LIMIT_SUBCLASS);
        if (limitSubclass == null || limitSubclass.equals(CURRENT_LIMIT)) {
            if (isPatl()) { // Permanent Admissible Transmission Loading
                convertPatlCurrent(value);
            } else if (isTatl()) { // Temporary Admissible Transmission Loading
                convertTatlCurrent(value);
            }
        } else if (limitSubclass.equals("ApparentPowerLimit")) {
            notAssigned();
        } else {
            notAssigned();
        }
    }

    private boolean isPatl() {
        String limitTypeName = p.getLocal(OPERATIONAL_LIMIT_TYPE_NAME);
        String limitType = p.getLocal(LIMIT_TYPE);
        return limitTypeName.equals("PATL") || "LimitTypeKind.patl".equals(limitType);
    }

    private void addPatlCurrent(double value, CurrentLimitsAdder adder) {
        if (Double.isNaN(adder.getPermanentLimit())) {
            adder.setPermanentLimit(value);
        } else {
            if (terminalId != null) {
                context.fixed(PERMANENT_CURRENT_LIMIT,
                    () -> String.format("Several permanent limits defined for Terminal %s. Only the lowest is kept.", terminalId));
            } else {
                context.fixed(PERMANENT_CURRENT_LIMIT,
                    () -> String.format("Several permanent limits defined for Equipment %s. Only the lowest is kept.", equipmentId));
            }
            if (adder.getPermanentLimit() > value) {
                adder.setPermanentLimit(value);
            }
        }
    }

    private void convertPatlCurrent(double value) {
        if (currentLimitsAdder != null) {
            addPatlCurrent(value, currentLimitsAdder);
        } else {
            if (currentLimitsAdder1 != null) {
                addPatlCurrent(value, currentLimitsAdder1);
            }
            if (currentLimitsAdder2 != null) {
                addPatlCurrent(value, currentLimitsAdder2);
            }
        }
    }

    private boolean isTatl() {
        String limitTypeName = p.getLocal(OPERATIONAL_LIMIT_TYPE_NAME);
        String limitType = p.getLocal(LIMIT_TYPE);
        return limitTypeName.equals("TATL") || "LimitTypeKind.tatl".equals(limitType);
    }

    private void addTatlCurrent(String name, double value, int acceptableDuration, CurrentLimitsAdder adder) {
        if (Double.isNaN(adder.getTemporaryLimitValue(acceptableDuration))) {
            adder.beginTemporaryLimit()
                    .setAcceptableDuration(acceptableDuration)
                    .setName(name)
                    .setValue(value)
                    .ensureNameUnicity()
                    .endTemporaryLimit();
        } else {
            if (terminalId != null) {
                context.fixed(TEMPORARY_CURRENT_LIMIT,
                    () -> String.format("Several temporary limits defined for same acceptable duration (%d s) for Terminal %s. Only the lowest is kept.", acceptableDuration, terminalId));
            } else {
                context.fixed(TEMPORARY_CURRENT_LIMIT,
                    () -> String.format("Several temporary limits defined for same acceptable duration (%d s) for Equipment %s. Only the lowest is kept.", acceptableDuration, equipmentId));
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

    private void convertTatlCurrent(double value) {
        int acceptableDuration = p.containsKey("acceptableDuration") ? (int) p.asDouble("acceptableDuration") : Integer.MAX_VALUE;
        // We only accept high or absoluteValue (considered as high when
        // current from the conducting equipment to the terminal) limits
        String direction = p.getId("direction");

        // if there is no direction, the limit is considered as absoluteValue (cf. CGMES specification)
        if (direction == null || direction.endsWith("high") || direction.endsWith("absoluteValue")) {
            if (currentLimitsAdder != null) {
                addTatlCurrent(context.namingStrategy().getId("TATL", id), value, acceptableDuration, currentLimitsAdder);
            } else {
                if (currentLimitsAdder1 != null) {
                    addTatlCurrent(context.namingStrategy().getId("TATL", id), value, acceptableDuration, currentLimitsAdder1);
                }
                if (currentLimitsAdder2 != null) {
                    addTatlCurrent(context.namingStrategy().getId("TATL", id), value, acceptableDuration, currentLimitsAdder2);
                }
            }
        } else if (direction.endsWith("low")) {
            context.invalid(TEMPORARY_CURRENT_LIMIT, () -> String.format("TATL %s is a low limit", id));
        } else {
            context.invalid(TEMPORARY_CURRENT_LIMIT, () -> String.format("TATL %s does not have a valid direction", id));
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

    private CurrentLimitsAdder currentLimitsAdder;
    private CurrentLimitsAdder currentLimitsAdder1;
    private CurrentLimitsAdder currentLimitsAdder2;
}
