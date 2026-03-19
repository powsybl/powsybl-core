package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyLevel;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.ObservabilityArea;
import com.powsybl.iidm.network.extensions.ObservabilityAreaAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.ExportOptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Set;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class ObservabilityAreaSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void testNodeBreaker() throws IOException {
        Network network = FictitiousSwitchFactory.create().setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00.000+01:00"));
        network.getVoltageLevel("C").newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByNodes(Set.of(0, 1, 2, 3, 4), 1, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .add();
        network.getVoltageLevel("N").newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByNodes(Set.of(0), 0, ObservabilityArea.ObservabilityStatus.NON_OBSERVABLE)
                .withObservabilityAreaByNodes(Set.of(6), 2, ObservabilityArea.ObservabilityStatus.BORDER)
                .withObservabilityAreaByNodes(Set.of(1, 7, 8, 9, 10, 15, 16, 17, 18, 19, 20, 21, 22), 2, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .add();

        allFormatsRoundTripTest(network, "/nbObservabilityAreaRef1.xml");

        // Topology modification
        network.getSwitch("F").setOpen(true);
        allFormatsRoundTripTest(network, "/nbObservabilityAreaRef2.xml");

        // Structure modification
        network.getSwitch("F").setOpen(false);
        network.getVoltageLevel("C").getNodeBreakerView().removeSwitch("H");
        allFormatsRoundTripTest(network, "/nbObservabilityAreaRef3.xml");
    }

    @Test
    void testBusBreaker() throws IOException {
        Network network = EurostagTutorialExample1Factory.create().setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00.000+01:00"));
        network.getVoltageLevel("VLGEN").newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByBusViewBus("VLGEN_0", 1, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .add();
        network.getVoltageLevel("VLHV1").newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByBusViewBus("VLHV1_0", 1, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .add();
        VoltageLevel vlhv2 = network.getVoltageLevel("VLHV2");
        vlhv2.getBusBreakerView().newBus().setId("test").add();
        vlhv2.newLoad().setId("load").setP0(1.0).setQ0(0.0).setBus("test").add();
        vlhv2.getBusBreakerView().newSwitch().setBus1("NHV2").setBus2("test").setOpen(false).setId("sw").add();
        vlhv2.newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByBusBreakerViewBuses(Set.of("NHV2"), 1, ObservabilityArea.ObservabilityStatus.BORDER)
                .withObservabilityAreaByBusBreakerViewBuses(Set.of("test"), 0, ObservabilityArea.ObservabilityStatus.NON_OBSERVABLE)
                .add();
        VoltageLevel vlload = network.getVoltageLevel("VLLOAD");
        vlload.getBusBreakerView().newBus().setId("test2").add();
        vlload.getBusBreakerView().newSwitch().setId("sw2").setOpen(false).setBus1("NLOAD").setBus2("test2").add();
        vlload.newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByBusViewBus("VLLOAD_0", 0, ObservabilityArea.ObservabilityStatus.NON_OBSERVABLE)
                .add();

        allFormatsRoundTripTest(network, "/bbObservabilityAreaRef1.xml");

        // Topology modification
        network.getSwitch("sw").setOpen(true);
        allFormatsRoundTripTest(network, "/bbObservabilityAreaRef2.xml");

        // Structure modification
        network.getLoad("load").remove();
        vlhv2.getBusBreakerView().removeSwitch("sw");
        vlhv2.getBusBreakerView().removeBus("test");
        allFormatsRoundTripTest(network, "/bbObservabilityAreaRef3.xml");

    }

    @Test
    void testCalculatedBusBranch() throws IOException {
        Network network = EurostagTutorialExample1Factory.create().setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00.000+01:00"));
        network.getVoltageLevel("VLGEN").newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByBusViewBus("VLGEN_0", 1, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .add();
        allFormatsRoundTripTest(network, "/calculatedBusBranchObservabilityAreaRef1.xml", new ExportOptions().setTopologyLevel(TopologyLevel.BUS_BRANCH));
    }

    @Test
    void testCalculatedBusBreaker() throws IOException {
        Network network = FictitiousSwitchFactory.create().setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00.000+01:00"));
        network.getVoltageLevel("C").newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByNodes(Set.of(0, 1, 2, 3, 4), 1, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .add();
        network.getVoltageLevel("N").newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByNodes(Set.of(0), 0, ObservabilityArea.ObservabilityStatus.NON_OBSERVABLE)
                .withObservabilityAreaByNodes(Set.of(6), 2, ObservabilityArea.ObservabilityStatus.BORDER)
                .withObservabilityAreaByNodes(Set.of(1, 7, 8, 9, 10, 15, 16, 17, 18, 19, 20, 21, 22), 2, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .add();

        allFormatsRoundTripTest(network, "/calculatedBusBreakerObservabilityAreaRef1.xml", new ExportOptions().setTopologyLevel(TopologyLevel.BUS_BREAKER));
    }
}
