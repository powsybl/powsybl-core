/**
 * Copyright (c) 2026, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.math.graph.TraverseResult;

import java.util.*;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class GetSwitchesBetweenBusBarTraverser implements Terminal.TopologyTraverser {

    private final List<List<Switch>> switchesBetweenBusBarSections = new ArrayList<>();
    private final List<Switch> currentSwitches = new ArrayList<>();
    private final int busBarIndex;
    private final BusbarSection initialBusbarSection;

    public GetSwitchesBetweenBusBarTraverser(BusbarSection initialBusbarSection) {
        this.initialBusbarSection = Objects.requireNonNull(initialBusbarSection);
        this.busBarIndex = initialBusbarSection.getExtension(BusbarSectionPosition.class).getBusbarIndex();
    }

    @Override
    public TraverseResult traverse(Terminal terminal, boolean connected) {
        if (terminal.getConnectable().getType() == IdentifiableType.BUSBAR_SECTION) {
            BusbarSection currentBusbarSection = (BusbarSection) terminal.getConnectable();
            BusbarSectionPosition busbarSectionPosition = currentBusbarSection.getExtension(BusbarSectionPosition.class);
            if (busbarSectionPosition != null) {
                if (busbarSectionPosition.getBusbarIndex() != busBarIndex) {
                    currentSwitches.clear();
                    return TraverseResult.TERMINATE_PATH;
                } else {
                    if (!currentBusbarSection.getId().equals(initialBusbarSection.getId())) {
                        switchesBetweenBusBarSections.add(new ArrayList<>(currentSwitches));
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

    public List<List<Switch>> getSwitchesBetweenBusBarSections() {
        return switchesBetweenBusBarSections;
    }
}
