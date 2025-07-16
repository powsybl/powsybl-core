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
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.Optional;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public abstract class AbstractBranchConversion extends AbstractConductingEquipmentConversion {

    protected AbstractBranchConversion(
            String type,
            PropertyBag p,
            Context context) {
        super(type, p, context, 2);
    }

    @Override
    public boolean valid() {
        if (!super.valid()) {
            return false;
        }
        String node1 = nodeId(1);
        String node2 = nodeId(2);
        if (context.boundary().containsNode(node1)
                || context.boundary().containsNode(node2)) {
            invalid("Has " + nodeIdPropertyName() + " on boundary");
            return false;
        }
        if (!p.containsKey("r") || !p.containsKey("x")) {
            invalid("No r,x attributes");
            return false;
        }
        return true;
    }

    protected void convertBranch(double r, double x, double gch, double bch, String originalClass) {
        if (isZeroImpedanceInsideVoltageLevel(r, x)) {
            // Convert to switch
            Switch sw;
            boolean open = !(terminalConnected(1) && terminalConnected(2));
            if (context.nodeBreaker()) {
                VoltageLevel.NodeBreakerView.SwitchAdder adder;
                adder = voltageLevel().getNodeBreakerView().newSwitch()
                                .setKind(SwitchKind.BREAKER)
                                .setRetained(true)
                                .setFictitious(true);
                identify(adder);
                connect(adder, open);
                sw = adder.add();
            } else {
                VoltageLevel.BusBreakerView.SwitchAdder adder;
                adder = voltageLevel().getBusBreakerView().newSwitch()
                                .setFictitious(true);
                identify(adder);
                connect(adder, open);
                sw = adder.add();
            }
            addAliasesAndProperties(sw);
        } else {
            final LineAdder adder = context.network().newLine()
                    .setEnsureIdUnicity(context.config().isEnsureIdAliasUnicity())
                    .setR(r)
                    .setX(x)
                    .setG1(gch / 2)
                    .setG2(gch / 2)
                    .setB1(bch / 2)
                    .setB2(bch / 2);
            identify(adder);
            connectWithOnlyEq(adder);
            final Line l = adder.add();
            addAliasesAndProperties(l);
            convertedTerminalsWithOnlyEq(l.getTerminal1(), l.getTerminal2());
            l.setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, originalClass);
        }
    }

    private boolean isZeroImpedanceInsideVoltageLevel(double r, double x) {
        Optional<VoltageLevel> vl1 = voltageLevel(1);
        Optional<VoltageLevel> vl2 = voltageLevel(2);
        if (vl1.isPresent()) {
            if (vl2.isPresent() && vl1.get() == vl2.get()) {
                return r == 0.0 && x == 0.0;
            }
            return false;
        } else {
            if (vl2.isPresent()) {
                return false;
            }
            return r == 0.0 && x == 0.0;
        }
    }

    protected static void updateBranch(Line line, Context context) {
        updateTerminals(line, context, line.getTerminal1(), line.getTerminal2());
        line.getOperationalLimitsGroups1().forEach(operationalLimitsGroup -> OperationalLimitConversion.update(line, operationalLimitsGroup, TwoSides.ONE, context));
        line.getOperationalLimitsGroups2().forEach(operationalLimitsGroup -> OperationalLimitConversion.update(line, operationalLimitsGroup, TwoSides.TWO, context));
    }
}
