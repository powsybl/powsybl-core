/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla.montecarlo;

import eu.itesla_project.computation.*;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.mcla.NetworkUtils;
import eu.itesla_project.mcla.montecarlo.data.SamplingNetworkData;
import eu.itesla_project.modules.mcla.ForecastErrorsDataStorage;
import eu.itesla_project.modules.mcla.MontecarloSamplerParameters;
import eu.itesla_project.modules.online.TimeHorizon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class StatsCondCalculator {

	private static final Logger LOGGER = LoggerFactory.getLogger(StatsCondCalculator.class);

	private static final String WORKING_DIR_PREFIX = "itesla_mcla_fpf_stats_";
	private static final String MCSINPUTFILEPREFIX = "mcsamplerinput_";
	private static final String MCSOUTPUTFILENAME = "mcsampleroutput.mat";
	private static final String FPFFORECASTERRORSFILENAME = "fea_stats_cond_for_FPF.mat";
	private static final String WP5_FPF = "wp5fpf";

	private final ComputationManager computationManager;
	private final ForecastErrorsDataStorage forecastErrorsDataStorage;

	private Network network;
	private StatsCondCalculatorConfig config = null;

	private TimeHorizon timeHorizon;
	private String feAnalysisId;
	private ArrayList<String> generatorsIds = new ArrayList<String>();
	private ArrayList<String> loadsIds = new ArrayList<String>();
	private ArrayList<String> connectedGeneratorsIds = new ArrayList<String>();
	private ArrayList<String> connectedLoadsIds = new ArrayList<String>();
	private SamplingNetworkData samplingNetworkData;
	private Path networkDataMatFile = null;

	public StatsCondCalculator(Network network, ComputationManager computationManager, ForecastErrorsDataStorage forecastErrorsDataStorage, StatsCondCalculatorConfig config) {
        Objects.requireNonNull(network, "network is null");
        Objects.requireNonNull(computationManager, "computationManager is null");
        Objects.requireNonNull(forecastErrorsDataStorage, "forecast errors data storage is null");
        Objects.requireNonNull(config, "config is null");
		LOGGER.info(config.toString());
		this.network = network;
		this.computationManager = computationManager;
		this.forecastErrorsDataStorage = forecastErrorsDataStorage;
		this.config = config;
	}

	public StatsCondCalculator(Network network, ComputationManager client, ForecastErrorsDataStorage forecastErrorsDataStorage) {
		this(network, client, forecastErrorsDataStorage, StatsCondCalculatorConfig.load());
	}

	public String getName() {
		return "RSE FEA Conditional stats calculator";
	}

	public String getVersion() {
		return null;
	}

	public void init(MontecarloSamplerParameters parameters) throws Exception {
		Objects.requireNonNull(parameters, "montecarlo sampler parameters value is null");
		this.timeHorizon = parameters.getTimeHorizon();
		this.feAnalysisId = parameters.getFeAnalysisId();
		// check if forecast error data file for this time horizon exists
		if ( !forecastErrorsDataStorage.isForecastErrorsDataAvailable(feAnalysisId, timeHorizon) ) {
			LOGGER.error("No forecast errors data for {} network, {} time horizon.", network.getId(), timeHorizon.getName());
			throw new Exception("Montecarlo sampler not ready to be used: No forecast errors data for " + network.getId() + " network, " + timeHorizon.getName() + " time horizon.");
		}
		// I put the generators and loads in a specific order, the same used when producing the matrix of historical data,
		// for the forecast errors analysis
		generatorsIds = NetworkUtils.getGeneratorsIds(network);
		connectedGeneratorsIds = NetworkUtils.getConnectedGeneratorsIds(network);
		loadsIds = NetworkUtils.getLoadsIds(network);
		connectedLoadsIds = NetworkUtils.getConnectedLoadsIds(network);
		// create the sampling network data
		LOGGER.info("Preparing sampling network data for {} network", network.getId());
		samplingNetworkData = new SamplingDataCreator(network, generatorsIds, loadsIds).createSamplingNetworkData();
		// write mat network data file 
		// TODO use common file transfer feature
		networkDataMatFile = Files.createTempFile(config.getTmpDir(), MCSINPUTFILEPREFIX + network.getId() + "_" + timeHorizon.getLabel() + "_", ".mat"); 
		LOGGER.info("Writing sampling network data for {} network into mat file {}", network.getId(), networkDataMatFile);
		new MCSMatFileWriter(networkDataMatFile).writeSamplingNetworkData(samplingNetworkData);
	}


	public void run(Path destPath) throws Exception {
		try (CommandExecutor executor = computationManager.newCommandExecutor(
				createEnv(), WORKING_DIR_PREFIX, config.isDebug())) {

			final Path workingDir = executor.getWorkingDir();
			// put mat files in working dir
			Path localForecastErrorsDataFile = Paths.get(workingDir.toString(), MCSINPUTFILEPREFIX + "forecast_errors_" + timeHorizon.getLabel() + ".mat");
			forecastErrorsDataStorage.getForecastErrorsFile(feAnalysisId, timeHorizon, localForecastErrorsDataFile);
			Path localNetworkDataMatFile = Paths.get(workingDir.toString(), MCSINPUTFILEPREFIX + network.getId() + ".mat");
			Files.copy(networkDataMatFile, localNetworkDataMatFile);

			LOGGER.info("Preparing FPF input data for {} network", network.getId());
			Command cmd = createFPFCommand(localForecastErrorsDataFile, localNetworkDataMatFile);
			ExecutionReport report = executor.start(new CommandExecution(cmd, 1, Integer.MAX_VALUE));
			report.log();

			LOGGER.debug("Retrieving FPF file {}", FPFFORECASTERRORSFILENAME + ".csv");
			Files.copy(Paths.get(workingDir.toString(), FPFFORECASTERRORSFILENAME+".csv"), destPath, StandardCopyOption.REPLACE_EXISTING);
		}
	}


	private Map<String, String> createEnv() {
        Map<String, String> env = new HashMap<>();
		env.put("MCRROOT", config.getRuntimeHomeDir().toString());
        env.put("LD_LIBRARY_PATH", config.getRuntimeHomeDir().resolve("runtime").resolve("glnxa64").toString()
                + ":" + config.getRuntimeHomeDir().resolve("bin").resolve("glnxa64").toString()
                + ":" + config.getRuntimeHomeDir().resolve("sys").resolve("os").resolve("glnxa64").toString());
        return env;
    }


	private Command createFPFCommand(Path localForecastErrorsDataFile, Path localNetworkDataMatFile) {
		List<String> args1 = new ArrayList<>();
		args1.add(localForecastErrorsDataFile.toAbsolutePath().toString());
		args1.add(localNetworkDataMatFile.toAbsolutePath().toString());
		args1.add(FPFFORECASTERRORSFILENAME);
		if (config.getRngSeed() != null) {
			args1.add(Integer.toString(config.getRngSeed()));
		}

		return new SimpleCommandBuilder()
				.id("matfpf")
				.program(config.getBinariesDir().resolve(WP5_FPF).toAbsolutePath().toString())
				.args(args1)
				.inputFiles(new InputFile(localForecastErrorsDataFile.getFileName().toString()),
						new InputFile(localNetworkDataMatFile.getFileName().toString()))
				.outputFiles(new OutputFile(MCSOUTPUTFILENAME), new OutputFile(MCSOUTPUTFILENAME+".csv"))
				.build();
	}



}
