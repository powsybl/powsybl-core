/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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

    R visit(FloatNodeCalc nodeCalc, A arg);

    R visit(DoubleNodeCalc nodeCalc, A arg);

    R visit(BigDecimalNodeCalc nodeCalc, A arg);

    R visit(BinaryOperation nodeCalc, A arg, R left, R right);

    R visit(TimeNodeCalc nodeCalc, A arg, R child);

    NodeCalc iterate(TimeNodeCalc nodeCalc, A arg);

    R visit(UnaryOperation nodeCalc, A arg, R child);

    NodeCalc iterate(UnaryOperation nodeCalc, A arg);

    R visit(MinNodeCalc nodeCalc, A arg, R child);

    NodeCalc iterate(MinNodeCalc nodeCalc, A arg);

    R visit(MaxNodeCalc nodeCalc, A arg, R child);

    NodeCalc iterate(MaxNodeCalc nodeCalc, A arg);

    R visit(CachedNodeCalc nodeCalc, A arg, R child);

    NodeCalc iterate(CachedNodeCalc nodeCalc, A arg);

    R visit(TimeSeriesNameNodeCalc nodeCalc, A arg);

    R visit(TimeSeriesNumNodeCalc nodeCalc, A arg);

    R visit(BinaryMinCalc nodeCalc, A arg, R left, R right);

    R visit(BinaryMaxCalc nodeCalc, A arg, R left, R right);

    Pair<NodeCalc, NodeCalc> iterate(AbstractBinaryNodeCalc nodeCalc, A arg);
}
