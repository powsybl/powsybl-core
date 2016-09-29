/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.StateManager;
import eu.itesla_project.loadflow.api.LoadFlow;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.loadflow.api.LoadFlowResult;
import eu.itesla_project.contingency.Contingency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StaticSecurityAnalysis {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticSecurityAnalysis.class);

    private final Network network;
    private final LoadFlowFactory loadFlowFactory;
    private final ComputationManager computationManager;

    public StaticSecurityAnalysis(Network network, LoadFlowFactory loadFlowFactory, ComputationManager computationManager) {
        this.network = Objects.requireNonNull(network);
        this.loadFlowFactory = Objects.requireNonNull(loadFlowFactory);
        this.computationManager = Objects.requireNonNull(computationManager);
    }

    public Map<String, List<LimitViolation>> run(List< Contingency > contingencies) throws Exception {
        LoadFlow loadFlow = loadFlowFactory.create(network, computationManager, 0);
        Map<String, List<LimitViolation>> statusPerContingency = new LinkedHashMap<>();
        for (Contingency contingency : contingencies) {
            String nm1StateId = network.getId() + "/" + contingency.getId();
            network.getStateManager().cloneState(StateManager.INITIAL_STATE_ID, nm1StateId);
            network.getStateManager().setWorkingState(nm1StateId);
            try {
                contingency.toTask().modify(network);
                List<LimitViolation> violations = null;
                LoadFlowResult result = loadFlow.run();
                if (!result.isOk()) {
                    LOGGER.error("Post contingency load flow diverged for contingency " + contingency.getId() + " on " + network.getId());
                } else {
                    violations = Security.checkLimits(network).stream()
                            .filter(violation -> violation.getLimitType() == LimitViolationType.CURRENT)
                            .collect(Collectors.toList());
                }
                statusPerContingency.put(contingency.getId(), violations);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            } finally {
                network.getStateManager().removeState(nm1StateId);
            }
        }
        return statusPerContingency;
    }

}
