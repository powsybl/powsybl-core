/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.db.mmap;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import eu.itesla_project.commons.io.FileUtil;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.histo.HistoDbNetworkAttributeId;
import eu.itesla_project.modules.histo.IIDM2DB;
import eu.itesla_project.modules.offline.*;
import eu.itesla_project.modules.securityindexes.SecurityIndex;
import eu.itesla_project.modules.securityindexes.SecurityIndexId;
import eu.itesla_project.modules.securityindexes.SecurityIndexParser;
import eu.itesla_project.offline.db.util.CsvWriter;
import eu.itesla_project.offline.db.util.MemoryMappedFileFactory;
import eu.itesla_project.offline.db.util.MemoryMappedFileImpl;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MMapOfflineDb implements OfflineDb {

    private static final Logger LOGGER = LoggerFactory.getLogger(MMapOfflineDb.class);

    final static Supplier<JsonFactory> JSON_FACTORY = Suppliers.memoize(JsonFactory::new);

    private static final char CSV_SEPARATOR = ';';
    private static final String SECURITY_INDEXES_XML_FILE_NAME = "security-indexes-xml.csv";
    private static final String PARAMETERS_FILE_NAME = "parameters.json";
    private static final String DEFAULT_WORKFLOW_ID_PREFIX = "workflow-";

    private static final boolean CHECK_MISSING_VALUES = false;

    public static class PersistenceContext implements AutoCloseable {

        private final Path workflowDir;

        private final OfflineWorkflowCreationParameters parameters;

        private final OfflineDbTable table;

        private final CsvWriter securityIndexesXmlWriter;

        static OfflineWorkflowCreationParameters readParameters(Path workflowDir) throws IOException {
            Set<Country> countries = null;
            DateTime baseCaseDate = null;
            Interval histoInterval = null;
            Boolean generationSampled = null;
            Boolean boundariesSampled = null;
            boolean initTopo = OfflineWorkflowCreationParameters.DEFAULT_INIT_TOPO;
            double correlationThreshold = OfflineWorkflowCreationParameters.DEFAULT_CORRELATION_THRESHOLD;
            double probabilityThreshold = OfflineWorkflowCreationParameters.DEFAULT_PROBABILITY_THRESHOLD;
            boolean loadFlowTransformerVoltageControlOn = OfflineWorkflowCreationParameters.DEFAULT_LOAD_FLOW_TRANSFORMER_VOLTAGE_CONTROL_ON;
            boolean simplifiedWorkflow = OfflineWorkflowCreationParameters.DEFAULT_SIMPLIFIED_WORKFLOW;
            boolean mergeOptimized = OfflineWorkflowCreationParameters.DEFAULT_MERGE_OPTIMIZED;
            Set<Country> attributesCountryFilter = OfflineWorkflowCreationParameters.DEFAULT_ATTRIBUTES_COUNTRY_FILTER;
            int attributesMinBaseVoltageFilter = OfflineWorkflowCreationParameters.DEFAULT_ATTRIBUTES_MIN_BASE_VOLTAGE_FILTER;
            Path file = workflowDir.resolve(PARAMETERS_FILE_NAME);
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
                 JsonParser parser = MMapOfflineDb.JSON_FACTORY.get().createParser(reader)) {
                parser.nextToken();
                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    String fieldname = parser.getCurrentName();
                    switch (fieldname) {
                        case "countries":
                            countries = EnumSet.noneOf(Country.class);
                            parser.nextToken();
                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                countries.add(Country.valueOf(parser.getText()));
                            }
                            break;

                        case "baseCaseDate":
                            parser.nextToken();
                            baseCaseDate = DateTime.parse(parser.getText());
                            break;

                        case "histoInterval":
                            parser.nextToken();
                            histoInterval = Interval.parse(parser.getText());
                            break;

                        case "generationSampled":
                            parser.nextToken();
                            generationSampled = parser.getValueAsBoolean();
                            break;

                        case "boundariesSampled":
                            parser.nextToken();
                            boundariesSampled = parser.getValueAsBoolean();
                            break;

                        case "initTopo":
                            parser.nextToken();
                            initTopo = parser.getValueAsBoolean();
                            break;

                        case "correlationThreshold":
                            parser.nextToken();
                            correlationThreshold = parser.getValueAsDouble();
                            break;

                        case "probabilityThreshold":
                            parser.nextToken();
                            probabilityThreshold = parser.getValueAsDouble();
                            break;

                        case "loadFlowTransformerVoltageControlOn":
                            parser.nextToken();
                            loadFlowTransformerVoltageControlOn = parser.getValueAsBoolean();
                            break;

                        case "simplifiedWorkflow":
                            parser.nextToken();
                            simplifiedWorkflow = parser.getValueAsBoolean();
                            break;

                        case "mergeOptimized":
                            parser.nextToken();
                            mergeOptimized = parser.getValueAsBoolean();
                            break;

                        case "attributesCountryFilter":
                            attributesCountryFilter = EnumSet.noneOf(Country.class);
                            parser.nextToken();
                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                attributesCountryFilter.add(Country.valueOf(parser.getText()));
                            }
                            break;

                        case "attributesMinBaseVoltageFilter":
                            parser.nextToken();
                            attributesMinBaseVoltageFilter = Integer.parseInt(parser.getText());
                            break;

                        default:
                            throw new AssertionError();
                    }
                }
            }

            if (countries == null) {
                throw new RuntimeException("countries not set");
            }
            if (baseCaseDate == null) {
                throw new RuntimeException("baseCaseDate not set");
            }
            if (histoInterval == null) {
                throw new RuntimeException("histoInterval not set");
            }
            if (generationSampled == null) {
                throw new RuntimeException("generationSampled not set");
            }
            if (boundariesSampled == null) {
                throw new RuntimeException("boundariesSampled not set");
            }
            return new OfflineWorkflowCreationParameters(countries, baseCaseDate, histoInterval, generationSampled, boundariesSampled,
                                                         initTopo, correlationThreshold, probabilityThreshold, loadFlowTransformerVoltageControlOn,
                                                         simplifiedWorkflow, mergeOptimized, attributesCountryFilter, attributesMinBaseVoltageFilter);
        }

        public static void saveParameters(OfflineWorkflowCreationParameters parameters, Path workflowDir) throws IOException {
            try (Writer writer = Files.newBufferedWriter(workflowDir.resolve(PARAMETERS_FILE_NAME), StandardCharsets.UTF_8);
                 JsonGenerator generator = JSON_FACTORY.get().createGenerator(writer)) {
                generator.useDefaultPrettyPrinter();
                generator.writeStartObject();

                generator.writeFieldName("countries");
                generator.writeStartArray();
                for (Country country : parameters.getCountries()) {
                    generator.writeString(country.toString());
                }
                generator.writeEndArray();
                generator.writeStringField("baseCaseDate", parameters.getBaseCaseDate().toString());
                generator.writeStringField("histoInterval", parameters.getHistoInterval().toString());
                generator.writeBooleanField("generationSampled", parameters.isGenerationSampled());
                generator.writeBooleanField("boundariesSampled", parameters.isBoundariesSampled());
                generator.writeBooleanField("initTopo", parameters.isInitTopo());
                generator.writeNumberField("correlationThreshold", parameters.getCorrelationThreshold());
                generator.writeNumberField("probabilityThreshold", parameters.getProbabilityThreshold());
                generator.writeBooleanField("loadFlowTransformerVoltageControlOn", parameters.isLoadFlowTransformerVoltageControlOn());
                generator.writeBooleanField("simplifiedWorkflow", parameters.isSimplifiedWorkflow());
                generator.writeBooleanField("mergeOptimized", parameters.isMergeOptimized());
                if (parameters.getAttributesCountryFilter() != null) {
                    generator.writeFieldName("attributesCountryFilter");
                    generator.writeStartArray();
                    for (Country country : parameters.getAttributesCountryFilter()) {
                        generator.writeString(country.toString());
                    }
                    generator.writeEndArray();
                }
                generator.writeNumberField("attributesMinBaseVoltageFilter", parameters.getAttributesMinBaseVoltageFilter());
                generator.writeEndObject();
            }
        }

        private static PersistenceContext load(Path workflowDir, MemoryMappedFileFactory memoryMappedFileFactory, MMapOfflineDbConfig config) throws IOException {
            OfflineWorkflowCreationParameters parameters = readParameters(workflowDir);
            return new PersistenceContext(workflowDir, parameters, memoryMappedFileFactory, config);
        }

        private PersistenceContext(Path workflowDir, OfflineWorkflowCreationParameters parameters, MemoryMappedFileFactory memoryMappedFileFactory, MMapOfflineDbConfig config) throws IOException {
            this.workflowDir = workflowDir;
            this.parameters = parameters;
            securityIndexesXmlWriter = new CsvWriter(workflowDir.resolve(SECURITY_INDEXES_XML_FILE_NAME), true, CSV_SEPARATOR);
            table = new OfflineDbTable(workflowDir, OfflineDbTableDescription.load(workflowDir, config), memoryMappedFileFactory);
        }

        private Path getWorkflowDir() {
            return workflowDir;
        }

        private String getWorkflowId() {
            try {
                return URLDecoder.decode(workflowDir.getFileName().toString(), StandardCharsets.UTF_8.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private OfflineWorkflowCreationParameters getParameters() {
            return parameters;
        }

        public OfflineDbTable getTable() {
            return table;
        }

        private void save() throws IOException {
            saveParameters(parameters, workflowDir);
        }

        @Override
        public void close() throws IOException {
            try {
                table.close();
            } catch (IOException e) {
                LOGGER.error(e.toString(), e);
            }
            try {
                securityIndexesXmlWriter.close();
            } catch (IOException e) {
                LOGGER.error(e.toString(), e);
            }
        }

    }

    private final MMapOfflineDbConfig config;

    private final String dbName;

    private final MemoryMappedFileFactory memoryMappedFileFactory;

    private final Map<String, PersistenceContext> contexts = new HashMap<>();

    private final Lock contextsLock = new ReentrantLock();

    public MMapOfflineDb(MMapOfflineDbConfig config, String dbName) throws IOException {
        this(config, dbName, MemoryMappedFileImpl::new);
    }

    public MMapOfflineDb(MMapOfflineDbConfig config, String dbName, MemoryMappedFileFactory memoryMappedFileFactory) throws IOException {
        Objects.requireNonNull(config);
        Objects.requireNonNull(dbName);
        Objects.requireNonNull(memoryMappedFileFactory);
        this.config = config;
        this.dbName = dbName;
        this.memoryMappedFileFactory = memoryMappedFileFactory;
        Path dbDir = config.getDirectory().resolve(dbName);
        Files.createDirectories(dbDir);
        try (Stream<Path> stream = Files.list(dbDir)) {
            stream.filter(Files::isDirectory).forEach(workflowDir -> {
                try {
                    PersistenceContext context = PersistenceContext.load(workflowDir, memoryMappedFileFactory, config);
                    contexts.put(context.getWorkflowId(), context);
                } catch (Exception e) {
                    LOGGER.error(e.toString(), e);
                }
            });
        }
    }

    private PersistenceContext getContext(String workflowId) {
        contextsLock.lock();
        try {
            PersistenceContext context = contexts.get(workflowId);
            if (context == null) {
                throw new RuntimeException("Context not found for workflow " + workflowId);
            }
            return context;
        } finally {
            contextsLock.unlock();
        }
    }

    private Path getWorkflowDir(String workflowId) {
        try {
            return config.getDirectory().resolve(dbName).resolve(URLEncoder.encode(workflowId, StandardCharsets.UTF_8.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> listWorkflows() {
        contextsLock.lock();
        try {
            return contexts.values().stream().map(PersistenceContext::getWorkflowId).collect(Collectors.toList());
        } finally {
            contextsLock.unlock();
        }
    }

    private boolean workflowExists(String workflowId) {
        contextsLock.lock();
        try {
            return contexts.containsKey(workflowId);
        } finally {
            contextsLock.unlock();
        }
    }

    private String generateWorkflowId() {
        String workflowId = null;
        int i = 0;
        while (i < Integer.MAX_VALUE && workflowExists(workflowId = DEFAULT_WORKFLOW_ID_PREFIX + i++)) {
        }
        return workflowId;
    }

    @Override
    public String createWorkflow(String workflowId, OfflineWorkflowCreationParameters parameters) {
        String nonNullworkflowId = workflowId;
        if (nonNullworkflowId == null) {
            nonNullworkflowId = generateWorkflowId();
        } else {
            if (workflowExists(nonNullworkflowId)) {
                throw new IllegalArgumentException("Workflow '" + nonNullworkflowId + "' already exists");
            }
        }
        try {
            Path workflowDir = getWorkflowDir(nonNullworkflowId);
            Files.createDirectories(workflowDir);
            PersistenceContext context = new PersistenceContext(workflowDir, parameters, memoryMappedFileFactory, config);
            context.save();
            contextsLock.lock();
            try {
                contexts.put(nonNullworkflowId, context);
            } finally {
                contextsLock.unlock();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return nonNullworkflowId;
    }

    @Override
    public OfflineWorkflowCreationParameters getParameters(String workflowId) {
        return getContext(workflowId).getParameters();
    }

    @Override
    public void deleteWorkflow(String workflowId) {
        // pas besoin de verouiller car jamais appele en cours d'execution
        if (contexts.remove(workflowId) == null) {
            throw new RuntimeException("Context not found for workflow " + workflowId);
        }
        Path workflowDir = getWorkflowDir(workflowId);
        try {
            FileUtil.removeDir(workflowDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int createSample(String workflowId) {
        return getContext(workflowId).getTable().getSampleCount().nextValue();
    }

    @Override
    public void storeState(String workflowId, int sampleId, Network network, Set<Country> countryFilter) {
        try {
            PersistenceContext context = getContext(workflowId);
            Map<HistoDbNetworkAttributeId, Float> values = IIDM2DB.extractCimValues(network, new IIDM2DB.Config(null, false, true, countryFilter)).getSingleValueMap().entrySet().stream()
                    .filter(e -> e.getKey() instanceof HistoDbNetworkAttributeId
                                 && ATTRIBUTE_FILTER.apply((HistoDbNetworkAttributeId) e.getKey())
                                 && e.getValue() != null)
                    .collect(Collectors.toMap(e -> (HistoDbNetworkAttributeId) e.getKey(), e -> ((Number) e.getValue()).floatValue()));
            context.getTable().writeNetworkAttributesValue(sampleId, values);
            context.getTable().getDescription().saveIfChanged(context.getWorkflowDir());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void storeTaskStatus(String workflowId, int sampleId, OfflineTaskType taskType, OfflineTaskStatus taskStatus, String taskFailureReason) {
        try {
            PersistenceContext context = getContext(workflowId);
            context.getTable().writeTaskStatus(sampleId, taskType, taskStatus);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void storeSecurityIndexes(String workflowId, int sampleId, Collection<SecurityIndex> securityIndexes) {
        try {
            PersistenceContext context = getContext(workflowId);
            context.getTable().writeSecurityIndexes(sampleId, securityIndexes);
            context.getTable().getDescription().saveIfChanged(context.getWorkflowDir());
            for (SecurityIndex index : securityIndexes) {
                context.securityIndexesXmlWriter.writeLine(Integer.toString(sampleId),
                                                           Integer.toString(context.getTable().getDescription().getColumnIndex(index.getId())),
                                                           index.toXml().replace("[\n\r]", "")); // to remove carriage return from the xml
            }
            context.securityIndexesXmlWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getSampleCount(String workflowId) {
        return getContext(workflowId).getTable().getSampleCount().getValue();
    }

    @Override
    public Collection<SecurityIndexId> getSecurityIndexIds(String workflowId) {
        return getContext(workflowId).getTable().getDescription().getSecurityIndexIds();
    }

    @Override
    public Map<Integer, SecurityIndex> getSecurityIndexes(String workflowId, SecurityIndexId securityIndexId) {
        PersistenceContext context = getContext(workflowId);
        int securityIndexNum = context.getTable().getDescription().getColumnIndex(securityIndexId);
        Map<Integer, SecurityIndex> securityIndexes = new TreeMap<>();
        try {
            try (BufferedReader securityIndexesXmlReader = Files.newBufferedReader(context.getWorkflowDir().resolve(SECURITY_INDEXES_XML_FILE_NAME), StandardCharsets.UTF_8)) {
                String line;
                while ((line = securityIndexesXmlReader.readLine()) != null) {
                    String[] tokens = line.split(Character.toString(CSV_SEPARATOR));
                    int sampleId = Integer.parseInt(tokens[0]);
                    int securityIndexNum2 = Integer.parseInt(tokens[1]);
                    if (securityIndexNum2 == securityIndexNum) {
                        String xml = tokens[2];
                        try (StringReader reader = new StringReader(xml)) {
                            SecurityIndex securityIndex = SecurityIndexParser.fromXml(securityIndexId.getContingencyId(), reader).get(0);
                            securityIndexes.put(sampleId, securityIndex);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return securityIndexes;
    }

    @Override
    public SecurityIndexSynthesis getSecurityIndexesSynthesis(String workflowId) {
        PersistenceContext context = getContext(workflowId);
        SecurityIndexSynthesis synthesis = new SecurityIndexSynthesis();
        try {
            Map<SecurityIndexId, Boolean> securityIndexIds = new LinkedHashMap<>(); // to keep right order !!!
            for (SecurityIndexId securityIndexId : context.getTable().getDescription().getSecurityIndexIds()) {
                securityIndexIds.put(securityIndexId, null);
            }
            for (int sampleId = 0; sampleId < context.getTable().getSampleCount().getValue(); sampleId++) {
                context.getTable().getSecurityIndexesOk(sampleId, securityIndexIds);
                for (Map.Entry<SecurityIndexId, Boolean> e : securityIndexIds.entrySet()) {
                    SecurityIndexId securityIndexId = e.getKey();
                    Boolean ok = e.getValue();
                    if (ok != null) {
                        synthesis.addSecurityIndex(securityIndexId, ok);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return synthesis;
    }

    interface SampleHandler {

        void onSample(int sampleId, Map<OfflineTaskType, OfflineTaskStatus> tasksStatus, Map<SecurityIndexId, Boolean> securityIndexesOk, Map<HistoDbNetworkAttributeId, Float> networkAttributesValue);

    }

    public void read(String workflowId, Predicate<HistoDbNetworkAttributeId> filter, boolean keepAllSamples, int startSample, int maxSamples, SampleHandler handler) {
        PersistenceContext context = getContext(workflowId);

        int sampleCount = maxSamples != -1 && (context.getTable().getSampleCount().getValue() - startSample) > maxSamples
                ? startSample + maxSamples : context.getTable().getSampleCount().getValue();
        if (startSample >= sampleCount) {
            throw new RuntimeException("startSample >= sampleCount");
        }

        try {
            Map<OfflineTaskType, OfflineTaskStatus> tasksStatus = new EnumMap<>(OfflineTaskType.class);
            for (OfflineTaskType taskType : OfflineTaskType.values()) {
                tasksStatus.put(taskType, null);
            }

            Map<SecurityIndexId, Boolean> securityIndexesOk = new LinkedHashMap<>(); // to keep right order !!!
            for (int i = 0 ; i < context.getTable().getDescription().getSecurityIndexesCount(); i++) {
                SecurityIndexId securityIndexId = context.getTable().getDescription().getSecurityIndexId(i);
                securityIndexesOk.put(securityIndexId, null);
            }

            Map<HistoDbNetworkAttributeId, Float> networkAttributesValue = new LinkedHashMap<>(); // to keep right order !!!
            Collection<HistoDbNetworkAttributeId> networkAttributeIds = context.getTable().getDescription().getNetworkAttributeIds();
            for (HistoDbNetworkAttributeId networkAttributeId : networkAttributeIds) {
                if (filter == null || filter.apply(networkAttributeId)) {
                    networkAttributesValue.put(networkAttributeId, Float.NaN);
                }
            }

            if (networkAttributesValue.size() != networkAttributeIds.size()) {
                LOGGER.info("{}/{} of the attributes are exported", networkAttributesValue.size(), networkAttributeIds.size());
            }

            // write csv rows
            for (int sampleId = startSample; sampleId < sampleCount; sampleId++) {
                context.getTable().getTasksStatus(sampleId, tasksStatus);
                if (keepAllSamples || OfflineTaskStatus.SUCCEED.equals(tasksStatus.get(OfflineTaskType.IMPACT_ANALYSIS))) {
                    context.getTable().getSecurityIndexesOk(sampleId, securityIndexesOk);
                    context.getTable().getNetworkAttributesValue(sampleId, networkAttributesValue);
                    handler.onSample(sampleId, tasksStatus, securityIndexesOk, networkAttributesValue);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void exportCsv(String workflowId, Writer writer, char delimiter, Predicate<HistoDbNetworkAttributeId> filter,
                          boolean addSampleColumn, boolean keepAllSamples, boolean addHeader, int startSample, int maxSample) {
        final boolean[] headerDone = new boolean[1];
        headerDone[0] = false;
        read(workflowId, filter, keepAllSamples, startSample, maxSample, (sampleId, tasksStatus, securityIndexesOk, networkAttributesValue) -> {
            try {
                if (addHeader && !headerDone[0]) {
                    headerDone[0] = true;

                    // write csv header
                    if (addSampleColumn) {
                        writer.append("sample").append(delimiter);
                    }

                    for (OfflineTaskType taskType : tasksStatus.keySet()) {
                        writer.append(taskType.getHistoDbName());
                        writer.append(delimiter);
                    }

                    for (SecurityIndexId securityIndexId : securityIndexesOk.keySet()) {
                        writer.append(securityIndexId.toString());
                        writer.append(delimiter);
                    }

                    for (HistoDbNetworkAttributeId networkAttributeId : networkAttributesValue.keySet()) {
                        writer.append(networkAttributeId.toString());
                        writer.append(delimiter);
                    }

                    writer.append("\n");
                }

                if (addSampleColumn) {
                    writer.append(Integer.toString(sampleId))
                            .append(delimiter);
                }

                for (OfflineTaskStatus taskStatus : tasksStatus.values()) {
                    if (taskStatus != null) {
                        writer.append(taskStatus == OfflineTaskStatus.SUCCEED ? "OK" : "NOK");
                    }
                    writer.append(delimiter);
                }

                for (Boolean ok : securityIndexesOk.values()) {
                    if (ok != null) {
                        writer.append(ok.toString());
                    }
                    writer.append(delimiter);
                }

                for (Map.Entry<HistoDbNetworkAttributeId, Float> e : networkAttributesValue.entrySet()) {
                    HistoDbNetworkAttributeId attributeId = e.getKey();
                    float value = e.getValue();
                    if (!Float.isNaN(value)) {
                        writer.append(Float.toString(value));
                    } else {
                        if (CHECK_MISSING_VALUES) {
                            throw new RuntimeException("Missing value for attribute "
                                    + attributeId + " and sample " + sampleId);
                        }
                    }
                    writer.append(delimiter);
                }

                writer.append("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void exportCsv(String workflowId, Writer writer, OfflineDbCsvExportConfig config) {
        Objects.requireNonNull(config);
        Predicate<HistoDbNetworkAttributeId> pred = null;
        switch (config.getFilter()) {
            case ALL:
                break;
            case BRANCHES:
                pred = BRANCH_ATTRIBUTE_FILTER;
                break;
            case ACTIVE_POWER:
                pred = ACTIVE_POWER_ATTRIBUTE_FILTER;
                break;
            default:
                throw new AssertionError();
        }
        exportCsv(workflowId, writer, config.getDelimiter(), pred, config.isAddSampleColumn(), config.isKeepAllSamples(),
                config.isAddHeader(), config.getStartSample(), config.getMaxSamples());
    }

    public void exportCsv(String workflowId, Writer writer, char delimiter, String regexFilter, boolean addSampleColumn,
                          boolean keepAllSamples, boolean addHeader, int startSample, int maxSamples) {
        Predicate<HistoDbNetworkAttributeId> filter = null;
        if (regexFilter != null) {
            final Pattern pattern = Pattern.compile(regexFilter);
            filter = attrId -> pattern.matcher(attrId.toString()).matches();
        }
        exportCsv(workflowId, writer, delimiter, filter, addSampleColumn, keepAllSamples, addHeader, startSample, maxSamples);
    }

    @Override
    public void close() throws IOException {
        for (PersistenceContext context : contexts.values()) {
            try {
                context.close();
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

}
