/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TwoWindingsTransformerConversion extends AbstractConductingEquipmentConversion {

    public TwoWindingsTransformerConversion(PropertyBags ends, Conversion.Context context) {
        super("PowerTransformer", ends, context);
        end1 = ends.get(0);
        end2 = ends.get(1);
    }

    @Override
    public void convert() {

        double r1 = end1.asDouble("r");
        double x1 = end1.asDouble("x");
        double b1 = end1.asDouble("b");
        double g1 = end1.asDouble("g", 0);
        double r2 = end2.asDouble("r");
        double x2 = end2.asDouble("x");
        double b2 = end2.asDouble("b");
        double g2 = end2.asDouble("g", 0);
        double ratedU1 = end1.asDouble("ratedU");
        double ratedU2 = end2.asDouble("ratedU");

        double rho0 = ratedU2 / ratedU1;
        double rho0Square = rho0 * rho0;
        double r0 = r1 * rho0Square + r2;
        double x0 = x1 * rho0Square + x2;
        double g0 = g1 / rho0Square + g2;
        double b0 = b1 / rho0Square + b2;

        TwoWindingsTransformer tx = substation().newTwoWindingsTransformer()
                .setId(iidmId())
                .setName(iidmName())
                .setEnsureIdUnicity(false)
                .setR(r0)
                .setX(x0)
                .setG(g0)
                .setB(b0)
                .setRatedU1(ratedU1)
                .setBus1(terminalConnected(1) ? busId(1) : null)
                .setConnectableBus1(busId(1))
                .setVoltageLevel1(iidmVoltageLevelId(1))
                .setRatedU2(ratedU2)
                .setBus2(terminalConnected(2) ? busId(2) : null)
                .setConnectableBus2(busId(2))
                .setVoltageLevel2(iidmVoltageLevelId(2))
                .add();

        convertedTerminals(tx.getTerminal1(), tx.getTerminal2());

        addTapChangers(tx);
    }

    private void addTapChangers(TwoWindingsTransformer tx) {
        String rtcPropertyName = "RatioTapChanger";
        String rtc1 = end1.getId(rtcPropertyName);
        String rtc2 = end2.getId(rtcPropertyName);
        String ptcPropertyName = "PhaseTapChanger";
        String ptc1 = end1.getId(ptcPropertyName);
        String ptc2 = end2.getId(ptcPropertyName);

        if (context.config().allowUnsupportedTapChangers()) {
            context.tapChangerTransformers().add(rtc1, tx, 1);
            context.tapChangerTransformers().add(rtc2, tx, 2);
            context.tapChangerTransformers().add(ptc1, tx, 1);
            context.tapChangerTransformers().add(ptc2, tx, 2);
            return;
        }

        // Ensure only one tap changer is defined
        String rtc = null;
        int rtcSide = 0;
        if (rtc1 != null) {
            if (rtc2 != null) {
                String reason = "Unsupported modelling: two winding transformer with two ratio tap changers";
                invalid(reason);
                throw new PowsyblException(
                        String.format("TwoWindingTransformer %s %s", id, reason));
            }
            rtc = rtc1;
            rtcSide = 1;
        } else if (rtc2 != null) {
            rtc = rtc2;
            rtcSide = 2;
        }
        if (rtc != null) {
            context.tapChangerTransformers().add(rtc, tx, rtcSide);
        }
        String ptc = null;
        int ptcSide = 0;
        if (ptc1 != null) {
            if (ptc2 != null) {
                String reason = "Unsupported modelling: transformer with two phase tap changers";
                invalid(reason);
                throw new PowsyblException(
                        String.format("TwoWindingTransformer %s %s", id, reason));
            }
            ptc = ptc1;
            ptcSide = 1;
        } else if (ptc2 != null) {
            ptc = ptc2;
            ptcSide = 2;
        }
        if (ptc != null) {
            context.tapChangerTransformers().add(ptc, tx, ptcSide);
        }
        if (rtcSide > 0 && ptcSide > 0 && rtcSide != ptcSide) {
            String reason = String.format(
                    "Unsupported modelling: transformer with ratio and tap changer not on the same winding, rtc: %s, ptc: %s",
                    rtc,
                    ptc);
            invalid(reason);
        }
    }

    private final PropertyBag end1;
    private final PropertyBag end2;
}
