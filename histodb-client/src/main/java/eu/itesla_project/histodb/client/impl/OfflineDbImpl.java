/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.client.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import eu.itesla_project.commons.net.ConnectionParameters;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.offline.*;
import eu.itesla_project.modules.securityindexes.SecurityIndex;
import eu.itesla_project.modules.securityindexes.SecurityIndexId;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class OfflineDbImpl implements OfflineDb {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfflineDbImpl.class);

    private static final String SIMULATIONS_PREFIX = "simulations/workflow-";

    private static final String STATE_ID_PREFIX = "";// "sample-" does not work, with updates

    private static final Map<String, String> SAMPLE_FILTER = ImmutableMap.of("filters", "TASK_IMPACT_ANALYSIS:OK");

    private static final Map<String, String> BRANCH_ATTRIBUTES_FILTER = ImmutableMap.of("cols", "TASK*,(.*__TO__).*_P,(.*__TO__).*_V,(.*__TO__).*_Q,SIM_*");

    private static final Pattern ID_PATTERN = Pattern.compile("(\\d*)-(\\d*)-(\\d*)");

    private final ConnectionParameters connectParams;
    private final String storeName;

    public OfflineDbImpl(ConnectionParameters connectParams) {
        this(connectParams, "iteslasim");
    }

    public OfflineDbImpl(ConnectionParameters connectParams, String storeName) {
        this.connectParams = connectParams;
        this.storeName = storeName;
    }

    private HistoDbClientImpl getHistoClient(String workflowId) {
        return new HistoDbClientImpl(new HistoDbConfig(
                connectParams,
                null,
                storeName, SIMULATIONS_PREFIX + workflowId)
        );
    }

    //TODO parametrize histoDB storeId and datasourceId

    @Override
    public List<String> listWorkflows() {
        List<String> ls = new ArrayList<>();
        HistoDbConfig config = new HistoDbConfig(connectParams, null, storeName, null);
        try (HistoDbHttpClientImpl httpClient = new HistoDbHttpClientImpl(null)) {
            try (InputStream is = httpClient.getHttpRequest(new HistoDbUrl(config, ".json", Collections.emptyMap()))) {
                String json = new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8);
                JSONObject jsonObj = JSONObject.fromObject(json);
                for (Object name : jsonObj.names()) {
                    if (((String) name).startsWith(SIMULATIONS_PREFIX)) {
                        String workflowId = ((String) name).substring(SIMULATIONS_PREFIX.length());
                        ls.add(workflowId);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ls;
    }

    @Override
    public String createWorkflow(String workflowId, OfflineWorkflowCreationParameters parameters) {
        if (workflowId != null) {
            throw new UnsupportedOperationException("Named workflow not supported");
        }
        return parameters.getBaseCaseDate().getMillis() + "-" +
               parameters.getHistoInterval().getStartMillis() + "-" +
               parameters.getHistoInterval().getEndMillis();
    }

    @Override
    public OfflineWorkflowCreationParameters getParameters(String workflowId) {
        Matcher m = ID_PATTERN.matcher(workflowId);
        if (!m.matches()) {
            throw new IllegalArgumentException("Unexpected workflow id");
        }
        return new OfflineWorkflowCreationParameters(EnumSet.of(Country.FR),
                                                     new DateTime(Long.parseLong(m.group(1))),
                                                     new Interval(new DateTime(Long.parseLong(m.group(2))), new DateTime(Long.parseLong(m.group(3)))),
                                                     false,
                                                     false);
    }

    @Override
    public void deleteWorkflow(String workflowId) {
        try {
            try (HistoDbClientImpl histoClient = getHistoClient(workflowId)) {
                histoClient.deleteTable();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int createSample(String workflowId) {
        try {
            try (HistoDbClientImpl histoClient = getHistoClient(workflowId)) {
                String newId = histoClient.updateRecord(null, new String[] {"datetime"}, new Object[] {System.currentTimeMillis() / 1000 });
                return Integer.parseInt(newId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create record for workflow "+workflowId, e);
        }
    }

    @Override
    public void storeState(String workflowId, int sampleId, Network network, Set<Country> countryFilter) {
        try {
            try (HistoDbClientImpl histoClient = getHistoClient(workflowId)) {
                histoClient.storeStartState(STATE_ID_PREFIX + sampleId, network, countryFilter);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to store state", e);
        }
    }

    @Override
    public void storeTaskStatus(String workflowId, int sampleId, OfflineTaskType taskType, OfflineTaskStatus taskStatus, String taskFailureReason) {
        String status = OfflineTaskStatus.SUCCEED == taskStatus ? "OK" : "NOK";
        try {
            try (HistoDbClientImpl histoClient = getHistoClient(workflowId)) {
                histoClient.updateRecord(STATE_ID_PREFIX + sampleId, new String[] {"TASK_"+taskType.name()}, new String[] {status});
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to store status", e);
        }
    }

    @Override
    public void storeSecurityIndexes(String workflowId, int sampleId, Collection<SecurityIndex> securityIndexes) {
        List<String> indexKeys = new ArrayList<>();
        List<Object> indexValues = new ArrayList<>();

        //TODO first attempt at filling
        for (SecurityIndex idx : securityIndexes) {
            indexKeys.add(idx.toString());
            indexValues.add(idx.isOk());
        }

        if (indexKeys.isEmpty()) {
            LOGGER.warn("No index values for the given workflow/sample : " + workflowId + "/" + sampleId);
            return;
        }

        try {
            try (HistoDbClientImpl histoClient = getHistoClient(workflowId)) {
                histoClient.updateRecord(STATE_ID_PREFIX+ sampleId, indexKeys.toArray(new String[]{}), indexValues.toArray());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to store indexes", e);
        }
    }

    @Override
    public int getSampleCount(String workflowId) {
        try {
            return getHistoClient(workflowId).totalCount(SAMPLE_FILTER);
        } catch (IOException e) {
            throw new RuntimeException("Failed to count records", e);
        }

    }

    @Override
    public Collection<SecurityIndexId> getSecurityIndexIds(String workflowId) {
        try {
            List<SecurityIndexId> securityIndexes = new ArrayList<>();

            String[] cols = getHistoClient(workflowId).getColumns(ImmutableMap.of("cols", SecurityIndexId.SIMULATION_PREFIX + "*"));

            for (String col: cols) {
                securityIndexes.add(SecurityIndexId.fromString(col));
            }

            return securityIndexes;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract security indexes", e);
        }

    }

    @Override
    public Map<Integer, SecurityIndex> getSecurityIndexes(String workflowId, SecurityIndexId securityIndexId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SecurityIndexSynthesis getSecurityIndexesSynthesis(String workflowId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exportCsv(String workflowId, Writer writer, OfflineDbCsvExportConfig config) {
        Objects.requireNonNull(config);
        if (config.isAddSampleColumn()) {
            throw new UnsupportedOperationException("addSampleColumn is not yet implemented");
        }
        if (config.isKeepAllSamples()) {
            throw new UnsupportedOperationException("keepAllSamples is not yet implemented");
        }
        try {
            // just filter sample that have been until impact analysis and
            // branches variables and security indexes
            Map<String, String> query = new HashMap<>(SAMPLE_FILTER);
            switch (config.getFilter()) {
                case ALL:
                    break;
                case BRANCHES:
                    query.putAll(BRANCH_ATTRIBUTES_FILTER);
                    break;
                default:
                    throw new AssertionError();
            }
            try (Reader reader = getHistoClient(workflowId).openDataStream(query, config.getDelimiter())) {
                IOUtils.copy(reader, writer);
            }
        } catch (IOException|InterruptedException e) {
            throw new RuntimeException("Failed to extract CSV data", e);
        }
    }

    @Override
    public void close() throws Exception {
    }

}
