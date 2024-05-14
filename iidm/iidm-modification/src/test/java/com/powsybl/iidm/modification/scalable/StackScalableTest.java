/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.modification.scalable;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.powsybl.iidm.modification.scalable.ProportionalScalable.DistributionMode.UNIFORM_DISTRIBUTION;
import static com.powsybl.iidm.modification.scalable.ScalableTestNetwork.createNetworkwithDanglingLineAndBattery;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.Priority.ONESHOT;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.Priority.RESPECT_OF_VOLUME_ASKED;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.ScalingType.DELTA_P;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.ScalingType.TARGET_P;
import static com.powsybl.iidm.modification.util.ModificationReports.scalingReport;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        g1 = Scalable.onGenerator("g1"); //initial targetP : 80MW
        g2 = Scalable.onGenerator("g2"); //initial targetP : 50MW
        g3 = Scalable.onGenerator("g3", -10, 80); //initial targetP : 30MW
        s = Scalable.onGenerator("s"); //initial targetP : 0MW
        unknownGenerator = Scalable.onGenerator("unknown");

        l1 = Scalable.onLoad("l1"); //initial P0 : 100MW
        l2 = Scalable.onLoad("l2", 20, 80); //initial P0 : 80MW
        l3 = Scalable.onLoad("l3", -50, 100); //initial P0 : 50MW
        unknownLoad = Scalable.onLoad("unknown");
        unknownDanglingLine = Scalable.onDanglingLine("unknown");
        dl1 = Scalable.onDanglingLine("dl1", 20, 80); //initial P0 : 50MW
    }

    private void reset() {
        Scalable.stack(g1, g2, g3).reset(network);
        Scalable.stack(l1, l2, s, unknownGenerator, unknownLoad, unknownDanglingLine, dl1).reset(network);
        l3.reset(network);
    }

    @Test
    void testScaleOnGeneratorsStackingUp() {
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("scaling", "default").build();
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        ScalingParameters scalingParameters = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, ONESHOT, true, DELTA_P);

        // Proportional to Target P
        StackScalable stackScalable = Scalable.stack(generatorList);
        double variationDone = stackScalable.scale(network, 100.0, scalingParameters);
        scalingReport(reportNode,
            "generators",
            scalingParameters.getScalingType(),
            100.0, variationDone);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(150.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(80.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(30.0, network.getGenerator("g3").getTargetP(), 1e-5);
        reset();
    }

    @Test
    void testScaleOnGeneratorsStackingTargetPMoreThanCurrent() {
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("scaling", "default").build();
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        ScalingParameters scalingParameters = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, ONESHOT, true, TARGET_P);

        // Proportional to Target P
        StackScalable stackScalable = Scalable.stack(generatorList);
        double variationDone = stackScalable.scale(network, 300.0, scalingParameters);
        scalingReport(reportNode,
            "generators",
            scalingParameters.getScalingType(),
            300.0, variationDone);
        assertEquals(140.0, variationDone, 1e-5);
        assertEquals(150.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(100.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(50.0, network.getGenerator("g3").getTargetP(), 1e-5);
        reset();
    }

    @Test
    void testScaleOnGeneratorsStackingTargetPLessThanCurrent() {
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("scaling", "default").build();
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        ScalingParameters scalingParameters = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, ONESHOT, true, TARGET_P);

        // Proportional to Target P
        StackScalable stackScalable = Scalable.stack(generatorList);
        double variationDone = stackScalable.scale(network, 100.0, scalingParameters);
        scalingReport(reportNode,
            "generators",
            scalingParameters.getScalingType(),
            100.0, variationDone);
        assertEquals(-60.0, variationDone, 1e-5);
        assertEquals(20.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(50.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(30.0, network.getGenerator("g3").getTargetP(), 1e-5);
        reset();
    }

    @Test
    void testMaxValueBoundsScalingUp() {
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("scaling", "default").build();
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        ScalingParameters scalingParameters = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, ONESHOT, true, DELTA_P);

        double initialValue = generatorList.stream().mapToDouble(Generator::getTargetP).sum();
        double maxValue = initialValue + 75.0;

        // Proportional to Target P
        StackScalable stackScalable = Scalable.stack(generatorList, -Double.MAX_VALUE, maxValue);
        double variationDone = stackScalable.scale(network, 100.0, scalingParameters);
        scalingReport(reportNode,
            "generators",
            scalingParameters.getScalingType(),
            100.0, variationDone);
        assertEquals(75.0, variationDone, 1e-5);
        assertEquals(150.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(55.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(30.0, network.getGenerator("g3").getTargetP(), 1e-5);
        reset();
    }

    @Test
    void testMinValueBoundsScalingDown() {
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("scaling", "default").build();
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        ScalingParameters scalingParameters = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, ONESHOT, true, DELTA_P);

        double initialValue = generatorList.stream().mapToDouble(Generator::getTargetP).sum();
        double minValue = initialValue - 75.0;

        // Proportional to Target P
        StackScalable stackScalable = Scalable.stack(generatorList, minValue, Double.MAX_VALUE);
        double variationDone = stackScalable.scale(network, -100.0, scalingParameters);
        scalingReport(reportNode,
            "generators",
            scalingParameters.getScalingType(),
            -100.0, variationDone);
        assertEquals(-75.0, variationDone, 1e-5);
        assertEquals(5.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(50.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(30.0, network.getGenerator("g3").getTargetP(), 1e-5);
        reset();
    }

    @Test
    void testDisableInjections() {
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("scaling", "default").build();
        List<Injection<?>> injectionsList = Arrays.asList(
            network.getGenerator("g1"), network.getGenerator("g2"),
            network.getDanglingLine("dl1"),
            network.getLoad("l1"), network.getLoad("l2"));
        StackScalable stackScalable;
        double variationDone;

        // Stack scalable with scalables on l1, g1 and dl1 disabled, should saturate g2, and use part of l2
        ScalingParameters scalingParameters = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, RESPECT_OF_VOLUME_ASKED, false, DELTA_P);
        scalingParameters.setIgnoredInjectionIds(Set.of("l1", "g1", "dl1"));
        stackScalable = Scalable.stack(injectionsList);
        double volumeAsked = 100.;
        variationDone = stackScalable.scale(network, volumeAsked, scalingParameters);
        scalingReport(reportNode,
            "generators, loads and dangling lines",
            scalingParameters.getScalingType(),
            volumeAsked, variationDone);
        assertEquals(volumeAsked, variationDone, 1e-5);
        assertEquals(80.0, network.getGenerator("g1").getTargetP(), 1e-5); //skipped, initial P
        assertEquals(100., network.getGenerator("g2").getTargetP(), 1e-5); //saturated, Pmax
        assertEquals(50.0, network.getDanglingLine("dl1").getP0(), 1e-5); //skipped, initial P
        assertEquals(100.0, network.getLoad("l1").getP0(), 1e-5); //skipped, initial P
        assertEquals(80. - 50., network.getLoad("l2").getP0(), 1e-5); //remaining P on this scalable
        reset();
    }
}
