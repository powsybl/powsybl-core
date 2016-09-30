/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.pclfsim;

import com.google.common.collect.ImmutableMap;
import eu.itesla_project.commons.Version;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.security.LimitViolation;
import eu.itesla_project.security.LimitViolationFilter;
import eu.itesla_project.security.Security;
import eu.itesla_project.simulation.SimulationParameters;
import eu.itesla_project.simulation.Stabilization;
import eu.itesla_project.simulation.StabilizationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author Quinary <itesla@quinary.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class PostContLoadFlowSimStabilization implements Stabilization, PostContLoadFlowSimConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostContLoadFlowSimStabilization.class);

    private final Network network;

    private final PostContLoadFlowSimConfig config;

    private final LimitViolationFilter baseVoltageFilter;

    PostContLoadFlowSimStabilization(Network network, PostContLoadFlowSimConfig config) {
        this.network = Objects.requireNonNull(network);
        this.config = config;
        baseVoltageFilter = new LimitViolationFilter(null, config.getMinBaseVoltageFilter());
    }

    @Override
    public String getName() {
        return PRODUCT_NAME;
    }

    @Override
    public String getVersion() {
        return ImmutableMap.builder().put("postContLoadFlowSimVersion", VERSION)
                                     .putAll(Version.VERSION.toMap())
                                     .build()
                                     .toString();
    }

    @Override
    public void init(SimulationParameters parameters, Map<String, Object> context) throws Exception {
    }

    @Override
    public StabilizationResult run() {
        String baseStateId = network.getStateManager().getWorkingStateId();

        List<LimitViolation> violations = baseVoltageFilter.apply(Security.checkLimits(network, config.getCurrentLimitType(),
                config.getMaxAcceptableDuration(), config.getLimitReduction()));
        String report = Security.printLimitsViolations(violations, CURRENT_FILTER);
        if (report != null) {
            LOGGER.warn("Constraints after stabilization for {}:\n{}", baseStateId, report);
        }

        PostContLoadFlowSimState state = new PostContLoadFlowSimState(baseStateId, violations);
        return new PostContLoadFlowSimStabilizationResult(state);
    }

    @Override
    public CompletableFuture<StabilizationResult> runAsync(String workingStateId) {
        return CompletableFuture.supplyAsync(() -> {
            network.getStateManager().setWorkingState(workingStateId);
            return run();
        });
    }

}
