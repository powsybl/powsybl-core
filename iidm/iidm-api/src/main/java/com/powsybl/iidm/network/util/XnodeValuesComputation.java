/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TieLine;

import java.util.function.ObjDoubleConsumer;
import java.util.function.ToDoubleFunction;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class XnodeValuesComputation {

    public static void computeAndSetXnodeV(TieLine tl, ObjDoubleConsumer<TieLine.HalfLine> halfLineVoltageSetter) {
        computeAndSetXnodeVOrAngle(tl, Bus::getV, halfLineVoltageSetter);
    }

    public static void computeAndSetXnodeAngle(TieLine tl, ObjDoubleConsumer<TieLine.HalfLine> halfLineAngleSetter) {
        computeAndSetXnodeVOrAngle(tl, Bus::getAngle, halfLineAngleSetter);
    }

    public static void computeAndSetXnodeP(TieLine tl, ObjDoubleConsumer<TieLine.HalfLine> halfLinePSetter) {
        computeAndSetXnodePOrQ(tl, Terminal::getP, halfLinePSetter);
    }

    public static void computeAndSetXnodeQ(TieLine tl, ObjDoubleConsumer<TieLine.HalfLine> halfLineQSetter) {
        computeAndSetXnodePOrQ(tl, Terminal::getQ, halfLineQSetter);
    }

    private static void computeAndSetXnodeVOrAngle(TieLine tl, ToDoubleFunction<Bus> voltageOrAngleGetter,
                                                   ObjDoubleConsumer<TieLine.HalfLine> halfLineVoltageOrAngleSetter) {
        // TODO(MRA): depending on the b/g in the middle of the TieLine, this computation is not correct
        Bus b1 = tl.getTerminal1().getBusView().getBus();
        Bus b2 = tl.getTerminal2().getBusView().getBus();
        if (b1 != null && b2 != null && !Double.isNaN(voltageOrAngleGetter.applyAsDouble(b1)) && !Double.isNaN(voltageOrAngleGetter.applyAsDouble(b2))) {
            double vOrAngle = (voltageOrAngleGetter.applyAsDouble(b1) + voltageOrAngleGetter.applyAsDouble(b2)) / 2.0;
            halfLineVoltageOrAngleSetter.accept(tl.getHalf1(), vOrAngle);
            halfLineVoltageOrAngleSetter.accept(tl.getHalf2(), vOrAngle);
        }
    }

    private static void computeAndSetXnodePOrQ(TieLine tl, ToDoubleFunction<Terminal> pOrQGetter, ObjDoubleConsumer<TieLine.HalfLine> halfLinePOrQSetter) {
        // TODO(mathbagu): depending on the b/g in the middle of the MergedLine, this computation is not correct
        double p1Orq1 = pOrQGetter.applyAsDouble(tl.getTerminal1());
        double p2Orq2 = pOrQGetter.applyAsDouble(tl.getTerminal2());
        if (!Double.isNaN(p1Orq1) && !Double.isNaN(p2Orq2)) {
            double losses = p1Orq1 + p2Orq2;
            halfLinePOrQSetter.accept(tl.getHalf1(), (p1Orq1 + losses / 2.0) * sign(p2Orq2));
            halfLinePOrQSetter.accept(tl.getHalf2(), (p2Orq2 + losses / 2.0) * sign(p1Orq1));
        }
    }

    private static int sign(double value) {
        // Sign depends on the transit flow:
        // P1 ---->-----DL1.P0 ---->----- DL2.P0 ---->---- P2
        // The sign of DL1.P0 is the same as P2, and respectively the sign of DL2.P0 is the same than P1
        return (value >= 0) ? 1 : -1;
    }

    private XnodeValuesComputation() {
    }
}
