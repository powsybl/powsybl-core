/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.powsybl.contingency.Contingency;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TimeLine {

    private Contingency contingency;

    private final Set<String> actions = new LinkedHashSet<>();

    public TimeLine(Contingency contingency) {
        this.contingency = contingency;
    }

    public Contingency getContingency() {
        return contingency;
    }

    Set<String> getActions() {
        return actions;
    }

    public boolean actionTaken(String action) {
        return actions.contains(action);
    }
}
