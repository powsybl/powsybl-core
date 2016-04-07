/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag_imp_exp.utils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.iidm.ddb.model.Parameter;
import eu.itesla_project.iidm.ddb.model.Parameters;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class Utils {

	static Logger log = LoggerFactory.getLogger(Utils.class.getName());

	public static byte[] stringAsByteArrayUTF8(String par) {
		try {
			return par.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
			return new byte[] {};
		}
	}
	
	public static HashMap<String, Object> getHashMapFromParameters(Parameters pars) {
		HashMap<String, Object> hm=new HashMap<String,Object>();
		if (pars != null) {
			List<Parameter> parList=pars.getParameters();
			for (Parameter parameter : parList) {
				String varName=parameter.getName();
				Object varValue=parameter.getValue();
				hm.put(varName, varValue);
			}
		}
		return hm;
	}
	

}
