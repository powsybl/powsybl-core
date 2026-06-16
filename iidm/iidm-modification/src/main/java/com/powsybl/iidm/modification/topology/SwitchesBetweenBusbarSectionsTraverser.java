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
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class SwitchesBetweenBusbarSectionsTraverser implements Terminal.TopologyTraverser {

    private final Pair<List<List<Switch>>, List<List<Switch>>> switchesBetweenBusBarSections = Pair.of(new ArrayList<>(), new ArrayList<>());
    private final List<Switch> currentSwitches = new ArrayList<>();
    private final BusbarSection initialBusbarSection;
    private final BusbarSectionPosition initialBusbarSectionPosition;

    public SwitchesBetweenBusbarSectionsTraverser(BusbarSection initialBusbarSection) {
        this.initialBusbarSection = Objects.requireNonNull(initialBusbarSection);
        this.initialBusbarSectionPosition = Objects.requireNonNull(initialBusbarSection.getExtension(BusbarSectionPosition.class));
    }

    @Override
    public TraverseResult traverse(Terminal terminal, boolean connected) {
        if (terminal.getConnectable().getType() == IdentifiableType.BUSBAR_SECTION) {
            BusbarSection currentBusbarSection = (BusbarSection) terminal.getConnectable();
            BusbarSectionPosition currentBusbarSectionPosition = currentBusbarSection.getExtension(BusbarSectionPosition.class);
            if (currentBusbarSectionPosition != null) {
                if (currentBusbarSectionPosition.getBusbarIndex() != initialBusbarSectionPosition.getBusbarIndex()) {
                    currentSwitches.clear();
                    return TraverseResult.TERMINATE_PATH;
                } else {
                    if (!currentBusbarSection.getId().equals(initialBusbarSection.getId())) {
                        if (currentBusbarSectionPosition.getSectionIndex() > initialBusbarSectionPosition.getSectionIndex()) {
                            switchesBetweenBusBarSections.getRight().add(new ArrayList<>(currentSwitches));
                        } else {
                            switchesBetweenBusBarSections.getLeft().add(new ArrayList<>(currentSwitches));
                        }

                    }
                    currentSwitches.clear();
                    return TraverseResult.CONTINUE;
                }
            } else {
                currentSwitches.clear();
                return TraverseResult.TERMINATE_PATH;
            }
        } else {
            currentSwitches.clear();
            return TraverseResult.TERMINATE_PATH;
        }
    }

    @Override
    public TraverseResult traverse(Switch aSwitch) {
        currentSwitches.add(aSwitch);
        return TraverseResult.CONTINUE;
    }

    public Pair<List<List<Switch>>, List<List<Switch>>> getSwitchesBetweenBusBarSections() {
        return switchesBetweenBusBarSections;
    }
}
