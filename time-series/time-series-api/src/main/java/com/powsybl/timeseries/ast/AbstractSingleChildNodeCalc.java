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
public abstract class AbstractSingleChildNodeCalc implements NodeCalc {

    protected NodeCalc child;

    AbstractSingleChildNodeCalc(NodeCalc child) {
        this.child = Objects.requireNonNull(child);
    }

    public NodeCalc getChild() {
        return child;
    }

    public void setChild(NodeCalc child) {
        this.child = Objects.requireNonNull(child);
    }
}
