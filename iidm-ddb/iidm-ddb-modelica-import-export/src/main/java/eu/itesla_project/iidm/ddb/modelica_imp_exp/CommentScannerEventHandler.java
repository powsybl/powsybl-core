/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.modelica_imp_exp;

/**
 * @author Luis Maria Zamarreno <zamarrenolm@aia.com>
 * @author Silvia Machado <machados@aia.es>
 */
public interface CommentScannerEventHandler
{
	void onQuoted(String quoted, boolean open, boolean close);

	void onLineComment(String lineComment);

	void onBlockComment(String blockComment, boolean open, boolean close);

	void onText(String text);

	void onLineScanned(String line);
}
