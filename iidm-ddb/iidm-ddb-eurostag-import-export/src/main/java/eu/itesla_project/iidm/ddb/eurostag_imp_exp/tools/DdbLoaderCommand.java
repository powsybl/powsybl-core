/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag_imp_exp.tools;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import eu.itesla_project.commons.tools.Command;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DdbLoaderCommand implements Command{
	
	public final static  DdbLoaderCommand INSTANCE=new DdbLoaderCommand();

	public final static String DATA_DIR="data-dir";
	public final static String EUROSTAG_VERSION="eurostag-version";
	public final static String HOST="host";
	public final static String PORT="port";
	public final static String USER="user";
	public final static String PASSWORD="password";
	
	@Override
	public String getName() {
		return "ddb-load-eurostag";
	}

	@Override
	public String getTheme() {
		return "Dynamic Database";
	}

	@Override
	public String getDescription() {
		return "load dynamic database data from Eurostag data";
	}

	@Override
	 @SuppressWarnings("static-access")
	public Options getOptions() {		
		Options opts = new Options();
		
		
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
		// TODO Auto-generated method stub
		return null;
	}

}
