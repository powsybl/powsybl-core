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

    private static IdentifierNetworkPredicate init(VoltageLevel rootVl, int maxDepth) {
        Objects.requireNonNull(rootVl);
        if (maxDepth < 0) {
            throw new IllegalArgumentException("Invalid max depth value: " + maxDepth);
        }

        Set<String> traversedVoltageLevelIds = new LinkedHashSet<>();
        traversedVoltageLevelIds.add(rootVl.getId());
        var currentDepth = Set.of(rootVl);

        // BFS traversal of voltage levels
        for (int i = 0; i < maxDepth; i++) {
            Set<VoltageLevel> nextDepth = new LinkedHashSet<>();
            for (VoltageLevel voltageLevel : currentDepth) {
                visitBranches(traversedVoltageLevelIds, voltageLevel, nextDepth);
            }
            currentDepth = nextDepth;
        }

        return new IdentifierNetworkPredicate(traversedVoltageLevelIds);
    }

    private static void visitBranches(Set<String> traversedVoltageLevelIds, VoltageLevel vl, Set<VoltageLevel> nextDepth) {
        vl.visitEquipments(new DefaultTopologyVisitor() {
            private void visitBranch(Branch<?> branch, TwoSides side) {
                VoltageLevel nextVl = switch (side) {
                    case ONE -> branch.getTerminal2().getVoltageLevel();
                    case TWO -> branch.getTerminal1().getVoltageLevel();
                };
                if (traversedVoltageLevelIds.add(nextVl.getId())) {
                    nextDepth.add(nextVl);
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
                VoltageLevel nextVl1 = switch (side) {
                    case ONE -> transformer.getLeg2().getTerminal().getVoltageLevel();
                    case TWO -> transformer.getLeg3().getTerminal().getVoltageLevel();
                    case THREE -> transformer.getLeg1().getTerminal().getVoltageLevel();
                };
                if (traversedVoltageLevelIds.add(nextVl1.getId())) {
                    nextDepth.add(nextVl1);
                }

                VoltageLevel nextVl2 = switch (side) {
                    case ONE -> transformer.getLeg3().getTerminal().getVoltageLevel();
                    case TWO -> transformer.getLeg1().getTerminal().getVoltageLevel();
                    case THREE -> transformer.getLeg2().getTerminal().getVoltageLevel();
                };
                if (traversedVoltageLevelIds.add(nextVl2.getId())) {
                    nextDepth.add(nextVl2);
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
