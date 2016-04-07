/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.tools;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.Table;

import com.google.auto.service.AutoService;

import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.modules.mcla.ForecastErrorsAnalysisDetails;
import eu.itesla_project.modules.mcla.ForecastErrorsAnalyzerParameters;
import eu.itesla_project.modules.mcla.ForecastErrorsDataStorage;
import eu.itesla_project.modules.online.TimeHorizon;
import eu.itesla_project.offline.forecast_errors.ForecastErrorsAnalysisConfig;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class ListForecastErrorsAnalysisTool implements Tool {
	
	private static int COLUMN_LENGTH = 55;
	
	private static Command COMMAND = new Command() {
		
		@Override
		public String getName() {
			return "list-forecast-errors-analysis";
		}

		@Override
		public String getTheme() {
			return Themes.MCLA;
		}

		@Override
		public String getDescription() {
			return "list stored forecast errors analysis";
		}

		@Override
		public Options getOptions() {
			return new Options();
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
		List<ForecastErrorsAnalysisDetails> analysisList = feDataStorage.listAnalysis();
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		Table table = new Table(5, BorderStyle.CLASSIC_WIDE);
        table.addCell("ID", new CellStyle(CellStyle.HorizontalAlign.center));
        table.addCell("Date", new CellStyle(CellStyle.HorizontalAlign.center));
        table.addCell("Errors Models", new CellStyle(CellStyle.HorizontalAlign.center));
        table.addCell("Statistics", new CellStyle(CellStyle.HorizontalAlign.center));
        table.addCell("Parameters", new CellStyle(CellStyle.HorizontalAlign.center));
        for (ForecastErrorsAnalysisDetails analysis : analysisList) {
        	ArrayList<TimeHorizon> mergedList = new ArrayList<TimeHorizon>(analysis.getForecastErrorsDataList());
            mergedList.removeAll(analysis.getForecastErrorsStatisticsList());
            mergedList.addAll(analysis.getForecastErrorsStatisticsList());
        	for (TimeHorizon timeHorizon : mergedList) {
        		table.addCell(analysis.getAnalysisId());
        		table.addCell(formatter.print(analysis.getAnalysisDate()));
        		if ( analysis.getForecastErrorsDataList().contains(timeHorizon))
        			table.addCell(timeHorizon.getName());
        		else
        			table.addCell("-");
        		if ( analysis.getForecastErrorsStatisticsList().contains(timeHorizon))
        			table.addCell(timeHorizon.getName());
        		else
        			table.addCell("-");
        		ForecastErrorsAnalyzerParameters parameters = feDataStorage.getParameters(analysis.getAnalysisId(), timeHorizon);
        		if ( parameters != null ) {
        			//table.addCell(parameters.toString().substring(32));
        			String value = parameters.toString().substring(32);
					while ( value.length() > COLUMN_LENGTH ) {
						table.addCell(value.substring(0, COLUMN_LENGTH), new CellStyle(CellStyle.HorizontalAlign.left));
						table.addCell(" ", new CellStyle(CellStyle.HorizontalAlign.left));
						table.addCell(" ", new CellStyle(CellStyle.HorizontalAlign.left));
						table.addCell(" ", new CellStyle(CellStyle.HorizontalAlign.left));
						table.addCell(" ", new CellStyle(CellStyle.HorizontalAlign.left));
						value = value.substring(COLUMN_LENGTH);
					}
					table.addCell(value, new CellStyle(CellStyle.HorizontalAlign.left));
        		} else
        			table.addCell("-");
			}            
        }
        System.out.println(table.render());
	}

}
