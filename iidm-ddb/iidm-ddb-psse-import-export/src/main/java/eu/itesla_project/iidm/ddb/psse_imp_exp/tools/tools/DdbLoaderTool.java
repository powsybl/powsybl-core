/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.psse_imp_exp.tools.tools;

import java.nio.file.Path;
import java.nio.file.Paths;

import eu.itesla_project.iidm.ddb.psse_imp_exp.DdbDyrLoader;
import org.apache.commons.cli.CommandLine;

import com.google.auto.service.AutoService;

import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.iidm.ddb.psse_imp_exp.DdbConfig;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class DdbLoaderTool implements Tool {

	@Override
	public Command getCommand() {
		return DdbLoaderCommand.INSTANCE;
	}

	@Override
	public void run(CommandLine line) throws Exception {
		String dyrPathS  = line.getOptionValue(DdbLoaderCommand.PSSE_DYRFILEPATH);
		String mappingPathS  = line.getOptionValue(DdbLoaderCommand.PSSE_MAPPINGFILEPATH);
		String jbossHost = line.getOptionValue(DdbLoaderCommand.HOST);
        String jbossPort = line.getOptionValue(DdbLoaderCommand.PORT);
        String jbossUser = line.getOptionValue(DdbLoaderCommand.USER);
        String jbossPassword = line.getOptionValue(DdbLoaderCommand.PASSWORD);
        String psseVersion = line.getOptionValue(DdbLoaderCommand.PSSE_VERSION);
		String optionRemoveData = line.getOptionValue(DdbLoaderCommand.OPTION_REMOVE);
        
        DdbConfig ddbConfig = new DdbConfig(jbossHost, jbossPort, jbossUser, jbossPassword);
        Path dyrPath=Paths.get(dyrPathS);
		Path mappingPath=Paths.get(mappingPathS);
		DdbDyrLoader dyrLoader = new DdbDyrLoader();

		boolean removeDataAfterHavingLoadedIt=Boolean.parseBoolean(optionRemoveData);

		dyrLoader.load(dyrPath, mappingPath, psseVersion, ddbConfig, removeDataAfterHavingLoadedIt);
	}

}
