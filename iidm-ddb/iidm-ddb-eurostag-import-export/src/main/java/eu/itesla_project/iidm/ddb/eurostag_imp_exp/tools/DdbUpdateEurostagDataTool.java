/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag_imp_exp.tools;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.iidm.ddb.eurostag_imp_exp.DdbConfig;
import eu.itesla_project.iidm.ddb.eurostag_imp_exp.DdbDtaImpExp;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class DdbUpdateEurostagDataTool implements Tool {

	public final static String DATA_DIR="data-dir";
	public final static String DD_FILE="dd-file";
	public final static String EUROSTAG_VERSION="eurostag-version";
	public final static String HOST="host";
	public final static String PORT="port";
	public final static String USER="user";
	public final static String PASSWORD="password";

	private static Command COMMAND = new Command() {

		@Override
		public String getName() {
			return "ddb-update-eurostag-dd";
		}

		@Override
		public String getTheme() {
			return "Dynamic Database";
		}

		@Override
		public String getDescription() {
			return "update dynamic database from Eurostag dd file";
		}

		@Override
		@SuppressWarnings("static-access")
		public Options getOptions() {
			Options opts = new Options();

			opts.addOption(Option.builder().longOpt(DD_FILE)
					.desc("dd file path")
					.hasArg()
					.argName("DDFILE")
					.required()
					.build());

			opts.addOption(Option.builder().longOpt(DATA_DIR)
					.desc("data directory")
					.hasArg()
					.argName("DIR")
					.required()
					.build());

			opts.addOption(Option.builder().longOpt(EUROSTAG_VERSION)
					.desc("eurostag Version ( i.e 5.1.1)")
					.hasArg()
					.argName("VERSION")
					.required()
					.build());

			opts.addOption(Option.builder().longOpt(HOST)
					.desc("jboss host")
					.hasArg()
					.argName("HOST")
					.required()
					.build());

			opts.addOption(Option.builder().longOpt(PORT)
					.desc("jboss port")
					.hasArg()
					.argName("PORT")
					.required()
					.build());


			opts.addOption(Option.builder().longOpt(USER)
					.desc("jboss username")
					.hasArg()
					.argName("USER")
					.required()
					.build());

			opts.addOption(Option.builder().longOpt(PASSWORD)
					.desc("jboss password")
					.hasArg()
					.argName("PASSWORD")
					.required()
					.build());

			return opts;
		}

		@Override
		public String getUsageFooter() {
			return null;
		}

	};

	@Override
	public Command getCommand() {
		return COMMAND;
	}

	@Override
	public void run(CommandLine line) throws Exception {
		String dataDir = line.getOptionValue(DATA_DIR);
		String ddFile = line.getOptionValue(DD_FILE);
		String jbossHost = line.getOptionValue(HOST);
        String jbossPort = line.getOptionValue(PORT);
        String jbossUser = line.getOptionValue(USER);
        String jbossPassword = line.getOptionValue(PASSWORD);
        String eurostagVersion = line.getOptionValue(EUROSTAG_VERSION);

        Path ddFilePath=Paths.get(ddFile);
		Path ddData=Paths.get(dataDir);
		Path ddPath =ddData.resolve("gene");
		Path genPath=ddData.resolve("reguls");
		Path dicoPath=ddData.resolve("dico.txt");

		if ((ddFilePath.toFile().isFile()) == false) {
			throw new RuntimeException(DD_FILE +": "+ ddFile + " is not a file!");
		}

		DdbDtaImpExp ddbImpExp = new DdbDtaImpExp(new DdbConfig(jbossHost, jbossPort, jbossUser, jbossPassword));
		ddbImpExp.setUpdateFlag(true);
		ddbImpExp.loadEurostagData(ddFilePath, dicoPath, eurostagVersion, genPath);

	}

}
