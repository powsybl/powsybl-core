/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.regulation.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.powsybl.iidm.network.regulation.EquipmentSide.THREE;
import static com.powsybl.iidm.network.regulation.EquipmentSide.TWO;
import static com.powsybl.iidm.network.regulation.RegulationKind.VOLTAGE;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractFaultyRegulationTest {

    private RegulatingControl control;
    private RegulationList<Generator> genRegulationList;
    private RegulationList<TwoWindingsTransformer> twtRegulationList;

    @Rule
    public final ExpectedException expected = ExpectedException.none();

    @Before
    public void setUp() {
        Network network = initNetwork();
        RegulatingControlList regulatingControlList = network.getExtension(RegulatingControlList.class);
        control = initRegulatingControl(regulatingControlList);
        Generator generator = network.getGenerator("GEN");
        generator.newExtension(RegulationListAdder.class).add();
        genRegulationList = generator.getExtension(RegulationList.class);
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("NHV2_NLOAD");
        twt.newExtension(RegulationListAdder.class).add();
        twtRegulationList = twt.getExtension(RegulationList.class);
    }

    @Test
    public void nullRegulatingControlTest() {
        expected.expect(ValidationException.class);
        expected.expectMessage("Generator 'GEN': Regulating control is null");
        createRegulation(genRegulationList, null, null);
    }

    @Test
    public void invalidRegulatingControlTest() {
        expected.expect(ValidationException.class);
        expected.expectMessage("Generator 'GEN': Regulating control r1 is not associated with network sim1");
        RegulatingControl control2 = initRegulatingControl(initNetwork().getExtension(RegulatingControlList.class));
        createRegulation(genRegulationList, control2, null);
    }

    @Test
    public void existingRegulationTest() {
        expected.expect(ValidationException.class);
        expected.expectMessage("Generator 'GEN': GEN has already a regulation associated to regulating control r1");
        createRegulation(genRegulationList, control, null);
        createRegulation(genRegulationList, control, null);
    }

    @Test
    public void undefinedRegulatingSideTest() {
        expected.expect(ValidationException.class);
        expected.expectMessage("2 windings transformer 'NHV2_NLOAD': Undefined side for regulating equipment NHV2_NLOAD");
        createRegulation(twtRegulationList, control, null);
    }

    @Test
    public void invalidRegulatingSide1Test() {
        expected.expect(ValidationException.class);
        expected.expectMessage("Generator 'GEN': Invalid side (2) for regulating equipment GEN");
        createRegulation(genRegulationList, control, TWO);
    }

    @Test
    public void invalidRegulatingSide2Test() {
        expected.expect(ValidationException.class);
        expected.expectMessage("2 windings transformer 'NHV2_NLOAD': Invalid side (3) for regulating equipment NHV2_NLOAD");
        createRegulation(twtRegulationList, control, THREE);
    }

    @Test
    public void setNullRegulatingControl() {
        expected.expect(ValidationException.class);
        expected.expectMessage("Generator 'GEN': Regulating control is null");
        createRegulation(genRegulationList, control, null)
                .setRegulatingControl(null);
    }

    @Test
    public void setInvalidRegulatingControl() {
        expected.expect(ValidationException.class);
        expected.expectMessage("Generator 'GEN': Regulating control r1 is not associated with network sim1");
        RegulatingControl control2 = initRegulatingControl(initNetwork().getExtension(RegulatingControlList.class));
        createRegulation(genRegulationList, control, null)
                .setRegulatingControl(control2);
    }

    private static Network initNetwork() {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(RegulatingControlListAdder.class).add();
        return network;
    }

    private static RegulatingControl initRegulatingControl(RegulatingControlList regulatingControlList) {
        return regulatingControlList.newRegulatingControl()
                .setId("r1")
                .setTargetValue(130.0)
                .setRegulationKind(VOLTAGE)
                .setEquipmentId("LOAD")
                .add();
    }

    private static Regulation createRegulation(RegulationList regulationList, RegulatingControl control, EquipmentSide regulatingSide) {
        return regulationList.newRegulation()
                .setRegulatingControl(control)
                .setRegulating(true)
                .setRegulatingSide(regulatingSide)
                .add();
    }
}
