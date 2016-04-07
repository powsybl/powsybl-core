/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.sampling.tools;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import eu.itesla_project.commons.tools.Command;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DataComparatorCommand implements Command{
	
	public final static  DataComparatorCommand INSTANCE=new DataComparatorCommand();

	//public final static String DATA_DIR="data-dir";

	@Override
	public String getName() {
		return "wp41-data-comparator";
	}

	@Override
	public String getTheme() {
		return "WP41 Validation";
	}

	@Override
	public String getDescription() {
		return "run WP41 data comparator tool";
	}

	@Override
	 @SuppressWarnings("static-access")
	public Options getOptions() {		
		Options opts = new Options();

        opts.addOption(Option.builder().longOpt("ofile")
                .desc("output files name prefix (e.g. './compared_data' will result in two files: compared_data.fig and compared_data.png; sufixes .png and .fig will be added automatically")
                .hasArg()
                .required()
                .argName("OFILE")
                .build());

        opts.addOption(Option.builder().longOpt("set1")
                .desc("first set of variables to be aggregated and compared ( individual variables or ranges e.g. 1 ,   [1:64],  [1:4,67,89] )")
                .hasArg()
                .argName("S1")
                .build());

        opts.addOption(Option.builder().longOpt("set2")
                .desc("second set of variables to be aggregated and compared ( individual variables or ranges e.g. 2 ,   [65:128],  [65:100,110,120] )")
                .hasArg()
                .argName("S2")
                .build());
		return opts;
	}

	@Override
	public String getUsageFooter() {
		return "Note: either specify both set1 and set2 parameters, or none of them to have the default half-and-half behaviour (ref. Imperial's documentation)";
	}

}
