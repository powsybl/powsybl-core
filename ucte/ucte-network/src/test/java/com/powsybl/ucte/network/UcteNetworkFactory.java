/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import java.util.function.Supplier;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public final class UcteNetworkFactory {

    private UcteNetworkFactory() {
    }

    public static UcteNetwork createNetwork(Supplier<UcteNetwork> factory) {
        UcteNetwork network = factory.get();

        UcteNodeCode code1 = new UcteNodeCode(UcteCountryCode.XX, "AAAAA", UcteVoltageLevelCode.VL_380, '1');
        UcteNode node1 = new UcteNode(code1, null, UcteNodeStatus.REAL, UcteNodeTypeCode.PU, 400.0f,
                Float.NaN, Float.NaN, 1000.0f, Float.NaN, 9999.0f, -9999.0f, 9999.0f, -9999.0f,
                Float.NaN, Float.NaN, Float.NaN, Float.NaN, UctePowerPlantType.C);
        network.addNode(node1);

        UcteNodeCode code2 = new UcteNodeCode(UcteCountryCode.XX, "BBBBB", UcteVoltageLevelCode.VL_220, '1');
        UcteNode node2 = new UcteNode(code2, null, UcteNodeStatus.REAL, UcteNodeTypeCode.PQ, Float.NaN,
                Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN,
                Float.NaN, Float.NaN, Float.NaN, Float.NaN, null);
        network.addNode(node2);

        UcteNodeCode code3 = new UcteNodeCode(UcteCountryCode.XX, "CCCCC", UcteVoltageLevelCode.VL_220, '1');
        UcteNode node3 = new UcteNode(code3, null, UcteNodeStatus.REAL, UcteNodeTypeCode.PQ, Float.NaN,
                500.0f, 350.f, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN,
                Float.NaN, Float.NaN, Float.NaN, Float.NaN, null);
        network.addNode(node3);

        UcteElementId lineId = new UcteElementId(code2, code3, '1');
        UcteLine line = new UcteLine(lineId, UcteElementStatus.REAL_ELEMENT_IN_OPERATION,
                1.0f, 0.1f, 1e-6f, 1250, null);
        network.addLine(line);

        UcteElementId transformerId = new UcteElementId(code1, code2, '1');
        UcteTransformer transformer = new UcteTransformer(transformerId, UcteElementStatus.REAL_ELEMENT_IN_OPERATION,
                1.0f, 0.1f, 1e-6f, 1500, null, 400.0f, 225.0f, 1000.0f, 1e-6f);
        network.addTransformer(transformer);

        UcteRegulation regulation = new UcteRegulation(transformerId, null, null);
        network.addRegulation(regulation);

        network.fix();

        return network;
    }
}
