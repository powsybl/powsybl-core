/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.regulation.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import static com.powsybl.iidm.network.regulation.EquipmentSide.TWO;
import static com.powsybl.iidm.network.regulation.RegulationKind.REACTIVE_POWER;
import static com.powsybl.iidm.network.regulation.RegulationKind.VOLTAGE;
import static org.junit.Assert.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractRegulationTest {

    @Test
    public void test() {
        // create network
        Network network = EurostagTutorialExample1Factory.create();

        // create regulating controls
        network.newExtension(RegulatingControlListAdder.class).add();
        RegulatingControlList regulatingControlList = network.getExtension(RegulatingControlList.class);
        RegulatingControl voltageControl = createRegulatingControl(regulatingControlList, "r1", 130.0, Double.NaN, VOLTAGE);
        RegulatingControl reactivePowerControl = createRegulatingControl(regulatingControlList, "r2", 600.0, 5.0, REACTIVE_POWER);

        // create regulations for generator
        Generator generator = network.getGenerator("GEN");
        generator.newExtension(RegulationListAdder.class).add();
        RegulationList<Generator> genRegulationList = generator.getExtension(RegulationList.class);
        createRegulation(genRegulationList, true, null, voltageControl);
        createRegulation(genRegulationList, false, null, reactivePowerControl);

        // create regulation for t2w
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("NHV2_NLOAD");
        twt.newExtension(RegulationListAdder.class).add();
        RegulationList<TwoWindingsTransformer> twtRegulationList = twt.getExtension(RegulationList.class);
        createRegulation(twtRegulationList, true, TWO, voltageControl);

        // check regulating control created
        RegulatingControl r1 = network.getExtension(RegulatingControlList.class).getRegulatingControl("r1");
        assertNotNull(r1);
        assertEquals(130.0, r1.getTargetValue(), 0.0);
        assertTrue(Double.isNaN(r1.getTargetDeadband()));
        assertEquals(VOLTAGE, r1.getRegulationKind());
        assertEquals("LOAD", r1.getRegulatedEquipmentId());
        assertNull(r1.getRegulatedEquipmentSide());
        assertEquals(2, r1.getRegulations().size());

        RegulatingControl r2 = network.getExtension(RegulatingControlList.class).getRegulatingControl("r2");
        assertNotNull(r2);
        assertEquals(600.0, r2.getTargetValue(), 0.0);
        assertEquals(5.0, r2.getTargetDeadband(), 0.0);
        assertEquals(REACTIVE_POWER, r2.getRegulationKind());
        assertEquals("LOAD", r2.getRegulatedEquipmentId());
        assertNull(r2.getRegulatedEquipmentSide());
        assertEquals(1, r2.getRegulations().size());

        // check regulations created for generator
        Regulation genVoltReg = generator.getExtension(RegulationList.class).getRegulation("r1");
        assertNotNull(genVoltReg);
        assertEquals(voltageControl, genVoltReg.getRegulatingControl());
        assertTrue(genVoltReg.isRegulating());
        assertNull(genVoltReg.getRegulatingEquipmentSide());
        assertEquals(generator, genVoltReg.getRegulatingEquipment());

        Regulation genReactPowReg = generator.getExtension(RegulationList.class).getRegulation("r2");
        assertNotNull(genReactPowReg);
        assertEquals(reactivePowerControl, genReactPowReg.getRegulatingControl());
        assertFalse(genReactPowReg.isRegulating());
        assertNull(genReactPowReg.getRegulatingEquipmentSide());
        assertEquals(generator, genReactPowReg.getRegulatingEquipment());

        // check regulation created for t2w
        Regulation twtReg = twt.getExtension(RegulationList.class).getRegulation("r1");
        assertNotNull(twtReg);
        assertEquals(voltageControl, twtReg.getRegulatingControl());
        assertTrue(twtReg.isRegulating());
        assertEquals(TWO, twtReg.getRegulatingEquipmentSide());
        assertEquals(twt, twtReg.getRegulatingEquipment());

        assertNull(twt.getExtension(RegulationList.class).getRegulation("r2"));
        assertFalse(twt.getExtension(RegulationList.class).getOptionalRegulation("r2").isPresent());
        assertFalse(twt.getExtension(RegulationList.class).hasRegulation("r2"));

        // modify generator regulations
        genReactPowReg.setRegulating(true);
        genVoltReg.setRegulating(false);
        assertTrue(genReactPowReg.isRegulating());
        assertFalse(genVoltReg.isRegulating());

        // modify regulating control
        r1.setTargetValue(140.0).setTargetDeadband(10.0);
        assertEquals(140.0, r1.getTargetValue(), 0.0);
        assertEquals(10.0, r1.getTargetDeadband(), 0.0);

        // remove regulation
        genVoltReg.remove();
        assertFalse(genRegulationList.hasRegulation("r1"));
        assertEquals(1, r1.getRegulations().size());

        // remove regulating control
        r1.remove();
        assertNull(network.getExtension(RegulatingControlList.class).getRegulatingControl("r1"));
        assertFalse(twt.getExtension(RegulationList.class).hasRegulation("r1"));
    }

    private static RegulatingControl createRegulatingControl(RegulatingControlList regulatingControlList, String id, double targetValue, double targetDeadband, RegulationKind regulationKind) {
        return regulatingControlList.newRegulatingControl()
                .setId(id)
                .setTargetValue(targetValue)
                .setTargetDeadband(targetDeadband)
                .setRegulationKind(regulationKind)
                .setEquipmentId("LOAD")
                .add();
    }

    private static <T extends Connectable> void createRegulation(RegulationList<T> regulationList, boolean regulating, EquipmentSide regulatingSide, RegulatingControl regulatingControl) {
        regulationList.newRegulation()
                .setRegulating(regulating)
                .setRegulatingSide(regulatingSide)
                .setRegulatingControl(regulatingControl)
                .add();
    }
}
