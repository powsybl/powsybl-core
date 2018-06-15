/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class UcteNetworkImplTest {

    @Test
    public void test() {
        UcteNetwork network = UcteNetworkFactory.createNetwork(UcteNetworkImpl::new);
        testNetwork(network);
    }

    protected void testNetwork(UcteNetwork network) {
        UcteNodeCode code1 = new UcteNodeCode(UcteCountryCode.XX, "AAAAA", UcteVoltageLevelCode.VL_380, '1');
        UcteNodeCode code2 = new UcteNodeCode(UcteCountryCode.XX, "BBBBB", UcteVoltageLevelCode.VL_220, '1');
        UcteNodeCode code3 = new UcteNodeCode(UcteCountryCode.XX, "CCCCC", UcteVoltageLevelCode.VL_220, '1');

        UcteElementId lineId = new UcteElementId(code2, code3, '1');
        UcteElementId transformerId = new UcteElementId(code1, code2, '1');

        assertNull(network.getVersion());
        network.setVersion(UcteFormatVersion.SECOND);
        assertEquals(UcteFormatVersion.SECOND, network.getVersion());

        assertEquals(0, network.getComments().size());

        assertEquals(3, network.getNodes().size());
        List<UcteNodeCode> codes = network.getNodes().stream().map(UcteNode::getCode).collect(Collectors.toList());
        assertTrue(codes.containsAll(Arrays.asList(code1, code2, code3)));
        UcteNode node = network.getNode(code1);
        assertEquals(1000.0f, node.getActivePowerGeneration(), 0.0f);

        assertEquals(1, network.getLines().size());
        assertEquals(lineId, network.getLines().iterator().next().getId());
        assertNull(network.getLine(transformerId));

        assertEquals(1, network.getTransformers().size());
        assertEquals(transformerId, network.getTransformers().iterator().next().getId());
        assertNull(network.getTransformer(lineId));

        assertEquals(1, network.getRegulations().size());
        assertEquals(transformerId, network.getRegulations().iterator().next().getTransfoId());
        assertNull(network.getRegulation(lineId));
    }
}
