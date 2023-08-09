/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.modification.scalable;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.powsybl.iidm.modification.scalable.Scalable.scaleOnGenerators;
import static com.powsybl.iidm.modification.scalable.Scalable.scaleOnLoads;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.DistributionMode.*;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.Priority.*;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.ScalingType.DELTA_P;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.ScalingType.TARGET_P;
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

        network = createNetwork();
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

    static Network createNetwork() {
        Network network = Network.create("network", "test");
        Substation s = network.newSubstation()
            .setId("s")
            .setCountry(Country.US)
            .add();
        VoltageLevel vl = s.newVoltageLevel()
            .setId("vl1")
            .setNominalV(380.0)
            .setLowVoltageLimit(0.8 * 380.0)
            .setHighVoltageLimit(1.2 * 380.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        vl.getBusBreakerView().newBus()
            .setId("bus1")
            .add();
        vl.newGenerator()
            .setId("g1")
            .setBus("bus1")
            .setConnectableBus("bus1")
            .setMinP(0.0)
            .setMaxP(150.0)
            .setTargetP(80.0)
            .setVoltageRegulatorOn(false)
            .setTargetQ(0.0)
            .add();
        vl.newGenerator()
            .setId("g2")
            .setBus("bus1")
            .setConnectableBus("bus1")
            .setMinP(10.0)
            .setMaxP(100.0)
            .setTargetP(50.0)
            .setVoltageRegulatorOn(false)
            .setTargetQ(0.0)
            .add();
        vl.newGenerator()
            .setId("g3")
            .setBus("bus1")
            .setConnectableBus("bus1")
            .setMinP(20.0)
            .setMaxP(80.0)
            .setTargetP(30.0)
            .setVoltageRegulatorOn(true)
            .setTargetV(1.0)
            .add();
        vl.newLoad()
            .setId("l1")
            .setBus("bus1")
            .setConnectableBus("bus1")
            .setP0(100.0)
            .setQ0(0.0)
            .setLoadType(LoadType.UNDEFINED)
            .add();
        vl.newLoad()
            .setId("l2")
            .setBus("bus1")
            .setConnectableBus("bus1")
            .setP0(80.0)
            .setQ0(0.0)
            .setLoadType(LoadType.UNDEFINED)
            .add();
        vl.newLoad()
            .setId("l3")
            .setBus("bus1")
            .setConnectableBus("bus1")
            .setP0(50.0)
            .setQ0(0.0)
            .setLoadType(LoadType.UNDEFINED)
            .add();

        VoltageLevel vl2 = s.newVoltageLevel()
            .setId("vl2")
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .setNominalV(380)
            .add();
        vl2.getBusBreakerView().newBus()
            .setId("bus2")
            .add();
        network.newLine()
            .setId("l12")
            .setVoltageLevel1("vl1")
            .setConnectableBus1("bus1")
            .setBus1("bus1")
            .setVoltageLevel2("vl2")
            .setConnectableBus2("bus2")
            .setBus2("bus2")
            .setR(1)
            .setX(1)
            .setG1(0)
            .setG2(0)
            .setB1(0)
            .setB2(0)
            .add();
        return network;
    }

    private void reset() {

        Scalable.stack(g1, g2, g3).reset(network);
        Scalable.stack(l1, l2, s, unknownGenerator, unknownLoad, unknownDanglingLine, dl1).reset(network);
        l3.reset(network);
    }

    @Test
    void testScaleOnLoads() {
        // Parameters
        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Load> loadList = Arrays.asList(network.getLoad("l1"), network.getLoad("l2"), network.getLoad("l3"));
        double variationDone;

        // Proportional to P0
        ScalingParameters scalingParametersProportional = new ScalingParameters(Scalable.ScalingConvention.LOAD,
            true, true, VOLUME, true,
            PROPORTIONAL_TO_P0, DELTA_P, 100.0);
        variationDone = scaleOnLoads(network, reporterModel, scalingParametersProportional, loadList);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(100.0 * (1.0 + 100 / 230.0), network.getLoad("l1").getP0(), 1e-5);
        assertEquals(80 * (1.0 + 100 / 230.0), network.getLoad("l2").getP0(), 1e-5);
        assertEquals(50.0 * (1.0 + 100 / 230.0), network.getLoad("l3").getP0(), 1e-5);
        reset();

        // Regular distribution
        ScalingParameters scalingParametersRegular = new ScalingParameters(Scalable.ScalingConvention.LOAD,
            true, false, VOLUME, true,
            REGULAR_DISTRIBUTION, TARGET_P, 100.0);
        variationDone = scaleOnLoads(network, reporterModel, scalingParametersRegular, loadList);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(100.0 / 3.0, network.getLoad("l1").getP0(), 1e-5);
        assertEquals(100.0 / 3.0, network.getLoad("l2").getP0(), 1e-5);
        assertEquals(100.0 / 3.0, network.getLoad("l3").getP0(), 1e-5);
        reset();

        // Error in other cases
        ScalingParameters scalingParametersError = new ScalingParameters(Scalable.ScalingConvention.LOAD,
            true, false, VOLUME, true,
            PROPORTIONAL_TO_PMAX, TARGET_P, 100.0);
        IllegalArgumentException e0 = assertThrows(IllegalArgumentException.class, () -> scaleOnLoads(
            network,
            reporterModel,
            scalingParametersError,
            loadList));
        assertEquals("Variation mode cannot be PROPORTIONAL_TO_PMAX for LoadScalables", e0.getMessage());
    }

    @Test
    void testScaleOnGenerators() {
        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        double variationDone;

        // Proportional to Target P
        ScalingParameters scalingParametersProportionalTarget = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, VOLUME, true,
            PROPORTIONAL_TO_TARGETP, DELTA_P, 100.0);
        variationDone = scaleOnGenerators(network, reporterModel, scalingParametersProportionalTarget, generatorList);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(80.0 * (1.0 + 100 / 160.0), network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(50.0 * (1.0 + 100 / 160.0), network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(30.0 * (1.0 + 100 / 160.0), network.getGenerator("g3").getTargetP(), 1e-5);
        reset();

        // Proportional to P_max
        ScalingParameters scalingParametersProportionalPMax = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, VOLUME, true,
            PROPORTIONAL_TO_PMAX, DELTA_P, 100.0);
        variationDone = scaleOnGenerators(network, reporterModel, scalingParametersProportionalPMax, generatorList);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(150.0 * 100.0 / 330.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(100.0 * 100.0 / 330.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(80.0 * 100.0 / 330.0, network.getGenerator("g3").getTargetP(), 1e-5);
        reset();

        // Proportional to the available P
        ScalingParameters scalingParametersProportionalAvailableP = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, VOLUME, true,
            PROPORTIONAL_TO_DIFF_PMAX_TARGETP, DELTA_P, 100.0);
        variationDone = scaleOnGenerators(network, reporterModel, scalingParametersProportionalAvailableP, generatorList);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(150.0 * 100.0 / 330.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(100.0 * 100.0 / 330.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(80.0 * 100.0 / 330.0, network.getGenerator("g3").getTargetP(), 1e-5);
        reset();

        // Regular distribution
        ScalingParameters scalingParametersRegular = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, false, VOLUME, true,
            REGULAR_DISTRIBUTION, TARGET_P, 100.0);
        variationDone = scaleOnGenerators(network, reporterModel, scalingParametersRegular, generatorList);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(100.0 / 3.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(100.0 / 3.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(100.0 / 3.0, network.getGenerator("g3").getTargetP(), 1e-5);
        reset();

        // Error in other cases
        ScalingParameters scalingParametersError = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, false, VOLUME, true,
            PROPORTIONAL_TO_P0, TARGET_P, 100.0);
        IllegalArgumentException e0 = assertThrows(IllegalArgumentException.class, () -> scaleOnGenerators(
            network,
            reporterModel,
            scalingParametersError,
            generatorList));
        assertEquals("Variation mode cannot be PROPORTIONAL_TO_P0 for GeneratorScalables", e0.getMessage());
    }

    @Test
    void testScaleOnGeneratorsUsedPower() {
        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        double variationDone;

        // Proportional to the used P
        ScalingParameters scalingParametersProportionalUsedP = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, VOLUME, true,
            PROPORTIONAL_TO_DIFF_TARGETP_PMIN, DELTA_P, 100.0);
        variationDone = scaleOnGenerators(network, reporterModel, scalingParametersProportionalUsedP, generatorList);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(80.0 + 80.0 * 100 / 130.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(50.0 + 40.0 * 100 / 130.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(30.0 + 10.0 * 100 / 130.0, network.getGenerator("g3").getTargetP(), 1e-5);
        reset();
    }

    @Test
    void testScaleOnGeneratorsVentilationPriority() {
        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        double variationDone;

        // Proportional to the used P
        ScalingParameters scalingParametersProportionalUsedP = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, VENTILATION, true,
            PROPORTIONAL_TO_TARGETP, DELTA_P, 200.0);
        variationDone = scaleOnGenerators(network, reporterModel, scalingParametersProportionalUsedP, generatorList);
        assertEquals(200.0 * 0.7, variationDone, 1e-5);
        assertEquals(80.0 * (1.0 + 200.0 * 0.7 / 160.0), network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(50.0 * (1.0 + 200.0 * 0.7 / 160.0), network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(30.0 * (1.0 + 200.0 * 0.7 / 160.0), network.getGenerator("g3").getTargetP(), 1e-5);
        reset();
    }

    @Test
    void testScaleOnGeneratorsStackingUp() {
        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        double variationDone;

        // Proportional to the used P
        ScalingParameters scalingParametersProportionalUsedP = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, ONESHOT, true,
            STACKING_UP, DELTA_P, 100.0);
        variationDone = scaleOnGenerators(network, reporterModel, scalingParametersProportionalUsedP, generatorList);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(150.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(80.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(30.0, network.getGenerator("g3").getTargetP(), 1e-5);
        reset();
    }

    @Test
    void testScaleOnGeneratorsWithWrongParameter() {
        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));

        // Set of parameter for loads
        ScalingParameters scalingParametersError = new ScalingParameters(Scalable.ScalingConvention.LOAD,
            true, false, VOLUME, true,
            REGULAR_DISTRIBUTION, TARGET_P, 100.0);

        // Error raised
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> scaleOnGenerators(
            network,
            reporterModel,
            scalingParametersError,
            generatorList));
        assertEquals("Scaling convention in the parameters cannot be LOAD for generators", e0.getMessage());
    }

    @Test
    void testScaleOnLoadsWithWrongParameter() {
        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Load> loadList = Arrays.asList(network.getLoad("l1"), network.getLoad("l2"), network.getLoad("l3"));

        // Set of parameter for loads
        ScalingParameters scalingParametersError = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, false, VOLUME, true,
            REGULAR_DISTRIBUTION, TARGET_P, 100.0);

        // Error raised
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> scaleOnLoads(
            network,
            reporterModel,
            scalingParametersError,
            loadList));
        assertEquals("Scaling convention in the parameters cannot be GENERATOR for loads", e0.getMessage());
    }

    @Test
    void testScaleOnGeneratorsTargetPowerAtZero() {
        // Modifications in the network in order to have a "used power" at zero
        network.getGenerator("g1").setTargetP(0.0);
        network.getGenerator("g2").setTargetP(0.0);
        network.getGenerator("g3").setTargetP(0.0);

        // Parameters
        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        double variationDone;

        // Proportional to the used P
        ScalingParameters scalingParametersProportionalUsedP = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, VOLUME, true,
            PROPORTIONAL_TO_TARGETP, DELTA_P, 100.0);
        variationDone = scaleOnGenerators(network, reporterModel, scalingParametersProportionalUsedP, generatorList);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(0.0 + 100.0 / 3.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(0.0 + 100.0 / 3.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(0.0 + 100.0 / 3.0, network.getGenerator("g3").getTargetP(), 1e-5);
    }

    @Test
    void testScaleOnGeneratorsUsedPowerAtZero() {
        // Modifications in the network in order to have a "used power" at zero
        network.getGenerator("g1").setTargetP(network.getGenerator("g1").getMinP());
        network.getGenerator("g2").setTargetP(network.getGenerator("g2").getMinP());
        network.getGenerator("g3").setTargetP(network.getGenerator("g3").getMinP());

        // Parameters
        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        double variationDone;

        // Proportional to the used P
        ScalingParameters scalingParametersProportionalUsedP = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, VOLUME, true,
            PROPORTIONAL_TO_DIFF_TARGETP_PMIN, DELTA_P, 100.0);
        variationDone = scaleOnGenerators(network, reporterModel, scalingParametersProportionalUsedP, generatorList);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(0.0 + 100.0 / 3.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(10.0 + 100.0 / 3.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(20.0 + 100.0 / 3.0, network.getGenerator("g3").getTargetP(), 1e-5);
    }

    @Test
    void testScaleOnGeneratorsAvailablePowerAtZero() {
        // Modifications in the network in order to have a "used power" at zero
        network.getGenerator("g1").setTargetP(network.getGenerator("g1").getMaxP());
        network.getGenerator("g2").setTargetP(network.getGenerator("g2").getMaxP());
        network.getGenerator("g3").setTargetP(network.getGenerator("g3").getMaxP());

        // Parameters
        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Generator> generatorList = Arrays.asList(network.getGenerator("g1"), network.getGenerator("g2"), network.getGenerator("g3"));
        double variationDone;

        // Proportional to the used P
        ScalingParameters scalingParametersProportionalUsedP = new ScalingParameters(Scalable.ScalingConvention.GENERATOR,
            true, true, VOLUME, true,
            PROPORTIONAL_TO_DIFF_PMAX_TARGETP, DELTA_P, 100.0);
        variationDone = scaleOnGenerators(network, reporterModel, scalingParametersProportionalUsedP, generatorList);
        assertEquals(0.0, variationDone, 1e-5);
        assertEquals(150.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(100.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(80.0, network.getGenerator("g3").getTargetP(), 1e-5);
    }

    @Test
    void testScaleOnLoadsP0AtZero() {
        // Modification of the network
        network.getLoad("l1").setP0(0.0);
        network.getLoad("l2").setP0(0.0);
        network.getLoad("l3").setP0(0.0);

        // Parameters
        ReporterModel reporterModel = new ReporterModel("scaling", "default");
        List<Load> loadList = Arrays.asList(network.getLoad("l1"), network.getLoad("l2"), network.getLoad("l3"));
        double variationDone;

        // Proportional to P0
        ScalingParameters scalingParametersProportional = new ScalingParameters(Scalable.ScalingConvention.LOAD,
            true, false, VOLUME, true,
            PROPORTIONAL_TO_P0, DELTA_P, 100.0);
        variationDone = scaleOnLoads(network, reporterModel, scalingParametersProportional, loadList);
        assertEquals(100.0, variationDone, 1e-5);
        assertEquals(100.0 / 3.0, network.getLoad("l1").getP0(), 1e-5);
        assertEquals(100.0 / 3.0, network.getLoad("l2").getP0(), 1e-5);
        assertEquals(100.0 / 3.0, network.getLoad("l3").getP0(), 1e-5);
        reset();
    }
}
