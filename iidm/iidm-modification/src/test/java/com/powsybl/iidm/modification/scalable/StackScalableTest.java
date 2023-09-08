/*
 * Copyright (c) 2023. , RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.modification.scalable;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.powsybl.iidm.modification.scalable.ScalableTestNetwork.createNetworkwithDanglingLineAndBattery;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.Priority.ONESHOT;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.ScalingType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StackScalableTest {

    private Network network;
    private Scalable g1;
    private Scalable g2;
    private Scalable g3;

    private Scalable l1;
    private Scalable l2;
    private Scalable l3;
    private Scalable s;
    private Scalable unknownGenerator;
    private Scalable unknownLoad;
    private Scalable unknownDanglingLine;
    private Scalable dl1;

    @BeforeEach
    void setUp() {

        network = createNetworkwithDanglingLineAndBattery();
        g1 = Scalable.onGenerator("g1");
        g2 = Scalable.onGenerator("g2");
        g3 = Scalable.onGenerator("g3", -10, 80);
        s = Scalable.onGenerator("s");
        unknownGenerator = Scalable.onGenerator("unknown");

        l1 = Scalable.onLoad("l1");
        l2 = Scalable.onLoad("l2", 20, 80);
        l3 = Scalable.onLoad("l3", -50, 100);
        unknownLoad = Scalable.onLoad("unknown");
        unknownDanglingLine = Scalable.onDanglingLine("unknown");
        dl1 = Scalable.onDanglingLine("dl1", 20, 80);
    }

    private void reset() {
        Scalable.stack(g1, g2, g3).reset(network);
        Scalable.stack(l1, l2, s, unknownGenerator, unknownLoad, unknownDanglingLine, dl1).reset(network);
        l3.reset(network);
    }

    @Test
    void testScaleOnGeneratorsStackingUp() {
        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        ScalingParameters scalingParameters = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, ONESHOT, true, DELTA_P);

        // Proportional to Target P
        StackScalable stackScalable = Scalable.stack(generatorList);
        double variationDone = stackScalable.scale(network, 100.0, scalingParameters);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(150.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(80.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(30.0, network.getGenerator("g3").getTargetP(), 1e-5);
        reset();
    }

    @Test
    void testScaleOnGeneratorsStackingUpWrongScalingType() {
        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        ScalingParameters scalingParameters = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, ONESHOT, true, TARGET_P);

        // Proportional to Target P
        StackScalable stackScalable = Scalable.stack(generatorList);

        // Error raised
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> stackScalable.scale(network, 100.0, scalingParameters));
        assertEquals("Stacking only works with DELTA_P ScalingType", e0.getMessage());
        reset();
    }
}
