/**
 * Copyright (c) 2022, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerWithExtensionsFactory;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public abstract class AbstractNetworkExtensionsTest {

    @Test
    public void testNetworkFourSubstationsExtensions() {
        Network network = FourSubstationsNodeBreakerWithExtensionsFactory.create();

        //Busbar sections
        BusbarSection bbsS1Vl1 = network.getBusbarSection("S1VL1_BBS");
        assertNotNull(bbsS1Vl1);
        BusbarSectionPosition bbsS1Vl1Position = bbsS1Vl1.getExtension(BusbarSectionPosition.class);
        assertNotNull(bbsS1Vl1Position);
        assertEquals(1, bbsS1Vl1Position.getBusbarIndex());
        assertEquals(1, bbsS1Vl1Position.getSectionIndex());

        BusbarSection bbsS1Vl21 = network.getBusbarSection("S1VL2_BBS1");
        assertNotNull(bbsS1Vl21);
        BusbarSectionPosition bbsS1Vl21Position = bbsS1Vl21.getExtension(BusbarSectionPosition.class);
        assertNotNull(bbsS1Vl21Position);
        assertEquals(1, bbsS1Vl21Position.getBusbarIndex());
        assertEquals(1, bbsS1Vl21Position.getSectionIndex());

        BusbarSection bbsS1Vl22 = network.getBusbarSection("S1VL2_BBS2");
        assertNotNull(bbsS1Vl22);
        BusbarSectionPosition bbsS1Vl22Position = bbsS1Vl22.getExtension(BusbarSectionPosition.class);
        assertNotNull(bbsS1Vl22Position);
        assertEquals(2, bbsS1Vl22Position.getBusbarIndex());
        assertEquals(1, bbsS1Vl22Position.getSectionIndex());

        BusbarSection bbsS2Vl1 = network.getBusbarSection("S2VL1_BBS");
        assertNotNull(bbsS2Vl1);
        BusbarSectionPosition bbsS2Vl1Position = bbsS2Vl1.getExtension(BusbarSectionPosition.class);
        assertNotNull(bbsS2Vl1Position);
        assertEquals(1, bbsS2Vl1Position.getBusbarIndex());
        assertEquals(1, bbsS2Vl1Position.getSectionIndex());

        BusbarSection bbsS3Vl1 = network.getBusbarSection("S3VL1_BBS");
        assertNotNull(bbsS3Vl1);
        BusbarSectionPosition bbsS3Vl1Position = bbsS3Vl1.getExtension(BusbarSectionPosition.class);
        assertNotNull(bbsS3Vl1Position);
        assertEquals(1, bbsS3Vl1Position.getBusbarIndex());
        assertEquals(1, bbsS3Vl1Position.getSectionIndex());

        BusbarSection bbsS4Vl1 = network.getBusbarSection("S4VL1_BBS");
        assertNotNull(bbsS4Vl1);
        BusbarSectionPosition bbsS4Vl1Position = bbsS4Vl1.getExtension(BusbarSectionPosition.class);
        assertNotNull(bbsS4Vl1Position);
        assertEquals(1, bbsS4Vl1Position.getBusbarIndex());
        assertEquals(1, bbsS4Vl1Position.getSectionIndex());

        //Loads
        Load load1 = network.getLoad("LD1");
        assertNotNull(load1);
        ConnectablePosition<Load> load1Position = load1.getExtension(ConnectablePosition.class);
        assertNotNull(load1Position);
        assertNotNull(load1Position.getFeeder());
        assertEquals("LD1", load1Position.getFeeder().getName().orElse(""));
        assertEquals(ConnectablePosition.Direction.TOP, load1Position.getFeeder().getDirection());
        assertEquals(Optional.of(10), load1Position.getFeeder().getOrder());

        Load load2 = network.getLoad("LD2");
        assertNotNull(load2);
        ConnectablePosition<Load> load2Position = load2.getExtension(ConnectablePosition.class);
        assertNotNull(load2Position);
        assertNotNull(load2Position.getFeeder());
        assertEquals("LD2", load2Position.getFeeder().getName().orElse(""));
        assertEquals(ConnectablePosition.Direction.BOTTOM, load2Position.getFeeder().getDirection());
        assertEquals(Optional.of(60), load2Position.getFeeder().getOrder());

        Load load3 = network.getLoad("LD3");
        assertNotNull(load3);
        ConnectablePosition<Load> load3Position = load3.getExtension(ConnectablePosition.class);
        assertNotNull(load3Position);
        assertNotNull(load3Position.getFeeder());
        assertEquals("LD3", load3Position.getFeeder().getName().orElse(""));
        assertEquals(ConnectablePosition.Direction.BOTTOM, load3Position.getFeeder().getDirection());
        assertEquals(Optional.of(70), load3Position.getFeeder().getOrder());

        Load load4 = network.getLoad("LD4");
        assertNotNull(load4);
        ConnectablePosition<Load> load4Position = load4.getExtension(ConnectablePosition.class);
        assertNotNull(load4Position);
        assertNotNull(load4Position.getFeeder());
        assertEquals("LD4", load4Position.getFeeder().getName().orElse(""));
        assertEquals(ConnectablePosition.Direction.BOTTOM, load4Position.getFeeder().getDirection());
        assertEquals(Optional.of(80), load4Position.getFeeder().getOrder());

        Load load5 = network.getLoad("LD5");
        assertNotNull(load5);
        ConnectablePosition<Load> load5Position = load5.getExtension(ConnectablePosition.class);
        assertNotNull(load5Position);
        assertNotNull(load5Position.getFeeder());
        assertEquals("LD5", load5Position.getFeeder().getName().orElse(""));
        assertEquals(ConnectablePosition.Direction.TOP, load5Position.getFeeder().getDirection());
        assertEquals(Optional.of(20), load5Position.getFeeder().getOrder());

        Load load6 = network.getLoad("LD6");
        assertNotNull(load6);
        ConnectablePosition<Load> load6Position = load6.getExtension(ConnectablePosition.class);
        assertNotNull(load6Position);
        assertNotNull(load6Position.getFeeder());
        assertEquals("LD6", load6Position.getFeeder().getName().orElse(""));
        assertEquals(ConnectablePosition.Direction.TOP, load6Position.getFeeder().getDirection());
        assertEquals(Optional.of(10), load6Position.getFeeder().getOrder());

        //Generators
        Generator generatorH1 = network.getGenerator("GH1");
        assertNotNull(generatorH1);
        ConnectablePosition<Generator> generatorH1Position = generatorH1.getExtension(ConnectablePosition.class);
        assertNotNull(generatorH1Position);
        assertNotNull(generatorH1Position.getFeeder());
        assertEquals("GH1", generatorH1Position.getFeeder().getName().orElse(""));
        assertEquals(ConnectablePosition.Direction.TOP, generatorH1Position.getFeeder().getDirection());
        assertEquals(Optional.of(30), generatorH1Position.getFeeder().getOrder());

        Generator generatorH2 = network.getGenerator("GH2");
        assertNotNull(generatorH2);
        ConnectablePosition<Generator> generatorH2Position = generatorH2.getExtension(ConnectablePosition.class);
        assertNotNull(generatorH2Position);
        assertNotNull(generatorH2Position.getFeeder());
        assertEquals("GH2", generatorH2Position.getFeeder().getName().orElse(""));
        assertEquals(ConnectablePosition.Direction.TOP, generatorH2Position.getFeeder().getDirection());
        assertEquals(Optional.of(40), generatorH2Position.getFeeder().getOrder());

        Generator generatorH3 = network.getGenerator("GH3");
        assertNotNull(generatorH3);
        ConnectablePosition<Generator> generatorH3Position = generatorH3.getExtension(ConnectablePosition.class);
        assertNotNull(generatorH3Position);
        assertNotNull(generatorH3Position.getFeeder());
        assertEquals("GH3", generatorH3Position.getFeeder().getName().orElse(""));
        assertEquals(ConnectablePosition.Direction.TOP, generatorH3Position.getFeeder().getDirection());
        assertEquals(Optional.of(50), generatorH3Position.getFeeder().getOrder());

        Generator generatorTH1 = network.getGenerator("GTH1");
        assertNotNull(generatorTH1);
        ConnectablePosition<Generator> generatorTH1Position = generatorTH1.getExtension(ConnectablePosition.class);
        assertNotNull(generatorTH1Position);
        assertNotNull(generatorTH1Position.getFeeder());
        assertEquals("GTH1", generatorTH1Position.getFeeder().getName().orElse(""));
        assertEquals(ConnectablePosition.Direction.TOP, generatorTH1Position.getFeeder().getDirection());
        assertEquals(Optional.of(10), generatorTH1Position.getFeeder().getOrder());

        Generator generatorTH2 = network.getGenerator("GTH2");
        assertNotNull(generatorTH2);
        ConnectablePosition<Generator> generatorTH2Position = generatorTH2.getExtension(ConnectablePosition.class);
        assertNotNull(generatorTH2Position);
        assertNotNull(generatorTH2Position.getFeeder());
        assertEquals("GTH2", generatorTH2Position.getFeeder().getName().orElse(""));
        assertEquals(ConnectablePosition.Direction.TOP, generatorTH2Position.getFeeder().getDirection());
        assertEquals(Optional.of(30), generatorTH2Position.getFeeder().getOrder());

        //Shunt
        ShuntCompensator shunt = network.getShuntCompensator("SHUNT");
        assertNotNull(shunt);
        ConnectablePosition<ShuntCompensator> shuntPosition = shunt.getExtension(ConnectablePosition.class);
        assertNotNull(shuntPosition);
        assertNotNull(shuntPosition.getFeeder());
        assertEquals("SHUNT", shuntPosition.getFeeder().getName().orElse(""));
        assertEquals(ConnectablePosition.Direction.TOP, shuntPosition.getFeeder().getDirection());
        assertEquals(Optional.of(90), shuntPosition.getFeeder().getOrder());

        //SVC
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC");
        assertNotNull(svc);
        ConnectablePosition<StaticVarCompensator> svcPosition = svc.getExtension(ConnectablePosition.class);
        assertNotNull(svcPosition);
        assertNotNull(svcPosition.getFeeder());
        assertEquals("SVC", svcPosition.getFeeder().getName().orElse(""));
        assertEquals(ConnectablePosition.Direction.TOP, svcPosition.getFeeder().getDirection());
        assertEquals(Optional.of(20), svcPosition.getFeeder().getOrder());

        //VSCs
        VscConverterStation vsc1 = network.getVscConverterStation("VSC1");
        assertNotNull(vsc1);
        ConnectablePosition<VscConverterStation> vsc1Position = vsc1.getExtension(ConnectablePosition.class);
        assertNotNull(vsc1Position);
        assertNotNull(vsc1Position.getFeeder());
        assertEquals("VSC1", vsc1Position.getFeeder().getName().orElse(""));
        assertEquals(ConnectablePosition.Direction.BOTTOM, vsc1Position.getFeeder().getDirection());
        assertEquals(Optional.of(20), vsc1Position.getFeeder().getOrder());

        VscConverterStation vsc2 = network.getVscConverterStation("VSC2");
        assertNotNull(vsc2);
        ConnectablePosition<VscConverterStation> vsc2Position = vsc2.getExtension(ConnectablePosition.class);
        assertNotNull(vsc2Position);
        assertNotNull(vsc2Position.getFeeder());
        assertEquals("VSC2", vsc2Position.getFeeder().getName().orElse(""));
        assertEquals(ConnectablePosition.Direction.TOP, vsc2Position.getFeeder().getDirection());
        assertEquals(Optional.of(20), vsc2Position.getFeeder().getOrder());

        //LCCs
        LccConverterStation lcc1 = network.getLccConverterStation("LCC1");
        assertNotNull(lcc1);
        ConnectablePosition<LccConverterStation> lcc1Position = lcc1.getExtension(ConnectablePosition.class);
        assertNotNull(lcc1Position);
        assertNotNull(lcc1Position.getFeeder());
        assertEquals("LCC1", lcc1Position.getFeeder().getName().orElse(""));
        assertEquals(ConnectablePosition.Direction.BOTTOM, lcc1Position.getFeeder().getDirection());
        assertEquals(Optional.of(100), lcc1Position.getFeeder().getOrder());

        LccConverterStation lcc2 = network.getLccConverterStation("LCC2");
        assertNotNull(lcc2);
        ConnectablePosition<LccConverterStation> lcc2Position = lcc2.getExtension(ConnectablePosition.class);
        assertNotNull(lcc2Position);
        assertNotNull(lcc2Position.getFeeder());
        assertEquals("LCC2", lcc2Position.getFeeder().getName().orElse(""));
        assertEquals(ConnectablePosition.Direction.TOP, lcc2Position.getFeeder().getDirection());
        assertEquals(Optional.of(50), lcc2Position.getFeeder().getOrder());

        //TWO-WINDINGS TRANSFORMER
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("TWT");
        assertNotNull(twt);
        ConnectablePosition<TwoWindingsTransformer> twtPosition = twt.getExtension(ConnectablePosition.class);
        assertNotNull(twtPosition);
        ConnectablePosition.Feeder feederTwt1 = twtPosition.getFeeder1();
        assertNotNull(feederTwt1);
        assertEquals(ConnectablePosition.Direction.TOP, feederTwt1.getDirection());
        assertEquals(Optional.of(20), feederTwt1.getOrder());
        assertEquals("TWT", feederTwt1.getName().orElse(""));
        ConnectablePosition.Feeder feederTwt2 = twtPosition.getFeeder2();
        assertNotNull(feederTwt2);
        assertEquals(ConnectablePosition.Direction.TOP, feederTwt2.getDirection());
        assertEquals(Optional.of(10), feederTwt2.getOrder());
        assertEquals("TWT", feederTwt2.getName().orElse(""));

        //LINES
        Line line1 = network.getLine("LINE_S2S3");
        assertNotNull(line1);
        ConnectablePosition<Line> line1Position = line1.getExtension(ConnectablePosition.class);
        assertNotNull(line1Position);
        ConnectablePosition.Feeder feederLine11 = line1Position.getFeeder1();
        assertNotNull(feederLine11);
        assertEquals(ConnectablePosition.Direction.BOTTOM, feederLine11.getDirection());
        assertEquals(Optional.of(30), feederLine11.getOrder());
        assertEquals("LINE_S2S3", feederLine11.getName().orElse(""));
        ConnectablePosition.Feeder feederLine12 = line1Position.getFeeder2();
        assertNotNull(feederLine12);
        assertEquals(ConnectablePosition.Direction.BOTTOM, feederLine12.getDirection());
        assertEquals(Optional.of(10), feederLine12.getOrder());
        assertEquals("LINE_S2S3", feederLine12.getName().orElse(""));

        Line line2 = network.getLine("LINE_S3S4");
        assertNotNull(line2);
        ConnectablePosition<Line> line2Position = line2.getExtension(ConnectablePosition.class);
        assertNotNull(line2Position);
        ConnectablePosition.Feeder feederLine21 = line2Position.getFeeder1();
        assertNotNull(feederLine21);
        assertEquals(ConnectablePosition.Direction.BOTTOM, feederLine21.getDirection());
        assertEquals(Optional.of(40), feederLine21.getOrder());
        assertEquals("LINE_S3S4", feederLine21.getName().orElse(""));
        ConnectablePosition.Feeder feederLine22 = line2Position.getFeeder2();
        assertNotNull(feederLine22);
        assertEquals(ConnectablePosition.Direction.TOP, feederLine22.getDirection());
        assertEquals(Optional.of(30), feederLine22.getOrder());
        assertEquals("LINE_S3S4", feederLine22.getName().orElse(""));
    }

}
