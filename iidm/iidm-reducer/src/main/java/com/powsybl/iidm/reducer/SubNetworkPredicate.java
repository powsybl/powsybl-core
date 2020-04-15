/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.*;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A network reducer predicate that allow reduction based on a center voltage level and all other voltage level neighbors
 * within a specified depth.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SubNetworkPredicate implements NetworkPredicate {

    private final IdentifierNetworkPredicate delegate;

    public SubNetworkPredicate(VoltageLevel vl, int maxDepth) {
        delegate = init(vl, maxDepth);
    }

    private static IdentifierNetworkPredicate init(VoltageLevel vl, int maxDepth) {
        Objects.requireNonNull(vl);
        if (maxDepth < 0) {
            throw new IllegalArgumentException("Invalid max depth value: " + maxDepth);
        }
        Set<String> voltageLevelIds = new LinkedHashSet<>();
        traverse(vl, 0, maxDepth, voltageLevelIds);
        return new IdentifierNetworkPredicate(voltageLevelIds);
    }

    private static void traverse(VoltageLevel vl, int depth, int maxDepth, Set<String> voltageLevelIds) {
        if (voltageLevelIds.contains(vl.getId()) || depth > maxDepth) {
            return;
        }

        voltageLevelIds.add(vl.getId());

        vl.visitEquipments(new DefaultTopologyVisitor() {
            private void visitBranch(Branch branch, Branch.Side side) {
                switch (side) {
                    case ONE:
                        traverse(branch.getTerminal2().getVoltageLevel(), depth + 1, maxDepth, voltageLevelIds);
                        break;
                    case TWO:
                        traverse(branch.getTerminal1().getVoltageLevel(), depth + 1, maxDepth, voltageLevelIds);
                        break;
                    default:
                        throw new IllegalStateException("Unknown side: " + side);
                }
            }

            @Override
            public void visitLine(Line line, Branch.Side side) {
                visitBranch(line, side);
            }

            @Override
            public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, Branch.Side side) {
                visitBranch(transformer, side);
            }

            @Override
            public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
                switch (side) {
                    case ONE:
                        traverse(transformer.getLeg2().getTerminal().getVoltageLevel(), depth + 1, maxDepth, voltageLevelIds);
                        traverse(transformer.getLeg3().getTerminal().getVoltageLevel(), depth + 1, maxDepth, voltageLevelIds);
                        break;
                    case TWO:
                        traverse(transformer.getLeg1().getTerminal().getVoltageLevel(), depth + 1, maxDepth, voltageLevelIds);
                        traverse(transformer.getLeg3().getTerminal().getVoltageLevel(), depth + 1, maxDepth, voltageLevelIds);
                        break;
                    case THREE:
                        traverse(transformer.getLeg1().getTerminal().getVoltageLevel(), depth + 1, maxDepth, voltageLevelIds);
                        traverse(transformer.getLeg2().getTerminal().getVoltageLevel(), depth + 1, maxDepth, voltageLevelIds);
                        break;
                    default:
                        throw new IllegalStateException("Unknown side: " + side);
                }
            }
        });
    }

    @Override
    public boolean test(Substation substation) {
        return delegate.test(substation);
    }

    @Override
    public boolean test(VoltageLevel voltageLevel) {
        return delegate.test(voltageLevel);
    }
}
