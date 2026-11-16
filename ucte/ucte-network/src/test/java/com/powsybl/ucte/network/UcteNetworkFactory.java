/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network;

import com.powsybl.commons.report.ReportNode;

import java.util.function.Supplier;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public final class UcteNetworkFactory {

    private UcteNetworkFactory() {
    }

    public static UcteNetwork createNetwork(Supplier<UcteNetwork> factory) {
        UcteNetwork network = factory.get();

        UcteNodeCode code1 = new UcteNodeCode(UcteCountryCode.XX, "AAAAA", UcteVoltageLevelCode.VL_380, '1');
        UcteNode node1 = new UcteNode(code1, null, UcteNodeStatus.REAL, UcteNodeTypeCode.PU, 400.0,
                Double.NaN, Double.NaN, 1000.0, Double.NaN, 9999.0, -9999.0, 9999.0, -9999.0,
                Double.NaN, Double.NaN, Double.NaN, Double.NaN, UctePowerPlantType.C);
        network.addNode(node1);

        UcteNodeCode code2 = new UcteNodeCode(UcteCountryCode.XX, "BBBBB", UcteVoltageLevelCode.VL_220, '1');
        UcteNode node2 = new UcteNode(code2, null, UcteNodeStatus.REAL, UcteNodeTypeCode.PQ, Double.NaN,
                Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                Double.NaN, Double.NaN, Double.NaN, Double.NaN, null);
        network.addNode(node2);

        UcteNodeCode code3 = new UcteNodeCode(UcteCountryCode.XX, "CCCCC", UcteVoltageLevelCode.VL_220, '1');
        UcteNode node3 = new UcteNode(code3, null, UcteNodeStatus.REAL, UcteNodeTypeCode.PQ, Double.NaN,
                500.0, 350., Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                Double.NaN, Double.NaN, Double.NaN, Double.NaN, null);
        network.addNode(node3);

        UcteElementId lineId = new UcteElementId(code2, code3, '1');
        UcteLine line = new UcteLine(lineId, UcteElementStatus.REAL_ELEMENT_IN_OPERATION,
                1.0, 0.1, 1e-6, 1250, null);
        network.addLine(line);

        UcteElementId transformerId = new UcteElementId(code1, code2, '1');
        UcteTransformer transformer = new UcteTransformer(transformerId, UcteElementStatus.REAL_ELEMENT_IN_OPERATION,
                1.0, 0.1, 1e-6, 1500, null, 400.0, 225.0, 1000.0, 1e-6);
        network.addTransformer(transformer);

        UcteRegulation regulation = new UcteRegulation(transformerId, null, null);
        network.addRegulation(regulation);

        network.fix(ReportNode.NO_OP);

        return network;
    }
}
