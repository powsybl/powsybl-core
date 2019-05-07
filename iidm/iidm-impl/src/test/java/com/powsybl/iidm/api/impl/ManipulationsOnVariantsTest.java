/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.api.impl;

import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.api.Network;
import com.powsybl.iidm.api.VariantManager;
import com.powsybl.iidm.api.VariantManagerConstants;
import com.powsybl.iidm.api.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
public class ManipulationsOnVariantsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private VariantManager variantManager;

    @Before
    public void setUp() {
        Network network = NoEquipmentNetworkFactory.create();
        variantManager = network.getVariantManager();
    }

    @Test
    public void errorRemoveInitialVariant() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Removing initial variant is forbidden");
        variantManager.removeVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
    }

    @Test
    public void errorNotExistingVariant() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("not found");
        variantManager.removeVariant("not_exists");
    }

    @Test
    public void errorCloneToEmptyVariants() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Empty target variant id list");
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, Collections.emptyList());
    }

    @Test
    public void errorCloneToExistingVariant() {
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "hello");
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("already exists");
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "hello");
    }

    @Test
    public void baseTests() {
        List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        // extend
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);
        assertEquals(Sets.newHashSet(VariantManagerConstants.INITIAL_VARIANT_ID, "s1", "s2", "s3", "s4"), variantManager.getVariantIds());

        // delete
        variantManager.removeVariant("s2");
        assertEquals(Sets.newHashSet(VariantManagerConstants.INITIAL_VARIANT_ID, "s1", "s3", "s4"), variantManager.getVariantIds());

        // allocate
        variantManager.cloneVariant("s4", "s2b");
        assertEquals(Sets.newHashSet(VariantManagerConstants.INITIAL_VARIANT_ID, "s1", "s2b", "s3", "s4"), variantManager.getVariantIds());

        // reduce
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        assertEquals(Sets.newHashSet(VariantManagerConstants.INITIAL_VARIANT_ID, "s2b", "s1", "s3"), variantManager.getVariantIds());

        try {
            variantManager.getWorkingVariantId();
            fail();
        } catch (Exception ignored) {
        }
    }
}
