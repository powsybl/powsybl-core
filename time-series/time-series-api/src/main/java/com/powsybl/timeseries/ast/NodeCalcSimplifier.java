/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.ast;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NodeCalcSimplifier extends NodeCalcModifier<Void> {

    public static NodeCalc simplify(NodeCalc nodeCalc) {
        Objects.requireNonNull(nodeCalc);
        NodeCalc simplifiedNodeCalc = nodeCalc.accept(new NodeCalcSimplifier(), null, 0);
        return simplifiedNodeCalc != null ? simplifiedNodeCalc : nodeCalc;
    }
}
