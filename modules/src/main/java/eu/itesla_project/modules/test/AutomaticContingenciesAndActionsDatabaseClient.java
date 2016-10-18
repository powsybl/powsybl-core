/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.test;

import com.google.common.collect.Iterables;

import eu.itesla_project.iidm.network.Line;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.contingencies.Action;
import eu.itesla_project.modules.contingencies.ActionPlan;
import eu.itesla_project.modules.contingencies.ActionsContingenciesAssociation;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.contingency.ContingencyElement;
import eu.itesla_project.contingency.LineContingency;
import eu.itesla_project.contingency.ContingencyImpl;
import eu.itesla_project.modules.contingencies.Scenario;
import eu.itesla_project.modules.contingencies.Zone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Contingencies and actions database, that automatically generates N-1 line
 * contingencies for the first n lines of the network.
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AutomaticContingenciesAndActionsDatabaseClient implements ContingenciesAndActionsDatabaseClient {

    private final int lineContigencyCount;

    public AutomaticContingenciesAndActionsDatabaseClient(int lineContigencyCount) {
        this.lineContigencyCount = lineContigencyCount;
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        List<Contingency> contingencies = new ArrayList<>(lineContigencyCount);
        for (Line l : Iterables.limit(network.getLines(), lineContigencyCount)) {
            contingencies.add(new ContingencyImpl(l.getId(), Arrays.<ContingencyElement>asList(new LineContingency(l.getId()))));
        }
        return contingencies;
    }

    

    @Override
    public List<Scenario> getScenarios() {
        return Collections.emptyList();
    }

	@Override
	public Set<Zone> getZones() {
		 throw new UnsupportedOperationException();
	}

	@Override
	public Zone getZone(String id) {
		 throw new UnsupportedOperationException();
	}

	@Override
	public Collection<ActionPlan> getActionPlans() {
		 throw new UnsupportedOperationException();
	}

	@Override
	public ActionPlan getActionPlan(String id) {
		 throw new UnsupportedOperationException();
	}

	@Override
	public Collection<ActionsContingenciesAssociation> getActionsCtgAssociations() {
		 throw new UnsupportedOperationException();
	}

	@Override
	public Collection<ActionsContingenciesAssociation> getActionsCtgAssociationsByContingency(String contingencyId) {
		 throw new UnsupportedOperationException();
	}

	@Override
	public Contingency getContingency(String name, Network network) 
	{
		throw new UnsupportedOperationException();
	}

	

	@Override
	public Collection<Action> getActions(Network network) 
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Action getAction(String id, Network network)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Zone> getZones(Network network) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<ActionPlan> getActionPlans(Network network) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ActionsContingenciesAssociation> getActionsCtgAssociations(
			Network network) {
		// TODO Auto-generated method stub
		return null;
	}

}
