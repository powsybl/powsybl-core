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
public interface ModelicaParserEventHandler
{
	void onStartFile(File file);

	void onEndFile(File file);

	void onStartClass(String specifier, String ident, String comment, boolean isComposition, String line);

	void onEndClass(String ident);

	void onParameter(ModelicaParameter param);

	void onLine(String line);
}