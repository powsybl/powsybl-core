/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.ast;

import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Jon Harper <jon.harper at rte-france.com>
 *
 *         The iterate methods allow the visitor to describe which children are
 *         traversed and their order. The handle methods compute results for
 *         nodes from the node and all the results of the children. The visit
 *         methods use the iterate and handle methods and recurse in the
 *         children up to a limit because performance is almost 5 times better
 *         when using recursion compared to the iterative algorithm using
 *         stacks.
 *
 * @see NodeCalcVisitors
 */
public interface NodeCalcVisitor<R, A> {

    R visit(IntegerNodeCalc nodeCalc, A arg);

    R visit(FloatNodeCalc nodeCalc, A arg);

    R visit(DoubleNodeCalc nodeCalc, A arg);

    R visit(BigDecimalNodeCalc nodeCalc, A arg);

    R visit(TimeNodeCalc nodeCalc, A arg, R child);

    NodeCalc iterate(TimeNodeCalc nodeCalc, A arg);

    R visit(BinaryOperation nodeCalc, A arg, R left, R right);

    Pair<NodeCalc, NodeCalc> iterate(BinaryOperation nodeCalc, A arg);

    R visit(UnaryOperation nodeCalc, A arg, R child);

    NodeCalc iterate(UnaryOperation nodeCalc, A arg);

    R visit(MinNodeCalc nodeCalc, A arg, R child);

    NodeCalc iterate(MinNodeCalc nodeCalc, A arg);

    R visit(MaxNodeCalc nodeCalc, A arg, R child);

    NodeCalc iterate(MaxNodeCalc nodeCalc, A arg);

    R visit(TimeSeriesNameNodeCalc nodeCalc, A arg);

    R visit(TimeSeriesNumNodeCalc nodeCalc, A arg);
}
