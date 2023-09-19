/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.modification.scalable;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.powsybl.iidm.modification.scalable.ProportionalScalable.DistributionMode.*;
import static com.powsybl.iidm.modification.scalable.ScalableTestNetwork.createNetworkwithDanglingLineAndBattery;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.Priority.*;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.ScalingType.DELTA_P;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.ScalingType.TARGET_P;
import static com.powsybl.iidm.modification.util.ModificationReports.scalingReport;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProportionalScalableTest {

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

//        reset();
    }

    private void reset() {

        Scalable.stack(g1, g2, g3).reset(network);
        Scalable.stack(l1, l2, s, unknownGenerator, unknownLoad, unknownDanglingLine, dl1).reset(network);
        l3.reset(network);
    }

    @Test
    void testOnInjections() {
        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Injection<?>> injectionsList = Arrays.asList(network.getLoad("l1"), network.getLoad("l2"), network.getDanglingLine("dl1"));
        ProportionalScalable proportionalScalable;
        double variationDone;

        // Proportional to P0
        ScalingParameters scalingParametersProportional = new ScalingParameters(Scalable.ScalingConvention.LOAD,
            true, true, RESPECT_OF_VOLUME_ASKED, true, DELTA_P);
        proportionalScalable = Scalable.proportional(injectionsList, PROPORTIONAL_TO_P0);
        variationDone = proportionalScalable.scale(network, 100.0, scalingParametersProportional);
        scalingReport(reporterModel,
            "loads and dangling lines",
            PROPORTIONAL_TO_P0,
            scalingParametersProportional.getScalingType(),
            100.0, variationDone);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(100.0 * (1.0 + 100 / 230.0), network.getLoad("l1").getP0(), 1e-5);
        assertEquals(80 * (1.0 + 100 / 230.0), network.getLoad("l2").getP0(), 1e-5);
        assertEquals(50.0 * (1.0 + 100 / 230.0), network.getDanglingLine("dl1").getP0(), 1e-5);
        reset();

        // Regular distribution
        ScalingParameters scalingParametersUniform = new ScalingParameters(Scalable.ScalingConvention.LOAD,
            true, false, RESPECT_OF_VOLUME_ASKED, true, DELTA_P);
        proportionalScalable = Scalable.proportional(injectionsList, UNIFORM_DISTRIBUTION);
        variationDone = proportionalScalable.scale(network, 100.0, scalingParametersUniform);
        scalingReport(reporterModel,
            "loads and dangling lines",
            UNIFORM_DISTRIBUTION,
            scalingParametersUniform.getScalingType(),
            100.0, variationDone);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(100.0 / 3.0, network.getLoad("l1").getP0(), 1e-5);
        assertEquals(100.0 / 3.0, network.getLoad("l2").getP0(), 1e-5);
        assertEquals(100.0 / 3.0, network.getDanglingLine("dl1").getP0(), 1e-5);
        reset();
    }

    @Test
    void testOnGenerator() {
        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        ScalingParameters scalingParameters = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, RESPECT_OF_VOLUME_ASKED, true, DELTA_P);
        ProportionalScalable proportionalScalable;
        double variationDone;

        // Proportional to Target P
        proportionalScalable = Scalable.proportional(generatorList, PROPORTIONAL_TO_TARGETP);
        variationDone = proportionalScalable.scale(network, 100.0, scalingParameters);
        scalingReport(reporterModel,
            "generators",
            PROPORTIONAL_TO_TARGETP,
            scalingParameters.getScalingType(),
            100.0, variationDone);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(80.0 * (1.0 + 100 / 160.0), network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(50.0 * (1.0 + 100 / 160.0), network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(30.0 * (1.0 + 100 / 160.0), network.getGenerator("g3").getTargetP(), 1e-5);
        reset();

        // Proportional to P_max
        proportionalScalable = Scalable.proportional(generatorList, PROPORTIONAL_TO_PMAX);
        variationDone = proportionalScalable.scale(network, 100.0, scalingParameters);
        scalingReport(reporterModel,
            "generators",
            PROPORTIONAL_TO_PMAX,
            scalingParameters.getScalingType(),
            100.0, variationDone);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(150.0 * 100.0 / 330.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(100.0 * 100.0 / 330.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(80.0 * 100.0 / 330.0, network.getGenerator("g3").getTargetP(), 1e-5);
        reset();

        // Proportional to the available P
        proportionalScalable = Scalable.proportional(generatorList, PROPORTIONAL_TO_DIFF_PMAX_TARGETP);
        variationDone = proportionalScalable.scale(network, 100.0, scalingParameters);
        scalingReport(reporterModel,
            "generators",
            PROPORTIONAL_TO_DIFF_PMAX_TARGETP,
            scalingParameters.getScalingType(),
            100.0, variationDone);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(150.0 * 100.0 / 330.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(100.0 * 100.0 / 330.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(80.0 * 100.0 / 330.0, network.getGenerator("g3").getTargetP(), 1e-5);
        reset();

        // Uniform distribution
        proportionalScalable = Scalable.proportional(generatorList, UNIFORM_DISTRIBUTION);
        variationDone = proportionalScalable.scale(network, 100.0, scalingParameters);
        scalingReport(reporterModel,
            "generators",
            UNIFORM_DISTRIBUTION,
            scalingParameters.getScalingType(),
            100.0, variationDone);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(100.0 / 3.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(100.0 / 3.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(100.0 / 3.0, network.getGenerator("g3").getTargetP(), 1e-5);
        reset();
    }

    @Test
    void testScaleOnGeneratorsUsedPower() {
        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        ScalingParameters scalingParameters = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, RESPECT_OF_VOLUME_ASKED, true, DELTA_P);
        ProportionalScalable proportionalScalable;
        double variationDone;

        // Proportional to Target P
        proportionalScalable = Scalable.proportional(generatorList, PROPORTIONAL_TO_DIFF_TARGETP_PMIN);
        variationDone = proportionalScalable.scale(network, 100.0, scalingParameters);
        assertEquals(100.0, variationDone, 1e-5);
        scalingReport(reporterModel,
            "generators",
            PROPORTIONAL_TO_DIFF_TARGETP_PMIN,
            scalingParameters.getScalingType(),
            100.0, variationDone);
        assertEquals(80.0 + 80.0 * 100 / 130.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(50.0 + 40.0 * 100 / 130.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(30.0 + 10.0 * 100 / 130.0, network.getGenerator("g3").getTargetP(), 1e-5);
        reset();
    }

    @Test
    void testScaleOnGeneratorsWithTargetPScalingType() {
        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        ScalingParameters scalingParameters = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, RESPECT_OF_VOLUME_ASKED, true, TARGET_P);
        ProportionalScalable proportionalScalable;
        double variationDone;

        // Proportional to Target P
        proportionalScalable = Scalable.proportional(generatorList, PROPORTIONAL_TO_TARGETP);
        variationDone = proportionalScalable.scale(network, 260.0, scalingParameters);
        scalingReport(reporterModel,
            "generators",
            PROPORTIONAL_TO_TARGETP,
            scalingParameters.getScalingType(),
            260.0, variationDone);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(80.0 * (1.0 + 100 / 160.0), network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(50.0 * (1.0 + 100 / 160.0), network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(30.0 * (1.0 + 100 / 160.0), network.getGenerator("g3").getTargetP(), 1e-5);
        reset();
    }

    @Test
    void testScaleOnLoadsWithTargetPScalingType() {
        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Load> loadList = Arrays.asList(network.getLoad("l1"), network.getLoad("l2"), network.getLoad("l3"));
        ScalingParameters scalingParameters = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, false, RESPECT_OF_VOLUME_ASKED, true, TARGET_P);
        ProportionalScalable proportionalScalable;
        double variationDone;

        // Proportional to Target P
        proportionalScalable = Scalable.proportional(loadList, PROPORTIONAL_TO_P0);
        variationDone = proportionalScalable.scale(network, -500.0, scalingParameters);
        scalingReport(reporterModel,
            "loads",
            PROPORTIONAL_TO_P0,
            scalingParameters.getScalingType(),
            -500, variationDone);
        assertEquals(-270, variationDone, 1e-5);
        assertEquals(100.0 * (1.0 + 270 / 230.0), network.getLoad("l1").getP0(), 1e-5);
        assertEquals(80.0 * (1.0 + 270 / 230.0), network.getLoad("l2").getP0(), 1e-5);
        assertEquals(50.0 * (1.0 + 270 / 230.0), network.getLoad("l3").getP0(), 1e-5);
        reset();
    }

    @Test
    void testScaleOnGeneratorsVentilationPriority() {
        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        ScalingParameters scalingParameters = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, RESPECT_OF_DISTRIBUTION, true, DELTA_P);
        ProportionalScalable proportionalScalable;
        double variationDone;

        // Proportional to Target P
        proportionalScalable = Scalable.proportional(generatorList, PROPORTIONAL_TO_TARGETP);
        variationDone = proportionalScalable.scale(network, 200.0, scalingParameters);
        scalingReport(reporterModel,
            "generators",
            PROPORTIONAL_TO_TARGETP,
            scalingParameters.getScalingType(),
            200.0, variationDone);
        assertEquals(200.0 * 0.7, variationDone, 1e-5);
        assertEquals(80.0 * (1.0 + 200.0 * 0.7 / 160.0), network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(50.0 * (1.0 + 200.0 * 0.7 / 160.0), network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(30.0 * (1.0 + 200.0 * 0.7 / 160.0), network.getGenerator("g3").getTargetP(), 1e-5);
        reset();
    }

    @Test
    void testScaleOnLoadsVentilationPriority() {
        List<Load> loadList = Collections.singletonList(network.getLoad("l1"));
        ScalingParameters scalingParameters = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, RESPECT_OF_DISTRIBUTION, true, DELTA_P);
        ProportionalScalable proportionalScalable;

        // Proportional to Target P
        proportionalScalable = Scalable.proportional(loadList, PROPORTIONAL_TO_P0);

        // Error raised
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> proportionalScalable.scale(network, 100.0, scalingParameters));
        assertEquals("RESPECT_OF_DISTRIBUTION mode can only be used with a Generator, not class com.powsybl.iidm.network.impl.LoadImpl", e0.getMessage());
        reset();
    }

    @Test
    void testScaleOnGeneratorsWithWrongParametersTargetP() {
        List<DanglingLine> danglinglineList = Collections.singletonList(network.getDanglingLine("dl1"));
        List<Load> loadList = Collections.singletonList(network.getLoad("l1"));
        List<Battery> batteryList = Collections.singletonList(network.getBattery("BAT"));

        // Error raised
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> Scalable.proportional(danglinglineList, PROPORTIONAL_TO_TARGETP));
        assertEquals("Variable TargetP inconsistent with injection type class com.powsybl.iidm.network.impl.DanglingLineImpl", e0.getMessage());

        // Error raised
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> Scalable.proportional(loadList, PROPORTIONAL_TO_TARGETP));
        assertEquals("Variable TargetP inconsistent with injection type class com.powsybl.iidm.network.impl.LoadImpl", e1.getMessage());

        // Error raised
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> Scalable.proportional(batteryList, PROPORTIONAL_TO_TARGETP));
        assertEquals("Unable to create a scalable from class com.powsybl.iidm.network.impl.BatteryImpl", e2.getMessage());
    }

    @Test
    void testScaleOnGeneratorsWithWrongParametersMaxP() {
        List<DanglingLine> danglinglineList = Collections.singletonList(network.getDanglingLine("dl1"));
        List<Load> loadList = Collections.singletonList(network.getLoad("l1"));
        List<Battery> batteryList = Collections.singletonList(network.getBattery("BAT"));

        // Error raised
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> Scalable.proportional(danglinglineList, PROPORTIONAL_TO_PMAX));
        assertEquals("Variable MaxP inconsistent with injection type class com.powsybl.iidm.network.impl.DanglingLineImpl", e0.getMessage());

        // Error raised
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> Scalable.proportional(loadList, PROPORTIONAL_TO_PMAX));
        assertEquals("Variable MaxP inconsistent with injection type class com.powsybl.iidm.network.impl.LoadImpl", e1.getMessage());

        // Error raised
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> Scalable.proportional(batteryList, PROPORTIONAL_TO_PMAX));
        assertEquals("Unable to create a scalable from class com.powsybl.iidm.network.impl.BatteryImpl", e2.getMessage());
    }

    @Test
    @Disabled("Error is raised on TargetP before being raised on MinP")
    void testScaleOnGeneratorsWithWrongParametersMinP() {
        List<DanglingLine> danglinglineList = Collections.singletonList(network.getDanglingLine("dl1"));
        List<Load> loadList = Collections.singletonList(network.getLoad("l1"));
        List<Battery> batteryList = Collections.singletonList(network.getBattery("BAT"));

        // Error raised
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> Scalable.proportional(danglinglineList, PROPORTIONAL_TO_DIFF_TARGETP_PMIN));
        assertEquals("Variable MinP inconsistent with injection type class com.powsybl.iidm.network.impl.DanglingLineImpl", e0.getMessage());

        // Error raised
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> Scalable.proportional(loadList, PROPORTIONAL_TO_DIFF_TARGETP_PMIN));
        assertEquals("Variable MinP inconsistent with injection type class com.powsybl.iidm.network.impl.LoadImpl", e1.getMessage());

        // Error raised
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> Scalable.proportional(batteryList, PROPORTIONAL_TO_DIFF_TARGETP_PMIN));
        assertEquals("Unable to create a scalable from class com.powsybl.iidm.network.impl.BatteryImpl", e2.getMessage());
    }

    @Test
    void testScaleOnLoadsWithWrongParameters() {
        List<Generator> generatorList = Collections.singletonList(network.getGenerator("g1"));
        List<Battery> batteryList = Collections.singletonList(network.getBattery("BAT"));

        // Error raised
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> Scalable.proportional(generatorList, PROPORTIONAL_TO_P0));
        assertEquals("Variable P0 inconsistent with injection type class com.powsybl.iidm.network.impl.GeneratorImpl", e0.getMessage());

        // Error raised
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> Scalable.proportional(batteryList, PROPORTIONAL_TO_P0));
        assertEquals("Unable to create a scalable from class com.powsybl.iidm.network.impl.BatteryImpl", e2.getMessage());
    }

    @Test
    void testScaleOnGeneratorsTargetPowerAtZero() {
        // Modifications in the network in order to have a "used power" at zero
        network.getGenerator("g1").setTargetP(0.0);
        network.getGenerator("g2").setTargetP(0.0);
        network.getGenerator("g3").setTargetP(0.0);

        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        ScalingParameters scalingParameters = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, RESPECT_OF_VOLUME_ASKED, true, DELTA_P);
        ProportionalScalable proportionalScalable;
        double variationDone;

        // Proportional to Target P
        proportionalScalable = Scalable.proportional(generatorList, PROPORTIONAL_TO_TARGETP);
        variationDone = proportionalScalable.scale(network, 100.0, scalingParameters);
        scalingReport(reporterModel,
            "generators",
            PROPORTIONAL_TO_TARGETP,
            scalingParameters.getScalingType(),
            100.0, variationDone);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(0.0 + 100.0 / 3.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(0.0 + 100.0 / 3.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(0.0 + 100.0 / 3.0, network.getGenerator("g3").getTargetP(), 1e-5);
        reset();
    }

    @Test
    void testScaleOnGeneratorsUsedPowerAtZero() {
        // Modifications in the network in order to have a "used power" at zero
        network.getGenerator("g1").setTargetP(network.getGenerator("g1").getMinP());
        network.getGenerator("g2").setTargetP(network.getGenerator("g2").getMinP());
        network.getGenerator("g3").setTargetP(network.getGenerator("g3").getMinP());

        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        ScalingParameters scalingParameters = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, RESPECT_OF_VOLUME_ASKED, true, DELTA_P);
        ProportionalScalable proportionalScalable;
        double variationDone;

        // Proportional to Target P
        proportionalScalable = Scalable.proportional(generatorList, PROPORTIONAL_TO_DIFF_TARGETP_PMIN);
        variationDone = proportionalScalable.scale(network, 100.0, scalingParameters);
        scalingReport(reporterModel,
            "generators",
            PROPORTIONAL_TO_DIFF_TARGETP_PMIN,
            scalingParameters.getScalingType(),
            100.0, variationDone);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(0.0 + 100.0 / 3.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(10.0 + 100.0 / 3.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(20.0 + 100.0 / 3.0, network.getGenerator("g3").getTargetP(), 1e-5);
        reset();
    }

    @Test
    void testScaleOnGeneratorsAvailablePowerAtZero() {
        // Modifications in the network in order to have a "used power" at zero
        network.getGenerator("g1").setTargetP(network.getGenerator("g1").getMaxP());
        network.getGenerator("g2").setTargetP(network.getGenerator("g2").getMaxP());
        network.getGenerator("g3").setTargetP(network.getGenerator("g3").getMaxP());

        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        ScalingParameters scalingParameters = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, RESPECT_OF_VOLUME_ASKED, true, DELTA_P);
        ProportionalScalable proportionalScalable;
        double variationDone;

        // Proportional to Target P
        proportionalScalable = Scalable.proportional(generatorList, PROPORTIONAL_TO_DIFF_TARGETP_PMIN);
        variationDone = proportionalScalable.scale(network, 100.0, scalingParameters);
        scalingReport(reporterModel,
            "generators",
            PROPORTIONAL_TO_DIFF_TARGETP_PMIN,
            scalingParameters.getScalingType(),
            100.0, variationDone);
        assertEquals(0.0, variationDone, 1e-5);
        assertEquals(150.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(100.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(80.0, network.getGenerator("g3").getTargetP(), 1e-5);
        reset();
    }

    @Test
    void testScaleOnLoadsP0AtZero() {
        // Modification of the network
        network.getLoad("l1").setP0(0.0);
        network.getLoad("l2").setP0(0.0);
        network.getLoad("l3").setP0(0.0);

        List<Load> loadList = Arrays.asList(network.getLoad("l1"), network.getLoad("l2"), network.getLoad("l3"));
        ProportionalScalable proportionalScalable;
        double variationDone;

        // Proportional to P0
        ScalingParameters scalingParametersProportional = new ScalingParameters(Scalable.ScalingConvention.LOAD,
            true, false, RESPECT_OF_VOLUME_ASKED, true, DELTA_P);
        proportionalScalable = Scalable.proportional(loadList, PROPORTIONAL_TO_P0);
        variationDone = proportionalScalable.scale(network, 100.0, scalingParametersProportional);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(100.0 / 3.0, network.getLoad("l1").getP0(), 1e-5);
        assertEquals(100.0 / 3.0, network.getLoad("l2").getP0(), 1e-5);
        assertEquals(100.0 / 3.0, network.getLoad("l3").getP0(), 1e-5);
        reset();
    }

    @Test
    void testResizeAskedForVentilation() {

        // Proportional to Target P
        ScalingParameters scalingParametersProportional = new ScalingParameters(Scalable.ScalingConvention.LOAD,
            true, false, RESPECT_OF_DISTRIBUTION, true, DELTA_P);

        // Works for generators in load convention
        List<Generator> generatorList = Collections.singletonList(network.getGenerator("g1"));
        ProportionalScalable proportionalGeneratorsScalable = Scalable.proportional(generatorList, PROPORTIONAL_TO_TARGETP);
        double variationDone = proportionalGeneratorsScalable.scale(network, 100.0, scalingParametersProportional);
        assertEquals(80.0, variationDone, 1e-5);
        assertEquals(0.0, network.getGenerator("g1").getTargetP(), 1e-5);
        reset();

        // Works for generators in load convention with negative asked value
        variationDone = proportionalGeneratorsScalable.scale(network, -200.0, scalingParametersProportional);
        assertEquals(-150.0, variationDone, 1e-5);
        assertEquals(150.0, network.getGenerator("g1").getTargetP(), 1e-5);
        reset();

        // Works for GeneratorScalable
        proportionalGeneratorsScalable = Scalable.proportional(100.0, g1);
        variationDone = proportionalGeneratorsScalable.scale(network, -200.0, scalingParametersProportional);
        assertEquals(-150.0, variationDone, 1e-5);
        assertEquals(150.0, network.getGenerator("g1").getTargetP(), 1e-5);
        reset();

        // Error raised for LoadScalable
        ProportionalScalable proportionalLoadScalable = Scalable.proportional(100.0, l1);
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> proportionalLoadScalable.scale(network, 100.0, scalingParametersProportional));
        assertEquals("RESPECT_OF_DISTRIBUTION mode can only be used with ScalableAdapter or GeneratorScalable, not class com.powsybl.iidm.modification.scalable.LoadScalable", e0.getMessage());

        // Error raised for Loads
        List<Load> loadList = Arrays.asList(network.getLoad("l1"), network.getLoad("l2"), network.getLoad("l3"));
        ProportionalScalable proportionalScalable = Scalable.proportional(loadList, PROPORTIONAL_TO_P0);
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> proportionalScalable.scale(network, 100.0, scalingParametersProportional));
        assertEquals("RESPECT_OF_DISTRIBUTION mode can only be used with a Generator, not class com.powsybl.iidm.network.impl.LoadImpl", e1.getMessage());
    }
}
