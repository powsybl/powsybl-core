/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.option;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FaultOptionsIndex {
    private final Map<String, FaultOptions> stateMonitors = new HashMap<>();

    public FaultOptionsIndex(List<FaultOptions> faultOptions) {
        faultOptions.forEach(monitor -> {
            String id = monitor.getFaultContext().getId();
            if (id != null) {
                this.stateMonitors.merge(id, monitor, FaultOptions::merge);
            }
        });
    }

    public Map<String, FaultOptions> getStateMonitors() {
        return stateMonitors;
    }
}
