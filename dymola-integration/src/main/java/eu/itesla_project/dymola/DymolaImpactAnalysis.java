/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola;

import com.google.common.collect.ImmutableMap;
import eu.itesla_project.commons.Version;
import eu.itesla_project.commons.io.PlatformConfig;
import eu.itesla_project.computation.*;
import eu.itesla_project.contingency.*;
import eu.itesla_project.dymola.contingency.*;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.util.Networks;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.modelica_events_adder.events.ModEventsExport;
import eu.itesla_project.modelica_export.ModelicaMainExporter;
import eu.itesla_project.simulation.securityindexes.SecurityIndex;
import eu.itesla_project.simulation.securityindexes.SecurityIndexParser;
import eu.itesla_project.simulation.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.nio.file.ShrinkWrapFileSystem;
import org.jboss.shrinkwrap.impl.nio.file.ShrinkWrapFileSystemProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static eu.itesla_project.computation.FilePreProcessor.ARCHIVE_UNZIP;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DymolaImpactAnalysis implements ImpactAnalysis {
    private static final Logger LOGGER = LoggerFactory.getLogger(DymolaImpactAnalysis.class);

    private static final String WP43_SMALLSIGNAL_SECURITY_INDEX_FILE_NAME =  "_wp43_smallsignal_security_indexes.xml";
    private static final String WP43_TRANSIENT_SECURITY_INDEX_FILE_NAME = "_wp43_transient_security_indexes.xml";
    private static final String WP43_OVERLOAD_SECURITY_INDEX_FILE_NAME = "_wp43_overload_security_indexes.xml";
    private static final String WP43_UNDEROVERVOLTAGE_SECURITY_INDEX_FILE_NAME = "_wp43_underovervoltage_security_indexes.xml";

    private static final String WP43_SMALLSIGNAL_SECURITY_INPUT_FILE_NAME =  "_wp43_smallsignal.mat";
    private static final String WP43_TRANSIENT_SECURITY_INPUT_FILE_NAME = "_wp43_transient.mat";
    private static final String WP43_OVERLOAD_SECURITY_INPUT_FILE_NAME = "_wp43_overload.mat";
    private static final String WP43_UNDEROVERVOLTAGE_SECURITY_INPUT_FILE_NAME = "_wp43_underovervoltage.mat";


    private static final String WORKING_DIR_PREFIX = "itesla_dymola_impact_analysis_";

    public static final String WP43_SCRIPT = "wp43_dymola.sh";
    private static final String MO_EXPORT_DIRECTORY = "modexport";
    private static final String REMOTE_DYMOLA_SCRIPT = "remoteDymola.sh";
    private static final String WP43_CONFIG_FILE_NAME = "wp43adapter.properties";
    private static final String MODELICA_EVENTS_CSV_FILENAME = "modelica_events.csv";

    private final Network network;

    private final ComputationManager computationManager;

    private final ContingenciesProvider contingenciesProvider;

    private final int priority;

    private final DymolaConfig config;

    private SimulationParameters parameters;

    private final List<Contingency> allContingencies = new ArrayList<>();


    public DymolaImpactAnalysis(Network network, ComputationManager computationManager, int priority,
                                ContingenciesProvider contingenciesProvider) {
        this(network, computationManager, priority, contingenciesProvider, DymolaConfig.load());
    }

    public DymolaImpactAnalysis(Network network, ComputationManager computationManager, int priority,
                                ContingenciesProvider contingenciesProvider, DymolaConfig config) {
        Objects.requireNonNull(network, "network is null");
        Objects.requireNonNull(computationManager, "computation manager is null");
        Objects.requireNonNull(contingenciesProvider, "contingencies provider is null");
        Objects.requireNonNull(config, "config is null");
        this.network = network;
        this.computationManager = computationManager;
        this.priority = priority;
        this.contingenciesProvider = contingenciesProvider;
        this.config = config;
    }

    //------------------------------------------------------------------------------
    //------------------------------------------------------------------------------
    // align to latest sources 2015 07 13
    //------------------------------------------------------------------------------
    //------------------------------------------------------------------------------

    //OK
    @Override
    public String getName() {
        return DymolaUtil.PRODUCT_NAME;
    }

    //OK
    @Override
    public String getVersion() {
        return ImmutableMap.builder().put("dymolaVersion", DymolaUtil.VERSION)
                .putAll(Version.VERSION.toMap())
                .build()
                .toString();
    }

    //OK
    private double getFaultDuration(ContingencyElement element) {
        if (element instanceof MoContingency) {
            switch (((MoContingency) element).getMoType()) {
//            case GENERATOR:
//                return parameters.getGeneratorFaultShortCircuitDuration();
//            case LINE:
//                return parameters.getBranchFaultShortCircuitDuration();
                case MO_BUS_FAULT:
                    return ((MoBusFaultContingency) element).getT2() - ((MoBusFaultContingency) element).getT1();
                case MO_LINE_FAULT:
                    return ((MoLineFaultContingency) element).getT2() - ((MoLineFaultContingency) element).getT1();
                case MO_LINE_OPEN_REC:
                    return ((MoLineOpenRecContingency) element).getT2() - ((MoLineOpenRecContingency) element).getT1();
                case MO_LINE_2_OPEN:
                    return (parameters.getPostFaultSimulationStopInstant() - ((MoLine2OpenContingency) element).getT1());
                case MO_BANK_MODIF:
                    return (parameters.getPostFaultSimulationStopInstant() - ((MoBankModifContingency) element).getT1());
                case MO_LOAD_MODIF:
                    return (parameters.getPostFaultSimulationStopInstant() - ((MoLoadModifContingency) element).getT1());
                case MO_BREAKER:
                    return (parameters.getPostFaultSimulationStopInstant() - ((MoBreakerContingency) element).getT1());
                case MO_SETPOINT_MODIF:
                    return (parameters.getPostFaultSimulationStopInstant() - ((MoSetPointModifContingency) element).getT1());
                default:
                    throw new AssertionError();
            }
        } else {
            throw new AssertionError();
        }
    }

    //OK
    private void readSecurityIndexes(List<Contingency> contingencies, Path workingDir, ImpactAnalysisResult result) throws IOException {
        long start = System.currentTimeMillis();
        int files = 0;
        //TODO TSO INDEXES HANDLING
        for (int i = 0; i < contingencies.size(); i++) {
            Contingency contingency = contingencies.get(i);
            String prefixFile=DymolaUtil.DYMOLA_SIM_MAT_OUTPUT_PREFIX+"_"+i;
            for (String securityIndexFileName : Arrays.asList(
                    prefixFile+WP43_SMALLSIGNAL_SECURITY_INDEX_FILE_NAME,
                    prefixFile+WP43_TRANSIENT_SECURITY_INDEX_FILE_NAME,
                    prefixFile+WP43_OVERLOAD_SECURITY_INDEX_FILE_NAME,
                    prefixFile+WP43_UNDEROVERVOLTAGE_SECURITY_INDEX_FILE_NAME)) {
                Path file = workingDir.resolve(securityIndexFileName.replace(Command.EXECUTION_NUMBER_PATTERN, Integer.toString(i)));
                LOGGER.info("reading indexes output from file  {} ", file);
                if (Files.exists(file)) {
                    try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                        for (SecurityIndex index : SecurityIndexParser.fromXml(contingency.getId(), reader)) {
                            result.addSecurityIndex(index);
                        }
                    }
                    files++;
                }
            }
            //TODO
            // also scan errors in output
            //EurostagUtil.searchErrorMessage(workingDir.resolve(FAULT_OUT_GZ_FILE_NAME.replace(Command.EXECUTION_NUMBER_PATTERN, Integer.toString(i))), result.getMetrics(), i);
        }
        LOGGER.trace("{} security indexes files read in {} ms", files, (System.currentTimeMillis() - start));
    }

    //OK
    @Override
    public void init(SimulationParameters parameters, Map<String, Object> context) throws Exception {
        Objects.requireNonNull(parameters, "parameters is null");
        Objects.requireNonNull(context, "context is null");

        this.parameters = parameters;
        allContingencies.addAll(contingenciesProvider.getContingencies(network));
    }


    //OK
    private static void checkState(SimulationState state) {
        Objects.requireNonNull(state, "state is null");
        if (!(state instanceof DymolaState)) {
            throw new RuntimeException("Incompatiblity between stabilization and impact analysis implementations");
        }
    }


    @Override
    public ImpactAnalysisResult run(SimulationState state) throws Exception {
        return run(state, null);
    }

    private Command before(SimulationState state, Set<String> contingencyIds, Path workingDir, List<Contingency> contingencies) throws IOException {
        // dump state info for debugging
        if (config.isDebug()) {
            Networks.dumpStateId(workingDir, state.getName());
        }

        Command cmd;
        if (contingencyIds == null) {
            // take all contingencies
            contingencies.addAll(allContingencies);
        } else {
            // filter contingencies
            for (Contingency c : contingenciesProvider.getContingencies(network)) {
                if (contingencyIds.contains(c.getId())) {
                    contingencies.add(c);
                }
            }
        }

        LOGGER.info("Contingencies involved in this simulation: ");
        for(Contingency c: contingencies) {
            LOGGER.info(" {}",c.getId());
        }

        LOGGER.info("Current state  {}", state.getName());
        network.getStateManager().setWorkingState(state.getName());

        //prepare dymola inputs in modelica format
        LOGGER.info("Writing dymola inputs in modelica format - start");
        List<String> cIds = writeDymolaInputs(workingDir, contingencies);
        LOGGER.info("Writing dymola inputs in modelica format - end");

        //parallelized computation, via platform's computation manager
        LOGGER.info("dymola impact analysis - start");
        cmd = createCommand(DymolaUtil.DYMOLAINPUTZIPFILENAMEPREFIX, network.getName(),DymolaSimulationConfig.load());
        return cmd;
    }

    private ImpactAnalysisResult after(Path workingDir, List<Contingency> contingencies, ExecutionReport report) throws IOException {
        report.log();

        // read security indexes files generated by impact analysis
        Map<String, String> metrics = new HashMap<>();
        fillMetrics(contingencies, report, metrics);
        ImpactAnalysisResult result = new ImpactAnalysisResult(metrics);
        readSecurityIndexes(contingencies, workingDir, result);

        return result;
    }

    @Override
    public ImpactAnalysisResult run(SimulationState state, Set<String> contingencyIds) throws Exception {
        checkState(state);

        try (CommandExecutor executor = computationManager.newCommandExecutor(DymolaUtil.createEnv(config), WORKING_DIR_PREFIX, config.isDebug())) {
            Path workingDir = executor.getWorkingDir();

            List<Contingency> contingencies = new ArrayList<>();
            Command cmd = before(state, contingencyIds, workingDir, contingencies);

            // start execution
            ExecutionReport report = executor.start(new CommandExecution(cmd, contingencies.size(), priority, ImmutableMap.of("state", state.getName())));

            return after(workingDir, contingencies, report);
        }
    }

    @Override
    public CompletableFuture<ImpactAnalysisResult> runAsync(SimulationState state, Set<String> contingencyIds, ImpactAnalysisProgressListener listener) {
        checkState(state);

        return computationManager.execute(new ExecutionEnvironment(DymolaUtil.createEnv(config), WORKING_DIR_PREFIX, config.isDebug()),
                new DefaultExecutionHandler<ImpactAnalysisResult>() {

                    private final List<Contingency> contingencies = new ArrayList<>();

                    @Override
                    public List<CommandExecution> before(Path workingDir) throws IOException {
                        Command cmd = DymolaImpactAnalysis.this.before(state, contingencyIds, workingDir, contingencies);
                        return Arrays.asList(new CommandExecution(cmd, contingencies.size(), priority, ImmutableMap.of("state", state.getName())));
                    }

                    @Override
                    public void onProgress(CommandExecution execution, int executionIndex) {
                        if (listener != null) {
                            listener.onProgress(executionIndex);
                        }
                    }

                    @Override
                    public ImpactAnalysisResult after(Path workingDir, ExecutionReport report) throws IOException {
                        return DymolaImpactAnalysis.this.after(workingDir, contingencies, report);
                    }
                });
    }

    private void fillMetrics(List<Contingency> contingencies, ExecutionReport report, Map<String, String> metrics) {
        float successPercent = 100f * (1 - ((float) report.getErrors().size()) / contingencies.size());
        metrics.put("successPercent", Float.toString(successPercent));
        DymolaUtil.putBadExitCode(report, metrics);
    }

    //------------------------------------------------------------------------------
    // Dymola integration specifics
    //------------------------------------------------------------------------------

    //TODO TSO indexes not supported
    //this composite command invokes 2 shell scripts: the first one calls the remote dymola service, the second drives KTH indexes against the dymola .mat results
    private Command createCommand(String inputScenarioZipPrefixFileName, String matFilePrefix, DymolaSimulationConfig simConfig) {
        String modelFileName=DymolaUtil.DYMOLA_SIM_MODEL_INPUT_PREFIX+".mo";
        String modelNamePrefix="M_"+matFilePrefix+"_events_";

        double startSimulationTime=0.0;
        double endSimulationTime=parameters.getPostFaultSimulationStopInstant();


        return new GroupCommandBuilder()
                .id("dym_imp")
                .inputFiles(new InputFile(inputScenarioZipPrefixFileName + "_" + Command.EXECUTION_NUMBER_PATTERN + ".zip"),
                        new InputFile(inputScenarioZipPrefixFileName + "_" + Command.EXECUTION_NUMBER_PATTERN + "_pars.zip", ARCHIVE_UNZIP))
                .subCommand()
                .program(REMOTE_DYMOLA_SCRIPT)
                .args(modelFileName, modelNamePrefix+Command.EXECUTION_NUMBER_PATTERN, "./", Command.EXECUTION_NUMBER_PATTERN, config.getDymolaSeviceWSDL(),
                        "" + startSimulationTime, "" + endSimulationTime, "" + simConfig.getNumberOfIntervals(),
                        "" + simConfig.getOutputInterval(), "" + simConfig.getMethod(), "" + simConfig.getTolerance(),
                        "" + simConfig.getOutputFixedstepSize(),
                        "" + DymolaUtil.DYMOLAINPUTZIPFILENAMEPREFIX + "_" + Command.EXECUTION_NUMBER_PATTERN + ".zip",
                        "" + DymolaUtil.DYMOLAOUTPUTZIPFILENAMEPREFIX + "_" + Command.EXECUTION_NUMBER_PATTERN + ".zip",
                        "" + DymolaUtil.DYMOLA_SIM_MAT_OUTPUT_PREFIX + "_" + Command.EXECUTION_NUMBER_PATTERN,
                        "" + DymolaUtil.DYMOLAOUTPUTZIPFILENAMEPREFIX + "_" + Command.EXECUTION_NUMBER_PATTERN + ".txt",
                        "" + config.isFakeDymolaExecution())
                .timeout(config.getSimTimeout())
                .add()
                .subCommand()
                .program(WP43_SCRIPT)
                .args("./", DymolaUtil.DYMOLA_SIM_MAT_OUTPUT_PREFIX + "_" + Command.EXECUTION_NUMBER_PATTERN, String.join(",", config.getIndexesNames()))
                .timeout(config.getIdxTimeout())
                .add()
                .outputFiles(
                        new OutputFile(DymolaUtil.DYMOLAOUTPUTZIPFILENAMEPREFIX + "_" + Command.EXECUTION_NUMBER_PATTERN + ".zip"),
                        new OutputFile(DymolaUtil.DYMOLAOUTPUTZIPFILENAMEPREFIX + "_" + Command.EXECUTION_NUMBER_PATTERN + ".txt"),
                        new OutputFile(DymolaUtil.DYMOLA_SIM_MAT_OUTPUT_PREFIX + "_" + Command.EXECUTION_NUMBER_PATTERN + WP43_SMALLSIGNAL_SECURITY_INDEX_FILE_NAME),
                        new OutputFile(DymolaUtil.DYMOLA_SIM_MAT_OUTPUT_PREFIX + "_" + Command.EXECUTION_NUMBER_PATTERN + WP43_TRANSIENT_SECURITY_INDEX_FILE_NAME),
                        new OutputFile(DymolaUtil.DYMOLA_SIM_MAT_OUTPUT_PREFIX + "_" + Command.EXECUTION_NUMBER_PATTERN + WP43_OVERLOAD_SECURITY_INDEX_FILE_NAME),
                        new OutputFile(DymolaUtil.DYMOLA_SIM_MAT_OUTPUT_PREFIX + "_" + Command.EXECUTION_NUMBER_PATTERN + WP43_UNDEROVERVOLTAGE_SECURITY_INDEX_FILE_NAME),
                        new OutputFile(DymolaUtil.DYMOLA_SIM_MAT_OUTPUT_PREFIX + "_" + Command.EXECUTION_NUMBER_PATTERN + WP43_SMALLSIGNAL_SECURITY_INPUT_FILE_NAME),
                        new OutputFile(DymolaUtil.DYMOLA_SIM_MAT_OUTPUT_PREFIX + "_" + Command.EXECUTION_NUMBER_PATTERN + WP43_TRANSIENT_SECURITY_INPUT_FILE_NAME),
                        new OutputFile(DymolaUtil.DYMOLA_SIM_MAT_OUTPUT_PREFIX + "_" + Command.EXECUTION_NUMBER_PATTERN + WP43_OVERLOAD_SECURITY_INPUT_FILE_NAME),
                        new OutputFile(DymolaUtil.DYMOLA_SIM_MAT_OUTPUT_PREFIX + "_" + Command.EXECUTION_NUMBER_PATTERN + WP43_UNDEROVERVOLTAGE_SECURITY_INPUT_FILE_NAME)
                )
                .build();
    }

    private void writeModelicaExporterContingenciesFile(Path eventsPath, List<Contingency> contingencies) throws IOException {
        LOGGER.info(" writing contingencies data to a .csv file, to feed the modelica exporter ..");
        File eventsFile=eventsPath.toFile();
        try (BufferedWriter csvWriter=new BufferedWriter(new FileWriter(eventsFile))) {
            for (int i = 0; i < contingencies.size(); i++) {
                Contingency contingency = contingencies.get(i);
                if (contingency.getElements().isEmpty()) {
                    throw new AssertionError("Empty contingency " + contingency.getId());
                }
                for (ContingencyElement element: contingency.getElements()) {
                    csvWriter.write(element.toString());
                    csvWriter.newLine();
                }
            }
        }
    }

    private List<String> writeDymolaInputs(Path workingDir, List<Contingency> contingencies) throws IOException {
        LOGGER.info(" Start writing dymola inputs");

        List<String> retList=new ArrayList<>();

        DdbConfig ddbConfig=DdbConfig.load();
        String jbossHost=ddbConfig.getJbossHost();
        String jbossPort=ddbConfig.getJbossPort();
        String jbossUser=ddbConfig.getJbossUser();
        String jbossPassword=ddbConfig.getJbossPassword();

        Path dymolaExportPath=workingDir.resolve(MO_EXPORT_DIRECTORY);
        if (!Files.exists(dymolaExportPath)) {
            Files.createDirectory(dymolaExportPath);
        }

        //retrieve modelica export parameters from configuration
        String modelicaVersion=config.getModelicaVersion();
        String sourceEngine=config.getSourceEngine();
        String sourceVersion=config.getSourceEngineVersion();
        Path modelicaPowerSystemLibraryPath=Paths.get(config.getModelicaPowerSystemLibraryFile());

        //write the modelica events file, to feed the modelica exporter
        Path eventsPath=workingDir.resolve(MODELICA_EVENTS_CSV_FILENAME);
        writeModelicaExporterContingenciesFile(eventsPath, contingencies);

        //these are only optional params needed if the source is eurostag
        Path modelicaLibPath=null;

        String slackId=config.getSlackId();
        if ("".equals(slackId)) {
            slackId=null; // null when not specified ()
        }

        LoadFlowFactory loadFlowFactory;
        try {
            loadFlowFactory = config.getLoadFlowFactoryClass().newInstance();
        } catch (InstantiationException|IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Exporting modelica data for network {}, working state-id {} ", network, network.getStateManager().getWorkingStateId());
        ModelicaMainExporter exporter = new ModelicaMainExporter(network, slackId, jbossHost, jbossPort, jbossUser, jbossPassword, modelicaVersion, sourceEngine, sourceVersion, modelicaLibPath, loadFlowFactory);
        exporter.export(dymolaExportPath);
        ModEventsExport eventsExporter = new ModEventsExport(dymolaExportPath.resolve(network.getId()+".mo").toFile(), eventsPath.toFile());
        eventsExporter.export(dymolaExportPath);
        LOGGER.info(" modelica data exported.");

        // now assemble the input files to feed dymola
        //  one .zip per contingency; in the zip, the .mo file and the powersystem library
        //TODO here it is assumed that contingencies ids in csv file start from 0 (i.e. 0 is the first contingency); id should be decoupled from the implementation
        try (final Stream<Path> pathStream = Files.walk(dymolaExportPath)) {
            pathStream.filter((p) -> !p.toFile().isDirectory() && p.toFile().getAbsolutePath().contains("events_") && p.toFile().getAbsolutePath().endsWith(".mo")).
                    forEach(p -> {
                        GenericArchive archive = ShrinkWrap.createDomain().getArchiveFactory().create(GenericArchive.class);
                        try (FileSystem fileSystem = new ShrinkWrapFileSystem(new ShrinkWrapFileSystemProvider(), archive)) {
                            Path rootDir = fileSystem.getPath("/");
                            Files.copy(modelicaPowerSystemLibraryPath, rootDir.resolve(modelicaPowerSystemLibraryPath.getFileName()));
                            Files.copy(Paths.get(p.toString()), rootDir.resolve(DymolaUtil.DYMOLA_SIM_MODEL_INPUT_PREFIX+".mo"));

                            String[] c = p.getFileName().toString().replace(".mo", "").split("_");
                            try (OutputStream os = Files.newOutputStream(dymolaExportPath.getParent().resolve(DymolaUtil.DYMOLAINPUTZIPFILENAMEPREFIX+"_" + c[c.length - 1] + ".zip"))) {
                                archive.as(ZipExporter.class).exportTo(os);
                                retList.add(new String( c[c.length - 1]));
                            } catch (IOException e) {
                                //e.printStackTrace();
                                throw new RuntimeException(e);
                            }

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    });
        }
        retList.sort(Comparator.<String>naturalOrder());

        //prepare param inputs for indexes from indexes properties file
        LOGGER.info("writing input indexes parameters in  .mat format - start ");
        try {
            Path baseWp43ConfigFile = PlatformConfig.CONFIG_DIR.resolve(WP43_CONFIG_FILE_NAME);
            HierarchicalINIConfiguration configuration = new HierarchicalINIConfiguration(baseWp43ConfigFile.toFile());

            //fix params for smallsignal index (cfr EurostagImpactAnalysis sources)
            SubnodeConfiguration node = configuration.getSection("smallsignal");
            node.setProperty("f_instant", Double.toString(parameters.getFaultEventInstant()));
            for (int i = 0; i < contingencies.size(); i++) {
                Contingency contingency = contingencies.get(i);
                if (contingency.getElements().isEmpty()) {
                    throw new AssertionError("Empty contingency " + contingency.getId());
                }
                Iterator<ContingencyElement> it = contingency.getElements().iterator();
                // compute the maximum fault duration
                double maxDuration = getFaultDuration(it.next());
                while (it.hasNext()) {
                    maxDuration = Math.max(maxDuration, getFaultDuration(it.next()));
                }
                node.setProperty("f_duration", Double.toString(maxDuration));
            }

            DymolaAdaptersMatParamsWriter writer = new DymolaAdaptersMatParamsWriter(configuration);
            for (String cId : retList) {
                String parFileNamePrefix = DymolaUtil.DYMOLA_SIM_MAT_OUTPUT_PREFIX + "_" + cId + "_wp43_";
                String parFileNameSuffix = "_pars.mat";
                String zippedParFileNameSuffix = "_pars.zip";

                try (OutputStream os = Files.newOutputStream(dymolaExportPath.getParent().resolve(DymolaUtil.DYMOLAINPUTZIPFILENAMEPREFIX + "_" +cId + zippedParFileNameSuffix))) {
                    JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
                    Path sfile1 = ShrinkWrapFileSystems.newFileSystem(archive).getPath("/");

                    Arrays.asList(config.getIndexesNames()).forEach(indexName -> writer.write(indexName, sfile1.resolve(parFileNamePrefix + indexName + parFileNameSuffix)));

                    archive.as(ZipExporter.class).exportTo(os);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }

        } catch (ConfigurationException exc){
            throw new RuntimeException(exc);
        }

        LOGGER.info("writing input indexes parameters in  .mat format - end - {}", retList);
        return retList;
    }

}
