/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingencyscreening.security.analysis;

import com.powsybl.commons.compress.ZipPackager;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingencyscreening.security.analysis.parameters.ContingencyScreeningSecurityAnalysisParameters;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.*;
import com.powsybl.security.results.OperatorStrategyResult;
import com.powsybl.security.results.PostContingencyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Business logic for contingency screening security analysis: first pass on all contingencies,
 * then second pass on those that did not converge or triggered an automaton, results merged.
 *
 * @author Riad Benradi {@literal <riad.benradi_externe at rte-france.com>}
 */
public class ContingencyScreeningSecurityAnalysis {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContingencyScreeningSecurityAnalysis.class);

    private final Network network;
    private final String workingVariantId;
    private final ContingenciesProvider contingenciesProvider;
    private final SecurityAnalysisRunParameters runParameters;
    private final ContingencyScreeningSecurityAnalysisParameters parameters;
    private final SecurityAnalysisProvider firstProvider;
    private final SecurityAnalysisProvider secondProvider;
    private ReportNode reportNode;
    private List<Contingency> allContingencies;

    public ContingencyScreeningSecurityAnalysis(Network network, String workingVariantId, ContingenciesProvider contingenciesProvider,
                                                SecurityAnalysisRunParameters runParameters, ContingencyScreeningSecurityAnalysisParameters parameters) {
        this(network, workingVariantId, contingenciesProvider, runParameters, parameters,
                findProvider(parameters.getFirstProviderName()),
                findProvider(parameters.getSecondProviderName()));
    }

    public ContingencyScreeningSecurityAnalysis(Network network, String workingVariantId, ContingenciesProvider contingenciesProvider,
                                                SecurityAnalysisRunParameters runParameters, ContingencyScreeningSecurityAnalysisParameters parameters,
                                                SecurityAnalysisProvider firstProvider, SecurityAnalysisProvider secondProvider) {
        this.network = network;
        this.workingVariantId = workingVariantId;
        this.contingenciesProvider = contingenciesProvider;
        this.runParameters = runParameters;
        this.parameters = parameters;
        this.firstProvider = java.util.Objects.requireNonNull(firstProvider, "First provider is required");
        this.secondProvider = java.util.Objects.requireNonNull(secondProvider, "Second provider is required");
    }

    private static SecurityAnalysisProvider findProvider(String providerName) {
        return SecurityAnalysisProvider.findAll().stream()
                .filter(p -> p.getName().equals(providerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Security analysis provider '" + providerName + "' not found by ServiceLoader."));
    }

    /**
     * Executes the full contingency screening analysis workflow.
     */
    public CompletableFuture<SecurityAnalysisReport> run() {
        reportNode = ContingencyScreeningSecurityAnalysisReports.createContingencyScreeningSecurityAnalysisReportNode(runParameters.getReportNode(), network.getId());
        LOGGER.info("Starting contingency screening security analysis");
        LOGGER.debug("First provider: {}, Second provider: {}", parameters.getFirstProviderName(), parameters.getSecondProviderName());

        // Step 1: Get all contingencies
        allContingencies = contingenciesProvider.getContingencies(network);
        ContingencyScreeningSecurityAnalysisReports.reportTotalContingencies(reportNode, allContingencies.size());

        // Step 2: Run first pass analysis
        CompletableFuture<SecurityAnalysisReport> firstAnalysisFuture = firstProvider.run(
            network, workingVariantId, contingenciesProvider, runParameters);
        ContingencyScreeningSecurityAnalysisReports.reportFirstPassStarted(reportNode, parameters.getFirstProviderName());

        // Step 3: Chain second pass analysis based on first pass results
        return firstAnalysisFuture.thenCompose(this::processFirstPassResults)
            .exceptionally(ex -> {
                LOGGER.error("Error during contingency screening security analysis", ex);
                return new SecurityAnalysisReport(SecurityAnalysisResult.empty());
            });
    }

    private CompletableFuture<SecurityAnalysisReport> processFirstPassResults(SecurityAnalysisReport firstReport) {
        LOGGER.info("First pass analysis completed");

        List<Contingency> contingenciesForSecondPass = selectContingenciesForSecondPass(
                firstReport.getResult(), allContingencies);
        ContingencyScreeningSecurityAnalysisReports.reportSecondPassRequired(reportNode, contingenciesForSecondPass.size());

        // If no contingencies need second pass analysis, return first pass results
        if (contingenciesForSecondPass.isEmpty()) {
            LOGGER.info("No contingencies require second pass analysis, returning first pass results");
            return CompletableFuture.completedFuture(firstReport);
        }

        // Run second pass analysis on filtered contingencies
        ContingencyScreeningSecurityAnalysisReports.reportSecondPassStarted(reportNode, parameters.getSecondProviderName());
        return runSecondPassAnalysis(contingenciesForSecondPass)
                .thenApply(secondReport -> mergeResults(firstReport, secondReport));
    }

    /**
     * Selects contingencies that should be re-analyzed with the second provider:
     * those that did not converge or triggered an automaton.
     */
    private List<Contingency> selectContingenciesForSecondPass(SecurityAnalysisResult firstResult,
                                                                     List<Contingency> allContingencies) {
        Map<String, Contingency> contingencyById = allContingencies.stream()
            .collect(Collectors.toMap(Contingency::getId, Function.identity()));

        return firstResult.getPostContingencyResults().stream()
            .filter(this::requiresSecondPass)
            .map(r -> contingencyById.get(r.getContingency().getId()))
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());
    }

    private boolean requiresSecondPass(PostContingencyResult result) {
        PostContingencyComputationStatus status = result.getStatus();
        boolean diverged = status != PostContingencyComputationStatus.CONVERGED && status != PostContingencyComputationStatus.NO_IMPACT;
        if (diverged) {
            LOGGER.debug("Contingency {} diverged (status: {}), scheduling second pass analysis", result.getContingency().getId(), status);
        }
        return diverged;
    }

    /**
     * Runs second pass analysis on a subset of contingencies.
     */
    private CompletableFuture<SecurityAnalysisReport> runSecondPassAnalysis(List<Contingency> contingencies) {
        LOGGER.info("Starting second pass analysis for {} contingencies", contingencies.size());

        ContingenciesProvider filteredProvider = n -> contingencies;
        return secondProvider.run(network, workingVariantId, filteredProvider, runParameters);
    }

    /**
     * Merges first and second pass analysis results.
     * For each contingency, the second pass result takes precedence over the first pass one.
     */
    private SecurityAnalysisReport mergeResults(SecurityAnalysisReport firstReport, SecurityAnalysisReport secondReport) {
        LOGGER.info("Merging first and second pass analysis results");
        SecurityAnalysisResult firstResult = firstReport.getResult();
        SecurityAnalysisResult secondResult = secondReport.getResult();

        Map<String, PostContingencyResult> secondResultsMap = secondResult.getPostContingencyResults()
                .stream()
                .collect(Collectors.toMap(r -> r.getContingency().getId(), Function.identity()));

        List<PostContingencyResult> mergedResults = firstResult.getPostContingencyResults()
                .stream()
                .map(firstResultItem -> {
                    String contingencyId = firstResultItem.getContingency().getId();
                    if (secondResultsMap.containsKey(contingencyId)) {
                        LOGGER.debug("Using second result for contingency {}", contingencyId);
                        return secondResultsMap.get(contingencyId);
                    } else {
                        LOGGER.debug("Using first result for contingency {}", contingencyId);
                        return firstResultItem;
                    }
                })
                .collect(Collectors.toList());

        List<OperatorStrategyResult> mergedOperatorStrategyResults = mergeOperatorStrategyResults(
                firstResult.getOperatorStrategyResults(),
                secondResult.getOperatorStrategyResults());

        SecurityAnalysisResult finalResult = new SecurityAnalysisResult(
                firstResult.getPreContingencyResult(),
                mergedResults,
                mergedOperatorStrategyResults);

        if (firstResult.getNetworkMetadata() != null) {
            finalResult.setNetworkMetadata(firstResult.getNetworkMetadata());
        }

        LOGGER.info("Merge complete: {} post-contingency results", mergedResults.size());
        byte[] mergedLogBytes = mergeLogBytes(firstReport, secondReport);

        return new SecurityAnalysisReport(finalResult)
                .setLogBytes(mergedLogBytes);
    }

    private byte[] mergeLogBytes(SecurityAnalysisReport firstReport, SecurityAnalysisReport secondReport) {
        java.util.Optional<byte[]> firstLogBytes = firstReport.getLogBytes();
        java.util.Optional<byte[]> secondLogBytes = secondReport.getLogBytes();

        if (firstLogBytes.isPresent() && secondLogBytes.isEmpty()) {
            return firstLogBytes.get();
        }
        if (firstLogBytes.isEmpty() && secondLogBytes.isPresent()) {
            return secondLogBytes.get();
        }

        if (firstLogBytes.isEmpty()) {
            // Both logs are empty
            return new byte[0];
        }

        Map<String, byte[]> logsByName = new java.util.HashMap<>();
        logsByName.put("first-pass-analysis.log", firstLogBytes.get());
        logsByName.put("second-pass-analysis.log", secondLogBytes.get());

        try {
            return ZipPackager.archiveBytesByNameToZipBytes(logsByName);
        } catch (Exception e) {
            LOGGER.warn("Failed to create merged log archive, returning first pass logs only", e);
            return firstLogBytes.orElse(new byte[0]);
        }
    }

    private List<OperatorStrategyResult> mergeOperatorStrategyResults(
            List<OperatorStrategyResult> firstStrategyResults,
            List<OperatorStrategyResult> secondStrategyResults) {

        Map<String, OperatorStrategyResult> firstStrategyMap = firstStrategyResults.stream()
                .collect(Collectors.toMap(r -> r.getOperatorStrategy().getId(), Function.identity()));

        Map<String, OperatorStrategyResult> secondStrategyMap = secondStrategyResults.stream()
                .collect(Collectors.toMap(r -> r.getOperatorStrategy().getId(), Function.identity()));

        Map<String, OperatorStrategyResult> mergedStrategyMap = new java.util.HashMap<>(firstStrategyMap);
        mergedStrategyMap.putAll(secondStrategyMap);

        return new java.util.ArrayList<>(mergedStrategyMap.values());
    }

}
