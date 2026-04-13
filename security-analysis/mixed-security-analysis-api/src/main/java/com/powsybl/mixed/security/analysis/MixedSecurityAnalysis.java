package com.powsybl.mixed.security.analysis;

import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.mixed.security.analysis.criteria.AnalysisSwitchCriteria;
import com.powsybl.mixed.security.analysis.criteria.SwitchDecision;
import com.powsybl.mixed.security.analysis.parameters.MixedModeParametersExtension;
import com.powsybl.security.SecurityAnalysisProvider;
import com.powsybl.security.SecurityAnalysisReport;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.SecurityAnalysisRunParameters;
import com.powsybl.security.results.PostContingencyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Business logic for mixed-mode security analysis: static pass on all contingencies,
 * then dynamic pass on those that meet the switch criteria, results merged.
 *
 * @author Riad Benradi {@literal <riad.benradi at rte-france.com>}
 */
public class MixedSecurityAnalysis {
    private static final Logger LOGGER = LoggerFactory.getLogger(MixedSecurityAnalysis.class);

    private final Network network;
    private final String workingVariantId;
    private final ContingenciesProvider contingenciesProvider;
    private final SecurityAnalysisRunParameters runParameters;
    private final MixedModeParametersExtension extension;
    private final List<SecurityAnalysisProvider> providers;

    public MixedSecurityAnalysis(Network network, String workingVariantId, ContingenciesProvider contingenciesProvider,
                                  SecurityAnalysisRunParameters runParameters, MixedModeParametersExtension extension) {
        this(network, workingVariantId, contingenciesProvider, runParameters, extension, null);
    }

    public MixedSecurityAnalysis(Network network, String workingVariantId, ContingenciesProvider contingenciesProvider,
                                  SecurityAnalysisRunParameters runParameters, MixedModeParametersExtension extension,
                                  List<SecurityAnalysisProvider> providers) {
        this.network = network;
        this.workingVariantId = workingVariantId;
        this.contingenciesProvider = contingenciesProvider;
        this.runParameters = runParameters;
        this.extension = extension;
        this.providers = providers;
    }

    /**
     * Executes the full mixed-mode analysis workflow.
     */
    public CompletableFuture<SecurityAnalysisReport> run() {
        LOGGER.info("Starting mixed-mode security analysis");
        LOGGER.debug("Static simulator: {}, Dynamic simulator: {}", extension.getStaticSimulator(), extension.getDynamicSimulator());

        // Step 1: Get all contingencies
        List<Contingency> allContingencies = contingenciesProvider.getContingencies(network);
        LOGGER.info("Total contingencies to analyze: {}", allContingencies.size());

        // Step 2: Run static analysis
        String staticProviderName = extension.getStaticSimulator();
        SecurityAnalysisProvider staticProvider = findProvider(staticProviderName);

        CompletableFuture<SecurityAnalysisReport> staticAnalysisFuture = staticProvider.run(
            network, workingVariantId, contingenciesProvider, runParameters);

        // Step 3: Chain dynamic analysis based on static results
        return staticAnalysisFuture.thenCompose(staticReport -> {
            LOGGER.info("Static analysis completed");
            // Evaluate switch criteria
            AnalysisSwitchCriteria switchCriteria = new AnalysisSwitchCriteria(extension);
            List<String> contingenciesToRunDynamic = identifyDynamicContingencies(
                staticReport.getResult(), switchCriteria);

            LOGGER.info("Contingencies requiring dynamic analysis: {}", contingenciesToRunDynamic.size());

            // If no contingencies need dynamic analysis, return static results
            if (contingenciesToRunDynamic.isEmpty()) {
                LOGGER.info("No contingencies require dynamic analysis, returning static results");
                return CompletableFuture.completedFuture(staticReport);
            }

            // Run dynamic analysis on filtered contingencies
            return runDynamicAnalysis(contingenciesToRunDynamic, allContingencies)
                .thenApply(dynamicReport -> mergeResults(staticReport, dynamicReport));
        }).exceptionally(ex -> {
            LOGGER.error("Error during mixed-mode security analysis", ex);
            return new SecurityAnalysisReport(SecurityAnalysisResult.empty());
        });
    }

    /**
     * Identifies which contingencies should be analyzed with the dynamic simulator.
     */
    private List<String> identifyDynamicContingencies(SecurityAnalysisResult staticResult,
                                                      AnalysisSwitchCriteria switchCriteria) {
        return staticResult.getPostContingencyResults().stream()
                .filter(result -> shouldRunDynamic(result, switchCriteria))
                .map(result -> result.getContingency().getId())
                .collect(Collectors.toList());
    }

    /**
     * Evaluates if a contingency result should trigger dynamic analysis.
     */
    private boolean shouldRunDynamic(PostContingencyResult result, AnalysisSwitchCriteria switchCriteria) {
        SwitchDecision decision = switchCriteria.evaluate(result);
        LOGGER.debug("Contingency {} - Switch decision: {}", result.getContingency().getId(), decision.getReason());
        return decision.shouldSwitch();
    }

    /**
     * Runs dynamic analysis on a subset of contingencies.
     * Uses the already-resolved {@code allContingencies} list to avoid a second provider call.
     */
    private CompletableFuture<SecurityAnalysisReport> runDynamicAnalysis(List<String> contingencyIds,
                                                                          List<Contingency> allContingencies) {
        LOGGER.info("Starting dynamic analysis pass for {} contingencies", contingencyIds.size());

        ContingenciesProvider filteredProvider = network ->
            allContingencies.stream()
            .filter(c -> contingencyIds.contains(c.getId()))
            .collect(Collectors.toList());

        String dynamicProviderName = extension.getDynamicSimulator();
        SecurityAnalysisProvider dynamicProvider = findProvider(dynamicProviderName);

        return dynamicProvider.run(network, workingVariantId, filteredProvider, runParameters);
    }

    /**
     * Merges static and dynamic analysis results.
     * Strategy: For each contingency, keep the result from the last (most relevant) analysis:
     * - If analyzed in dynamic pass: use dynamic result
     * - Otherwise: use static result
     */
    private SecurityAnalysisReport mergeResults(SecurityAnalysisReport staticReport, SecurityAnalysisReport dynamicReport) {
        LOGGER.info("Merging static and dynamic analysis results");
        SecurityAnalysisResult staticResult = staticReport.getResult();
        SecurityAnalysisResult dynamicResult = dynamicReport.getResult();

        // Create a map of dynamic results by contingency ID
        Map<String, PostContingencyResult> dynamicResultsMap = dynamicResult.getPostContingencyResults()
                .stream()
                .collect(Collectors.toMap(r -> r.getContingency().getId(), r -> r));

        // Merge: use dynamic result if available, otherwise use static
        List<PostContingencyResult> mergedResults = staticResult.getPostContingencyResults()
                .stream()
                .map(staticResultItem -> {
                    String contingencyId = staticResultItem.getContingency().getId();
                    if (dynamicResultsMap.containsKey(contingencyId)) {
                        LOGGER.debug("Using dynamic result for contingency {}", contingencyId);
                        return dynamicResultsMap.get(contingencyId);
                    } else {
                        LOGGER.debug("Using static result for contingency {}", contingencyId);
                        return staticResultItem;
                    }
                })
                .collect(Collectors.toList());

        // Create final result
        SecurityAnalysisResult finalResult = new SecurityAnalysisResult(
                staticResult.getPreContingencyResult(),
                mergedResults,
                staticResult.getOperatorStrategyResults());

        if (staticResult.getNetworkMetadata() != null) {
            finalResult.setNetworkMetadata(staticResult.getNetworkMetadata());
        }

        LOGGER.info("Merge complete: {} post-contingency results", mergedResults.size());
        return new SecurityAnalysisReport(finalResult)
                .setLogBytes(staticReport.getLogBytes().orElse(null));
    }

    /**
     * Finds a security analysis provider by name using ServiceLoader.
     */
    private SecurityAnalysisProvider findProvider(String providerName) {
        Map<String, SecurityAnalysisProvider> allProviders = StreamSupport.stream(ServiceLoader.load(SecurityAnalysisProvider.class).spliterator(), false)
                .collect(Collectors.toMap(SecurityAnalysisProvider::getName, p -> p));

        if (providers != null) {
            providers.forEach(p -> allProviders.put(p.getName(), p));
        }

        SecurityAnalysisProvider foundProvider = allProviders.get(providerName);

        if (foundProvider == null) {
            throw new IllegalArgumentException(
                    "Security analysis provider '" + providerName + "' not found. " +
                            "Available providers: " + String.join(", ", allProviders.keySet()));
        }
        return foundProvider;
    }
}
