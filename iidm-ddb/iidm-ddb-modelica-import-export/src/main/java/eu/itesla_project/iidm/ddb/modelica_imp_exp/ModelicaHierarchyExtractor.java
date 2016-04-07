/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.modelica_imp_exp;

import java.io.File;

/**
 * @author Luis Maria Zamarreno <zamarrenolm@aia.com>
 * @author Silvia Machado <machados@aia.es>
 */
public class ModelicaHierarchyExtractor implements ModelicaParserEventHandler
{
	private ModelicaHierarchy	h;

	public ModelicaHierarchy getHierarchy()
	{
		return h;
	}

	@Override
	public void onStartClass(String specifier, String ident, String comment, boolean isComposition, String line)
	{
		// Prepare the hierarchy before handling the line
		h.push(new ModelicaHierarchy.Item(specifier, ident, comment, isComposition, line));
	}

	@Override
	public void onEndClass(String ident)
	{
		h.pop();
	}

	@Override
	public void onParameter(ModelicaParameter param)
	{
	}

	@Override
	public void onLine(String line)
	{
	}

	@Override
	public void onStartFile(File file)
	{
		// A new hierarchy for every parsed file
		h = new ModelicaHierarchy();
	}

	@Override
	public void onEndFile(File file)
	{
		h = null;
	}
}