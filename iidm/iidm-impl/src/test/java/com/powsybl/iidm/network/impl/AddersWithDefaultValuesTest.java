package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Anne Tilloy <anne.tilloy@rte-france.com>
 */
public class AddersWithDefaultValuesTest {

    @Test
    public void createGenerator() {
        Network network = Network.create("network-test", "test");
        network.setAddersWithDefaultValues(true);
        Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        vl1.newGenerator()
                .setId("g1")
                .setBus("b1")
                .setTargetP(120.0)
                .setVoltageRegulatorOn(false)
                .setTargetQ(121.0)
                .add();

        Generator gen = network.getGenerator("g1");
        assertEquals(Double.NaN, gen.getTargetV(), 0);
        assertEquals(121.0, gen.getTargetQ(), 0);
        assertEquals(120.0, gen.getTargetP(), 0);
        assertEquals(EnergySource.OTHER, gen.getEnergySource());
        assertEquals(gen.getTerminal(), gen.getRegulatingTerminal());
        assertEquals(Double.MAX_VALUE, gen.getMaxP(), 0);
        assertEquals(-Double.MAX_VALUE, gen.getMinP(), 0);

        vl1.newGenerator()
                .setId("g2")
                .setBus("b1")
                .setTargetP(10.0)
                .setTargetV(402.0)
                .setVoltageRegulatorOn(true)
                .add();

        Generator gen2 = network.getGenerator("g2");
        assertEquals(402.0, gen2.getTargetV(), 0.0);
        assertEquals(Double.NaN, gen2.getTargetQ(), 0.0);
        assertEquals(10.0, gen2.getTargetP(), 0.0);
        assertEquals(EnergySource.OTHER, gen2.getEnergySource());
        assertEquals(gen2.getTerminal(), gen2.getRegulatingTerminal());
        assertEquals(Double.MAX_VALUE, gen2.getMaxP(), 0);
        assertEquals(-Double.MAX_VALUE, gen2.getMinP(), 0);
    }

    @Test
    public void createStaticVarCompensator() {
        Network network = Network.create("network-test", "test");
        network.setAddersWithDefaultValues(true);
        Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        vl1.newStaticVarCompensator()
                .setId("svc1")
                .setBus("b1")
                .setVoltageSetpoint(445.0)
                .add();

        StaticVarCompensator svc = network.getStaticVarCompensator("svc1");
        assertEquals(445, svc.getVoltageSetpoint(), 0);
        assertEquals(Double.NaN, svc.getReactivePowerSetpoint(), 0);
        assertEquals(svc.getTerminal(), svc.getRegulatingTerminal());
        assertEquals(-Double.MAX_VALUE, svc.getBmin(), 0);
        assertEquals(Double.MAX_VALUE, svc.getBmax(), 0);
        assertEquals(StaticVarCompensator.RegulationMode.OFF, svc.getRegulationMode());
    }

    @Test
    public void createLoad() {
        Network network = Network.create("network-test", "test");
        network.setAddersWithDefaultValues(true);
        Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        vl1.newLoad()
                .setId("load1")
                .setBus("b1")
                .setP0(120.0)
                .add();

        Load load = network.getLoad("load1");
        assertEquals(120.0, load.getP0(), 0.0);
        assertEquals(0.0, load.getQ0(), 0.0);
        assertEquals(LoadType.UNDEFINED, load.getLoadType());
    }

    @Test
    public void createBattery() {
        Network network = Network.create("network-test", "test");
        network.setAddersWithDefaultValues(true);
        Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        vl1.newBattery()
                .setId("battery1")
                .setBus("b1")
                .setP0(120.0)
                .add();

        Battery battery = network.getBattery("battery1");
        assertEquals(120.0, battery.getP0(), 0.0);
        assertEquals(0.0, battery.getQ0(), 0.0);
        assertEquals(-Double.MAX_VALUE, battery.getMinP(), 0);
        assertEquals(Double.MAX_VALUE, battery.getMaxP(), 0);
    }

    @Test
    public void createTwoWindingsTransformerWithRtc() {
        Network network = Network.create("network-test", "test");
        network.setAddersWithDefaultValues(true);
        Substation s2 = network.newSubstation()
                .setId("S2")
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("vl2")
                .setNominalV(140.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
        VoltageLevel vl3 = s2.newVoltageLevel()
                .setId("vl3")
                .setNominalV(33.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl3.getBusBreakerView().newBus()
                .setId("b3")
                .add();

        TwoWindingsTransformer t2wt = network.getSubstation("S2").newTwoWindingsTransformer()
                .setId("T2wT")
                .setVoltageLevel1("vl2")
                .setVoltageLevel2("vl3")
                .setR(17.0)
                .setX(10.0)
                .setBus1("b2")
                .setBus2("b3")
                .add();

        t2wt.newRatioTapChanger()
                .beginStep()
                .setRho(0.9)
                .endStep()
                .beginStep()
                .setRho(1.0)
                .endStep()
                .beginStep()
                .setRho(1.05)
                .endStep()
                .beginStep()
                .setRho(1.1)
                .endStep()
                .setRegulating(true)
                .setRegulationTerminal(t2wt.getTerminal2())
                .add();

        assertEquals(140.0, t2wt.getRatedU1(), 0.0);
        assertEquals(33.0, t2wt.getRatedU2(), 0.0);
        assertEquals(33.0, t2wt.getRatioTapChanger().getTargetV(), 0.0);
        assertEquals(1, t2wt.getRatioTapChanger().getTapPosition(), 0);
        assertEquals(0, t2wt.getRatioTapChanger().getLowTapPosition(), 0);
        assertEquals(0.0, t2wt.getRatioTapChanger().getTargetDeadband(), 0);
        assertEquals(false, t2wt.getRatioTapChanger().hasLoadTapChangingCapabilities());
        assertEquals(0.0, t2wt.getB(), 0);
        assertEquals(0.0, t2wt.getG(), 0);
    }

    @Test
    public void createTwoWindingsTransformerWithPtc() {
        Network network = Network.create("network-test", "test");
        network.setAddersWithDefaultValues(true);
        Substation s2 = network.newSubstation()
                .setId("S2")
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("vl2")
                .setNominalV(140.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
        VoltageLevel vl3 = s2.newVoltageLevel()
                .setId("vl3")
                .setNominalV(33.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl3.getBusBreakerView().newBus()
                .setId("b3")
                .add();

        TwoWindingsTransformer t2wt = network.getSubstation("S2").newTwoWindingsTransformer()
                .setId("T2wT")
                .setVoltageLevel1("vl2")
                .setVoltageLevel2("vl3")
                .setRatedU1(145.0)
                .setR(17.0)
                .setX(10.0)
                .setBus1("b2")
                .setBus2("b3")
                .add();

        t2wt.newPhaseTapChanger()
                .beginStep()
                .setAlpha(-10)
                .endStep()
                .beginStep()
                .setAlpha(-5)
                .endStep()
                .beginStep()
                .setAlpha(0)
                .endStep()
                .beginStep()
                .setAlpha(5)
                .endStep()
                .setRegulating(false)
                .setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setRegulationTerminal(t2wt.getTerminal2())
                .add();

        assertEquals(145.0, t2wt.getRatedU1(), 0.0);
        assertEquals(33.0, t2wt.getRatedU2(), 0.0);
        assertTrue(!t2wt.getPhaseTapChanger().isRegulating());
        assertEquals(2, t2wt.getPhaseTapChanger().getTapPosition(), 0);
        assertEquals(0, t2wt.getPhaseTapChanger().getLowTapPosition(), 0);
        assertEquals(0.0, t2wt.getPhaseTapChanger().getTargetDeadband(), 0);
        assertEquals(0.0, t2wt.getB(), 0);
        assertEquals(0.0, t2wt.getG(), 0);
    }

    @Test
    public void createLinearShuntCompensator() {
        Network network = Network.create("network-test", "test");
        network.setAddersWithDefaultValues(true);
        Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        vl1.newShuntCompensator()
                .setId("shunt1")
                .setBus("b1")
                .setTargetV(400.0)
                .setSectionCount(1)
                .setVoltageRegulatorOn(true)
                .newLinearModel()
                .setMaximumSectionCount(25)
                .setBPerSection(-1E-2)
                .add()
                .add();

        ShuntCompensator shunt = network.getShuntCompensator("shunt1");
        assertEquals(1, shunt.getSectionCount(), 0);
        assertEquals(400, shunt.getTargetV(), 0.0);
        assertEquals(0.0, shunt.getTargetDeadband(), 0.0);
        assertEquals(-1E-2, shunt.getB(), 0.0);
        assertEquals(0.0, shunt.getG(shunt.getSectionCount()), 0.0);
        assertEquals(true, shunt.isVoltageRegulatorOn());
        assertEquals(shunt.getTerminal(), shunt.getRegulatingTerminal());
    }

    @Test
    public void createNonLinearShuntCompensator() {
        Network network = Network.create("network-test", "test");
        network.setAddersWithDefaultValues(true);
        Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        vl1.newShuntCompensator()
                .setId("shunt1")
                .setBus("b1")
                .setSectionCount(1)
                .newNonLinearModel()
                .beginSection()
                .setB(0.0001)
                .endSection()
                .beginSection()
                .setB(0.0002)
                .endSection()
                .add()
                .add();

        ShuntCompensator shunt = network.getShuntCompensator("shunt1");
        assertEquals(1, shunt.getSectionCount(), 0);
        assertEquals(Double.NaN, shunt.getTargetV(), 0.0);
        assertEquals(0.0, shunt.getTargetDeadband(), 0.0);
        assertEquals(0.0001, shunt.getB(), 0.0);
        assertEquals(0.0, shunt.getG(shunt.getSectionCount()), 0.0);
        assertEquals(shunt.getTerminal(), shunt.getRegulatingTerminal());
        assertEquals(false, shunt.isVoltageRegulatorOn());
    }

    @Test
    public void createDanglingLine() {
        Network network = Network.create("dl", "test");
        network.setAddersWithDefaultValues(true);
        Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();

        vl1.newDanglingLine()
                .setId("dl1")
                .setBus("b1")
                .setR(0.7)
                .setX(1)
                .setP0(101)
                .newGeneration()
                .setTargetQ(10.0)
                .setVoltageRegulationOn(false)
                .add()
                .add();

        DanglingLine dl = network.getDanglingLine("dl1");
        assertEquals(101, dl.getP0(), 0.0);
        assertEquals(0, dl.getQ0(), 0.0);
        assertEquals(0.0, dl.getB(), 0.0);
        assertEquals(0.0, dl.getG(), 0.0);
        assertEquals(10.0, dl.getGeneration().getTargetQ(), 0.0);
        assertEquals(0.0, dl.getGeneration().getTargetP(), 0.0);
    }

    @Test
    public void createLine() {
        Network network = Network.create("network-test", "test");
        Substation s2 = network.newSubstation()
                .setId("S2")
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("vl2")
                .setNominalV(140.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
        VoltageLevel vl3 = s2.newVoltageLevel()
                .setId("vl3")
                .setNominalV(140.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl3.getBusBreakerView().newBus()
                .setId("b3")
                .add();
        network.newLine()
                .setVoltageLevel1("vl2")
                .setVoltageLevel2("vl3")
                .setBus1("b2")
                .setBus2("b3")
                .setId("line")
                .setX(10.0)
                .setR(1.0)
                .add();

        Line line = network.getLine("line");
        assertEquals(0.0, line.getB1(), 0.0);
        assertEquals(0.0, line.getB2(), 0.0);
        assertEquals(0.0, line.getG1(), 0.0);
        assertEquals(0.0, line.getG2(), 0.0);
    }

    @Test
    public void createTieLine() {
        Network network = Network.create("network-test", "test");
        network.setAddersWithDefaultValues(true);
        Substation s2 = network.newSubstation()
                .setId("S2")
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("vl2")
                .setNominalV(140.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
        VoltageLevel vl3 = s2.newVoltageLevel()
                .setId("vl3")
                .setNominalV(140.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl3.getBusBreakerView().newBus()
                .setId("b3")
                .add();
        network.newTieLine()
                .setVoltageLevel1("vl2")
                .setVoltageLevel2("vl3")
                .setBus1("b2")
                .setBus2("b3")
                .setId("tieline")
                .setUcteXnodeCode("xnode")
                .newHalfLine1()
                .setId("half1")
                .setX(1.0)
                .setR(10.0)
                .add()
                .newHalfLine2()
                .setId("half2")
                .setX(1.1)
                .setR(10.1)
                .add()
                .add();

        Line line = network.getLine("tieline");
        assertEquals(0.0, line.getB1(), 0.0);
        assertEquals(0.0, line.getB2(), 0.0);
        assertEquals(0.0, line.getG1(), 0.0);
        assertEquals(0.0, line.getG2(), 0.0);
    }

    @Test
    public void createHvdcLine() {
        Network network = Network.create("network-test", "test");
        network.setAddersWithDefaultValues(true);
        Substation s2 = network.newSubstation()
                .setId("S2")
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("vl2")
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
        VoltageLevel vl3 = s2.newVoltageLevel()
                .setId("vl3")
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl3.getBusBreakerView().newBus()
                .setId("b3")
                .add();

        vl2.newVscConverterStation()
                .setId("vsc1")
                .setBus("b2")
                .setVoltageRegulatorOn(true)
                .setVoltageSetpoint(401)
                .setLossFactor(1.0f)
                .add();

        VscConverterStation station1 = network.getVscConverterStation("vsc1");
        assertEquals(Double.NaN, station1.getReactivePowerSetpoint(), 0.0);
        assertEquals(station1.getTerminal(), station1.getRegulatingTerminal());

        vl3.newVscConverterStation()
                .setId("vsc2")
                .setBus("b3")
                .setVoltageRegulatorOn(false)
                .setReactivePowerSetpoint(200)
                .setLossFactor(1.0f)
                .add();

        VscConverterStation station2 = network.getVscConverterStation("vsc2");
        assertEquals(200.0, station2.getReactivePowerSetpoint(), 0.0);
        assertEquals(station2.getTerminal(), station2.getRegulatingTerminal());

        HvdcLine hvdc = network.newHvdcLine()
                .setId("hvdc")
                .setConverterStationId1("vsc1")
                .setConverterStationId2("vsc2")
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)
                .setActivePowerSetpoint(250)
                .add();

        assertEquals(0.0, hvdc.getR(), 0.0);
        assertEquals(380.0, hvdc.getNominalV(), 0.0);
        assertEquals(Double.MAX_VALUE, hvdc.getMaxP(), 0.0);
    }

    @Test
    public void createThreeWindingsTransformerWithRtc() {
        Network network = Network.create("network-test", "test");
        network.setAddersWithDefaultValues(true);
        Substation s2 = network.newSubstation()
                .setId("S2")
                .add();
        VoltageLevel vl1 = s2.newVoltageLevel()
                .setId("vl1")
                .setNominalV(225.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("vl2")
                .setNominalV(140.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
        VoltageLevel vl3 = s2.newVoltageLevel()
                .setId("vl3")
                .setNominalV(33.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl3.getBusBreakerView().newBus()
                .setId("b3")
                .add();

        ThreeWindingsTransformer t3wt = network.getSubstation("S2").newThreeWindingsTransformer()
                .setId("T3wT")
                .newLeg1()
                .setR(1.1)
                .setX(10.0)
                .setVoltageLevel("vl1")
                .setBus("b1")
                .add()
                .newLeg2()
                .setR(1.2)
                .setX(10.2)
                .setVoltageLevel("vl2")
                .setBus("b2")
                .add()
                .newLeg3()
                .setR(1.3)
                .setX(10.3)
                .setVoltageLevel("vl3")
                .setBus("b3")
                .add()
                .add();

        t3wt.getLeg1().newRatioTapChanger()
                .beginStep()
                .setRho(0.9)
                .endStep()
                .beginStep()
                .setRho(1.0)
                .endStep()
                .beginStep()
                .setRho(1.05)
                .endStep()
                .beginStep()
                .setRho(1.1)
                .endStep()
                .setRegulating(true)
                .setRegulationTerminal(t3wt.getLeg1().getTerminal())
                .add();

        assertEquals(t3wt.getLeg1().getTerminal().getVoltageLevel().getNominalV(), t3wt.getRatedU0(), 0.0);
        assertEquals(0.0, t3wt.getLeg1().getG(), 0.0);
        assertEquals(0.0, t3wt.getLeg2().getB(), 0.0);
        assertEquals(0.0, t3wt.getLeg2().getB(), 0.0);
        assertEquals(t3wt.getTerminal(ThreeWindingsTransformer.Side.ONE).getVoltageLevel().getNominalV(), t3wt.getLeg1().getRatedU(), 0.0);
        assertEquals(t3wt.getTerminal(ThreeWindingsTransformer.Side.TWO).getVoltageLevel().getNominalV(), t3wt.getLeg2().getRatedU(), 0.0);
        assertEquals(t3wt.getTerminal(ThreeWindingsTransformer.Side.THREE).getVoltageLevel().getNominalV(), t3wt.getLeg3().getRatedU(), 0.0);
    }
}
