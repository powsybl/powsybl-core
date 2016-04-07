/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies.impl;

import eu.itesla_project.modules.contingencies.Scenario;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ScenarioImpl implements Scenario {

    private final String contingency;

    private final List<String> actions;

    public ScenarioImpl(String contingency, List<String> actions) {
        this.contingency = contingency;
        this.actions = actions;
    }

    @Override
    public String getContingency() {
        return contingency;
    }

    @Override
    public List<String> getActions() {
        return actions;
    }

}
