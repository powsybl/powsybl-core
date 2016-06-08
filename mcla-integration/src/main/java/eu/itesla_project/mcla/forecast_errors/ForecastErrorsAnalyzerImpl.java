/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla.forecast_errors;

import eu.itesla_project.computation.*;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.mcla.NetworkUtils;
import eu.itesla_project.mcla.forecast_errors.data.ForecastErrorsHistoricalData;
import eu.itesla_project.modules.mcla.ForecastErrorsAnalyzer;
import eu.itesla_project.modules.mcla.ForecastErrorsAnalyzerParameters;
import eu.itesla_project.modules.mcla.ForecastErrorsDataStorage;
import eu.itesla_project.modules.online.TimeHorizon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/*
   MCLA code 1.8
*/
/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ForecastErrorsAnalyzerImpl implements ForecastErrorsAnalyzer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ForecastErrorsAnalyzerImpl.class);

	private static final String WORKING_DIR_PREFIX = "itesla_forecasterrorsanalysis_";
	private static final String FEACSVFILENAME = "forecasterrors_historicaldata.csv";
	private static final String FEAINPUTFILENAME = "feanalyzerinput.mat";
	private static final String FEAOUTPUTFILENAME = "feanalyzeroutput.mat";
	private static final String FEASAMPLERFILENAME = "feasampleroutput.mat";

	private static final String M2PATHPREFIX="feam2output_";
	private static final String MATPATHSUFFIX=".mat";
	private static final String M1OUTPUTFILENAME="feam1output.mat";
	private static final String M0OUTPUTFILENAME="feam0output.mat";
	private static final String M0OUTPUTFILENAMECSV=M0OUTPUTFILENAME+".csv";
	private static final String GUIOUTPUTFILENAME="feaguioutput.mat";
	private static final String M2OUTPUTFILENAME= M2PATHPREFIX + Command.EXECUTION_NUMBER_PATTERN + MATPATHSUFFIX;

	private static final String WP5_M1 = "wp5fea_m1";
	private static final String WP5_M2 = "wp5fea_m2";
	private static final String WP5_M2_REDUCE = "wp5fea_m2_reduce";
	private static final String WP5_M3_SAMPLING = "wp5fea_m3_sampling";


	private final ComputationManager computationManager;
	private final ForecastErrorsDataStorage forecastErrorsDataStorage;

	private Network network;
	private ForecastErrorsAnalyzerConfig config = null;
	private ForecastErrorsAnalyzerParameters parameters;
	private ArrayList<String> generatorsIds = new ArrayList<String>();
	private ArrayList<String> loadsIds = new ArrayList<String>();

	public ForecastErrorsAnalyzerImpl(Network network, ComputationManager computationManager, ForecastErrorsDataStorage forecastErrorsDataStorage,
										ForecastErrorsAnalyzerConfig config) {
		Objects.requireNonNull(network, "network is null");
		Objects.requireNonNull(computationManager, "computation manager is null");
		Objects.requireNonNull(forecastErrorsDataStorage, "forecast errors data storage is null");
		Objects.requireNonNull(config, "config is null");
		LOGGER.info(config.toString());
		this.network = network;
		this.computationManager = computationManager;
		this.forecastErrorsDataStorage = forecastErrorsDataStorage;
		this.config = config;
	}

	public ForecastErrorsAnalyzerImpl(Network network, ComputationManager computationManager, ForecastErrorsDataStorage forecastErrorsDataStorage) {
		this(network, computationManager, forecastErrorsDataStorage, ForecastErrorsAnalyzerConfig.load());
	}

	@Override
	public String getName() {
		return "RSE Forecast Error Analyser";
	}

	@Override
	public String getVersion() {
		return null;
	}

	@Override
	public void init(ForecastErrorsAnalyzerParameters parameters) {
		Objects.requireNonNull(parameters, "forecast errors analizer parameters value is null");
		this.parameters = parameters;
		generatorsIds = NetworkUtils.getRenewableGeneratorsIds(network);
		loadsIds = NetworkUtils.getLoadsIds(network);
	}

	@Override
	public void run(TimeHorizon timeHorizon) throws Exception {

		if (forecastErrorsDataStorage.isForecastErrorsDataAvailable(parameters.getFeAnalysisId(), timeHorizon)) {
			throw new RuntimeException("Forecast errors data for " + parameters.getFeAnalysisId() + " analysis and " + timeHorizon.getName() + " time horizon already exists");
		}

		try (CommandExecutor executor = computationManager.newCommandExecutor(
				createEnv(), WORKING_DIR_PREFIX, config.isDebug())) {

			final Path workingDir = executor.getWorkingDir();

			// get forecast errors historical data from histodb
			FEAHistoDBFacade histoDBFacade = new FEAHistoDBFacade(
					new DataMiningFacadeRestConfig(
							config.getHistoDBServiceUrl(),
							config.getHistoDBUser(),
							config.getHistoDBPassword(),
							workingDir,
							config.isDebug()),
					timeHorizon,
					parameters.getHistoInterval(),
					generatorsIds,
					loadsIds);
			Path historicalDataCsvFile = Paths.get(workingDir.toString(), FEACSVFILENAME);
			histoDBFacade.historicalDataToCsvFile(historicalDataCsvFile);

			//Path historicalDataCsvFile = Paths.get("/itesla_data/MAT", "forecastsDiff_7nodes.csv");
			ForecastErrorsHistoricalData forecastErrorsHistoricalData = new HistoricalDataCreator(network, generatorsIds, loadsIds)
					.createForecastErrorsHistoricalData(historicalDataCsvFile);
			Path historicalDataMatFile = Paths.get(workingDir.toString(), FEAINPUTFILENAME);
			new FEAMatFileWriter(historicalDataMatFile).writeHistoricalData(forecastErrorsHistoricalData);

			LOGGER.info("Running forecast errors analysis for {} network, {} time horizon", network.getId(), timeHorizon.getName());
			ExecutionReport report = executor.start(new CommandExecution(createMatm1Cmd(historicalDataMatFile), 1, Integer.MAX_VALUE));
			report.log();
			if (report.getErrors().isEmpty()) {
				report = executor.start(new CommandExecution(createMatm2Cmd(), parameters.getnClusters(), Integer.MAX_VALUE));
				report.log();
				if (report.getErrors().isEmpty()) {
					report = executor.start(new CommandExecution(createMatm2reduceCmd(), 1, Integer.MAX_VALUE));
					report.log();
					if (report.getErrors().isEmpty()) {
						report = executor.start(new CommandExecution(createMatm3Cmd(), 1, Integer.MAX_VALUE));
						report.log();
					}
				}
			}

/*
			//fake start
			//M1OUTPUTFILENAME M0OUTPUTFILENAME M0OUTPUTFILENAMECSV GUIOUTPUTFILENAME
			Arrays.asList(M1OUTPUTFILENAME,M0OUTPUTFILENAME,M0OUTPUTFILENAMECSV,GUIOUTPUTFILENAME,FEAOUTPUTFILENAME).stream()
				.forEach( sf -> {
					try {
						Files.copy(Paths.get("/adisk05/ITESLA_DATA/itesla_forecasterrorsanalysis").resolve(sf), workingDir.resolve(sf));
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			ExecutionReport report = executor.start(new CommandExecution(createMatm3Cmd(), 1, Integer.MAX_VALUE));
			report.log();
			//fake end
*/
			// store forecast errors data, offline sampling, statistics and uncertainties data for the GUI
			forecastErrorsDataStorage.storeForecastErrorsFiles(parameters.getFeAnalysisId(), timeHorizon, workingDir.resolve(FEAOUTPUTFILENAME), workingDir.resolve(FEASAMPLERFILENAME), workingDir.resolve(M0OUTPUTFILENAMECSV), workingDir.resolve(GUIOUTPUTFILENAME));
			// store analysis parameters
			forecastErrorsDataStorage.storeParameters(parameters.getFeAnalysisId(), timeHorizon, parameters);
		}
	}

	//function exitcode=FEA_MODULE1_HELPER(ifile, ofile,natS,ofile_forFPF,ofileGUI, IRs, Ks, s_flagPQ,s_method,tolvar,Nmin_obs_fract,Nmin_obs_interv,outliers,koutlier,imputation_meth,Ngaussians,percentile_historical,check_module0,toleranceS,iterationsS,epsiloS,conditional_samplingS,histo_estremeQs,thresGUIs,s_rng_seed)

	private Command createMatm1Cmd(Path historicalDataMatFile) {
		List<String> args1 = new ArrayList<>();
		args1.add(historicalDataMatFile.toAbsolutePath().toString());
		args1.add(M1OUTPUTFILENAME);
		args1.add(config.getNats());  //added in v1.8
		args1.add(M0OUTPUTFILENAME);
		args1.add(GUIOUTPUTFILENAME);
		args1.add("" + parameters.getIr());
		args1.add("" + parameters.getnClusters());
		args1.add("" + parameters.getFlagPQ());
		args1.add("" + parameters.getMethod());
		args1.add("" + config.getTolVar());
		args1.add("" + config.getnMinObsFract());
		args1.add("" + config.getnMinObsInterv());
		args1.add("" + parameters.getOutliers());
		args1.add("" + config.getkOutlier());
		args1.add("" + config.getImputationMeth());
		args1.add("" + config.getnGaussians());
		args1.add("" + parameters.getPercentileHistorical());
		// args1.add("" + parameters.getModalityGaussian()); removed in v1.8
		args1.add("" + config.getCheckModule0());
		args1.add("" + config.getTolerance());
		args1.add("" + config.getIterations());
		args1.add("" + config.getEpsilo());
		args1.add("" + parameters.getConditionalSampling());
		args1.add("" + config.getHisto_estremeQ()); //added in v1.8
		args1.add("" + config.getThresGUI()); //added in v1.8

		if (config.getRngSeed() != null) {
			args1.add(Integer.toString(config.getRngSeed()));
		}

		String wp5_m1;
		if (config.getBinariesDir() != null) {
			wp5_m1 = config.getBinariesDir().resolve(WP5_M1).toAbsolutePath().toString();
		} else {
			wp5_m1 = WP5_M1;
		}

		return new SimpleCommandBuilder()
				.id("wp5_m1")
				.program(wp5_m1)
				.args(args1)
				.inputFiles(new InputFile(historicalDataMatFile.getFileName().toString()))
				.outputFiles(new OutputFile(M1OUTPUTFILENAME),new OutputFile(M0OUTPUTFILENAME), new OutputFile(M0OUTPUTFILENAMECSV), new OutputFile(GUIOUTPUTFILENAME))
				.build();
	}

	private Command createMatm2Cmd() {
		String wp5_m2;
		if (config.getBinariesDir() != null) {
			wp5_m2 = config.getBinariesDir().resolve(WP5_M2).toAbsolutePath().toString();
		} else {
			wp5_m2 = WP5_M2;
		}
		return new SimpleCommandBuilder()
				.id("wp5_m2")
				.program(wp5_m2)
				.args(M1OUTPUTFILENAME,
						M2OUTPUTFILENAME,
						Command.EXECUTION_NUMBER_PATTERN,
						// ""+parameters.getModalityGaussian(), removed in v1.8
						""+parameters.getIr(),
						""+config.gettFlags())
				.inputFiles(new InputFile(M1OUTPUTFILENAME))
				.outputFiles(new OutputFile(M2OUTPUTFILENAME))
				.build();
	}

	private Command createMatm2reduceCmd() {
		String wp5_m2_reduce;
		if (config.getBinariesDir() != null) {
			wp5_m2_reduce = config.getBinariesDir().resolve(WP5_M2_REDUCE).toAbsolutePath().toString();
		} else {
			wp5_m2_reduce = WP5_M2_REDUCE;
		}
		List<InputFile> m2partsfiles=new ArrayList<InputFile>(parameters.getnClusters()+1);
		m2partsfiles.add(new InputFile(M1OUTPUTFILENAME));
		for (int i = 0; i < parameters.getnClusters(); i++) {
			m2partsfiles.add(new InputFile(M2PATHPREFIX+i+MATPATHSUFFIX));
		}
		return new SimpleCommandBuilder()
				.id("wp5_m2_reduce")
				.program(wp5_m2_reduce)
				.args(M1OUTPUTFILENAME,".",
						M2PATHPREFIX,
						""+parameters.getnClusters(),
						FEAOUTPUTFILENAME,
						//  ""+parameters.getModalityGaussian(),  removed in v1.8
						""+config.getPercpuGaussLoad(),
						""+config.getPercpuGaussRes(),
						""+config.getCorrelationGauss())
				.inputFiles(m2partsfiles)
				.outputFiles(new OutputFile(FEAOUTPUTFILENAME))
				.build();
	}

	private Command createMatm3Cmd() {
		String wp5_m3;
		if (config.getBinariesDir() != null) {
			wp5_m3 = config.getBinariesDir().resolve(WP5_M3_SAMPLING).toAbsolutePath().toString();
		} else {
			wp5_m3 = WP5_M3_SAMPLING;
		}
		List<String> args1 = new ArrayList<>();
		args1.add(FEAOUTPUTFILENAME);
		args1.add(FEASAMPLERFILENAME);
		args1.add(""+parameters.getnSamples());
		if (config.getRngSeed() != null) {
			args1.add(Integer.toString(config.getRngSeed()));
		}

		return new SimpleCommandBuilder()
				.id(WP5_M3_SAMPLING)
				.program(wp5_m3)
				.args(args1)
				.inputFiles(new InputFile(FEAOUTPUTFILENAME))
				.outputFiles(new OutputFile(FEASAMPLERFILENAME))
				.build();

	}



	private Map<String, String> createEnv() {
		Map<String, String> env = new HashMap<>();
		env.put("MCRROOT", config.getRuntimeHomeDir().toString());
		env.put("LD_LIBRARY_PATH", config.getRuntimeHomeDir().resolve("runtime").resolve("glnxa64").toString()
				+ ":" + config.getRuntimeHomeDir().resolve("bin").resolve("glnxa64").toString()
				+ ":" + config.getRuntimeHomeDir().resolve("sys").resolve("os").resolve("glnxa64").toString());
		return env;
	}

}
