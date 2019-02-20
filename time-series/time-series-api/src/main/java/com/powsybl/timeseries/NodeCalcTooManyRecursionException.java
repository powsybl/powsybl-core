/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.powsybl.commons.PowsyblException;
import com.powsybl.timeseries.ast.NodeCalc;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeCalcTooManyRecursionException extends PowsyblException {

    private final NodeCalc nodeCalc;

    public NodeCalcTooManyRecursionException(NodeCalc nodeCalc, Throwable cause) {
        super(cause);
        this.nodeCalc = Objects.requireNonNull(nodeCalc);
    }

    public NodeCalc getNodeCalc() {
        return nodeCalc;
    }
}
