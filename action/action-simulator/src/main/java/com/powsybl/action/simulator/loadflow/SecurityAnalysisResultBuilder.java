/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.google.auto.service.AutoService;
import com.powsybl.contingency.Contingency;
import com.powsybl.security.*;
import com.powsybl.security.converter.SecurityAnalysisResultExporters;
import com.powsybl.security.json.SecurityAnalysisResultDeserializer;

import java.io.InputStream;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An implementation of result builder which builds a standard security analysis result.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(LoadFlowActionSimulatorResultBuilder.class)
public class SecurityAnalysisResultBuilder extends AbstractLoadFlowActionSimulatorResultBuilder<SecurityAnalysisResult> {

    /**
     * Name of that result builder, used to identify and retrieve it.
     */
    @Override
    public String getName() {
        return "security-analysis-result";
    }


    /**
     * Writes the output to an output stream.
     * A specific format may be requested, but does not have to be supported by all implementations.
     */
    @Override
    public void writeResult(Writer writer, String format) {
        Objects.requireNonNull(format, "Output format is required.");

        SecurityAnalysisResultExporters.export(getResult(), writer, format);
    }

    @Override
    public ResultMerger createMerger() {

        return new ResultMerger() {
            private final List<SecurityAnalysisResult> results = new ArrayList<>();

            @Override
            public void readResult(InputStream is) {
                results.add(SecurityAnalysisResultDeserializer.read(is));
            }

            @Override
            public void mergeResults() {
                setResult(SecurityAnalysisResultMerger.merge(results));
            }
        };
    }


    public LoadFlowActionSimulatorObserver createObserver() {

        return new DefaultLoadFlowActionSimulatorObserver() {

            private LimitViolationsResult preContingencyResult;

            private final Map<String, PostContingencyResult> postContingencyResults = new HashMap<>();

            private final List<String> preContingencyActions = new ArrayList<>();

            private final Map<String, List<String>> postContingencyActions = new HashMap<>();

            private boolean precontingency;

            @Override
            public void beforePreContingencyAnalysis(RunningContext runningContext) {
                precontingency = true;
                preContingencyResult = null;
            }

            @Override
            public void afterPreContingencyAnalysis() {
                precontingency = false;
                postContingencyResults.clear();
            }

            @Override
            public void loadFlowDiverged(RunningContext runningContext) {
                if (precontingency) {
                    preContingencyResult = new LimitViolationsResult(false, Collections.emptyList(), preContingencyActions);
                } else {
                    Objects.requireNonNull(runningContext.getContingency());
                    postContingencyResults.put(runningContext.getContingency().getId(), new PostContingencyResult(runningContext.getContingency(), false, Collections.emptyList(), getPostContingencyActions(runningContext.getContingency())));
                }
            }

            @Override
            public void loadFlowConverged(RunningContext runningContext, List<LimitViolation> violations) {
                if (precontingency) {
                    preContingencyResult = new LimitViolationsResult(true, violations, preContingencyActions);
                } else {
                    Objects.requireNonNull(runningContext.getContingency());
                    postContingencyResults.put(runningContext.getContingency().getId(), new PostContingencyResult(runningContext.getContingency(),
                            true,
                            violations,
                            getPostContingencyActions(runningContext.getContingency())));
                }
            }

            private List<String> getPostContingencyActions(Contingency contingency) {
                return postContingencyActions.computeIfAbsent(contingency.getId(), k -> new ArrayList<>());
            }

            @Override
            public void afterAction(RunningContext runningContext, String actionId) {
                Objects.requireNonNull(actionId);
                if (precontingency) {
                    preContingencyActions.add(actionId);
                } else {
                    Objects.requireNonNull(runningContext.getContingency());
                    getPostContingencyActions(runningContext.getContingency()).add(actionId);
                }
            }

            @Override
            public void afterPostContingencyAnalysis() {
                SecurityAnalysisResult result = new SecurityAnalysisResult(preContingencyResult,
                        postContingencyResults.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));

                setResult(result);
            }
        };
    }

}
