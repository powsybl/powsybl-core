/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.tools;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.google.auto.service.AutoService;

import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.modules.mcla.ForecastErrorsDataStorage;
import eu.itesla_project.modules.online.TimeHorizon;
import eu.itesla_project.offline.forecast_errors.ForecastErrorsAnalysisConfig;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class DeleteForecastErrorsAnalysisTool implements Tool {
	
	private static Command COMMAND = new Command() {
		
		@Override
		public String getName() {
			return "delete-forecast-errors-analysis";
		}

		@Override
		public String getTheme() {
			return Themes.MCLA;
		}

		@Override
		public String getDescription() {
			return "delete stored forecast errors analysis";
		}

		@Override
		public Options getOptions() {
			Options options = new Options();
			options.addOption(Option.builder().longOpt("analysis")
	                .desc("analysis id")
	                .hasArg()
	                .required()
	                .argName("ID")
	                .build());
			options.addOption(Option.builder().longOpt("time-horizon")
	                .desc("time horizon (example DACF)")
	                .hasArg()
	                .required()
	                .argName("TH")
	                .build());
			return options;
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
		ForecastErrorsAnalysisConfig config = ForecastErrorsAnalysisConfig.load();
		ForecastErrorsDataStorage feDataStorage = config.getForecastErrorsDataStorageFactoryClass().newInstance().create(); 
		String analysisId = line.getOptionValue("analysis");
		TimeHorizon timeHorizon = TimeHorizon.fromName(line.getOptionValue("time-horizon"));
		System.out.println("Deleting analysis " + analysisId + " with time horizon " + timeHorizon);
		if ( feDataStorage.isForecastErrorsDataAvailable(analysisId, timeHorizon)
			 || feDataStorage.areStatisticsAvailable(analysisId, timeHorizon) ) {
			if ( feDataStorage.deleteAnalysis(analysisId, timeHorizon) )
				System.out.println("Analysis " + analysisId + " with time horizon " + timeHorizon + " deleted");
			else
				System.out.println("Cannot delete analysis " + analysisId + " with time horizon " + timeHorizon);
		} else
			System.out.println("No analysis " + analysisId + " with time horizon " + timeHorizon);
	}

}
