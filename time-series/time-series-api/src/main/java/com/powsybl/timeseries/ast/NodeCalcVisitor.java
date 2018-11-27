/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.ast;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface NodeCalcVisitor<R, A> {

    R visit(IntegerNodeCalc nodeCalc, A arg);

    R visit(FloatNodeCalc nodeCalc, A arg);

    R visit(DoubleNodeCalc nodeCalc, A arg);

    R visit(BigDecimalNodeCalc nodeCalc, A arg);

    R visit(TimeNodeCalc nodeCalc, A arg);

    R visit(BinaryOperation nodeCalc, A arg);

    R visit(UnaryOperation nodeCalc, A arg);

    R visit(MinNodeCalc nodeCalc, A arg);

    R visit(MaxNodeCalc nodeCalc, A arg);

    R visit(TimeSeriesNameNodeCalc nodeCalc, A arg);

    R visit(TimeSeriesNumNodeCalc nodeCalc, A arg);
}
