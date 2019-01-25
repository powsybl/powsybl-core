/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class AmplUtilTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();

        StringToIntMapper<AmplSubset> mapper = new StringToIntMapper<>(AmplSubset.class);
        testEmptyMapper(mapper);

        mapper = AmplUtil.createMapper(network);
        testFilledMapper(mapper);

        AmplUtil.resetNetworkMapping(mapper);
        testEmptyMapper(mapper);

        AmplUtil.fillMapper(mapper, network);
        testFilledMapper(mapper);
    }

    private void testEmptyMapper(StringToIntMapper<AmplSubset> mapper) {
        try {
            mapper.getId(AmplSubset.VOLTAGE_LEVEL, 1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            mapper.getId(AmplSubset.BUS, 1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            mapper.getId(AmplSubset.BRANCH, 1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            mapper.getId(AmplSubset.LOAD, 1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            mapper.getId(AmplSubset.GENERATOR, 1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            mapper.getId(AmplSubset.RATIO_TAP_CHANGER, 1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            mapper.getId(AmplSubset.TAP_CHANGER_TABLE, 1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void testFilledMapper(StringToIntMapper<AmplSubset> mapper) {
        for (int i = 1; i <= 4; i++) {
            assertTrue(Arrays.asList("VLGEN", "VLHV1", "VLHV2", "VLLOAD").contains(mapper.getId(AmplSubset.VOLTAGE_LEVEL, i)));
        }
        for (int i = 1; i <= 4; i++) {
            assertTrue(Arrays.asList("VLGEN_0", "VLHV1_0", "VLHV2_0", "VLLOAD_0").contains(mapper.getId(AmplSubset.BUS, i)));
        }
        for (int i = 1; i <= 4; i++) {
            assertTrue(Arrays.asList("NHV1_NHV2_1", "NHV1_NHV2_2", "NGEN_NHV1", "NHV2_NLOAD").contains(mapper.getId(AmplSubset.BRANCH, i)));
        }
        assertEquals("LOAD", mapper.getId(AmplSubset.LOAD, 1));
        assertEquals("GEN", mapper.getId(AmplSubset.GENERATOR, 1));
        assertEquals("NHV2_NLOAD", mapper.getId(AmplSubset.RATIO_TAP_CHANGER, 1));
        assertEquals("NHV2_NLOAD_ratio_table", mapper.getId(AmplSubset.TAP_CHANGER_TABLE, 1));
    }

}
