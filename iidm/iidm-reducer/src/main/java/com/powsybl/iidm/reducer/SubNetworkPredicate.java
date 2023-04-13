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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
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
        Set<String> voltageLevelIds = new LinkedHashSet<>();
        Queue<Node> queue = new ArrayDeque<>();
        queue.add(new Node(rootVl, 0));

        while (!queue.isEmpty()) {
            Node node = queue.poll();
            VoltageLevel vl = node.vl;
            int depth = node.depth;
            voltageLevelIds.add(vl.getId());

            if (depth < maxDepth) {
                vl.visitEquipments(new DefaultTopologyVisitor() {
                    private void visitBranch(Branch branch, Branch.Side side) {
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
                        if (!voltageLevelIds.contains(nextVl.getId())) {
                            queue.add(new Node(nextVl, depth + 1));
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
                        if (!voltageLevelIds.contains(nextVl1.getId())) {
                            queue.add(new Node(nextVl1, depth + 1));
                        }
                        if (!voltageLevelIds.contains(nextVl2.getId())) {
                            queue.add(new Node(nextVl2, depth + 1));
                        }
                    }
                });
            }
        }

        return new IdentifierNetworkPredicate(voltageLevelIds);
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
