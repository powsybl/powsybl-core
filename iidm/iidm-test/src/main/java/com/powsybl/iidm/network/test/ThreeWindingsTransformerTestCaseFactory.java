package com.powsybl.iidm.network.test;

import org.joda.time.DateTime;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;

public final class ThreeWindingsTransformerTestCaseFactory {

    private ThreeWindingsTransformerTestCaseFactory() {
    }

    public static Network create() {
        Network network = NetworkFactory.create("threeWindingsTransformerTestCase", "code");
        network.setCaseDate(DateTime.parse("2018-03-05T11:50:03.427+02:00"));
        Substation s = network.newSubstation()
                .setId("_37e14a0f-5e34-4647-a062-8bfd9305fa9d")
                .setName("PP_Brussels")
                .setCountry(Country.BE)
                .add();
        VoltageLevel vl21 = s.newVoltageLevel()
                .setId("_929ba893-c9dc-44d7-b1fd-30834bd3ab85")
                .setName("21.0")
                .setNominalV(21.0f)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setLowVoltageLimit(18.9f)
                .setHighVoltageLimit(23.1f)
                .add();
        Bus b21 = vl21.getBusBreakerView().newBus()
                .setId("_f96d552a-618d-4d0c-a39a-2dea3c411dee")
                .add();
        b21.setV(21.987f);
        b21.setAngle(-6.6508f);
        VoltageLevel vl225 = s.newVoltageLevel()
                .setId("_b10b171b-3bc5-4849-bb1f-61ed9ea1ec7c")
                .setName("225.0")
                .setNominalV(225.0f)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setLowVoltageLimit(202.5f)
                .setHighVoltageLimit(247.5f)
                .add();
        Bus b225 = vl225.getBusBreakerView().newBus()
                .setId("_99b219f3-4593-428b-a4da-124a54630178")
                .add();
        b225.setV(224.31526f);
        b225.setAngle(-8.77012f);
        VoltageLevel vl380 = s.newVoltageLevel()
                .setId("_469df5f7-058f-4451-a998-57a48e8a56fe")
                .setName("380.0")
                .setNominalV(380.0f)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setLowVoltageLimit(342.0f)
                .setHighVoltageLimit(418.0f)
                .add();
        Bus v380 = vl380.getBusBreakerView().newBus()
                .setId("_e44141af-f1dc-44d3-bfa4-b674e5c953d7")
                .add();
        v380.setV(412.989f);
        v380.setAngle(-6.78071f);
        ThreeWindingsTransformerAdder ta = s.newThreeWindingsTransformer()
                .setId("_84ed55f4-61f5-4d9d-8755-bba7b877a246")
                .setName("BE-TR3_1");
        ta.newLeg1()
                .setConnectableBus("_e44141af-f1dc-44d3-bfa4-b674e5c953d7")
                .setBus("_e44141af-f1dc-44d3-bfa4-b674e5c953d7")
                .setVoltageLevel("_469df5f7-058f-4451-a998-57a48e8a56fe")
                .setR(0.898462f)
                .setX(17.204128f)
                .setG(0.0f)
                .setB(2.4375E-6f)
                .setRatedU(400.0f)
                .add();
        ta.newLeg2()
                .setConnectableBus("_99b219f3-4593-428b-a4da-124a54630178")
                .setBus("_99b219f3-4593-428b-a4da-124a54630178")
                .setVoltageLevel("_b10b171b-3bc5-4849-bb1f-61ed9ea1ec7c")
                .setR(1.0707703f)
                .setX(19.6664f)
                .setRatedU(220.0f)
                .add();
        ta.newLeg3()
                .setConnectableBus("_f96d552a-618d-4d0c-a39a-2dea3c411dee")
                .setBus("_f96d552a-618d-4d0c-a39a-2dea3c411dee")
                .setVoltageLevel("_929ba893-c9dc-44d7-b1fd-30834bd3ab85")
                .setR(4.837007f)
                .setX(21.760727f)
                .setRatedU(21.0f)
                .add();
        ThreeWindingsTransformer t = ta.add();
        RatioTapChangerAdder rtca = t.getLeg2().newRatioTapChanger()
                .setLowTapPosition(1)
                .setTapPosition(17)
                .setLoadTapChangingCapabilities(true)
                .setRegulating(false)
                .setRegulationTerminal(t.getLeg2().getTerminal());
        for (double rho = 0.9; rho < 1.101; rho += 0.00625) {
            rtca.beginStep()
                .setR(0f)
                .setX(0f)
                .setG(0f)
                .setB(0f)
                .setRho((float) rho)
                .endStep();
        }
        rtca.add();
        return network;
    }
}
