/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Identifiable;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.contingencies.ActionParameterBooleanValue;
import eu.itesla_project.modules.contingencies.ActionParameterFloatValue;
import eu.itesla_project.modules.contingencies.ActionParameterIntegerValue;
import eu.itesla_project.modules.contingencies.ActionParameterStringValue;
import eu.itesla_project.modules.contingencies.ActionParameters;
import eu.itesla_project.security.LimitViolation;
import eu.itesla_project.security.LimitViolationType;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class OnlineDbMVStoreUtils {
	
	
	public static String contingenciesIdsToJson(Collection<String> contingenciesIds) {
		return JSONSerializer.toJSON(contingenciesIds).toString();
	}
	
	public static Collection<String> jsonToContingenciesIds(String json) {
		return (Collection<String>) JSONSerializer.toJava(JSONSerializer.toJSON(json));
	}
	
	public static String actionsIdsToJson(List<String> actionsIds) {
		return JSONSerializer.toJSON(actionsIds).toString();
	}
	
	public static List<String> jsonToActionsIds(String json) {
		return (List<String>) JSONSerializer.toJava(JSONSerializer.toJSON(json));
	}
	
	public static String indexesDataToJson(Map<String, Boolean> indexesData) {
		return JSONSerializer.toJSON(indexesData).toString();
	}
	
	public static Map<String, Boolean> jsonToIndexesData(String json) {
		JSONObject jsonObj = (JSONObject) JSONSerializer.toJSON(json);
		return (Map<String, Boolean>) JSONObject.toBean(jsonObj, Map.class);
	}
	
	public static String stateIdsToJson(Collection<Integer> statesIds) {
		return JSONSerializer.toJSON(statesIds).toString();
	}
	
	public static Collection<Integer> jsonToStatesIds(String json) {
		return (Collection<Integer>) JSONSerializer.toJava(JSONSerializer.toJSON(json));
	}
	
	
	public static String indexesTypesToJson(Set<SecurityIndexType> securityIndexTypes) {
		List<String> indexTypes = securityIndexTypes.stream().map(SecurityIndexType::name).collect(Collectors.toList());
		return JSONSerializer.toJSON(indexTypes).toString();
	}
	
	public static Set<SecurityIndexType> jsonToIndexesTypes(String json) {
		List<String> securityIndexesTypes = (List<String>) JSONSerializer.toJava(JSONSerializer.toJSON(json));
		Set<SecurityIndexType> securityIndexes = securityIndexesTypes.stream().map(SecurityIndexType::valueOf).collect(Collectors.toSet());
		return securityIndexes;
	}
	
	public static String violationToJson(Violation violation) {
		Map<String,String> limitViolation = new HashMap<String,String>();
		limitViolation.put("Subject", violation.getSubject());
		limitViolation.put("LimitType", violation.getLimitType().name());
		limitViolation.put("Limit", Float.toString(violation.getLimit()));
		limitViolation.put("Value", Float.toString(violation.getValue()));
		return JSONSerializer.toJSON(limitViolation).toString();
	}
	
	public static Violation jsonToViolation(String json) {
		JSONObject jsonObj = (JSONObject) JSONSerializer.toJSON(json);
		Map<String,String> limitViolation = (Map<String, String>) JSONObject.toBean(jsonObj, Map.class);
		return new Violation(limitViolation.get("Subject"), 
							 LimitViolationType.valueOf(limitViolation.get("LimitType")), 
							 Float.parseFloat(limitViolation.get("Limit")), 
							 Float.parseFloat(limitViolation.get("Value")));
	}
	
	public static String limitViolationToJson(LimitViolation violation) {
		Map<String,String> limitViolation = new HashMap<String,String>();
		limitViolation.put("Subject", violation.getSubject().getId());
		limitViolation.put("LimitType", violation.getLimitType().name());
		limitViolation.put("Limit", Float.toString(violation.getLimit()));
		limitViolation.put("LimitReduction", Float.toString(violation.getLimitReduction()));
		limitViolation.put("Value", Float.toString(violation.getValue()));
		limitViolation.put("Country", violation.getCountry().name());
		limitViolation.put("BaseVoltage", Float.toString(violation.getBaseVoltage()));
		return JSONSerializer.toJSON(limitViolation).toString();
	}
	
	public static LimitViolation jsonToLimitViolation(String json, Network network) {
		JSONObject jsonObj = (JSONObject) JSONSerializer.toJSON(json);
		Map<String,String> limitViolation = (Map<String, String>) JSONObject.toBean(jsonObj, Map.class);
		Identifiable subject = network.getIdentifiable(limitViolation.get("Subject"));
		if ( subject != null ) {
			Country country = null;
			if ( limitViolation.containsKey("Country") )
				country = Country.valueOf(limitViolation.get("Country"));
			float baseVoltage = Float.NaN;
			if ( limitViolation.containsKey("BaseVoltage") )
				baseVoltage = Float.parseFloat(limitViolation.get("BaseVoltage"));
			float limitReduction = 1f;
			if ( limitViolation.containsKey("LimitReduction") )
				limitReduction = Float.parseFloat(limitViolation.get("LimitReduction"));
			return new LimitViolation(subject, 
								 LimitViolationType.valueOf(limitViolation.get("LimitType")), 
								 Float.parseFloat(limitViolation.get("Limit")),
							 	 null,
								 limitReduction,
								 Float.parseFloat(limitViolation.get("Value")),
								 country,
								 baseVoltage);
		}
		return null;
	}
	
	public static String actionParametersToJson(ActionParameters actionParameters) {
		List<Map<String, String>> parameters = new ArrayList<Map<String,String>>();
		if ( actionParameters != null ) {
			for(String name : actionParameters.getNames()) {
				String type = "";
				String value = "";
				if ( actionParameters.getValue(name) instanceof Float ) {
					type = "FLOAT";
					value = Float.toHexString((Float) actionParameters.getValue(name));
				}
				if ( actionParameters.getValue(name) instanceof Integer ) {
					type = "INTEGER";
					value = Integer.toString((Integer) actionParameters.getValue(name));
				}
				if ( actionParameters.getValue(name) instanceof String ) {
					type = "STRING";
					value = (String) actionParameters.getValue(name);
				}
				if ( actionParameters.getValue(name) instanceof Boolean ) {
					type = "BOOLEAN";
					value = Boolean.toString((Boolean) actionParameters.getValue(name));
				}
				Map<String,String> parameter = new HashMap<String, String>();
				parameter.put("name", name);
				parameter.put("value", value);
				parameter.put("type", type);
				parameters.add(parameter);
			}
		}
		return JSONSerializer.toJSON(parameters).toString();
	}
	
	public static ActionParameters jsonToActionParameters(String json) {
		ActionParameters actionParameters = new ActionParameters();
		if ( json != null ) {
			List<Object> parameters = (List<Object>) JSONSerializer.toJava(JSONSerializer.toJSON(json));
			for(Object parameterObject : parameters) {
				JSONObject parameterJsonObject = JSONObject.fromObject(parameterObject);
				Map<String, String> parameter = (Map<String, String>) JSONObject.toBean(parameterJsonObject, Map.class);
				String name = parameter.get("name");
				String type = parameter.get("type");
				switch (type) {
					case "FLOAT":
						float floatValue = Float.parseFloat(parameter.get("value"));
						actionParameters.addParameter(name, new ActionParameterFloatValue(floatValue));
						break;
					case "INTEGER":
						int intValue = Integer.parseInt(parameter.get("value"));
						actionParameters.addParameter(name, new ActionParameterIntegerValue(intValue));
						break;
					case "STRING":
						String stringValue = parameter.get("value");
						actionParameters.addParameter(name, new ActionParameterStringValue(stringValue));
						break;
					case "BOOLEAN":
						boolean booleanValue = Boolean.parseBoolean(parameter.get("value"));
						actionParameters.addParameter(name, new ActionParameterBooleanValue(booleanValue));
						break;
					default:
						break;
				}
			}
		}
		return actionParameters;
	}
	
	public static String countriesToJson(Set<Country> countries) {
		List<String> countryNames = countries.stream().map(Country::name).collect(Collectors.toList());
		return JSONSerializer.toJSON(countryNames).toString();
	}
	
	public static Set<Country> jsonToCountries(String json) {
		List<String> countryNames = (List<String>) JSONSerializer.toJava(JSONSerializer.toJSON(json));
		Set<Country> countries = countryNames.stream().map(Country::valueOf).collect(Collectors.toSet());
		return countries;
	}
	
	
//	public static void main(String[] args) throws Exception {
//		Collection<String> contingenciesIds = new ArrayList<String>();
//		contingenciesIds.add("c1");
//		contingenciesIds.add("c3");
//		contingenciesIds.add("c4");
//		System.out.println(contingenciesIdsToJson(contingenciesIds));
//		
//		String jsonContingenciesIds = "[\"c1\",\"c3\",\"c4\"]";
//		contingenciesIds = JsonToContingenciesIds(jsonContingenciesIds);
//		for (String contingencyId : contingenciesIds) {
//			System.out.println(contingencyId);
//		}
//		
//		List<String> actionsIds = new ArrayList<String>();
//		actionsIds.add("a1");
//		actionsIds.add("a3");
//		actionsIds.add("a4");
//		System.out.println(actionsIdsToJson(actionsIds));
//		
//		String jsonActionsIds = "[\"a1\",\"a3\",\"a4\"]";
//		actionsIds = JsonToActionsIds(jsonActionsIds);
//		for (String actionId : actionsIds) {
//			System.out.println(actionId);
//		}
//		
//		Map<String, Boolean> indexesData = new HashMap<String, Boolean>();
//		indexesData.put("index1", true);
//		indexesData.put("index2", false);
//		indexesData.put("index3", true);
//		System.out.println(indexesDataToJson(indexesData));
//		 
//		String jsonIndexesData = "{\"index1\":true,\"index3\":true,\"index2\":false}";
//		indexesData = JsonToIndexesData(jsonIndexesData);
//		for (String index : indexesData.keySet()) {
//			System.out.println(index + " = " + indexesData.get(index));
//		}
//		ActionParameters actionParameters = new ActionParameters();
//		actionParameters.addParameter("param1", new ActionParameterStringValue("value2"));
//		actionParameters.addParameter("param2", new ActionParameterFloatValue(5.2f));
//		String json = actionParametersToJson(actionParameters);
//		System.out.println(json);
//		ActionParameters actionParameters1 = jsonToActionParameters(json);
//		System.out.println(actionParameters1);
//	}
//	

}
