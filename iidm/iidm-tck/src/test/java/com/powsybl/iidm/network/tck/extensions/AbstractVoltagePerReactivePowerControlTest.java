/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControlAdder;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.Assert.*;

/**
 * @author Anne Tilloy <anne.tilloy at rte-france.com>
 */
public abstract class AbstractVoltagePerReactivePowerControlTest {

    private StaticVarCompensator svc;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        Network network = SvcTestCaseFactory.create();
        svc = network.getStaticVarCompensator("SVC2");
    }

    @Test
    public void test() {
        svc.newExtension(VoltagePerReactivePowerControlAdder.class)
                .withSlope(0.2)
                .add();
        VoltagePerReactivePowerControl control = svc.getExtension(VoltagePerReactivePowerControl.class);
        assertEquals(0.2, control.getSlope(), 0.0);
        control.setSlope(0.5);
        assertEquals(0.5, control.getSlope(), 0.0);
        assertEquals("SVC2", control.getExtendable().getId());
    }

    @Test
    public void testUndefined() {
        exception.expect(PowsyblException.class);
        exception.expectMessage("Undefined value for slope");
        svc.newExtension(VoltagePerReactivePowerControlAdder.class)
                .withSlope(Double.NaN)
                .add();
    }

    @Test
    public void variantsCloneTest() {
        String variant1 = "variant1";
        String variant2 = "variant2";
        String variant3 = "variant3";

        svc.newExtension(VoltagePerReactivePowerControlAdder.class)
                .withSlope(0.25)
                .add();
        VoltagePerReactivePowerControl control = svc.getExtension(VoltagePerReactivePowerControl.class);

        // Testing variant cloning
        VariantManager variantManager = svc.getNetwork().getVariantManager();
        variantManager.cloneVariant(INITIAL_VARIANT_ID, variant1);
        variantManager.cloneVariant(variant1, variant2);
        variantManager.setWorkingVariant(variant1);
        assertEquals(0.25, control.getSlope(), 0);

        // Testing setting different values in the cloned variant and going back to the initial one
        control.setSlope(0.35);
        assertEquals(0.35, control.getSlope(), 0);
        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertEquals(0.25, control.getSlope(), 0);

        // Removes a variant then adds another variant to test variant recycling (hence calling allocateVariantArrayElement)
        variantManager.removeVariant(variant1);
        List<String> targetVariantIds = Arrays.asList(variant1, variant3);
        variantManager.cloneVariant(INITIAL_VARIANT_ID, targetVariantIds);
        variantManager.setWorkingVariant(variant1);
        assertEquals(0.25, control.getSlope(), 0);
        variantManager.setWorkingVariant(variant3);
        assertEquals(0.25, control.getSlope(), 0);

        // Test removing current variant
        variantManager.removeVariant(variant3);
        try {
            control.getSlope();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Variant index not set", e.getMessage());
        }
    }
}

