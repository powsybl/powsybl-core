/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.tools;

import com.google.auto.service.AutoService;

import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.modules.cases.CaseType;
import eu.itesla_project.offline.forecast_errors.ForecastErrorsAnalysisParameters;
import eu.itesla_project.online.LocalOnlineApplicationMBean;
import eu.itesla_project.online.OnlineWorkflowStartParameters;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class RunForecastErrorsAnalysisMpiTool implements Tool {

	private ForecastErrorsAnalysisParameters defaultParameters;

	private static Command COMMAND = new Command() {

		@Override
		public String getName() {
			return "run-forecast-errors-analysis-mpi";
		}

		@Override
		public String getTheme() {
			return Themes.MCLA;
		}

		@Override
		public String getDescription() {
			return "run forecast errors analysis with MPI";
		}

		@Override
		public Options getOptions() {
			Options options = new Options();
			options.addOption(Option.builder().longOpt("analysis")
					.desc("the analysis id")
					.hasArg()
					.required()
					.argName("ID")
					.build());
			options.addOption(Option.builder().longOpt("time-horizon")
					.desc("time horizon (example DACF)")
					.hasArg()
					.argName("TH")
					.build());
			options.addOption(Option.builder().longOpt("base-case-date")
					.desc("base case date (example 2013-01-15T18:45:00+01:00)")
					.hasArg()
					.argName("DATE")
					.build());
			options.addOption(Option.builder().longOpt("history-interval")
					.desc("historical time interval (example 2013-01-01T00:00:00+01:00/2013-01-31T23:59:00+01:00)")
					.hasArg()
					.argName("DATE1/DATE2")
					.build());
			options.addOption(Option.builder().longOpt("ir")
					.desc("fraction of explained variance for PCA (example 0.9)")
					.hasArg()
					.argName("IR")
					.build());
			options.addOption(Option.builder().longOpt("flagPQ")
					.desc("1 = P and Q sampled separately, 0 = P sampled and Q computed from P (constant pf)")
					.hasArg()
					.argName("FLAGPQ")
					.build());
			options.addOption(Option.builder().longOpt("method")
					.desc("method for missing data imputation: 1 = new method proposed by RSE, 2 = gaussian conditional sampling, 3 = gaussian mixture imputation, 4 = interpolation based method")
					.hasArg()
					.argName("METHOD")
					.build());
			options.addOption(Option.builder().longOpt("nClusters")
					.desc("number of clusters for PCA (example 3)")
					.hasArg()
					.argName("NCLUSTERS")
					.build());
			options.addOption(Option.builder().longOpt("percentileHistorical")
					.desc("quantile of the distribution of historical data related to Q vars, to set realistic limits of Q samples in case of using a constant power factor to produce Q samples starting from P samples")
					.hasArg()
					.argName("PERCENTILEHISTORICAL")
					.build());
			options.addOption(Option.builder().longOpt("modalityGaussian")
					.desc("1 = fictitious gaussians simulate forecast errors, 0 = historical data with copula estimation are considered")
					.hasArg()
					.argName("MODALITY_GAUSSIAN")
					.build());
			options.addOption(Option.builder().longOpt("outliers")
					.desc("0 = outliers are included as valid samples, 1 = outliers are excluded")
					.hasArg()
					.argName("OUTLIERS")
					.build());
			options.addOption(Option.builder().longOpt("conditionalSampling")
					.desc("1 = activated conditional sampling, 0 = not active")
					.hasArg()
					.argName("CONDITIONAL_SAMPLING")
					.build());
			options.addOption(Option.builder().longOpt("nSamples")
					.desc("number of samples to create, offline")
					.hasArg()
					.required()
					.argName("NSAMPLES")
					.build());
			options.addOption(Option.builder().longOpt("countries")
					.desc("the countries of the base case, separated by comma")
					.hasArg()
					.argName("COUNTRY,COUNTRY,...")
					.build());
			options.addOption(Option.builder().longOpt("case-type")
					.desc("the type (FO/SN) of the base case")
					.hasArg()
					.argName("case-type")
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

	private ForecastErrorsAnalysisParameters getDefaultParameters() {
		if (defaultParameters == null) {
			defaultParameters = ForecastErrorsAnalysisParameters.load();
		}
		return defaultParameters;
	}

	@Override
	public void run(CommandLine line) throws Exception {
		
		OnlineWorkflowStartParameters startconfig=OnlineWorkflowStartParameters.loadDefault();
		
		String host=line.getOptionValue(OnlineWorkflowCommand.HOST);
		String port=line.getOptionValue(OnlineWorkflowCommand.PORT);
		String threads=line.getOptionValue(OnlineWorkflowCommand.THREADS);
		if(host!=null)
			startconfig.setJmxHost(host);
		if(port!=null)
			startconfig.setJmxPort(port);
		if(threads!=null)
			startconfig.setThreads(Integer.valueOf(threads));

		String analysisId = line.getOptionValue("analysis");
		DateTime baseCaseDate = line.hasOption("base-case-date")
				? DateTime.parse(line.getOptionValue("base-case-date"))
				: getDefaultParameters().getBaseCaseDate();
		Interval histoInterval = line.hasOption("history-interval")
				? Interval.parse(line.getOptionValue("history-interval"))
				: getDefaultParameters().getHistoInterval();
		double ir = line.hasOption("ir")
				? Double.parseDouble(line.getOptionValue("ir"))
				: getDefaultParameters().getIr();
		int flagPQ = line.hasOption("flagPQ")
				? Integer.parseInt(line.getOptionValue("flagPQ"))
				: getDefaultParameters().getFlagPQ();
		int method = line.hasOption("method")
				? Integer.parseInt(line.getOptionValue("method"))
				: getDefaultParameters().getMethod();
		Integer nClusters = line.hasOption("nClusters")
				? Integer.parseInt(line.getOptionValue("nClusters"))
				: getDefaultParameters().getnClusters();
		double percentileHistorical = line.hasOption("percentileHistorical")
				? Double.parseDouble(line.getOptionValue("percentileHistorical"))
				: getDefaultParameters().getPercentileHistorical();
		Integer modalityGaussian = line.hasOption("modalityGaussian")
				? Integer.parseInt(line.getOptionValue("modalityGaussian"))
				: getDefaultParameters().getModalityGaussian();
		Integer outliers = line.hasOption("outliers")
				? Integer.parseInt(line.getOptionValue("outliers"))
				: getDefaultParameters().getOutliers();
		Integer conditionalSampling = line.hasOption("conditionalSampling")
				? Integer.parseInt(line.getOptionValue("conditionalSampling"))
				: getDefaultParameters().getConditionalSampling();
		Integer nSamples = line.hasOption("nSamples")
				? Integer.parseInt(line.getOptionValue("nSamples"))
				: getDefaultParameters().getnSamples();
		Set<Country> countries = line.hasOption("countries") 
				? Arrays.stream(line.getOptionValue("countries").split(",")).map(Country::valueOf).collect(Collectors.toSet())
				: getDefaultParameters().getCountries();
		CaseType caseType = line.hasOption("case-type") 
				? CaseType.valueOf(line.getOptionValue("case-type"))
				: getDefaultParameters().getCaseType();
				
		ForecastErrorsAnalysisParameters parameters = new ForecastErrorsAnalysisParameters(baseCaseDate, histoInterval, analysisId, ir, flagPQ, method, nClusters, 
																						   percentileHistorical, modalityGaussian, outliers, conditionalSampling,
																						   nSamples, countries, caseType);


		String urlString = "service:jmx:rmi:///jndi/rmi://"+startconfig.getJmxHost()+":"+startconfig.getJmxPort()+"/jmxrmi";

		JMXServiceURL serviceURL = new JMXServiceURL(urlString);
		Map<String, String> jmxEnv = new HashMap<>();
		JMXConnector connector = JMXConnectorFactory.connect(serviceURL, jmxEnv) ;
		MBeanServerConnection mbsc = connector.getMBeanServerConnection();
		
		ObjectName name = new ObjectName(LocalOnlineApplicationMBean.BEAN_NAME);
		LocalOnlineApplicationMBean application = MBeanServerInvocationHandler.newProxyInstance(mbsc, name, LocalOnlineApplicationMBean.class, false);
		String timeHorizonS="";
		if ( line.hasOption("time-horizon") ) {
			timeHorizonS=line.getOptionValue("time-horizon");
		}
		application.runFeaAnalysis(startconfig, parameters, timeHorizonS);

	}

}
