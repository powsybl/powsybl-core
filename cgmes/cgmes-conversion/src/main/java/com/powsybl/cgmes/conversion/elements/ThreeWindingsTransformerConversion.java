/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.elements.transformers.NewThreeWindingsTransformerConversion;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.LegAdder;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 *
 * @deprecated Use {@link NewThreeWindingsTransformerConversion} instead.
 */
@Deprecated
public class ThreeWindingsTransformerConversion extends AbstractConductingEquipmentConversion {

    public ThreeWindingsTransformerConversion(PropertyBags ends, Context context) {
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
            String name1 = voltageLevel(1).getSubstation().getNameOrId();
            String name2 = voltageLevel(2).getSubstation().getNameOrId();
            String name3 = voltageLevel(3).getSubstation().getNameOrId();
            invalid(() -> String.format("different substations at ends %s %s %s", name1, name2, name3));
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

        ThreeWindingsTransformerAdder txadder = substation().newThreeWindingsTransformer();
        identify(txadder);

        LegAdder l1adder = txadder.newLeg1()
            .setR(ir1)
            .setX(ix1)
            .setG(ig1)
            .setB(ib1)
            .setRatedU(ratedU1);
        LegAdder l2adder = txadder.newLeg2()
            .setR(ir2)
            .setX(ix2)
            .setG(0.0)
            .setB(0.0)
            .setRatedU(ratedU2);
        LegAdder l3adder = txadder.newLeg3()
            .setR(ir3)
            .setX(ix3)
            .setG(0.0)
            .setB(0.0)
            .setRatedU(ratedU3);
        connect(l1adder, 1);
        connect(l2adder, 2);
        connect(l3adder, 3);
        l1adder.add();
        l2adder.add();
        l3adder.add();
        ThreeWindingsTransformer tx = txadder.add();

        convertedTerminals(
            tx.getLeg1().getTerminal(),
            tx.getLeg2().getTerminal(),
            tx.getLeg3().getTerminal());

        // We do not follow here the same schema of two-windings,
        // we are saving for later all possible tap changers
        context.tapChangerTransformers().add(rtc1, tx, "rtc", 1);
        context.tapChangerTransformers().add(rtc2, tx, "rtc", 2);
        context.tapChangerTransformers().add(rtc3, tx, "rtc", 3);
        context.tapChangerTransformers().add(ptc1, tx, "ptc", 1);
        context.tapChangerTransformers().add(ptc2, tx, "ptc", 2);
        context.tapChangerTransformers().add(ptc3, tx, "ptc", 3);

        setRegulatingControlContext(tx, rtc2, rtc3);
    }

    private void setRegulatingControlContext(ThreeWindingsTransformer tx, String rtc2Id, String rtc3Id) {

        PropertyBag rtc2 = null;
        if (rtc2Id != null) {
            rtc2 = context.ratioTapChanger(rtc2Id);
        }
        PropertyBag rtc3 = null;
        if (rtc3Id != null) {
            rtc3 = context.ratioTapChanger(rtc3Id);
        }

        context.regulatingControlMapping().forTransformers().add(tx.getId(), rtc2Id, rtc2, rtc3Id, rtc3);
    }

    private final PropertyBag winding1;
    private final PropertyBag winding2;
    private final PropertyBag winding3;
}
