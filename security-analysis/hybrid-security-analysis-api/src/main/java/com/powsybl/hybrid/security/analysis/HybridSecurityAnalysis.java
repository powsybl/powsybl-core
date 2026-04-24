package com.powsybl.hybrid.security.analysis;

import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.hybrid.security.analysis.parameters.HybridModeParametersExtension;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.*;
import com.powsybl.security.results.PostContingencyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Business logic for hybrid-mode security analysis: first pass on all contingencies,
 * then second pass on those that did not converge or triggered an automaton, results merged.
 *
 * @author Riad Benradi {@literal <riad.benradi at rte-france.com>}
 */
public class HybridSecurityAnalysis {
    private static final Logger LOGGER = LoggerFactory.getLogger(HybridSecurityAnalysis.class);

    private final Network network;
    private final String workingVariantId;
    private final ContingenciesProvider contingenciesProvider;
    private final SecurityAnalysisRunParameters runParameters;
    private final HybridModeParametersExtension extension;
    private final SecurityAnalysisProvider firstProvider;
    private final SecurityAnalysisProvider secondProvider;

    public HybridSecurityAnalysis(Network network, String workingVariantId, ContingenciesProvider contingenciesProvider,
                                  SecurityAnalysisRunParameters runParameters, HybridModeParametersExtension extension) {
        this(network, workingVariantId, contingenciesProvider, runParameters, extension,
                findProvider(extension.getFirstProviderName()),
                findProvider(extension.getSecondProviderName()));
    }

    public HybridSecurityAnalysis(Network network, String workingVariantId, ContingenciesProvider contingenciesProvider,
                                  SecurityAnalysisRunParameters runParameters, HybridModeParametersExtension extension,
                                  SecurityAnalysisProvider firstProvider, SecurityAnalysisProvider secondProvider) {
        this.network = network;
        this.workingVariantId = workingVariantId;
        this.contingenciesProvider = contingenciesProvider;
        this.runParameters = runParameters;
        this.extension = extension;
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
     * Executes the full hybrid-mode analysis workflow.
     */
    public CompletableFuture<SecurityAnalysisReport> run() {
        LOGGER.info("Starting hybrid-mode security analysis");
        LOGGER.debug("First provider: {}, Second provider: {}", extension.getFirstProviderName(), extension.getSecondProviderName());

        // Step 1: Get all contingencies
        List<Contingency> allContingencies = contingenciesProvider.getContingencies(network);
        LOGGER.info("Total contingencies to analyze: {}", allContingencies.size());

        // Step 2: Run first pass analysis
        CompletableFuture<SecurityAnalysisReport> firstAnalysisFuture = firstProvider.run(
            network, workingVariantId, contingenciesProvider, runParameters);

        // Step 3: Chain second pass analysis based on first pass results
        return firstAnalysisFuture.thenCompose(firstReport -> {
            LOGGER.info("First pass analysis completed");

            List<Contingency> contingenciesForSecondPass = selectContingenciesForSecondPass(
                firstReport.getResult(), allContingencies);

            LOGGER.info("Contingencies requiring second pass analysis: {}", contingenciesForSecondPass.size());

            // If no contingencies need second pass analysis, return first pass results
            if (contingenciesForSecondPass.isEmpty()) {
                LOGGER.info("No contingencies require second pass analysis, returning first pass results");
                return CompletableFuture.completedFuture(firstReport);
            }

            // Run second pass analysis on filtered contingencies
            return runSecondPassAnalysis(contingenciesForSecondPass)
                .thenApply(secondReport -> mergeResults(firstReport, secondReport));
        }).exceptionally(ex -> {
            LOGGER.error("Error during hybrid-mode security analysis", ex);
            return new SecurityAnalysisReport(SecurityAnalysisResult.empty());
        });
    }

    /**
     * Selects contingencies that should be re-analyzed with the second provider:
     * those that did not converge or triggered an automaton.
     */
    private List<Contingency> selectContingenciesForSecondPass(SecurityAnalysisResult firstResult,
                                                                     List<Contingency> allContingencies) {
        Map<String, Contingency> contingencyById = allContingencies.stream()
            .collect(Collectors.toMap(Contingency::getId, c -> c));

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
            .collect(Collectors.toMap(r -> r.getContingency().getId(), r -> r));

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

        SecurityAnalysisResult finalResult = new SecurityAnalysisResult(
            firstResult.getPreContingencyResult(),
            mergedResults,
            firstResult.getOperatorStrategyResults());

        if (firstResult.getNetworkMetadata() != null) {
            finalResult.setNetworkMetadata(firstResult.getNetworkMetadata());
        }

        LOGGER.info("Merge complete: {} post-contingency results", mergedResults.size());
        return new SecurityAnalysisReport(finalResult)
            .setLogBytes(firstReport.getLogBytes().orElse(null));
    }

}
