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
public class ModelicaRegulatorModelExtractor extends ModelicaModelExtractor
{
	ModelicaRegulatorModelExtractor(String libPackageName, String regulatorsPackageName)
	{
		this.libPackageName = libPackageName;
		this.regulatorsPackageName = regulatorsPackageName;
	}
	
	@Override
	public void onStartFile(File file)
	{
		String newLine = System.getProperty("line.separator");
		super.onStartFile(file);
		text.append(ModelicaGrammar.PACKAGE);
		text.append(" ");
		text.append(libPackageName);
		text.append(newLine);
		text.append(ModelicaGrammar.PACKAGE);
		text.append(" ");
		text.append(regulatorsPackageName);
		text.append(newLine);
	}

	@Override
	public void onEndFile(File file)
	{
		String newLine = System.getProperty("line.separator");
		super.onEndFile(file);
		text.append(ModelicaGrammar.END);
		text.append(" ");
		text.append(regulatorsPackageName);
		text.append(";");
		text.append(newLine);
		text.append(ModelicaGrammar.END);
		text.append(" ");
		text.append(libPackageName);
		text.append(";");
	}

	@Override
	public String getMainClassQualifiedName()
	{
		return libPackageName + "." + regulatorsPackageName + "." + mainClassQualifiedName;
	}

	private String libPackageName;
	private String regulatorsPackageName;
}
