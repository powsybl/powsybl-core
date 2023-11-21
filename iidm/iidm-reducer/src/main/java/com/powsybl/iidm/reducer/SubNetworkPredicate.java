/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.*;

import java.util.*;

/**
 * A network reducer predicate that allow reduction based on a center voltage level and all other voltage level neighbors
 * within a specified depth.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SubNetworkPredicate implements NetworkPredicate {

    private final IdentifierNetworkPredicate delegate;

    public SubNetworkPredicate(VoltageLevel vl, int maxDepth) {
        delegate = init(vl, maxDepth);
    }

    static class Node {
        private final VoltageLevel vl;
        private final int depth;

        Node(VoltageLevel vl, int depth) {
            this.vl = vl;
            this.depth = depth;
        }
    }

    private static IdentifierNetworkPredicate init(VoltageLevel rootVl, int maxDepth) {
        Objects.requireNonNull(rootVl);
        if (maxDepth < 0) {
            throw new IllegalArgumentException("Invalid max depth value: " + maxDepth);
        }
        Set<String> traversedVoltageLevelIds = new LinkedHashSet<>();

        // BFS traversal of voltage levels
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(new Node(rootVl, 0));
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            traversedVoltageLevelIds.add(node.vl.getId());
            if (node.depth < maxDepth) {
                visitBranches(traversedVoltageLevelIds, stack, node.vl, node.depth);
            }
        }

        return new IdentifierNetworkPredicate(traversedVoltageLevelIds);
    }

    private static void visitBranches(Set<String> traversedVoltageLevelIds, Deque<Node> stack, VoltageLevel vl, int depth) {
        vl.visitEquipments(new DefaultTopologyVisitor() {
            private void visitBranch(Branch<?> branch, TwoSides side) {
                VoltageLevel nextVl;
                switch (side) {
                    case ONE:
                        nextVl = branch.getTerminal2().getVoltageLevel();
                        break;
                    case TWO:
                        nextVl = branch.getTerminal1().getVoltageLevel();
                        break;
                    default:
                        throw new IllegalStateException("Unknown side: " + side);
                }
                if (!traversedVoltageLevelIds.contains(nextVl.getId())) {
                    stack.push(new Node(nextVl, depth + 1));
                }
            }

            @Override
            public void visitLine(Line line, TwoSides side) {
                visitBranch(line, side);
            }

            @Override
            public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, TwoSides side) {
                visitBranch(transformer, side);
            }

            @Override
            public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeSides side) {
                VoltageLevel nextVl1;
                VoltageLevel nextVl2;
                switch (side) {
                    case ONE:
                        nextVl1 = transformer.getLeg2().getTerminal().getVoltageLevel();
                        nextVl2 = transformer.getLeg3().getTerminal().getVoltageLevel();
                        break;
                    case TWO:
                        nextVl1 = transformer.getLeg1().getTerminal().getVoltageLevel();
                        nextVl2 = transformer.getLeg3().getTerminal().getVoltageLevel();
                        break;
                    case THREE:
                        nextVl1 = transformer.getLeg1().getTerminal().getVoltageLevel();
                        nextVl2 = transformer.getLeg2().getTerminal().getVoltageLevel();
                        break;
                    default:
                        throw new IllegalStateException("Unknown side: " + side);
                }
                if (!traversedVoltageLevelIds.contains(nextVl1.getId())) {
                    stack.push(new Node(nextVl1, depth + 1));
                }
                if (!traversedVoltageLevelIds.contains(nextVl2.getId())) {
                    stack.push(new Node(nextVl2, depth + 1));
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
