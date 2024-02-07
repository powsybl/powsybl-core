/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.ast;

import org.apache.commons.lang3.tuple.Pair;

/**
 * A NodeCalcVisitor controls the traversal and performs a computation on {@link NodeCalc} trees.
 *
 * <p>The iterate methods allow the visitor to describe which children are
 * traversed and their order. The visit methods compute results for
 * nodes from the node and all the results of the children.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 * @see NodeCalcVisitors
 */
public interface NodeCalcVisitor<R, A> {

    R visit(IntegerNodeCalc nodeCalc, A arg);

    R visit(IntegerNodeCalc nodeCalc, A arg, int depth);

    R visit(FloatNodeCalc nodeCalc, A arg);

    R visit(FloatNodeCalc nodeCalc, A arg, int depth);

    R visit(DoubleNodeCalc nodeCalc, A arg);

    R visit(DoubleNodeCalc nodeCalc, A arg, int depth);

    R visit(BigDecimalNodeCalc nodeCalc, A arg);

    R visit(BigDecimalNodeCalc nodeCalc, A arg, int depth);

    R visit(BinaryOperation nodeCalc, A arg, R left, R right);

    R visit(BinaryOperation nodeCalc, A arg, int depth);

    R visit(BinaryMinCalc nodeCalc, A arg, R left, R right);

    R visit(BinaryMinCalc nodeCalc, A arg, int depth);

    R visit(BinaryMaxCalc nodeCalc, A arg, R left, R right);

    R visit(BinaryMaxCalc nodeCalc, A arg, int depth);

    Pair<NodeCalc, NodeCalc> iterate(AbstractBinaryNodeCalc nodeCalc, A arg);

    R visit(TimeNodeCalc nodeCalc, A arg, R child);

    R visit(TimeNodeCalc nodeCalc, A arg, int depth);

    NodeCalc iterate(TimeNodeCalc nodeCalc, A arg);

    R visit(UnaryOperation nodeCalc, A arg, R child);

    R visit(UnaryOperation nodeCalc, A arg, int depth);

    NodeCalc iterate(UnaryOperation nodeCalc, A arg);

    R visit(MinNodeCalc nodeCalc, A arg, R child);

    R visit(MinNodeCalc nodeCalc, A arg, int depth);

    NodeCalc iterate(MinNodeCalc nodeCalc, A arg);

    R visit(MaxNodeCalc nodeCalc, A arg, R child);

    R visit(MaxNodeCalc nodeCalc, A arg, int depth);

    NodeCalc iterate(MaxNodeCalc nodeCalc, A arg);

    R visit(TimeSeriesNameNodeCalc nodeCalc, A arg);

    R visit(TimeSeriesNameNodeCalc nodeCalc, A arg, int depth);

    R visit(TimeSeriesNumNodeCalc nodeCalc, A arg);

    R visit(TimeSeriesNumNodeCalc nodeCalc, A arg, int depth);
}
