/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.monitor;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.contingency.ContingencyContextType;

import java.util.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 * index all state monitors according to their ContingencyContextType
 *
 */
public class StateMonitorIndex {
    private final StateMonitor allStateMonitor;
    private final StateMonitor noneStateMonitor;
    private final Map<String, StateMonitor> specificStateMonitors = new HashMap<>();

    public StateMonitorIndex(List<StateMonitor> stateMonitors) {
        allStateMonitor = new StateMonitor(new ContingencyContext(null, ContingencyContextType.ALL),
                new HashSet<>(), new HashSet<>(), new HashSet<>());
        noneStateMonitor = new StateMonitor(new ContingencyContext(null, ContingencyContextType.NONE),
                new HashSet<>(), new HashSet<>(), new HashSet<>());
        stateMonitors.forEach(monitor -> {
            String id = monitor.getContingencyContext().getContingencyId();
            if (id != null) {
                if (this.specificStateMonitors.containsKey(id)) {
                    this.specificStateMonitors.get(id).merge(monitor);
                } else {
                    this.specificStateMonitors.put(id, monitor);
                }
            } else if (monitor.getContingencyContext().getContextType() == ContingencyContextType.ALL) {
                allStateMonitor.merge(monitor);
            } else if (monitor.getContingencyContext().getContextType() == ContingencyContextType.NONE) {
                noneStateMonitor.merge(monitor);
            }
        });
    }

    public StateMonitor getAllStateMonitor() {
        return allStateMonitor;
    }

    public StateMonitor getNoneStateMonitor() {
        return noneStateMonitor;
    }

    public StateMonitor getSpecificStateMonitor(String id) {
        return specificStateMonitors.get(id);
    }

    public Map<String, StateMonitor> getSpecificStateMonitors() {
        return specificStateMonitors;
    }
}
