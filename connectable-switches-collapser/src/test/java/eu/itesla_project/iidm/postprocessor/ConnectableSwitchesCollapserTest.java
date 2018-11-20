/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.postprocessor;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.postprocessor.ConnectableSwitchesCollapserPostProcessor;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class ConnectableSwitchesCollapserTest {

    private static final String BUS_1_ID = "bus1";
    private static final String BUS_2_ID = "bus2";
    private static final String GEN_ID = "g400";
    private static final String AUX_ID = "aux";
    private static final String SWITCH_ID = "sw1";
    private static final String VL1 = "vl1";
    private static final String AUX_2_ID = "ld2";

    private Network createNetwork() {
        Network n = NetworkFactory.create("UNIT_TEST", "MANUAL");
        Substation s = n.newSubstation()
                .setId("s1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId(VL1)
                .setNominalV(380f)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus b400 = vl.getBusBreakerView().newBus()
                .setId(BUS_1_ID)
                .add();
        b400.setV(412.3f);
        b400.setAngle(0f);
        Bus b2 = vl.getBusBreakerView().newBus()
                .setId(BUS_2_ID)
                .add();
        b2.setV(412.3f);
        b2.setAngle(0f);
        Generator g1 = vl.newGenerator()
                .setId(GEN_ID)
                .setBus(BUS_1_ID)
                .setConnectableBus(BUS_1_ID)
                .setMinP(220f)
                .setMaxP(910f)
                .setTargetP(910f)
                .setTargetQ(79f)
                .setTargetV(412.3f)
                .setVoltageRegulatorOn(true)
                .add();
        g1.getTerminal().setP(910f).setQ(79f);
        g1.newReactiveCapabilityCurve()
                    .beginPoint()
                        .setP(220f)
                        .setMinQ(-520f)
                        .setMaxQ(300f)
                    .endPoint()
                    .beginPoint()
                        .setP(910f)
                        .setMinQ(-600f)
                        .setMaxQ(300f)
                    .endPoint()
                .add();
        vl.newLoad()
                .setId(AUX_ID)
                .setBus(BUS_2_ID)
                .setConnectableBus(BUS_2_ID)
                .setP0(2)
                .setQ0(3)
                .add();

        vl.newLoad()
                .setId(AUX_2_ID)
                .setBus(BUS_2_ID)
                .setConnectableBus(BUS_2_ID)
                .setP0(2)
                .setQ0(3)
                .add();

        vl.getBusBreakerView().newSwitch()
                .setId(SWITCH_ID)
                .setBus1(BUS_1_ID)
                .setBus2(BUS_2_ID)
                .add();
        return n;
    }

    @Test
    public void test() {
        ConnectableSwitchesCollapserPostProcessor remover = new ConnectableSwitchesCollapserPostProcessor();
        Network network = createNetwork();
        try {
            remover.process(network, null);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNull(network.getSwitch(SWITCH_ID));
        Assert.assertNull(network.getVoltageLevel(VL1).getBusBreakerView().getBus(BUS_1_ID));
        Optional<Generator> gen = network.getVoltageLevel(VL1).getGeneratorStream().filter(g -> g.getId().equals(GEN_ID)).findFirst();
        Assert.assertTrue(gen.get().getTerminals().get(0).getBusBreakerView().getBus().getId().equals(BUS_2_ID));
    }
}
