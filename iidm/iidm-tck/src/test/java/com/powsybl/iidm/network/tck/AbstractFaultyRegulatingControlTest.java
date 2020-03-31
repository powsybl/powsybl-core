/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.regulation.EquipmentSide;
import com.powsybl.iidm.network.regulation.RegulatingControlList;
import com.powsybl.iidm.network.regulation.RegulatingControlListAdder;
import com.powsybl.iidm.network.regulation.RegulationKind;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.powsybl.iidm.network.regulation.EquipmentSide.THREE;
import static com.powsybl.iidm.network.regulation.EquipmentSide.TWO;
import static com.powsybl.iidm.network.regulation.RegulationKind.REACTIVE_POWER;
import static com.powsybl.iidm.network.regulation.RegulationKind.VOLTAGE;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractFaultyRegulatingControlTest {

    private RegulatingControlList regulatingControlList;

    @Rule
    public final ExpectedException expected = ExpectedException.none();

    @Before
    public void setUp() {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(RegulatingControlListAdder.class).add();
        regulatingControlList = network.getExtension(RegulatingControlList.class);
    }

    @Test
    public void nullRegulatingControlIdTest() {
        expected.expect(ValidationException.class);
        expected.expectMessage("Regulating control 'null': Id is null");
        createRegulatingControl(null, 130.0, Double.NaN, VOLTAGE, "LOAD", null);
    }

    @Test
    public void existingRegulatingControlIdTest() {
        expected.expect(ValidationException.class);
        expected.expectMessage("Regulating control 'r1': Regulating control r1 already exists");
        createRegulatingControl("r1", 130.0, Double.NaN, VOLTAGE, "LOAD", null);
        createRegulatingControl("r1", 600.0, 5.0, REACTIVE_POWER, "GEN", null);
    }

    @Test
    public void undefinedTargetValueTest() {
        expected.expect(ValidationException.class);
        expected.expectMessage("Regulating control 'r1': target value is undefined");
        createRegulatingControl("r1", Double.NaN, Double.NaN, VOLTAGE, "LOAD", null);
    }

    @Test
    public void nullRegulationKindTest() {
        expected.expect(ValidationException.class);
        expected.expectMessage("Regulating control 'r1': regulation kind is null");
        createRegulatingControl("r1", 130.0, Double.NaN, null, "LOAD", null);
    }

    @Test
    public void nullEquipmentIdTest() {
        expected.expect(ValidationException.class);
        expected.expectMessage("Regulating control 'r1': ID of regulated equipment is null");
        createRegulatingControl("r1", 130.0, Double.NaN, VOLTAGE, null, null);
    }

    @Test
    public void notExistingEquipmentTest() {
        expected.expect(ValidationException.class);
        expected.expectMessage("Regulating control 'r1': Regulated equipment does not exist in network sim1");
        createRegulatingControl("r1", 130.0, Double.NaN, VOLTAGE, "NOT_EXISTING", null);
    }

    @Test
    public void undefinedEquipmentSideTest() {
        expected.expect(ValidationException.class);
        expected.expectMessage("Regulating control 'r1': Undefined side for regulated equipment");
        createRegulatingControl("r1", 130.0, Double.NaN, VOLTAGE, "NHV2_NLOAD", null);
    }

    @Test
    public void invalidEquipmentSide1Test() {
        expected.expect(ValidationException.class);
        expected.expectMessage("Regulating control 'r1': Invalid side (2) for regulated equipment LOAD");
        createRegulatingControl("r1", 130.0, Double.NaN, VOLTAGE, "LOAD", TWO);
    }

    @Test
    public void invalidEquipmentSide2Test() {
        expected.expect(ValidationException.class);
        expected.expectMessage("Regulating control 'r1': Invalid side (3) for regulated equipment LOAD");
        createRegulatingControl("r1", 130.0, Double.NaN, VOLTAGE, "LOAD", THREE);
    }

    @Test
    public void setUndefinedTargetValue() {
        expected.expect(ValidationException.class);
        expected.expectMessage("Regulating control 'r1': Undefined target value for regulating control r1");
        createRegulatingControl("r1", 130.0, Double.NaN, VOLTAGE, "LOAD", null);
        regulatingControlList.getRegulatingControl("r1").setTargetValue(Double.NaN);
    }

    private void createRegulatingControl(String id, double targetValue, double targetDeadband, RegulationKind regulationKind, String equipmentId, EquipmentSide side) {
        regulatingControlList.newRegulatingControl()
                .setId(id)
                .setTargetValue(targetValue)
                .setTargetDeadband(targetDeadband)
                .setRegulationKind(regulationKind)
                .setEquipmentId(equipmentId)
                .setEquipmentSide(side)
                .add();
    }
}
