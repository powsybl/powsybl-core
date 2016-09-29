/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies.mock;

import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.contingencies.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class ContingenciesAndActionsDatabaseClientMock implements ContingenciesAndActionsDatabaseClient {

    @Override
    public Contingency getContingency(String id, Network network) {
        return null;
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Action> getActions(Network network) {
        return null;
    }

    @Override
    public Action getAction(String id, Network network) {
        return null;
    }

    @Override
    public Collection<Zone> getZones() {
        return null;
    }

    @Override
    public Collection<Zone> getZones(Network network) {
        return null;
    }

    @Override
    public Zone getZone(String id) {
        return null;
    }

    @Override
    public Collection<ActionPlan> getActionPlans() {
        return null;
    }

    @Override
    public ActionPlan getActionPlan(String id) {
        return null;
    }

    @Override
    public Collection<ActionPlan> getActionPlans(Network network) {
        return null;
    }

    @Override
    public Collection<ActionsContingenciesAssociation> getActionsCtgAssociationsByContingency(String contingencyId) {
        return null;
    }

    @Override
    public List<ActionsContingenciesAssociation> getActionsCtgAssociations(Network network) {
        return null;
    }

    @Override
    public Collection<ActionsContingenciesAssociation> getActionsCtgAssociations() {
        return null;
    }

    @Override
    public List<Scenario> getScenarios() {
        return null;
    }
}
