package com.powsybl.security;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.security.action.PhaseTapChangerRegulationAction;
import org.junit.Assert;
import org.junit.Test;

public class PhaseTapChangerRegulationActionTest {

    @Test
    public void test() {
        String message = Assert.assertThrows(IllegalArgumentException.class, () -> new PhaseTapChangerRegulationAction("id17", "transformerId5", ThreeWindingsTransformer.Side.ONE,
                false, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)).getMessage();
        Assert.assertEquals("PhaseTapChangerRegulationAction can not have a regulation mode " +
                "if it is not regulating", message);
        PhaseTapChangerRegulationAction action = new PhaseTapChangerRegulationAction("id17", "transformerId5",
                ThreeWindingsTransformer.Side.ONE);
        Assert.assertFalse(action.isRegulating());
        Assert.assertTrue(action.getRegulationMode().isEmpty());
    }
}
