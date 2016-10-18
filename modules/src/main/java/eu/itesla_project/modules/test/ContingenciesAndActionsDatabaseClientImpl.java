/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.contingencies.Action;
import eu.itesla_project.modules.contingencies.ActionPlan;
import eu.itesla_project.modules.contingencies.ActionsContingenciesAssociation;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.modules.contingencies.Scenario;
import eu.itesla_project.modules.contingencies.Zone;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ContingenciesAndActionsDatabaseClientImpl implements ContingenciesAndActionsDatabaseClient {

    private final Map<String, Contingency> contingencies = new HashMap<>();
    private final Map<String, Action> actions = new HashMap<>();
    private final List<Scenario> scenarios = new ArrayList<>();
    private final Map<String, Zone> zones = new HashMap<>();
    private final Map<String, ActionPlan> actionPlans = new HashMap<>();
    private final List<ActionsContingenciesAssociation> associations = new ArrayList<>();
    
    
    public ContingenciesAndActionsDatabaseClientImpl() {
    }

    public void addContingency(Contingency contingency) {
        contingencies.put(contingency.getId(), contingency);
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
    	return new ArrayList<Contingency>(contingencies.values());
    }
    
    @Override
	public Contingency getContingency(String id, Network network) {
		return contingencies.get(id);
	}

    public void addAction(Action action) {
        actions.put(action.getId(), action);
    }

    @Override
	public Collection<Action> getActions(Network network) {
		return new ArrayList<Action>(actions.values());
	}
       

	@Override
	public Action getAction(String id, Network network) {
		return actions.get(id);
	}

    public void addScenario(Scenario scenario) {
        scenarios.add(scenario);
    }

    @Override
    public List<Scenario> getScenarios() {
        return scenarios;
    }
    
    public void addZone(Zone zone) {
    	zones.put(zone.getName(), zone);
    }

    @Override
	public Collection<Zone> getZones() {
		return zones.values();
	}
    
    @Override
	public Collection<Zone> getZones(Network network) {
    	return zones.values();
	}
    
	@Override
	public Zone getZone(String id) {
		return zones.get(id);
	}
	
	public void addActionPlan(ActionPlan actionPlan) {
		actionPlans.put(actionPlan.getName(), actionPlan);
	}
	
	@Override
	public Collection<ActionPlan> getActionPlans() {
		return actionPlans.values();
	}
	
	@Override
	public Collection<ActionPlan> getActionPlans(Network network) {
		return actionPlans.values();
	}

	@Override
	public ActionPlan getActionPlan(String id) {
		return actionPlans.get(id);
	}	

	public void addActionsCtgAssociations(ActionsContingenciesAssociation association) {
		associations.add(association);
	}
	
	@Override
	public Collection<ActionsContingenciesAssociation> getActionsCtgAssociations() {
		return associations;
	}

	@Override
	public List<ActionsContingenciesAssociation> getActionsCtgAssociations(Network network) {
		return associations;
	}

	@Override
	public Collection<ActionsContingenciesAssociation> getActionsCtgAssociationsByContingency(String contingencyId) {
		List<ActionsContingenciesAssociation> associationForContingency = new ArrayList<ActionsContingenciesAssociation>();
		for (ActionsContingenciesAssociation association : associations) {
			if ( association.getContingenciesId().contains(contingencyId))
				associationForContingency.add(association);
		}
		return associationForContingency;
	}

}
