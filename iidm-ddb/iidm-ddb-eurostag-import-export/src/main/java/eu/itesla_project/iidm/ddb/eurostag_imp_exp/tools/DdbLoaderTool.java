/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag_imp_exp.tools;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;

import com.google.auto.service.AutoService;

import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.iidm.ddb.eurostag_imp_exp.DdbConfig;
import eu.itesla_project.iidm.ddb.eurostag_imp_exp.DynDataLoader;

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
		String dataDir = line.getOptionValue(DdbLoaderCommand.DATA_DIR);
		String jbossHost = line.getOptionValue(DdbLoaderCommand.HOST);
        String jbossPort = line.getOptionValue(DdbLoaderCommand.PORT);
        String jbossUser = line.getOptionValue(DdbLoaderCommand.USER);
        String jbossPassword = line.getOptionValue(DdbLoaderCommand.PASSWORD);
        String eurostagVersion = line.getOptionValue(DdbLoaderCommand.EUROSTAG_VERSION);
        
        DdbConfig ddbConfig = new DdbConfig(jbossHost, jbossPort, jbossUser, jbossPassword);
        Path ddData=Paths.get(dataDir);
        Path ddPath =ddData.resolve("gene");
        Path genPath=ddData.resolve("reguls");
        Path dicoPath=ddData.resolve("dico.txt");
        DynDataLoader dn= new DynDataLoader(
        		dicoPath,
        		ddPath,
        		genPath,
        		eurostagVersion,//"5.1.1",
				ddbConfig.getJbossHost(), ddbConfig.getJbossPort(), ddbConfig.getJbossUser(), ddbConfig.getJbossPassword()
				);
		dn.loadDynData();
	}

}
