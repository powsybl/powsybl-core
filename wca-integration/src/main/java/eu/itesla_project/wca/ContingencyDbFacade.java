/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.wca;

import eu.itesla_project.modules.contingencies.Action;
import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.security.LimitViolation;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ContingencyDbFacade {

    List<Contingency> getContingencies();

    List<List<Action>> getCurativeActions(Contingency contingency, List<LimitViolation> limitViolations);

}
