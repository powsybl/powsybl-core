/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.google.common.collect.Lists;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.parameters.ConfiguredParameter;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.Networks;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;

/**
 * What a state estimation implementation provides. Implementations are discovered with {@link ServiceLoader}.
 *
 * <p>Measurements are read from the network rather than passed in, through the IIDM measurement
 * extensions carried by its equipment. Every entry point names the variant it runs against,
 * because the topology and the voltages an estimation works from are variant dependent. The
 * measurement extensions are not, so their values are shared by every variant.</p>
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public interface StateEstimationProvider extends Versionable, PlatformConfigNamedProvider {

    /** Every implementation found on the classpath. */
    static List<StateEstimationProvider> findAll() {
        return Lists.newArrayList(ServiceLoader.load(StateEstimationProvider.class, StateEstimationProvider.class.getClassLoader()));
    }

    /**
     * Which parts of variant {@code workingVariantId} of {@code network} its measurements determine.
     *
     * @param network the network
     * @param workingVariantId the identifier of the network variant to run against
     * @param runParameters the run parameters
     * @return which parts of the network its measurements determine
     */
    CompletableFuture<ObservabilityResult> analyseObservability(Network network, String workingVariantId,
                                                                StateEstimationRunParameters runParameters);

    /**
     * The state of variant {@code workingVariantId} of {@code network} that best fits its measurements.
     *
     * @param network the network
     * @param workingVariantId the identifier of the network variant to run against
     * @param runParameters the run parameters
     * @return the estimated state
     */
    CompletableFuture<StateEstimationResult> estimate(Network network, String workingVariantId,
                                                      StateEstimationRunParameters runParameters);

    /**
     * Tests {@code estimate} for measurements that disagree with it. The estimate is used as given
     * and is not recomputed. {@code observability} tells which measurements can be tested.
     *
     * @param network the network
     * @param estimate the estimate to test
     * @param observability the observability result, telling which measurements can be tested
     * @param runParameters the run parameters
     * @return the outcome of the test
     */
    CompletableFuture<BadDataResult> detectBadData(Network network, StateEstimationResult estimate,
                                                   ObservabilityResult observability,
                                                   StateEstimationRunParameters runParameters);

    /**
     * The settings this implementation adds on top of {@link StateEstimationParameters}, as an
     * extension class. An empty {@code Optional} means the implementation adds none.
     */
    default Optional<Class<? extends Extension<StateEstimationParameters>>> getSpecificParametersClass() {
        return Optional.empty();
    }

    /**
     * The serializer for this implementation's specific parameters. An empty {@code Optional} means
     * the implementation has none, or does not support JSON serialization for them.
     *
     * <p>The serializer type is {@code ExtensionJsonSerializer<StateEstimationParameters, X>}, where
     * {@code X} is the class returned by {@link #getSpecificParametersClass()}.</p>
     */
    default Optional<ExtensionJsonSerializer> getSpecificParametersSerializer() {
        return Optional.empty();
    }

    /**
     * Reads this implementation's specific parameters from the platform configuration. An empty
     * {@code Optional} means the implementation has none, or does not support loading them from
     * configuration.
     *
     * @param config the platform configuration to read from
     * @return the specific parameters read from the platform configuration
     */
    default Optional<Extension<StateEstimationParameters>> loadSpecificParameters(PlatformConfig config) {
        return Optional.empty();
    }

    /**
     * Reads this implementation's specific parameters from a map of property names to values. An
     * empty {@code Optional} means the implementation has none, or does not support loading them
     * this way.
     *
     * @param properties property names and values
     * @return the specific parameters read from the map
     */
    default Optional<Extension<StateEstimationParameters>> loadSpecificParameters(Map<String, String> properties) {
        return Optional.empty();
    }

    /**
     * Writes this implementation's specific parameters out as property names and values. Empty when
     * the implementation has no specific parameters.
     *
     * @param extension this implementation's specific parameters
     * @return the property names and values
     */
    default Map<String, String> createMapFromSpecificParameters(Extension<StateEstimationParameters> extension) {
        return Collections.emptyMap();
    }

    /**
     * Applies the properties that are present to this implementation's specific parameters, leaving
     * the others as they are.
     *
     * @param extension this implementation's specific parameters
     * @param properties property names and values
     */
    default void updateSpecificParameters(Extension<StateEstimationParameters> extension, Map<String, String> properties) {
        // nothing to update when the implementation has no specific parameters
    }

    /**
     * Applies the platform configuration to this implementation's specific parameters, leaving the
     * settings it does not mention as they are.
     *
     * @param extension this implementation's specific parameters
     * @param config the platform configuration to read from
     */
    default void updateSpecificParameters(Extension<StateEstimationParameters> extension, PlatformConfig config) {
        // nothing to update when the implementation has no specific parameters
    }

    /**
     * This implementation's specific parameters with the default values the implementation itself
     * declares, before any configuration is applied.
     */
    default List<Parameter> getRawSpecificParameters() {
        return Collections.emptyList();
    }

    /**
     * This implementation's specific parameters with the defaults from the default platform
     * configuration applied.
     */
    default List<Parameter> getSpecificParameters() {
        return getSpecificParameters(PlatformConfig.defaultConfig());
    }

    /**
     * This implementation's specific parameters with the defaults from the given platform
     * configuration applied.
     *
     * @param config the platform configuration to read from
     * @return this implementation's specific parameters with the defaults from the given platform configuration applied
     */
    default List<Parameter> getSpecificParameters(PlatformConfig config) {
        return ConfiguredParameter.load(getRawSpecificParameters(), getModuleConfig(config).orElse(null));
    }

    /**
     * The configuration module holding this implementation's specific parameters. An empty
     * {@code Optional} means the configuration does not define one.
     *
     * @param config the platform configuration to read from
     * @return the configuration module holding this implementation's specific parameters
     */
    default Optional<ModuleConfig> getModuleConfig(PlatformConfig config) {
        return config.getOptionalModuleConfig(getName() + "-state-estimation-parameters");
    }

    /**
     * Writes a result that this implementation produced back into the network, under the variant it
     * was computed against.
     *
     * <p>Every solved value on the variant is cleared first, so a partly estimated network says
     * which parts carry an estimate instead of mixing one with whatever was there before. The
     * estimated voltages then go onto the buses and the flows onto the branch terminals.</p>
     *
     * <p>This default writes what {@link StateEstimationResult} models. An implementation with more
     * to publish, such as the IIDM observability extensions or solved tap positions, overrides it
     * and calls this one first.</p>
     *
     * <p>The result is only meaningful against the topology it was computed on. Nothing here checks
     * that the topology still matches, so a caller holding an older result is responsible for
     * knowing whether it still applies.</p>
     *
     * <p>This selects {@code workingVariantId} on the calling thread and puts back whatever was
     * selected before, so two calls on one thread are not safe to interleave. A caller running
     * estimations concurrently gives each its own thread and turns on
     * {@link VariantManager#allowVariantMultiThreadAccess(boolean)}. In that mode a thread may have
     * nothing selected yet, in which case there is nothing to put back and none is left
     * selected.</p>
     *
     * @param network the network to write into
     * @param workingVariantId the identifier of the variant the result was computed against
     * @param result the result to write
     */
    default void writeResultsToNetwork(Network network, String workingVariantId, StateEstimationResult result) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(workingVariantId);
        Objects.requireNonNull(result);

        VariantManager variantManager = network.getVariantManager();
        String previousVariantId = selectedVariantId(variantManager);
        variantManager.setWorkingVariant(workingVariantId);
        try {
            Networks.unsetSolvedValues(network);
            for (StateEstimationResult.BusResult busResult : result.getBusResults()) {
                findBus(network, busResult).ifPresent(bus -> bus.setV(busResult.getV())
                        .setAngle(busResult.getAngle()));
            }
            for (StateEstimationResult.BranchResult branchResult : result.getBranchResults()) {
                writeBranchResult(network, branchResult);
            }
            for (StateEstimationResult.ThreeWindingsTransformerResult twtResult : result.getThreeWindingsTransformerResults()) {
                ThreeWindingsTransformer twt = network.getThreeWindingsTransformer(twtResult.getThreeWindingsTransformerId());
                if (twt != null) {
                    twt.getLeg1().getTerminal().setP(twtResult.getP1()).setQ(twtResult.getQ1());
                    twt.getLeg2().getTerminal().setP(twtResult.getP2()).setQ(twtResult.getQ2());
                    twt.getLeg3().getTerminal().setP(twtResult.getP3()).setQ(twtResult.getQ3());
                }
            }
            for (StateEstimationResult.InjectionResult injectionResult : result.getInjectionResults()) {
                if (network.getIdentifiable(injectionResult.getInjectionId()) instanceof Injection<?> injection) {
                    injection.getTerminal().setP(injectionResult.getP()).setQ(injectionResult.getQ());
                }
            }
        } finally {
            if (previousVariantId != null) {
                variantManager.setWorkingVariant(previousVariantId);
            }
        }
    }

    /**
     * The variant selected on this thread, or null when none is. Reading it throws under
     * {@link VariantManager#allowVariantMultiThreadAccess(boolean)} on a thread that has not
     * selected one yet, which is the case a write-back from a worker thread runs into.
     */
    private static String selectedVariantId(VariantManager variantManager) {
        return variantManager.isVariantMultiThreadAccessAllowed() ? null : variantManager.getWorkingVariantId();
    }

    /** Writes a branch result, which an unpaired boundary line is also reported as. */
    private static void writeBranchResult(Network network, StateEstimationResult.BranchResult branchResult) {
        Branch<?> branch = network.getBranch(branchResult.getBranchId());
        if (branch != null) {
            branch.getTerminal1().setP(branchResult.getP1()).setQ(branchResult.getQ1());
            branch.getTerminal2().setP(branchResult.getP2()).setQ(branchResult.getQ2());
            return;
        }
        BoundaryLine boundaryLine = network.getBoundaryLine(branchResult.getBranchId());
        if (boundaryLine != null) {
            boundaryLine.getTerminal().setP(branchResult.getP1()).setQ(branchResult.getQ1());
        }
    }

    /** The bus a result refers to, in the bus view of its voltage level. */
    private static Optional<Bus> findBus(Network network, StateEstimationResult.BusResult busResult) {
        VoltageLevel voltageLevel = network.getVoltageLevel(busResult.getVoltageLevelId());
        if (voltageLevel == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(voltageLevel.getBusView().getBus(busResult.getBusId()));
    }

    /**
     * Whether this implementation can honor the given parameters. An implementation that cannot
     * returns false instead of failing later in the run.
     *
     * @param runParameters the run parameters
     * @return true if the implementation can honor the parameters, false otherwise
     */
    default boolean checkParameters(StateEstimationRunParameters runParameters) {
        return true;
    }
}
