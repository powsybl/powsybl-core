/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
class PropertiesSerDeTest extends AbstractIidmSerDeTest {

    public static final String TEST_PROPERTY = "test";

    @Test
    void roundTripTest() throws IOException {
        allFormatsRoundTripAllVersionedXmlTest("eurostag-tutorial-example1-properties.xml");
    }

    private TwoWindingsTransformer createTwoWindingsTransformer(Substation substation) {
        return substation.newTwoWindingsTransformer()
                .setId("twt2")
                .setName("twt2_name")
                .setR(1.0)
                .setX(2.0)
                .setG(3.0)
                .setB(4.0)
                .setRatedU1(5.0)
                .setRatedU2(6.0)
                .setVoltageLevel1("vl1")
                .setVoltageLevel2("vl2")
                .setConnectableBus1("busA")
                .setConnectableBus2("busB")
                .add();
    }

    private void createPhaseTapChanger(PhaseTapChangerHolder ptch) {
        PhaseTapChanger phaseTapChanger = ptch.newPhaseTapChanger()
                .setTapPosition(1)
                .setLowTapPosition(0)
                .setRegulating(false)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .beginStep()
                .setR(1.0)
                .setX(2.0)
                .setG(3.0)
                .setB(4.0)
                .setAlpha(5.0)
                .setRho(6.0)
                .endStep()
                .beginStep()
                .setR(1.0)
                .setX(2.0)
                .setG(3.0)
                .setB(4.0)
                .setAlpha(5.0)
                .setRho(6.0)
                .endStep()
                .add();
        phaseTapChanger.setProperty(TEST_PROPERTY, "valuePhaseTapChanger");
        phaseTapChanger.getCurrentStep().setProperty(TEST_PROPERTY, "value");
    }

    private void createRatioTapChanger(RatioTapChangerHolder rtch) {
        RatioTapChanger ratioTapChanger = rtch.newRatioTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(1)
                .setLoadTapChangingCapabilities(false)
                .beginStep()
                .setR(39.78473)
                .setX(39.784725)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .endStep()
                .beginStep()
                .setR(39.78474)
                .setX(39.784726)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .endStep()
                .beginStep()
                .setR(39.78475)
                .setX(39.784727)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .endStep()
                .add();
        ratioTapChanger.setProperty(TEST_PROPERTY, "valueRatioTapChanger");
        ratioTapChanger.getCurrentStep().setProperty(TEST_PROPERTY, "value");
    }

    @Test
    void propertiesHolderSerDeTest() throws IOException {
        Network network = NetworkTest1Factory.create();

        ReactiveCapabilityCurve reactiveCapabilityCurve = network.getGenerator("generator1").getReactiveLimits(ReactiveCapabilityCurve.class);
        reactiveCapabilityCurve.setProperty(TEST_PROPERTY, "valueReactiveCapabilityCurve");
        reactiveCapabilityCurve.getPoints().iterator().next().setProperty(TEST_PROPERTY, "valueReactiveCapabilityCurvePoint");
        VoltageLevel voltageLevel = network.getVoltageLevel("voltageLevel1");
        voltageLevel.setProperty(TEST_PROPERTY, "valueVoltageLevel");

        Load zipLoad = voltageLevel.newLoad()
                .setId("zipLoad")
                .setNode(3)
                .setP0(10)
                .setQ0(3)
                .newZipModel().setC0p(0.5).setC0q(0.25).setC1p(0.25).setC1q(0.25).setC2p(0.25).setC2q(0.5).add()
                .add();

        zipLoad.setProperty(TEST_PROPERTY, "valueZipLoad");
        zipLoad.getModel().orElseThrow().setProperty(TEST_PROPERTY, "valueZipLoadModel");

        Load expLoad = voltageLevel.newLoad()
                .setId("expLoad")
                .setNode(4)
                .setP0(10)
                .setQ0(3)
                .newExponentialModel().add()
                .add();

        expLoad.setProperty(TEST_PROPERTY, "valueExpLoad");
        expLoad.getModel().orElseThrow().setProperty(TEST_PROPERTY, "valueExpLoadModel");

        ShuntCompensator shuntCompensator = voltageLevel.newShuntCompensator()
                .setId("shunt")
                .setNode(6)
                .setSectionCount(1)
                .setVoltageRegulatorOn(true)
                .setRegulatingTerminal(zipLoad.getTerminal())
                .setTargetV(200)
                .setTargetDeadband(5.0)
                .newLinearModel()
                .setMaximumSectionCount(1)
                .setBPerSection(3)
                .add()
                .add();
        shuntCompensator.setProperty(TEST_PROPERTY, "valueLinearShuntCompensator");
        ShuntCompensatorLinearModel linearModel = (ShuntCompensatorLinearModel) shuntCompensator.getModel();
        linearModel.setProperty(TEST_PROPERTY, "valueLinearShuntCompensatorModel");

        ShuntCompensator nonLinearShuntCompensator = voltageLevel.newShuntCompensator()
                .setId("shuntNonLinear")
                .setNode(8)
                .setSectionCount(1)
                .setVoltageRegulatorOn(true)
                .setRegulatingTerminal(zipLoad.getTerminal())
                .setTargetV(200)
                .setTargetDeadband(5.0)
                .newNonLinearModel()
                .beginSection()
                .setB(1.0)
                .setG(2.0).endSection()
                .add()
                .add();
        nonLinearShuntCompensator.setProperty(TEST_PROPERTY, "valueNonLinearShuntCompensator");
        ShuntCompensatorNonLinearModel nonLinearModel = (ShuntCompensatorNonLinearModel) nonLinearShuntCompensator.getModel();
        nonLinearModel.setProperty(TEST_PROPERTY, "valueNonLinearShuntCompensatorModel");
        for (ShuntCompensatorNonLinearModel.Section s : nonLinearModel.getAllSections()) {
            s.setProperty(TEST_PROPERTY, "valueNonLinearShuntCompensatorModelSection");
        }

        Area defaultControlArea = network.newArea().setId("defaultControlArea").setAreaType(TEST_PROPERTY).add();
        defaultControlArea.setProperty(TEST_PROPERTY, "testValue");

        BusbarSection bb = network.getBusbarSection("voltageLevel1BusbarSection1");
        bb.setProperty(TEST_PROPERTY, "testBusbarSectionValue");

        ExportOptions options = new ExportOptions();
        Network nodeBreakerNetwork = NetworkSerDeTest.writeAndRead(network, options);

        //Check that busbar and its extension is still here
        BusbarSection bb2 = nodeBreakerNetwork.getBusbarSection("voltageLevel1BusbarSection1");
        assertEquals("testBusbarSectionValue", bb2.getProperty(TEST_PROPERTY));
        assertEquals("testValue", nodeBreakerNetwork.getArea("defaultControlArea").getProperty(TEST_PROPERTY));

        Load load1 = nodeBreakerNetwork.getLoad("zipLoad");
        assertEquals("valueZipLoad", load1.getProperty(TEST_PROPERTY));
        assertEquals("valueZipLoadModel", load1.getModel().orElseThrow().getProperty(TEST_PROPERTY));

        Load load2 = nodeBreakerNetwork.getLoad("expLoad");
        assertEquals("valueExpLoad", load2.getProperty(TEST_PROPERTY));
        assertEquals("valueExpLoadModel", load2.getModel().orElseThrow().getProperty(TEST_PROPERTY));

        assertEquals("valueVoltageLevel", nodeBreakerNetwork.getVoltageLevel("voltageLevel1").getProperty(TEST_PROPERTY));
        for (ShuntCompensator shuntCompensator1 : voltageLevel.getShuntCompensators()) {
            ShuntCompensatorModel model1 = shuntCompensator1.getModel();
            if (model1 instanceof ShuntCompensatorNonLinearModel) {
                assertEquals("valueNonLinearShuntCompensator", shuntCompensator1.getProperty(TEST_PROPERTY));
                assertEquals("valueNonLinearShuntCompensatorModel", model1.getProperty(TEST_PROPERTY));
                for (ShuntCompensatorNonLinearModel.Section s : ((ShuntCompensatorNonLinearModel) model1).getAllSections()) {
                    assertEquals("valueNonLinearShuntCompensatorModelSection", s.getProperty(TEST_PROPERTY));
                }
            } else if (model1 instanceof ShuntCompensatorLinearModel) {
                assertEquals("valueLinearShuntCompensator", shuntCompensator1.getProperty(TEST_PROPERTY));
                assertEquals("valueLinearShuntCompensatorModel", model1.getProperty(TEST_PROPERTY));
            }
        }

        ReactiveCapabilityCurve reactiveCapabilityCurve1 = nodeBreakerNetwork.getGenerator("generator1").getReactiveLimits(ReactiveCapabilityCurve.class);
        assertEquals("valueReactiveCapabilityCurve", reactiveCapabilityCurve1.getProperty(TEST_PROPERTY));
        assertEquals("valueReactiveCapabilityCurvePoint", reactiveCapabilityCurve1.getPoints().iterator().next().getProperty(TEST_PROPERTY));

    }

    @Test
    void testTrippings() throws IOException {
        Network network = OverloadManagementSystemSerDeTest.createNetwork();
        for (OverloadManagementSystem.Tripping tripping : network.getOverloadManagementSystem("OMS1").getTrippings()) {
            tripping.setProperty(TEST_PROPERTY, "valueTripping");
        }
        ExportOptions options = new ExportOptions();
        Network network2 = NetworkSerDeTest.writeAndRead(network, options);
        for (OverloadManagementSystem.Tripping tripping : network2.getOverloadManagementSystem("OMS1").getTrippings()) {
            assertEquals("valueTripping", tripping.getProperty(TEST_PROPERTY));
        }
    }

    @Test
    void propertiesHolderSerDeTestTapChangers() throws IOException {
        Network network = NoEquipmentNetworkFactory.create();
        Substation substation = network.getSubstation("sub");
        substation.setProperty(TEST_PROPERTY, "value");
        substation.setProperty("test2", "value2");

        // Check name for two winding transformers
        TwoWindingsTransformer twt2 = createTwoWindingsTransformer(substation);
        twt2.setProperty(TEST_PROPERTY, "twt2Value");
        createPhaseTapChanger(twt2);
        createRatioTapChanger(twt2);

        ExportOptions options = new ExportOptions();
        Network network2 = NetworkSerDeTest.writeAndRead(network, options);
        assertEquals("value", network2.getSubstation("sub").getProperty(TEST_PROPERTY));
        assertEquals("value2", network2.getSubstation("sub").getProperty("test2"));
        TwoWindingsTransformer transformer = network2.getSubstation("sub").getTwoWindingsTransformers().iterator().next();
        assertEquals("twt2Value", transformer.getProperty(TEST_PROPERTY));
        PhaseTapChanger phaseTapChanger = transformer.getPhaseTapChanger();
        RatioTapChanger ratioTapChanger = transformer.getRatioTapChanger();
        assertEquals("valuePhaseTapChanger", phaseTapChanger.getProperty(TEST_PROPERTY));
        assertEquals("valueRatioTapChanger", ratioTapChanger.getProperty(TEST_PROPERTY));
        assertEquals("value", phaseTapChanger.getStep(1).getProperty(TEST_PROPERTY));

    }

    @Test
    void testPowerLimits() throws IOException {
        Network network = DanglingLineNetworkFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00.000+01:00"));
        DanglingLine dl = network.getDanglingLine("DL");
        OperationalLimitsGroup operationalLimitsGroup = dl.getOrCreateSelectedOperationalLimitsGroup();
        ActivePowerLimits activePowerLimit = createLoadingLimits(operationalLimitsGroup::newActivePowerLimits);
        ApparentPowerLimits apparentPowerLimit = createLoadingLimits(operationalLimitsGroup::newApparentPowerLimits);
        CurrentLimits currentLimits = createLoadingLimits(operationalLimitsGroup::newCurrentLimits);
        VoltageAngleLimit voltageAngleLimit = network.newVoltageAngleLimit()
                .from(dl.getTerminal()).to(dl.getTerminal())
                .setId("voltageAngleLimit").setHighLimit(100).setLowLimit(0).add();
        voltageAngleLimit.setProperty(TEST_PROPERTY, "valueVoltageAngleLimit");
        activePowerLimit.setProperty(TEST_PROPERTY, "valueActivePowerLimits");
        apparentPowerLimit.setProperty(TEST_PROPERTY, "valueApparentPowerLimits");
        currentLimits.setProperty(TEST_PROPERTY, "valueCurrentLimits");
        ExportOptions options = new ExportOptions();
        Network network2 = NetworkSerDeTest.writeAndRead(network, options);
        DanglingLine dl2 = network2.getDanglingLine("DL");
        assertEquals("valueActivePowerLimits", dl2.getActivePowerLimits().orElseThrow().getProperty(TEST_PROPERTY));
        assertEquals("valueApparentPowerLimits", dl2.getApparentPowerLimits().orElseThrow().getProperty(TEST_PROPERTY));
        assertEquals("valueCurrentLimits", dl2.getCurrentLimits().orElseThrow().getProperty(TEST_PROPERTY));
        assertEquals("valueVoltageAngleLimit", network2.getVoltageAngleLimit("voltageAngleLimit").getProperty(TEST_PROPERTY));
    }

    private static <L extends LoadingLimits, A extends LoadingLimitsAdder<L, A>> L createLoadingLimits(Supplier<A> limitsAdderSupplier) {
        A adder = limitsAdderSupplier.get()
                .setPermanentLimit(350)
                .beginTemporaryLimit()
                .setValue(370)
                .setAcceptableDuration(20 * 60)
                .setName("20'")
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setValue(380)
                .setAcceptableDuration(10 * 60)
                .setName("10'")
                .endTemporaryLimit();
        adder.setProperty(TEST_PROPERTY, "testLoadingLimit");
        return adder.add();
    }
}
