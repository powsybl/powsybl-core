/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies.impl;

import eu.itesla_project.modules.contingencies.Action;
import eu.itesla_project.modules.contingencies.ActionElement;
import eu.itesla_project.modules.contingencies.ActionParameters;
import eu.itesla_project.modules.contingencies.GenerationRedispatching;
import eu.itesla_project.contingency.tasks.CompoundModificationTask;
import eu.itesla_project.contingency.tasks.ModificationTask;

import java.util.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ActionImpl implements Action {

    private final String id;

    private final boolean preventive;

    private final boolean curative;

    private final List<ActionElement> elements;
    
    private Set<String> zones; 
    
    private Number startTime;
    
    private ActionParameters parameters;

    public ActionImpl(String id, boolean preventive, boolean curative, ActionElement... elements) {
        this(id, preventive, curative, Arrays.asList(elements));
    }

    public ActionImpl(String id, boolean preventive, boolean curative, List<ActionElement> elements) {
        this.id = id;
        this.preventive = preventive;
        this.curative = curative;
        this.elements = elements;
    }
    
    public ActionImpl(String id, boolean preventive, boolean curative, List<ActionElement> elements,  List<String> zones) {
        this.id = id;
        this.preventive = preventive;
        this.curative = curative;
        this.elements = elements;
        this.zones = new HashSet<String>(zones);
    }
    
    public ActionImpl(String id, boolean preventive, boolean curative, List<ActionElement> elements,  List<String> zones, Number startTime) {
        this.id = id;
        this.preventive = preventive;
        this.curative = curative;
        this.elements = elements;
        this.zones = new HashSet<String>(zones);
        this.startTime = startTime;
    }
   

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isPreventive() {
        return preventive;
    }

    @Override
    public boolean isCurative() {
        return curative;
    }

    @Override
    public Collection<ActionElement> getElements() {
        return elements;
    }

    @Override
    public Collection<String> getZones() 
    {
    	return zones;
    }
 
    @Override
    public ModificationTask toTask() {
        List<ModificationTask> subTasks = new ArrayList<>(elements.size());
        for (ActionElement element : elements) {
        	if ( element instanceof GenerationRedispatching)
        		subTasks.add(element.toTask(parameters));
        	else
        		subTasks.add(element.toTask());
        }
        return new CompoundModificationTask(subTasks);
    }

	@Override
	public Number getStartTime() {
		return startTime;
	}

	@Override
	public void setParameters(ActionParameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public ActionParameters getParameters() {
		return parameters;
	}

}
