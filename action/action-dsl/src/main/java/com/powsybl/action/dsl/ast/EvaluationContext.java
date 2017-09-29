/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.action.dsl.ast;

import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.iidm.network.Network;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface EvaluationContext {

    Network getNetwork();

    Contingency getContingency();

    boolean isActionTaken(String actionId);
}
