/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.google.common.collect.Lists;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Sensitivity analysis provider
 *
 * <p>
 *     Sensitivity analysis is used to assess the impact of a small modification
 *     of a network variables on the value of network functions.
 *     This analysis can be assimilated to a partial derivative computed on a given
 *     network variant and on that variant modified based on a list of contingencies, if specified.
 * </p>
 * <p>
 *     PTDFs used in Flowbased methodology for example are sensitivity analysis
 *     results. The sensitivity variables are the GSK shift and the function reference
 *     are the monitored lines/transformers flows.
 * </p>
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public interface SensitivityAnalysisProvider extends Versionable, PlatformConfigNamedProvider {

    static List<SensitivityAnalysisProvider> findAll() {
        return Lists.newArrayList(ServiceLoader.load(SensitivityAnalysisProvider.class, SensitivityAnalysisProvider.class.getClassLoader()));
    }

    /**
     * Run a single sensitivity analysis.
     * Factors are given by a {@code factorReader} on the {@code workingVariantId} of the {@code network}
     * on pre-contingency state and after each {@link com.powsybl.contingency.Contingency} provided by
     * {@code contingencies} according to the {@code parameters}.
     *
     * @param network IIDM network on which the sensitivity analysis will be performed
     * @param workingVariantId network variant ID on which the analysis will be performed
     * @param factorReader provider of sensitivity factors to be computed
     * @param resultWriter provider of sensitivity results
     * @param contingencies list of contingencies after which sensitivity factors will be computed
     * @param variableSets list of variableSets
     * @param parameters specific sensitivity analysis parameters
     * @param computationManager a computation manager to external program execution
     * @param reportNode a reportNode for functional logs
     * @return a {@link CompletableFuture} on {@link SensitivityAnalysisResult} that gathers sensitivity factor values
     */
    CompletableFuture<Void> run(Network network,
                                String workingVariantId,
                                SensitivityFactorReader factorReader,
                                SensitivityResultWriter resultWriter,
                                List<Contingency> contingencies,
                                List<SensitivityVariableSet> variableSets,
                                SensitivityAnalysisParameters parameters,
                                ComputationManager computationManager,
                                ReportNode reportNode);

    /**
     * The serializer for implementation-specific parameters, or {@link Optional#empty()} if the implementation
     * does not have any specific parameters, or does not support JSON serialization.
     *
     * <p>Note that the actual serializer type should be {@code ExtensionJsonSerializer<SensitivityAnalysisParameters, MyParametersExtension>}
     * where {@code MyParametersExtension} is the specific parameters class.
     *
     * @return The serializer for implementation-specific parameters.
     */
    default Optional<ExtensionJsonSerializer> getSpecificParametersSerializer() {
        return Optional.empty();
    }

    /**
     * Reads implementation-specific parameters from platform config, or return {@link Optional#empty()}
     * if the implementation does not have any specific parameters, or does not support loading from config.
     *
     * @return The specific parameters read from platform config.
     */
    default Optional<Extension<SensitivityAnalysisParameters>> loadSpecificParameters(PlatformConfig config) {
        return Optional.empty();
    }

    /**
     * Reads implementation-specific parameters from a Map, or return {@link Optional#empty()}
     * if the implementation does not have any specific parameters, or does not support loading from config.
     *
     * @return The specific parameters read from Map.
     */
    default Optional<Extension<SensitivityAnalysisParameters>> loadSpecificParameters(Map<String, String> properties) {
        return Optional.empty();
    }

    /**
     * Updates implementation-specific parameters from a Map.
     */
    default void updateSpecificParameters(Extension<SensitivityAnalysisParameters> extension, Map<String, String> properties) {
    }

    /**
     *
     * @return The name of the loadflow used for the sensitivity analysis.
     */
    default Optional<String> getLoadFlowProviderName() {
        return Optional.empty();
    }

    /**
     * get the list of the specific parameters names.
     *
     * @return the list of the specific parameters names.
     */
    default List<String> getSpecificParametersNames() {
        return Collections.emptyList();
    }
}
