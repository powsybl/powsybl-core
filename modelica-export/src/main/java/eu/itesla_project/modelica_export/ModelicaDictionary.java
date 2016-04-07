/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.iidm.ddb.model.Internal;
import eu.itesla_project.iidm.network.Identifiable;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public final class ModelicaDictionary {
	
	public ModelicaDictionary(Map<String, String> dictionary) {
		this.dictionary = dictionary;
	}

    public boolean add(Identifiable identifiable, String modelicaName) {

        if (dictionary.containsKey(identifiable.getId())) return false;
        
        dictionary.put(identifiable.getId(), modelicaName);
        return true;
    }
    
    public boolean add(Internal internal, String modelicaName) {

        if (dictionary.containsKey(internal.getNativeId())) return false;
        
        dictionary.put(internal.getNativeId(), modelicaName);
        return true;
    }
    
    public boolean add(String sourceId, String modelicaName) {

        if (dictionary.containsKey(sourceId)) return false;
        
        dictionary.put(sourceId, modelicaName);
        return true;
    }
    
    public void change(Identifiable identifiable, String modelicaName) {
        if (dictionary.containsKey(identifiable.getId())) {
        	dictionary.put(dictionary.get(identifiable.getId()), modelicaName);
        }
    }

    public String getModelicaName(Identifiable identifiable) {
        if (dictionary.containsKey(identifiable.getId())) {
        	return dictionary.get(identifiable.getId());
        }

        if (!dictionary.containsKey(identifiable.getId())) {
        	_log.info("IIDM name: " + identifiable.getId() + ". Modelica name: " + dictionary.get(identifiable.getId()));
        }
        else {
        	_log.info("IIDM name: " + identifiable.getName());
        }
        
        return identifiable.getId();
    }
    
    public String getModelicaName(Internal internal) {
        if (dictionary.containsKey(internal.getNativeId())) {
        	return dictionary.get(internal.getNativeId());
        }

        if (!dictionary.containsKey(internal.getNativeId())) {
        	_log.info("IIDM name: " + internal.getNativeId());
        }
        
        return internal.getNativeId();
    }
    
    public String getModelicaName(String sourceId) {
        if (dictionary.containsKey(sourceId)) {
        	return dictionary.get(sourceId);
        }

        if (!dictionary.containsKey(sourceId)) {
        	_log.info("IIDM name: " + sourceId);
        }
        
        return sourceId;
    }

    public boolean isModelicaNameDefined(String name) {
        return this.dictionary.containsValue(name);
    }
    
	public Map<String, String> getDictionary() {
		return dictionary;
	}
	
	private final Map<String, String>	dictionary;

	private static final Logger			_log		= LoggerFactory.getLogger(ModelicaDictionary.class);

}