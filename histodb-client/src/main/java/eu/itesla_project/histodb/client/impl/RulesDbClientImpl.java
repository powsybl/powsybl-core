/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.client.impl;

import com.csvreader.CsvReader;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import eu.itesla_project.modules.histo.cache.HistoDbCache;
import eu.itesla_project.modules.rules.RuleAttributeSet;
import eu.itesla_project.modules.rules.RuleId;
import eu.itesla_project.modules.rules.RulesDbClient;
import eu.itesla_project.modules.rules.SecurityRule;
import eu.itesla_project.modules.securityindexes.SecurityIndexId;
import eu.itesla_project.modules.securityindexes.SecurityIndexType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RulesDbClientImpl implements RulesDbClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RulesDbClientImpl.class);

    private static final String RULES_PREFIX = "rules/workflow-";

    private final HistoDbConfig config;
    private final HistoDbHttpClient httpClient;

    public RulesDbClientImpl(HistoDbConfig config) {
        this(config, new HistoDbHttpClientImpl(null));
    }

    public RulesDbClientImpl(HistoDbConfig config, HistoDbCache cache) {
        this(config, new HistoDbHttpClientImpl(cache));
    }

    public RulesDbClientImpl(HistoDbConfig config, HistoDbHttpClient httpClient) {
        this.config = Objects.requireNonNull(config, "config is null");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient is null");
    }

    @Override
    public void close() throws Exception {
        httpClient.close();
    }

    @Override
    public List<String> listWorkflows() {
        List<String> ls = new ArrayList<>();
        try (InputStream is = httpClient.getHttpRequest(new HistoDbUrl(config, ".json", Collections.emptyMap()))) {
            String json = new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8);
            JSONObject jsonObj = JSONObject.fromObject(json);
            for (Object name : jsonObj.names()) {
                if (((String) name).startsWith(RULES_PREFIX)) {
                    String workflowId = ((String) name).substring(RULES_PREFIX.length());
                    ls.add(workflowId);
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return ls;
    }

    @Override
    public void updateRule(SecurityRule rule) {
        if (!(rule instanceof JsonSecurityRule)) {
            throw new RuntimeException("Imcompatible SecurityRule implementation");
        }

        String path = RULES_PREFIX + rule.getWorkflowId() + "/itesla/rules/" + rule.getId().getAttributeSet()
                + "/" + rule.getId().getSecurityIndexId().getContingencyId() + "/" + rule.getId().getSecurityIndexId().getSecurityIndexType().getLabel()
                + ".json";

        try {
            httpClient.postHttpRequest(new HistoDbUrl(config, path, Collections.emptyMap()), ((JsonSecurityRule) rule).toJSON().toString().getBytes("UTF-8"))
                    .close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException("Failed to encode rule", e);
        }
    }

    @Override
    public List<SecurityRule> getRules(String workflowId, RuleAttributeSet attributeSet, String contingencyId, SecurityIndexType securityIndexType) {
        Objects.requireNonNull(workflowId);
        Objects.requireNonNull(attributeSet);
        StringBuilder path = new StringBuilder();
        path.append(RULES_PREFIX).append(workflowId)
                .append("/itesla/rules/").append(attributeSet);
        if (contingencyId != null) path.append("/").append(contingencyId);
        if (securityIndexType != null) path.append("/").append(securityIndexType.getLabel());
        path.append(".json");

        List<SecurityRule> result = new ArrayList<>();

        try (InputStream is = httpClient.getHttpRequest(new HistoDbUrl(config, path.toString(), Collections.emptyMap()))) {
            String stringResult = new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8);
            for (Object jsonRule: JSONArray.fromObject(stringResult).toArray()) {
                result.add(JsonSecurityRule.fromJSON((JSONObject) jsonRule));
            }

        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException("Failed to encode rule", e);
        }

        return result;
    }

    @Override
    public Collection<RuleId> listRules(String workflowId, RuleAttributeSet attributeSet) {
        Objects.requireNonNull(workflowId);

        String path = RULES_PREFIX + workflowId + "/data.csv";
        Map<String, String> query = ImmutableMap.of("start", "0",
                                                    "count", "-1",
                                                    "headers", "true",
                                                    "cols", "algoType,contingencyId,indexType");

        try {
            CsvReader csvReader = new CsvReader(httpClient.getHttpRequest(new HistoDbUrl(config, path, query)), ',', StandardCharsets.UTF_8);
            try {
                csvReader.setSafetySwitch(false);
                csvReader.readHeaders();

                List<RuleId> ruleIds = new ArrayList<>();

                while(csvReader.readRecord()) {
                    String[] values = csvReader.getValues();
                    ruleIds.add(new RuleId(RuleAttributeSet.valueOf(values[0]),
                                new SecurityIndexId(values[1], SecurityIndexType.fromLabel(values[2]))));
                }

                return ruleIds;
            } finally {
                csvReader.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
