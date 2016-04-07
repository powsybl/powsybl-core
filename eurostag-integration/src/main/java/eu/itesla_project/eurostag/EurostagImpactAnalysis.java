/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import eu.itesla_project.commons.Version;
import eu.itesla_project.commons.io.PlatformConfig;
import eu.itesla_project.computation.*;
import eu.itesla_project.iidm.eurostag.export.EurostagDictionary;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.util.Identifiables;
import eu.itesla_project.iidm.network.util.Networks;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.contingencies.Contingency;
import eu.itesla_project.modules.contingencies.ContingencyElement;
import eu.itesla_project.modules.securityindexes.SecurityIndex;
import eu.itesla_project.modules.securityindexes.SecurityIndexParser;
import eu.itesla_project.modules.simulation.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.jboss.shrinkwrap.api.Domain;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.impl.nio.file.ShrinkWrapFileSystem;
import org.jboss.shrinkwrap.impl.nio.file.ShrinkWrapFileSystemProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static eu.itesla_project.computation.FilePostProcessor.FILE_GZIP;
import static eu.itesla_project.computation.FilePreProcessor.ARCHIVE_UNZIP;
import static eu.itesla_project.computation.FilePreProcessor.FILE_GUNZIP;

/**
 * Example:
 * <p><pre>
 *import eu.itesla_project.modules.Stabilization;
 *import eu.itesla_project.modules.ImpactAnalysis;
 *import eu.itesla_project.modules.SimulationParameters;
 *import eu.itesla_project.modules.StabilizationResult;
 *import eu.itesla_project.modules.ImpactAnalysisResult;
 *import eu.itesla_project.modules.ddb.DynamicDatabaseClient;
 *import eu.itesla_project.modules.ContingenciesAndActionsDatabaseClient;
 *import eu.itesla_project.modules.test.AutomaticContingenciesAndActionsDatabaseClient;
 *import eu.itesla_project.computation.ComputationPlatformClient;
 *import eu.itesla_project.computation.local.LocalComputationPlatformClient;
 *import eu.itesla_project.eurostag.EurostagConfig;
 *import eu.itesla_project.eurostag.EurostagStabilization;
 *import eu.itesla_project.eurostag.EurostagImpactAnalysis;
 *import eu.itesla_project.iidm.ddb.eurostag_imp_exp.DdbDtaImpExp;
 *import eu.itesla_project.iidm.network.Network;
 *import java.nio.file.Path;
 *import java.nio.file.Paths;
 *
 *public class EurostagDemo {
 *
 *    public static void main(String[] args) throws Exception {
 *        Network network = ...;
 *        ComputationPlatformClient cpClient = new LocalComputationPlatformClient(Paths.get("/tmp"));
 *        DynamicDatabaseClient ddbClient = new DdbDtaImpExp();
 *        ContingenciesAndActionsDatabaseClient cadbClient = new AutomaticContingenciesAndActionsDatabaseClient(5);
 *        EurostagConfig config = new EurostagConfig();
 *        try (Stabilization stabilization = new EurostagStabilization(network, cpClient, ddbClient);
 *             ImpactAnalysis impactAnalysis = new EurostagImpactAnalysis(network, cpClient, cadbClient, config)) {
 *            Map<String, Object> initContext = new HashMap<>();
 *            SimulationParameters simulationParameters = new SimulationParameters();
 *            stabilization.init(simulationParameters, initContext);
 *            impactAnalysis.init(simulationParameters, initContext);
 *            StabilizationResult sr = stabilization.run();
 *            ImpactAnalysisResult result = impactAnalysis.run(sr.getState());
 *            System.out.println("Simulation complete");
 *        }
 *    }
 *}
 * </pre>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EurostagImpactAnalysis implements ImpactAnalysis, EurostagConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(EurostagImpactAnalysis.class);

    private static final String PARTIAL_SCENARIOS_ZIP_FILE_NAME = "eurostag-partial-scenarios.zip";
    private static final String FAULT_BASE_NAME = FAULT_SEQ_FILE_NAME.replace(".seq", "");
    private static final String FAULT_OUT_FILE_NAME = FAULT_SEQ_FILE_NAME.replace(".seq", ".out");
    private static final String FAULT_OUT_GZ_FILE_NAME = FAULT_OUT_FILE_NAME + ".gz";
    private static final String CURRENT_LIMITS_CSV = "current_limits.csv";
    private static final String VOLTAGE_LIMITS_CSV = "voltage_limits.csv";
    private static final String TSO_LIMITS_SECURITY_INDEX_FILE_NAME = FAULT_SEQ_FILE_NAME.replace(".seq", "_tso_limits_security_indexes.xml");
    private static final String WP43_SMALLSIGNAL_SECURITY_INDEX_FILE_NAME = FAULT_SEQ_FILE_NAME.replace(".seq", "_wp43_smallsignal_security_indexes.xml");
    private static final String WP43_TRANSIENT_SECURITY_INDEX_FILE_NAME = FAULT_SEQ_FILE_NAME.replace(".seq", "_wp43_transient_security_indexes.xml");
    private static final String WP43_OVERLOAD_SECURITY_INDEX_FILE_NAME = FAULT_SEQ_FILE_NAME.replace(".seq", "_wp43_overload_security_indexes.xml");
    private static final String WP43_UNDEROVERVOLTAGE_SECURITY_INDEX_FILE_NAME = FAULT_SEQ_FILE_NAME.replace(".seq", "_wp43_underovervoltage_security_indexes.xml");
    private static final String WP43_ALL_CONFIGS_ZIP_FILE_NAME = "wp43-all-configs.zip";
    private static final String WP43_PARTIAL_CONFIGS_ZIP_FILE_NAME = "wp43-partial-configs.zip";
    private static final String WP43_CONFIGS_FILE_NAME = "wp43adapter.properties";
    private static final String WP43_CONFIGS_PER_FAULT_FILE_NAME = "wp43adapter_fault_" + Command.EXECUTION_NUMBER_PATTERN + ".properties";

    private static final String WORKING_DIR_PREFIX = "itesla_eurostag_impact_analysis_";

    private final Network network;

    private final ComputationManager computationManager;

    private final ContingenciesAndActionsDatabaseClient cadbClient;

    private final int priority;

    private final EurostagConfig config;

    private final Command allCmd;

    private final List<Contingency> allContingencies = new ArrayList<>();

    private final Command subsetCmd;

    private EurostagDictionary dictionary;

    private SimulationParameters parameters;

    public EurostagImpactAnalysis(Network network, ComputationManager computationManager, int priority,
                                  ContingenciesAndActionsDatabaseClient cadbClient) {
        this(network, computationManager, priority, cadbClient, EurostagConfig.load());
    }

    public EurostagImpactAnalysis(Network network, ComputationManager computationManager, int priority,
                                  ContingenciesAndActionsDatabaseClient cadbClient, EurostagConfig config) {
        Objects.requireNonNull(network, "network is null");
        Objects.requireNonNull(computationManager, "computation manager is null");
        Objects.requireNonNull(cadbClient, "contingencies and actions database client is null");
        Objects.requireNonNull(config, "config is null");
        this.network = network;
        this.computationManager = computationManager;
        this.priority = priority;
        this.cadbClient = cadbClient;
        this.config = config;
        allCmd = createCommand(ALL_SCENARIOS_ZIP_FILE_NAME, WP43_ALL_CONFIGS_ZIP_FILE_NAME);
        subsetCmd = createCommand(PARTIAL_SCENARIOS_ZIP_FILE_NAME, WP43_PARTIAL_CONFIGS_ZIP_FILE_NAME);
    }

    private Command createCommand(String scenarioZipFileName, String wp43ConfigsZipFileName) {
        return new GroupCommandBuilder()
                .id("esg_fs")
                .inputFiles(new InputFile(PRE_FAULT_SAC_GZ_FILE_NAME, FILE_GUNZIP),
                            new InputFile(LIMITS_ZIP_FILE_NAME, ARCHIVE_UNZIP),
                            new InputFile(scenarioZipFileName, ARCHIVE_UNZIP),
                            new InputFile(wp43ConfigsZipFileName, ARCHIVE_UNZIP),
                            new InputFile(DDB_DICT_GENS_CSV))
                .subCommand()
                    .program(EUSTAG_CPT)
                    .args("-s", FAULT_SEQ_FILE_NAME, PRE_FAULT_SAC_FILE_NAME)
                    .timeout(config.getSimTimeout())
                .add()
                .subCommand()
                    .program(TSOINDEXES)
                    .args(".", FAULT_BASE_NAME)
                    .timeout(config.getIdxTimeout())
                .add()
                .subCommand()
                    .program(WP43)
                    .args("./", FAULT_BASE_NAME, WP43_CONFIGS_PER_FAULT_FILE_NAME)
                    .timeout(config.getIdxTimeout())
                .add()
                .outputFiles(new OutputFile(TSO_LIMITS_SECURITY_INDEX_FILE_NAME),
                        new OutputFile(WP43_SMALLSIGNAL_SECURITY_INDEX_FILE_NAME),
                        new OutputFile(WP43_TRANSIENT_SECURITY_INDEX_FILE_NAME),
                        new OutputFile(WP43_OVERLOAD_SECURITY_INDEX_FILE_NAME),
                        new OutputFile(WP43_UNDEROVERVOLTAGE_SECURITY_INDEX_FILE_NAME),
                        new OutputFile(FAULT_OUT_FILE_NAME, FILE_GZIP))
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

    //needed by wp43 integration
    //contains lines, only
    private static void dumpLinesDictionary(Network network, EurostagDictionary dictionary, Path dir) throws IOException {
        try (BufferedWriter os = Files.newBufferedWriter(dir.resolve("dict_lines.csv"), StandardCharsets.UTF_8)) {
            for (Identifiable obj : Identifiables.sort(Iterables.concat(network.getLines(),
                                                                        network.getTwoWindingsTransformers(),
                                                                        network.getDanglingLines()))) {
                os.write(obj.getId() + ";" + dictionary.getEsgId(obj.getId()));
                os.newLine();
            }
            for (ThreeWindingsTransformer twt : Identifiables.sort(network.getThreeWindingsTransformers())) {
                throw new AssertionError("TODO");
            }
        }
    }

    //needed by wp43 integration
    //contains buses, only
    private static void dumpBusesDictionary(Network network, EurostagDictionary dictionary, Path dir) throws IOException {
        try (BufferedWriter os = Files.newBufferedWriter(dir.resolve("dict_buses.csv"), StandardCharsets.UTF_8)) {
            for (Bus bus: Identifiables.sort(network.getBusBreakerView().getBuses())) {
                os.write(bus.getId() + ";" + dictionary.getEsgId(bus.getId()));
                os.newLine();
            }
        }
    }

    private static void dumpLimits(EurostagDictionary dictionary, BufferedWriter writer, TwoTerminalsConnectable branch) throws IOException {
        dumpLimits(dictionary, writer, branch.getId(),
                branch.getCurrentLimits1(),
                branch.getCurrentLimits2(),
                branch.getTerminal1().getVoltageLevel().getNominalV(),
                branch.getTerminal2().getVoltageLevel().getNominalV());
    }

    private static void dumpLimits(EurostagDictionary dictionary, BufferedWriter writer, String branchId, CurrentLimits cl1, CurrentLimits cl2,
                                   float nominalV1, float nominalV2) throws IOException {
        writer.write(dictionary.getEsgId(branchId));
        writer.write(";");
        writer.write(Float.toString(cl1 != null ? cl1.getPermanentLimit() : Float.MAX_VALUE));
        writer.write(";");
        writer.write(Float.toString(cl2 != null ? cl2.getPermanentLimit() : Float.MAX_VALUE));
        writer.write(";");
        writer.write(Float.toString(nominalV1));
        writer.write(";");
        writer.write(Float.toString(nominalV2));
        writer.write(";");
        writer.write(branchId);
        writer.newLine();
    }

    private static void writeLimits(Network network, EurostagDictionary dictionary, Domain domain, OutputStream os) throws IOException {
        GenericArchive archive = domain.getArchiveFactory().create(GenericArchive.class);
        try (FileSystem fileSystem = new ShrinkWrapFileSystem(new ShrinkWrapFileSystemProvider(), archive)) {
            Path rootDir = fileSystem.getPath("/");
            // dump first current limits for each of the branches
            try (BufferedWriter writer = Files.newBufferedWriter(rootDir.resolve(CURRENT_LIMITS_CSV), StandardCharsets.UTF_8)) {
                for (Line l : network.getLines()) {
                    dumpLimits(dictionary, writer, l);
                }
                for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
                    dumpLimits(dictionary, writer, twt);
                }
                for (DanglingLine dl : network.getDanglingLines()) {
                    dumpLimits(dictionary, writer, dl.getId(),
                                       dl.getCurrentLimits(),
                                       null,
                                       dl.getTerminal().getVoltageLevel().getNominalV(),
                                       dl.getTerminal().getVoltageLevel().getNominalV());
                }
            }
            try (BufferedWriter writer = Files.newBufferedWriter(rootDir.resolve(VOLTAGE_LIMITS_CSV), StandardCharsets.UTF_8)) {
                for (Bus b : network.getBusBreakerView().getBuses()) {
                    VoltageLevel vl = b.getVoltageLevel();
                    if (!Float.isNaN(vl.getLowVoltageLimit()) && !Float.isNaN(vl.getHighVoltageLimit())) {
                        writer.write(dictionary.getEsgId(b.getId()));
                        writer.write(";");
                        writer.write(Float.toString(vl.getLowVoltageLimit()));
                        writer.write(";");
                        writer.write(Float.toString(vl.getHighVoltageLimit()));
                        writer.write(";");
                        writer.write(Float.toString(vl.getNominalV()));
                        writer.newLine();
                    }
                }
            }

            //dump lines dictionary, for WP43 integration
            dumpLinesDictionary(network, dictionary, rootDir);
            //dump buses dictionary, for WP43 integration
            dumpBusesDictionary(network, dictionary, rootDir);
        }

        archive.as(ZipExporter.class).exportTo(os);
    }

    private void writeLimits(Domain domain, OutputStream os) throws IOException {
        writeLimits(network, dictionary, domain, os);
    }

    static void writeLimits(Network network, EurostagDictionary dictionary, OutputStream os) throws IOException {
        writeLimits(network, dictionary, ShrinkWrap.createDomain(), os);
    }

    private void writeScenarios(Domain domain, List<Contingency> contingencies, OutputStream os) throws IOException {
        GenericArchive archive = new EurostagScenario(parameters, config).writeFaultSeqArchive(domain, contingencies, network, dictionary,
                faultNum -> FAULT_SEQ_FILE_NAME.replace(Command.EXECUTION_NUMBER_PATTERN, Integer.toString(faultNum)));
        archive.as(ZipExporter.class).exportTo(os);
    }

    private void writeAllScenarios(Domain domain, OutputStream os) throws IOException {
        writeScenarios(domain, allContingencies, os);
    }

    private double getFaultDuration(Contingency contingency, ContingencyElement element) {
        switch (element.getType()) {
            case GENERATOR:
                return parameters.getGeneratorFaultShortCircuitDuration(contingency.getId(), element.getId());
            case LINE:
                return parameters.getBranchFaultShortCircuitDuration(contingency.getId(), element.getId());
            default: throw new AssertionError();
        }
    }

    private void writeWp43Configs(List<Contingency> contingencies, Path workingDir) throws IOException, ConfigurationException {
        Path baseWp43ConfigFile = PlatformConfig.CONFIG_DIR.resolve(WP43_CONFIGS_FILE_NAME);

        // generate one variant of the base config for all the contingency
        // this allow to add extra variables for some indexes
        HierarchicalINIConfiguration configuration = new HierarchicalINIConfiguration(baseWp43ConfigFile.toFile());
        SubnodeConfiguration node = configuration.getSection("smallsignal");
        node.setProperty("f_instant", parameters.getFaultEventInstant());
        for (int i = 0; i < contingencies.size(); i++) {
            Contingency contingency = contingencies.get(i);
            if (contingency.getElements().isEmpty()) {
                throw new AssertionError("Empty contingency " + contingency.getId());
            }
            Iterator<ContingencyElement> it = contingency.getElements().iterator();
            // compute the maximum fault duration
            double maxDuration = getFaultDuration(contingency, it.next());
            while (it.hasNext()) {
                maxDuration = Math.max(maxDuration, getFaultDuration(contingency, it.next()));
            }
            node.setProperty("f_duration", maxDuration);
            Path wp43Config = workingDir.resolve(WP43_CONFIGS_PER_FAULT_FILE_NAME.replace(Command.EXECUTION_NUMBER_PATTERN, Integer.toString(i)));
            try (Writer writer = Files.newBufferedWriter(wp43Config, StandardCharsets.UTF_8)) {
                configuration.save(writer);
            }
        }
    }

    private void writeWp43Configs(Domain domain, List<Contingency> contingencies, OutputStream os) throws IOException, ConfigurationException {
        // copy wp43 configuration files
        GenericArchive archive = domain.getArchiveFactory().create(GenericArchive.class);
        try (FileSystem fileSystem = new ShrinkWrapFileSystem(new ShrinkWrapFileSystemProvider(), archive)) {
            Path rootDir = fileSystem.getPath("/");
            writeWp43Configs(contingencies, rootDir);
        }
        archive.as(ZipExporter.class).exportTo(os);
    }

    private void writeAllWp43Configs(Domain domain, OutputStream os) throws IOException, ConfigurationException {
        writeWp43Configs(domain, allContingencies, os);
    }

    private void readSecurityIndexes(List<Contingency> contingencies, Path workingDir, ImpactAnalysisResult result) throws IOException {
        long start = System.currentTimeMillis();
        int files = 0;

        for (int i = 0; i < contingencies.size(); i++) {
            Contingency contingency = contingencies.get(i);
            for (String securityIndexFileName : Arrays.asList(TSO_LIMITS_SECURITY_INDEX_FILE_NAME,
                                                              WP43_SMALLSIGNAL_SECURITY_INDEX_FILE_NAME,
                                                              WP43_TRANSIENT_SECURITY_INDEX_FILE_NAME,
                                                              WP43_OVERLOAD_SECURITY_INDEX_FILE_NAME,
                                                              WP43_UNDEROVERVOLTAGE_SECURITY_INDEX_FILE_NAME)) {
                Path file = workingDir.resolve(securityIndexFileName.replace(Command.EXECUTION_NUMBER_PATTERN, Integer.toString(i)));
                if (Files.exists(file)) {
                    try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                        for (SecurityIndex index : SecurityIndexParser.fromXml(contingency.getId(), reader)) {
                            result.addSecurityIndex(index);
                        }
                    }
                    files++;
                }
	        }
            // also scan errors in output
            EurostagUtil.searchErrorMessage(workingDir.resolve(FAULT_OUT_GZ_FILE_NAME.replace(Command.EXECUTION_NUMBER_PATTERN, Integer.toString(i))), result.getMetrics(), i);
        }

        LOGGER.trace("{} security indexes files read in {} ms", files, (System.currentTimeMillis() - start));
    }

    private static EurostagDictionary getDictionary(Map<String, Object> context) {
        Object dictionary = context.get("dictionary");
        if (dictionary == null) {
            throw new RuntimeException("Stabilization must be initialized first");
        }
        if (!(dictionary instanceof EurostagDictionary)) {
            throw new RuntimeException("Incompatiblity between stabilization and impact analysis implementations");
        }
        return (EurostagDictionary) dictionary;
    }

    @Override
    public void init(SimulationParameters parameters, Map<String, Object> context) throws Exception {
        Objects.requireNonNull(parameters, "parameters is null");
        Objects.requireNonNull(context, "context is null");

        this.parameters = parameters;
        dictionary = getDictionary(context);

        // read all contingencies
        allContingencies.addAll(cadbClient.getContingencies(network));

        if (config.isUseBroadcast()) {
            Domain domain = ShrinkWrap.createDomain();
            try (OutputStream os = computationManager.newCommonFile(ALL_SCENARIOS_ZIP_FILE_NAME)) {
                writeAllScenarios(domain, os);
            }
            try (OutputStream os = computationManager.newCommonFile(WP43_ALL_CONFIGS_ZIP_FILE_NAME)) {
                writeAllWp43Configs(domain, os);
            }
            try (OutputStream os = computationManager.newCommonFile(LIMITS_ZIP_FILE_NAME)) {
                writeLimits(domain, os);
            }
        }
    }

    private static void checkState(SimulationState state) {
        Objects.requireNonNull(state, "state is null");
        if (!(state instanceof EurostagState)) {
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

        try (OutputStream os = Files.newOutputStream(workingDir.resolve(PRE_FAULT_SAC_GZ_FILE_NAME))) {
            os.write(((EurostagState) state).getSacGz());
        }

        Supplier<Domain> domain = Suppliers.memoize(ShrinkWrap::createDomain);
        if (!config.isUseBroadcast()) {
            Files.write(workingDir.resolve(DDB_DICT_GENS_CSV), ((EurostagState) state).getDictGensCsv());

            try (OutputStream os = Files.newOutputStream(workingDir.resolve(LIMITS_ZIP_FILE_NAME))) {
                writeLimits(domain.get(), os);
            }
        }

        Command cmd;
        if (contingencyIds == null) {
            // take all contingencies
            contingencies.addAll(allContingencies);
            cmd = allCmd;

            if (config.isUseBroadcast()) {
                // all scenarios zip file has already been sent in the common dir
            } else {
                try (OutputStream os = Files.newOutputStream(workingDir.resolve(ALL_SCENARIOS_ZIP_FILE_NAME))) {
                    writeAllScenarios(domain.get(), os);
                }
                try (OutputStream os = Files.newOutputStream(workingDir.resolve(WP43_ALL_CONFIGS_ZIP_FILE_NAME))) {
                    writeAllWp43Configs(domain.get(), os);
                } catch (ConfigurationException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            // filter contingencies
            for (Contingency c : cadbClient.getContingencies(network)) {
                if (contingencyIds.contains(c.getId())) {
                    contingencies.add(c);
                }
            }
            cmd = subsetCmd;

            // write scenarios subset in the working dir
            try (OutputStream os = Files.newOutputStream(workingDir.resolve(PARTIAL_SCENARIOS_ZIP_FILE_NAME))) {
                writeScenarios(domain.get(), contingencies, os);
            }
            try (OutputStream os = Files.newOutputStream(workingDir.resolve(WP43_PARTIAL_CONFIGS_ZIP_FILE_NAME))) {
                writeWp43Configs(domain.get(), contingencies, os);
            } catch (ConfigurationException e) {
                throw new RuntimeException(e);
            }
        }

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

        try (CommandExecutor executor = computationManager.newCommandExecutor(EurostagUtil.createEnv(config), WORKING_DIR_PREFIX, config.isDebug())) {

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

        return computationManager.execute(new ExecutionEnvironment(EurostagUtil.createEnv(config), WORKING_DIR_PREFIX, config.isDebug()),
                new DefaultExecutionHandler<ImpactAnalysisResult>() {

                    private final List<Contingency> contingencies = new ArrayList<>();

                    @Override
                    public List<CommandExecution> before(Path workingDir) throws IOException {
                        Command cmd = EurostagImpactAnalysis.this.before(state, contingencyIds, workingDir, contingencies);
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
                        return EurostagImpactAnalysis.this.after(workingDir, contingencies, report);
                    }
                });
    }

    private void fillMetrics(List<Contingency> contingencies, ExecutionReport report, Map<String, String> metrics) {
        float successPercent = 100f * (1 - ((float) report.getErrors().size()) / contingencies.size());
        metrics.put("successPercent", Float.toString(successPercent));
        EurostagUtil.putBadExitCode(report, metrics);
    }

}
