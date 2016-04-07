/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies;

import java.util.Collection;
import java.util.List;

import eu.itesla_project.iidm.network.Network;

/**
 * Contingencies and Actions Database Client
 *
 * @author Quinary <itesla@quinary.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ContingenciesAndActionsDatabaseClient {

	/**
	 * Get a contingency of a network
	 * @param id the id of the contingency
	 * @param network the network
	 * @return the contingency
	 */
	Contingency getContingency(String id, Network network);

	/**
	 * Get the contingencies of a network
	 * @param network the network
	 * @return the list of contingencies
	 */
	List<Contingency> getContingencies(Network network);
    
    /**
     * Get the actions of a network
     * @param network the network
     * @return the list of actions
     */
    Collection<Action> getActions(Network network);
    
    /**
     * Get an action of a network
     * @param id the id of the action
     * @param network the network
     * @return the action
     */
    Action getAction(String id, Network network);
    
    /**
     * Get the zones
     * @return the zones
     */
    Collection<Zone> getZones();    
    
    /**
     * Get the zones of a network
     * @param network the network
     * @return the list of zones
     */
    Collection<Zone> getZones(Network network);
    
    /**
     * Get a zone
     * @param id the id of the zone
     * @return the zone
     */
    Zone getZone(String id);
    
    /**
     * Get the action plans
     * @return the list of action plans
     */
    Collection<ActionPlan> getActionPlans();
    
    /**
     * Get an action plan
     * @param id the id of the action plan
     * @return the action plan
     */
    ActionPlan getActionPlan(String id);
    
    /**
     * Get the action plans of a network
     * @param network the network
     * @return the list of action plans
     */
    Collection<ActionPlan> getActionPlans(Network network);    
    
    /**
     * Get the associations, that include a specific contingency, between actions, contingencies and constraints
     * @param contingencyId the id of the contingency
     * @return the list of associations
     */
    Collection<ActionsContingenciesAssociation> getActionsCtgAssociationsByContingency(String contingencyId);
    
    /**
     * Get the associations between actions, contingencies and constraints of a network
     * @param network the network
     * @return the list of associations
     */
    List<ActionsContingenciesAssociation> getActionsCtgAssociations(Network network);
    
    /**
     * Get the associations between actions, contingencies and constraints
     * @return the list of associations
     */
    Collection<ActionsContingenciesAssociation> getActionsCtgAssociations();

    /**
     * Get the scenarios
     * @return the list of scenarios
     */
    List<Scenario> getScenarios();
	
    

}
