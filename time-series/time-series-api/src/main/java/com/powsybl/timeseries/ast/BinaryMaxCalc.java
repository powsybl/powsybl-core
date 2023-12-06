/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.ast;

import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class BinaryMaxCalc extends AbstractBinaryMinMax {

    static final String NAME = "binaryMax";

    public BinaryMaxCalc(NodeCalc left, NodeCalc right) {
        super(left, right);
    }

    @Override
    protected String getJsonName() {
        return NAME;
    }

    static NodeCalc parseJson(JsonParser parser) throws IOException {
        ParsingContext context = parseJson2(parser);
        return new BinaryMaxCalc(context.left, context.right);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BinaryMaxCalc binaryMaxCalc) {
            return binaryMaxCalc.left.equals(left) && binaryMaxCalc.right.equals(right);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return left.hashCode() + right.hashCode();
    }
}
