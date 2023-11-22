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

        Set<String> traversedVoltageLevelIds = new HashSet<>();
        traversedVoltageLevelIds.add(rootVl.getId());
        var currentDepth = Set.of(rootVl);

        // BFS traversal of voltage levels
        for (int i = 1; i <= maxDepth; i++) {
            Set<VoltageLevel> nextDepth = i < maxDepth ? new LinkedHashSet<>() : null; // No need to calculate the last nextDepth set
            for (VoltageLevel voltageLevel : currentDepth) {
                findNextDepthVoltageLevels(voltageLevel, traversedVoltageLevelIds, nextDepth);
            }
            currentDepth = nextDepth;
        }

        return new IdentifierNetworkPredicate(traversedVoltageLevelIds);
    }

    private static void findNextDepthVoltageLevels(VoltageLevel vl, Set<String> traversedVoltageLevelIds, Set<VoltageLevel> nextDepth) {
        vl.getLineStream().forEach(l -> visitBranch(l, traversedVoltageLevelIds, vl, nextDepth));
        vl.getTwoWindingsTransformerStream().forEach(t -> visitBranch(t, traversedVoltageLevelIds, vl, nextDepth));
        vl.getThreeWindingsTransformerStream().forEach(t -> visitConnectable(t, traversedVoltageLevelIds, vl, nextDepth));
    }

    private static void visitBranch(Branch<?> branch, Set<String> traversedVoltageLevelIds, VoltageLevel vl, Set<VoltageLevel> nextDepth) {
        visitTerminals(List.of(branch.getTerminal1(), branch.getTerminal2()), traversedVoltageLevelIds, vl, nextDepth);
    }

    private static void visitConnectable(Connectable<?> connectable, Set<String> traversedVoltageLevelIds, VoltageLevel voltageLevel, Set<VoltageLevel> nextDepth) {
        visitTerminals(connectable.getTerminals(), traversedVoltageLevelIds, voltageLevel, nextDepth);
    }

    private static void visitTerminals(List<? extends Terminal> terminals, Set<String> traversedVoltageLevelIds, VoltageLevel voltageLevel, Set<VoltageLevel> nextDepth) {
        terminals.stream().map(Terminal::getVoltageLevel).forEach(vl -> {
            if (!vl.getId().equals(voltageLevel.getId()) && traversedVoltageLevelIds.add(vl.getId()) && nextDepth != null) {
                nextDepth.add(vl);
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
