/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.ast;

import java.math.BigDecimal;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface LiteralNodeCalc extends NodeCalc {

    double toDouble();

    static IntegerNodeCalc createInteger(int value) {
        return new IntegerNodeCalc(value);
    }

    static FloatNodeCalc createFloat(float value) {
        return new FloatNodeCalc(value);
    }

    static DoubleNodeCalc createDouble(double value) {
        return new DoubleNodeCalc(value);
    }

    static BigDecimalNodeCalc createBigDecimal(BigDecimal value) {
        return new BigDecimalNodeCalc(value);
    }
}
