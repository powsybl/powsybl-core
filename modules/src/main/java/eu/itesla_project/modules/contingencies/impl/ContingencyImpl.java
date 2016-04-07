/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.itesla_project.modules.contingencies.Contingency;
import eu.itesla_project.modules.contingencies.ContingencyElement;
import eu.itesla_project.modules.contingencies.tasks.CompoundModificationTask;
import eu.itesla_project.modules.contingencies.tasks.ModificationTask;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ContingencyImpl implements Contingency {

    private final String id;

    private final List<ContingencyElement> elements;
    
    private Set<String> zones; 

    public ContingencyImpl(String id, ContingencyElement elements) {
        this(id, Arrays.asList(elements));
    }

    public ContingencyImpl(String id, List<ContingencyElement> elements) {
        this.id = id;
        this.elements = elements;
        
    }
    
    public ContingencyImpl(String id, List<ContingencyElement> elements, List<String> zones) {
        this.id = id;
        this.elements = elements;
        this.zones = new HashSet<String>(zones);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Collection<ContingencyElement> getElements() {
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
        for (ContingencyElement element : elements) {
            subTasks.add(element.toTask());
        }
        return new CompoundModificationTask(subTasks);
    }
    

}
