/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class ThreeWindingsTransformerConversion extends AbstractConductingEquipmentConversion {

    public ThreeWindingsTransformerConversion(PropertyBags ends, Conversion.Context context) {
        super("PowerTransformer", ends, context);
        winding1 = ends.get(0);
        winding2 = ends.get(1);
        winding3 = ends.get(2);
    }

    @Override
    public boolean valid() {
        if (!super.valid()) {
            return false;
        }
        if (context.boundary().containsNode(nodeId(1))
                || context.boundary().containsNode(nodeId(2))
                || context.boundary().containsNode(nodeId(3))) {
            invalid("3 windings transformer end point at boundary is not supported");
            return false;
        }
        // This should not happen,
        // The substationIdMapping should ensure all three ends
        // are in the same IIDM substation
        if (voltageLevel(1).getSubstation() != voltageLevel(2).getSubstation() ||
                voltageLevel(1).getSubstation() != voltageLevel(3).getSubstation()) {
            String name1 = voltageLevel(1).getSubstation().getName();
            String name2 = voltageLevel(2).getSubstation().getName();
            String name3 = voltageLevel(3).getSubstation().getName();
            invalid(String.format("different substations at ends %s %s %s", name1, name2, name3));
            return false;
        }
        return true;
    }

    @Override
    public void convert() {
        // g is optional
        double r1 = winding1.asDouble("r");
        double x1 = winding1.asDouble("x");
        double b1 = winding1.asDouble("b");
        double g1 = winding1.asDouble("g", 0);
        double r2 = winding2.asDouble("r");
        double x2 = winding2.asDouble("x");
        double b2 = winding2.asDouble("b");
        double g2 = winding2.asDouble("g", 0);
        double r3 = winding3.asDouble("r");
        double x3 = winding3.asDouble("x");
        double b3 = winding3.asDouble("b");
        double g3 = winding3.asDouble("g", 0);
        String ratedU = "ratedU";
        double ratedU1 = winding1.asDouble(ratedU);
        double ratedU2 = winding2.asDouble(ratedU);
        double ratedU3 = winding3.asDouble(ratedU);
        String rtc = "RatioTapChanger";
        String rtc1 = winding1.getId(rtc);
        String rtc2 = winding2.getId(rtc);
        String rtc3 = winding3.getId(rtc);
        String ptc = "PhaseTapChanger";
        String ptc1 = winding1.getId(ptc);
        String ptc2 = winding2.getId(ptc);
        String ptc3 = winding3.getId(ptc);

        double rho2Square = Math.pow(ratedU2 / ratedU1, 2);
        double rho3Square = Math.pow(ratedU3 / ratedU1, 2);

        // IIDM model impedances for each leg, computed from winding impedances
        double ir1 = r1;
        double ix1 = x1;
        double ig1 = g1 + g2 * rho2Square + g3 * rho3Square;
        double ib1 = b1 + b2 * rho2Square + b3 * rho3Square;
        double ir2 = r2 / rho2Square;
        double ix2 = x2 / rho2Square;
        double ir3 = r3 / rho3Square;
        double ix3 = x3 / rho3Square;

        ThreeWindingsTransformer tx = substation().newThreeWindingsTransformer()
                .setId(context.namingStrategy().getId("Transformer", id))
                .setName(context.namingStrategy().getName("Transformer", name))
                .setEnsureIdUnicity(false)
                .newLeg1()
                .setR(ir1)
                .setX(ix1)
                .setG(ig1)
                .setB(ib1)
                .setRatedU(ratedU1)
                .setVoltageLevel(iidmVoltageLevelId(1))
                .setBus(terminalConnected() ? busId(1) : null)
                .setConnectableBus(busId(1))
                .add()
                .newLeg2()
                .setR(ir2)
                .setX(ix2)
                .setRatedU(ratedU2)
                .setVoltageLevel(iidmVoltageLevelId(2))
                .setBus(terminalConnected(2) ? busId(2) : null)
                .setConnectableBus(busId(2))
                .add()
                .newLeg3()
                .setR(ir3)
                .setX(ix3)
                .setRatedU(ratedU3)
                .setVoltageLevel(iidmVoltageLevelId(3))
                .setBus(terminalConnected(3) ? busId(3) : null)
                .setConnectableBus(busId(3))
                .add()
                .add();

        convertedTerminals(
                tx.getLeg1().getTerminal(),
                tx.getLeg2().getTerminal(),
                tx.getLeg3().getTerminal());

        // We do not follow here the same schema of two-windings,
        // we are saving for later all possible tap changers
        context.tapChangerTransformers().add(rtc1, tx, 1);
        context.tapChangerTransformers().add(rtc2, tx, 2);
        context.tapChangerTransformers().add(rtc3, tx, 3);
        context.tapChangerTransformers().add(ptc1, tx, 1);
        context.tapChangerTransformers().add(ptc2, tx, 2);
        context.tapChangerTransformers().add(ptc3, tx, 3);
    }

    private final PropertyBag winding1;
    private final PropertyBag winding2;
    private final PropertyBag winding3;
}
