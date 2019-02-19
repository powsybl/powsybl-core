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
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class OperationalLimitConversion extends AbstractIdentifiedObjectConversion {

    private static final String CURRENT_LIMIT = "CurrentLimit";
    private static final String LIMIT_TYPE = "limitType";
    private static final String OPERATIONAL_LIMIT = "Operational limit";
    private static final String OPERATIONAL_LIMIT_TYPE_NAME = "operationalLimitTypeName";
    private static final String OPERATIONAL_LIMIT_SUBCLASS = "OperationalLimitSubclass";

    public OperationalLimitConversion(PropertyBag l, Context context) {
        super("OperationalLimit", l, context);
        // Limit can associated to a Terminal or to an Equipment
        terminalId = l.getId("Terminal");
        equipmentId = l.getId("Equipment");
        Terminal terminal = null;
        if (terminalId != null) {
            terminal = context.terminalMapping().find(terminalId);
            if (terminal != null) {
                createCurrentLimitsAdder(terminal);
            }
        }
        if (terminal == null && equipmentId != null) {
            // The equipment may be a Branch, a Dangling line, a Switch ...
            Identifiable identifiable = context.network().getIdentifiable(equipmentId);
            if (identifiable instanceof Branch) {
                Branch branch = context.network().getBranch(equipmentId);
                createCurrentLimitsAdder(branch);
            } else if (identifiable instanceof DanglingLine) {
                DanglingLine danglingLine = context.network().getDanglingLine(equipmentId);
                createCurrentLimitsAdder(danglingLine);
            } else if (identifiable instanceof Switch) {
                Switch aswitch = context.network().getSwitch(equipmentId);
                notAssigned(aswitch);
            } else {
                notAssigned();
            }
        }
    }

    private void createCurrentLimitsAdder(Terminal terminal) {
        Connectable<?> connectable = terminal.getConnectable();
        if (connectable instanceof Branch) {
            int terminalNumber = context.terminalMapping().number(terminalId);
            Branch<?> b = (Branch<?>) connectable;
            if (terminalNumber == 1) {
                adder1 = context.currentLimitsMapping().computeAdderIfAbsent(terminal, t -> b.newCurrentLimits1());
            } else if (terminalNumber == 2) {
                adder2 = context.currentLimitsMapping().computeAdderIfAbsent(terminal, t -> b.newCurrentLimits2());
            } else {
                notAssigned(b);
            }
        } else if (connectable instanceof DanglingLine) {
            adder = context.currentLimitsMapping().computeAdderIfAbsent(terminal, t -> ((DanglingLine) connectable).newCurrentLimits());
        } else {
            notAssigned(connectable);
        }
    }

    private void createCurrentLimitsAdder(Branch branch) {
        if (branch instanceof TwoWindingsTransformer) {
            context.ignored(CURRENT_LIMIT, "Defined for Equipment TwoWindingsTransformer. Should be defined for one Terminal of Two");
            notAssigned(branch);
        } else {
            adder1 = context.currentLimitsMapping().computeAdderIfAbsent(branch.getTerminal1(), t -> branch.newCurrentLimits1());
            adder2 = context.currentLimitsMapping().computeAdderIfAbsent(branch.getTerminal2(), t -> branch.newCurrentLimits2());
        }
    }

    private void createCurrentLimitsAdder(DanglingLine danglingLine) {
        adder = context.currentLimitsMapping().computeAdderIfAbsent(danglingLine.getTerminal(), t -> danglingLine.newCurrentLimits());
    }

    @Override
    public boolean valid() {
        if (adder == null && adder1 == null && adder2 == null) {
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

    private void convertPatlCurrent(double value) {
        if (adder != null) {
            context.currentLimitsMapping().addPermanentLimit(value, adder, terminalId, equipmentId);
        } else {
            if (adder1 != null) {
                context.currentLimitsMapping().addPermanentLimit(value, adder1, terminalId, equipmentId);
            }
            if (adder2 != null) {
                context.currentLimitsMapping().addPermanentLimit(value, adder2, terminalId, equipmentId);
            }
        }
    }

    private boolean isTatl() {
        String limitTypeName = p.getLocal(OPERATIONAL_LIMIT_TYPE_NAME);
        String limitType = p.getLocal(LIMIT_TYPE);
        return limitTypeName.equals("TATL") || "LimitTypeKind.tatl".equals(limitType);
    }

    private void convertTatlCurrent(double value) {
        int acceptableDuration = (int) p.asDouble("acceptableDuration");
        // We only accept high or absoluteValue (considered as high when
        // current from the conducting equipment to the terminal) limits
        String direction = p.getId("direction");

        // if there is no direction, the limit is considered as absoluteValue (cf. CGMES specification)
        if (direction == null || direction.endsWith("high") || direction.endsWith("absoluteValue")) {
            if (adder != null) {
                adder.beginTemporaryLimit()
                        .setName(context.namingStrategy().getId("TATL", id))
                        .setValue(value)
                        .setAcceptableDuration(60 * acceptableDuration)
                        .endTemporaryLimit();
            } else if (adder1 != null) {
                // Should we chose one terminal randomly for branches ? Here by default, we only look at terminal1
                adder1.beginTemporaryLimit()
                        .setName(context.namingStrategy().getId("TATL", id))
                        .setValue(value)
                        .setAcceptableDuration(60 * acceptableDuration)
                        .endTemporaryLimit();
            }
        } else if (direction.endsWith("low")) {
            context.invalid(OPERATIONAL_LIMIT, String.format("TATL %s is a low limit", id));
        } else {
            context.invalid(OPERATIONAL_LIMIT, String.format("TATL %s does not have a valid direction", id));
        }
    }

    private void notAssigned() {
        notAssigned(null);
    }

    private void notAssigned(Identifiable<?> eq) {
        String type = p.getLocal(LIMIT_TYPE);
        String typeName = p.getLocal(OPERATIONAL_LIMIT_TYPE_NAME);
        String subclass = p.getLocal(OPERATIONAL_LIMIT_SUBCLASS);
        String reason = String.format(
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

    private CurrentLimitsAdder adder;
    private CurrentLimitsAdder adder1;
    private CurrentLimitsAdder adder2;
}
