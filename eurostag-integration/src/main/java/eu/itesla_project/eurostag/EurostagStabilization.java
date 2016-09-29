/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import eu.itesla_project.commons.Version;
import eu.itesla_project.computation.*;
import eu.itesla_project.eurostag.network.EsgGeneralParameters;
import eu.itesla_project.eurostag.network.EsgNetwork;
import eu.itesla_project.eurostag.network.io.EsgWriter;
import eu.itesla_project.eurostag.tools.EurostagNetworkModifier;
import eu.itesla_project.iidm.datasource.FileDataSource;
import eu.itesla_project.iidm.ddb.eurostag_imp_exp.IIDMDynamicDatabaseFactory;
import eu.itesla_project.iidm.eurostag.export.BranchParallelIndexes;
import eu.itesla_project.iidm.eurostag.export.EurostagDictionary;
import eu.itesla_project.iidm.eurostag.export.EurostagEchExport;
import eu.itesla_project.iidm.eurostag.export.EurostagEchExportConfig;
import eu.itesla_project.iidm.export.Exporter;
import eu.itesla_project.iidm.export.Exporters;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.util.Networks;
import eu.itesla_project.iidm.ddb.eurostag_imp_exp.DynamicDatabaseClient;
import eu.itesla_project.iidm.ddb.eurostag_imp_exp.DynamicDatabaseClientFactory;
import eu.itesla_project.simulation.SimulationParameters;
import eu.itesla_project.simulation.Stabilization;
import eu.itesla_project.simulation.StabilizationResult;
import eu.itesla_project.simulation.StabilizationStatus;
import org.jboss.shrinkwrap.api.Domain;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static eu.itesla_project.computation.FilePostProcessor.FILE_GZIP;
import static eu.itesla_project.computation.FilePreProcessor.ARCHIVE_UNZIP;
import static eu.itesla_project.computation.FilePreProcessor.FILE_GUNZIP;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EurostagStabilization implements Stabilization, EurostagConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(EurostagStabilization.class);

    private static final String DDB_ZIP_FILE_NAME = "eurostag-ddb.zip";

    private static final String ECH_FILE_NAME = "sim.ech";
    private static final String ECH_GZ_FILE_NAME = ECH_FILE_NAME + ".gz";
    private static final String DTA_FILE_NAME = "sim.dta";

    private static final String SAV_FILE_NAME = ECH_FILE_NAME.replace(".ech", ".sav");
    private static final String LF_FILE_NAME = ECH_FILE_NAME.replace(".ech", ".lf");
    private static final String LF_GZ_FILE_NAME = LF_FILE_NAME + ".gz";
    private static final String PRE_FAULT_OUT_FILE_NAME = PRE_FAULT_SEQ_FILE_NAME.replace(".seq", ".out");
    private static final String PRE_FAULT_OUT_GZ_FILE_NAME = PRE_FAULT_OUT_FILE_NAME + ".gz";
    private static final String PRE_FAULT_RES_FILE_NAME = PRE_FAULT_SEQ_FILE_NAME.replace(".seq", ".res");
    private static final String INTEGRATION_STEP_FILE_NAME = "integration_step.csv";

    private static final String WORKING_DIR_PREFIX = "itesla_eurostag_stabilization_";

    private static final String CMD_ID = "esg_pfs";

    private static final String TSEXTRACT = "tsextract";

    private final Network network;

    private final ComputationManager computationManager;

    private final DynamicDatabaseClient ddbClient;

    private final int priority;

    private final EurostagConfig config;

    private final Command cmd;

    private EurostagEchExportConfig exportConfig;

    private BranchParallelIndexes parallelIndexes;

    private EurostagDictionary dictionary;

    private final EurostagNetworkModifier networkModifier = new EurostagNetworkModifier();

    private SimulationParameters parameters;

    public EurostagStabilization(Network network, ComputationManager computationManager, int priority) {
        this(network, computationManager, priority, EurostagConfig.load());
    }

    public EurostagStabilization(Network network, ComputationManager computationManager, int priority, EurostagConfig config) {
        Objects.requireNonNull(network, "network is null");
        Objects.requireNonNull(computationManager, "computation manager is null");
        Objects.requireNonNull(config, "config is null");
        this.network = network;
        this.computationManager = computationManager;
        this.priority = priority;
        this.ddbClient = new IIDMDynamicDatabaseFactory().create(config.isDdbCaching());
        this.config = config;

        LOGGER.info(config.toString());

        // create a command with a load flow followed by a stabilization simulation
        cmd = new GroupCommandBuilder()
                .id(CMD_ID)
                .inputFiles(new InputFile(ECH_GZ_FILE_NAME, FILE_GUNZIP),
                        new InputFile(PRE_FAULT_SEQ_FILE_NAME),
                        new InputFile(DDB_ZIP_FILE_NAME, ARCHIVE_UNZIP))
                .subCommand()
                    .program(EUSTAG_CPT)
                    .args("-lf", ECH_FILE_NAME)
                    .timeout(config.getLfTimeout())
                .add()
                .subCommand()
                    .program(EUSTAG_CPT)
                    .args("-s", PRE_FAULT_SEQ_FILE_NAME, DTA_FILE_NAME, SAV_FILE_NAME)
                    .timeout(config.getSimTimeout())
                .add()
                .subCommand()
                    .program(TSEXTRACT)
                    .args(PRE_FAULT_RES_FILE_NAME, "unused", "INTEGRATION_STEP", INTEGRATION_STEP_FILE_NAME)
                .add()
                .outputFiles(new OutputFile(LF_FILE_NAME, FILE_GZIP),
                        new OutputFile(PRE_FAULT_SAC_FILE_NAME, FILE_GZIP),
                        new OutputFile(PRE_FAULT_OUT_FILE_NAME, FILE_GZIP),
                        new OutputFile(INTEGRATION_STEP_FILE_NAME))
                .build();
    }

    @Override
    public String getName() {
        return EurostagUtil.PRODUCT_NAME;
    }

    @Override
    public String getVersion() {
        return ImmutableMap.builder().put("eurostagVersion", EurostagUtil.VERSION)
                                     .putAll(Version.VERSION.toMap())
                                     .build()
                                     .toString();
    }

    private void writeDtaAndControls(Domain domain, OutputStream ddbOs, OutputStream dictGensOs) throws IOException {
        GenericArchive archive = domain.getArchiveFactory().create(GenericArchive.class);
        try (FileSystem fileSystem = ShrinkWrapFileSystems.newFileSystem(archive)) {
            Path rootDir = fileSystem.getPath("/");
            ddbClient.dumpDtaFile(rootDir, DTA_FILE_NAME, network, parallelIndexes.toMap(), EurostagUtil.VERSION, dictionary.toMap());
        }
        archive.as(ZipExporter.class).exportTo(ddbOs);
        //put just the generators dict csv file (extracted from the ddb files) in the common files set, to be used by wp43 transient stability index
        if (archive.get(DDB_DICT_GENS_CSV) != null) {
            ByteStreams.copy(archive.get(DDB_DICT_GENS_CSV).getAsset().openStream(), dictGensOs);
        } else {
            LOGGER.warn(DDB_DICT_GENS_CSV + " is missing in the dynamic data files set: some security indexers (e.g. transient stability) need this file");
        }
    }

    private void writePreFaultSeq(BufferedWriter writer) throws IOException {
        new EurostagScenario(parameters, config).writePreFaultSeq(writer, PRE_FAULT_SAC_FILE_NAME);
    }

    private void writeEch(Path workingDir) throws IOException {
        try (Writer writer = new OutputStreamWriter(new GZIPOutputStream(Files.newOutputStream(workingDir.resolve(ECH_GZ_FILE_NAME))), StandardCharsets.UTF_8)) {
            EsgGeneralParameters parameters = new EsgGeneralParameters();
            parameters.setTransformerVoltageControl(false);
            parameters.setSvcVoltageControl(false);
            parameters.setMaxNumIteration(config.getLfMaxNumIteration());
            parameters.setStartMode(config.isLfWarmStart() ? EsgGeneralParameters.StartMode.WARM_START : EsgGeneralParameters.StartMode.FLAT_START);
            EsgNetwork networkEch = new EurostagEchExport(network, exportConfig, parallelIndexes, dictionary).createNetwork(parameters);
            networkModifier.hvLoadModelling(networkEch);
            new EsgWriter(networkEch, parameters).write(writer, network.getId() + "/" + network.getStateManager().getWorkingStateId());
        }
        if (config.isDebug()) {
            dictionary.dump(workingDir.resolve("dict.csv"));
        }
    }

    @Override
    public void init(SimulationParameters parameters, Map<String, Object> context) throws Exception {
        Objects.requireNonNull(parameters, "parameters is null");
        Objects.requireNonNull(context, "context is null");

        this.parameters = parameters;

        exportConfig = new EurostagEchExportConfig(config.isLfNoGeneratorMinMaxQ());
        parallelIndexes = BranchParallelIndexes.build(network, exportConfig);

        // fill iTesla id to Esg id dictionary
        dictionary = EurostagDictionary.create(network, parallelIndexes, exportConfig);

        if (config.isUseBroadcast()) {
            Domain domain = ShrinkWrap.createDomain();
            try (OutputStream ddbOs = computationManager.newCommonFile(DDB_ZIP_FILE_NAME);
                 OutputStream dictGensOs = computationManager.newCommonFile(DDB_DICT_GENS_CSV)) {
                writeDtaAndControls(domain, ddbOs, dictGensOs);
            }
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(computationManager.newCommonFile(PRE_FAULT_SEQ_FILE_NAME), StandardCharsets.UTF_8))) {
                writePreFaultSeq(writer);
            }
        }

        context.put("dictionary", dictionary);
    }

    class EurostagContext {

        byte[] dictGensCsv;

    }

    private EurostagContext before(Path workingDir) throws IOException {
        if (config.isDebug()) {
            // dump state info for debugging
            Networks.dumpStateId(workingDir, network);

            Exporter exporter = Exporters.getExporter("XML");
            if (exporter != null) {
                Properties parameters = new Properties();
                parameters.setProperty("iidm.export.xml.indent", "true");
                parameters.setProperty("iidm.export.xml.with-branch-state-variables", "true");
                parameters.setProperty("iidm.export.xml.with-breakers", "true");
                try {
                    exporter.export(network, parameters, new FileDataSource(workingDir, "network"));
                } catch (Exception e) {
                    LOGGER.error(e.toString(), e);
                }
            }
        }

        EurostagContext context = new EurostagContext();

        if (!config.isUseBroadcast()) {
            Domain domain = ShrinkWrap.createDomain();
            try (OutputStream ddbOs = Files.newOutputStream(workingDir.resolve(DDB_ZIP_FILE_NAME));
                 ByteArrayOutputStream dictGensOs = new ByteArrayOutputStream()) {
                writeDtaAndControls(domain, ddbOs, dictGensOs);
                dictGensOs.flush();
                context.dictGensCsv = dictGensOs.toByteArray();
            }
            try (BufferedWriter writer = Files.newBufferedWriter(workingDir.resolve(PRE_FAULT_SEQ_FILE_NAME), StandardCharsets.UTF_8)) {
                writePreFaultSeq(writer);
            }
        }

        writeEch(workingDir);

        return context;
    }

    private EurostagStabilizationResult after(Path workingDir, EurostagContext context, ExecutionReport report) throws IOException {
        report.log();

        Map<String, String> metrics = new HashMap<>();
        EurostagUtil.putBadExitCode(report, metrics);
        Path preFaultOutGzFile = workingDir.resolve(PRE_FAULT_OUT_GZ_FILE_NAME);
        try {
            EurostagUtil.searchErrorMessage(preFaultOutGzFile, metrics, null);
            String initialValueErrors = EurostagUtil.searchInitialValueErrors(preFaultOutGzFile);
            if (initialValueErrors != null) {
                LOGGER.error("Simulation intialization errors: {}", initialValueErrors);
                metrics.put("initialValueErrors", initialValueErrors);
            }
            String steadyStateErrors = EurostagUtil.searchSteadyStateErrors(preFaultOutGzFile);
            if (steadyStateErrors != null) {
                LOGGER.error("Simulation steady state errors: {}", steadyStateErrors);
                metrics.put("steadyStateErrors", steadyStateErrors);
            }
            // search for iteration message
            EurostagUtil.EurostagLfStatus lfStatus = null;
            Path outFile = workingDir.resolve(CMD_ID + "_0.out");
            if (Files.exists(outFile)) {
                try (BufferedReader reader = Files.newBufferedReader(outFile, StandardCharsets.UTF_8)) {
                    lfStatus = EurostagUtil.searchLfStatusMessages(reader);
                }
            } else {
                Path outGzFile = workingDir.resolve(CMD_ID + "_0.out.gz");
                if (Files.exists(outGzFile)) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(outGzFile)), StandardCharsets.UTF_8))) {
                        lfStatus = EurostagUtil.searchLfStatusMessages(reader);
                    }
                }
            }
            if (lfStatus != null) {
                metrics.put("lf_diverge", Boolean.toString(lfStatus.diverge));
                metrics.put("lf_iterations", Integer.toString(lfStatus.iterations));
                if (lfStatus.diverge) {
                    LOGGER.warn("Eurostag load flow diverged in {} iterations", lfStatus.iterations);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
        StabilizationStatus status;
        EurostagState state = null;
        boolean ok = report.getErrors().isEmpty();
        if (ok) {
            status = EurostagUtil.isSteadyStateReached(workingDir.resolve(INTEGRATION_STEP_FILE_NAME), config.getMinStepAtEndOfStabilization())
                    ? StabilizationStatus.COMPLETED : StabilizationStatus.COMPLETED_BUT_NOT_TO_STEADY_STATE;
            state = new EurostagState(network.getStateManager().getWorkingStateId(),
                                      Files.readAllBytes(workingDir.resolve(PRE_FAULT_SAC_GZ_FILE_NAME)),
                                      context.dictGensCsv);
        } else {
            status = StabilizationStatus.FAILED;
        }
        metrics.put("stab_rich_status", status.name());
        return new EurostagStabilizationResult(status, metrics, state);
    }

    private CommandExecution createCommandExecution() {
        return new CommandExecution(cmd, 1, priority, Networks.getExecutionTags(network));
    }

    @Override
    public StabilizationResult run() throws Exception {
        try (CommandExecutor executor = computationManager.newCommandExecutor(EurostagUtil.createEnv(config), WORKING_DIR_PREFIX, config.isDebug())) {

            Path workingDir = executor.getWorkingDir();

            EurostagContext context = before(workingDir);

            // start the execution
            ExecutionReport report = executor.start(createCommandExecution());

            return after(workingDir, context, report);
        }
    }

    @Override
    public CompletableFuture<StabilizationResult> runAsync(String workingStateId) {
        return computationManager.execute(new ExecutionEnvironment(EurostagUtil.createEnv(config), WORKING_DIR_PREFIX, config.isDebug()),
                new DefaultExecutionHandler<StabilizationResult>() {

                    private EurostagContext context;

                    @Override
                    public List<CommandExecution> before(Path workingDir) throws IOException {
                        network.getStateManager().setWorkingState(workingStateId);

                        context = EurostagStabilization.this.before(workingDir);

                        return Arrays.asList(createCommandExecution());
                    }

                    @Override
                    public StabilizationResult after(Path workingDir, ExecutionReport report) throws IOException {
                        network.getStateManager().setWorkingState(workingStateId);

                        return EurostagStabilization.this.after(workingDir, context, report);
                    }
                });
    }

}
