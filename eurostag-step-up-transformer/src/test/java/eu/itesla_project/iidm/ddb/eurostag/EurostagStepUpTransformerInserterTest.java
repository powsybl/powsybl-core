/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag;

import eu.itesla_project.iidm.network.*;
import org.junit.*;

import java.util.Arrays;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EurostagStepUpTransformerInserterTest {

    private static final String BUS_400_ID = "b400";
    private static final String BUS_24_ID = "b24";
    private static final String GEN_400_ID = "g400";
    private static final String GEN_24_ID = "g24";
    private static final String AUX_ID = "aux";

    private Network n;
    private IdDictionary dict;
    private TG tg;

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");
    }

    @Before
    public void setUp() throws Exception {
        n = createNetwork();
        dict = new IdDictionary();
        dict.add(GEN_400_ID, AUX_ID);
        tg = createTg();
    }

    @After
    public void tearDown() throws Exception {
    }

    private Network createNetwork() {
        Network n = NetworkFactory.create("UNIT_TEST", "MANUAL");
        Substation s = n.newSubstation()
                .setId("s1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("vl1")
                .setNominalV(380f)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus b400 = vl.getBusBreakerView().newBus()
                .setId(BUS_400_ID)
                .add();
        b400.setV(412.3f);
        b400.setAngle(0f);
        Generator g1 = vl.newGenerator()
                .setId(GEN_400_ID)
                .setBus(BUS_400_ID)
                .setConnectableBus(BUS_400_ID)
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
                .setBus(BUS_400_ID)
                .setConnectableBus(BUS_400_ID)
                .setP0(2)
                .setQ0(3)
                .add();
        return n;
    }

    private TG createTg() {
        F f1 = new F(BUS_400_ID, 380);
        F f2 = new F(BUS_24_ID, 24);
        T4X t4X = new T4X(BUS_400_ID, BUS_24_ID, 1080f, 0.2537f, 0.04935f, 0.15f, 1.0f, 2, 2,
                          Arrays.asList(1, 2, 3), Arrays.asList(24f, 24f, 24f), Arrays.asList(415f, 400f, 385f),
                          Arrays.asList(13.62f, 13.62f, 13.62f), Arrays.asList(0f, 0f, 0f));
        LH lh = new LH(BUS_24_ID, 37, 16);
        return new TG(GEN_24_ID, f1, f2, t4X, lh);
    }

    @Test
    @Ignore
    public void test() {
        Generator g400 = n.getGenerator(GEN_400_ID);
        EurostagStepUpTransformerInserter.InsertionStatus status = EurostagStepUpTransformerInserter.insert(g400, tg, dict, new EurostagStepUpTransformerConfig(), new EurostagStepUpTransformerInserter.StateBefore());
        Assert.assertTrue(status == EurostagStepUpTransformerInserter.InsertionStatus.OK);
        Generator g24 = n.getGenerator(GEN_400_ID);
        Assert.assertTrue(g400.getMinP() == g24.getMinP());
        Assert.assertTrue(g400.getMaxP() == g24.getMaxP());
        Assert.assertTrue(g400.getTargetP() == g24.getTargetP());
        Assert.assertTrue(g24.getTargetV() == 23.223017f);
        Assert.assertTrue(g24.getTargetQ() == 195.01202f);
        Assert.assertTrue(g24.getReactiveLimits().getMinQ(220f) == -475.13937f);
        Assert.assertTrue(g24.getReactiveLimits().getMaxQ(220f) == 318.96487f);
        Assert.assertTrue(g24.getReactiveLimits().getMinQ(910f) == -433.98605f);
        Assert.assertTrue(g24.getReactiveLimits().getMaxQ(910f) == 427.53082f);
    }
}