/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.modelica_imp_exp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Luis Maria Zamarreno <zamarrenolm@aia.com>
 * @author Silvia Machado <machados@aia.es>
 */
public class ModelicaMainClassExtractor extends ModelicaHierarchyExtractor
{
	@Override
	public void onStartFile(File file)
	{
		super.onStartFile(file);
		initFile(file);
	}

	@Override
	public void onStartClass(String specifier, String ident, String comment, boolean isComposition, String line)
	{
		super.onStartClass(specifier, ident, comment, isComposition, line);
		log.debug("Class [" + specifier + "], [" + ident + "]");
		checkMainClass();
	}

	@Override
	public void onParameter(ModelicaParameter param)
	{
		ModelicaHierarchy.Item currentItem = getHierarchy().getStack().peek();
		if (currentItem == mainClass)
		{
			parameters.add(param);
		}
	}

	public String getMainClassQualifiedName()
	{
		return mainClassQualifiedName;
	}
	
	public String getMainClassComment()
	{
		return mainClass.comment;
	}
	
	public List<ModelicaParameter> getParameters()
	{
		return parameters;
	}

	void initFile(File file)
	{
		mainClass = null;
		mainClassQualifiedName = null;
		parameters = null;
	}

	void checkMainClass()
	{
		// This is not the main class if the main class has already been identified
		if (mainClass != null) return;

		// Consider if the current context of hierarchy is the main class
		ModelicaHierarchy.Item current = getHierarchy().getStack().peek();

		// Packages are not considered
		if (current.isClassGroup) return;

		// We have found the main class
		mainClass = current;
		mainClassQualifiedName = getHierarchy().getQualifiedName();
		parameters = new ArrayList<ModelicaParameter>();
	}

	ModelicaHierarchy.Item	mainClass;
	String					mainClassQualifiedName;
	List<ModelicaParameter>	parameters;

	static final Logger log	= LoggerFactory.getLogger(ModelicaMainClassExtractor.class);
}
