/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.math.graph.TraverseResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * Traverse the network to find the switch kinds between all busbar sections of a busbar.
 * if there are several switches between two busbar sections, they are merged into a single switch kind.
 * if it contains a breaker, it is considered as a breaker.
 * if it contains a load breaker switch but no breaker, it is considered as a load breaker switch.
 * elsewhere, it is considered as a disconnector.
 *
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class SwitchKindsBetweenBusbarSectionsTraverser implements Terminal.TopologyTraverser {

    private final List<SwitchKind> leftSwitchesBetweenBusbar = new ArrayList<>();
    private final List<SwitchKind> rightSwitchesBetweenBusbar = new ArrayList<>();
    private final List<Switch> currentSwitches = new ArrayList<>();
    private final BusbarSection initialBusbarSection;
    private final BusbarSectionPosition initialBusbarSectionPosition;

    public SwitchKindsBetweenBusbarSectionsTraverser(BusbarSection initialBusbarSection) {
        this.initialBusbarSection = Objects.requireNonNull(initialBusbarSection);
        this.initialBusbarSectionPosition = Objects.requireNonNull(initialBusbarSection.getExtension(BusbarSectionPosition.class));
    }

    @Override
    public TraverseResult traverse(Terminal terminal, boolean connected) {
        if (terminal.getConnectable().getType() != IdentifiableType.BUSBAR_SECTION) {
            return clearAndTerminatePath();
        }
        BusbarSection currentBusbarSection = (BusbarSection) terminal.getConnectable();
        BusbarSectionPosition currentBusbarSectionPosition = currentBusbarSection.getExtension(BusbarSectionPosition.class);
        if (currentBusbarSectionPosition == null || currentBusbarSectionPosition.getBusbarIndex() != initialBusbarSectionPosition.getBusbarIndex()) {
            return clearAndTerminatePath();
        }
        if (!currentBusbarSection.getId().equals(initialBusbarSection.getId())) {
            addCurrentSwitches(currentBusbarSectionPosition);
        }
        currentSwitches.clear();
        return TraverseResult.CONTINUE;
    }

    private TraverseResult clearAndTerminatePath() {
        currentSwitches.clear();
        return TraverseResult.TERMINATE_PATH;
    }

    private void addCurrentSwitches(BusbarSectionPosition currentBusbarSectionPosition) {
        List<SwitchKind> targetSide = currentBusbarSectionPosition.getSectionIndex() > initialBusbarSectionPosition.getSectionIndex()
                ? rightSwitchesBetweenBusbar
                : leftSwitchesBetweenBusbar;
        SwitchKind switchKind = getSwitchKind(currentSwitches);
        if (switchKind != null) {
            targetSide.add(switchKind);
        }
    }

    @Override
    public TraverseResult traverse(Switch aSwitch) {
        currentSwitches.add(aSwitch);
        return TraverseResult.CONTINUE;
    }

    public List<SwitchKind> getLeftSwitchesBetweenBusbar() {
        return leftSwitchesBetweenBusbar;
    }

    public List<SwitchKind> getRightSwitchesBetweenBusbar() {
        return rightSwitchesBetweenBusbar;
    }

    private SwitchKind getSwitchKind(List<Switch> switchList) {
        if (switchList == null || switchList.isEmpty()) {
            return null;
        }
        if (switchList.stream().anyMatch(sw -> sw.getKind().equals(SwitchKind.BREAKER))) {
            return SwitchKind.BREAKER;
        }
        if (switchList.stream().anyMatch(sw -> sw.getKind().equals(SwitchKind.LOAD_BREAK_SWITCH))) {
            return SwitchKind.LOAD_BREAK_SWITCH;
        }
        return SwitchKind.DISCONNECTOR;
    }
}
