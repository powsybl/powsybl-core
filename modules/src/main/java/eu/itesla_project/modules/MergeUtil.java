/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.loadflow.api.LoadFlow;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.loadflow.api.LoadFlowParameters;
import eu.itesla_project.loadflow.api.LoadFlowResult;
import eu.itesla_project.modules.cases.CaseRepository;
import eu.itesla_project.modules.cases.CaseType;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MergeUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergeUtil.class);

    private MergeUtil() {
    }

    public static Network merge(CaseRepository caseRepository, DateTime date, CaseType caseType, Set<Country> countries, LoadFlowFactory loadFlowFactory,
                                int loadFlowPriority, MergeOptimizerFactory mergeOptimizerFactory,
                                ComputationManager computationManager) {
        return merge(caseRepository, date, caseType, countries, loadFlowFactory, loadFlowPriority, mergeOptimizerFactory, computationManager, true);
    }

    public static Network merge(CaseRepository caseRepository, DateTime date, CaseType caseType, Set<Country> countries, LoadFlowFactory loadFlowFactory,
                                int loadFlowPriority, MergeOptimizerFactory mergeOptimizerFactory,
                                ComputationManager computationManager, boolean optimize) {
        try {
            if (countries.isEmpty()) {
                throw new RuntimeException("Empty country list");
            }

            LoadFlowParameters loadFlowParameters = new LoadFlowParameters()
                    .setTransformerVoltageControlOn(false)
                    .setPhaseShifterRegulationOn(false)
                    .setVoltageInitMode(LoadFlowParameters.VoltageInitMode.DC_VALUES);

            List<Network> networksToMerge = new ArrayList<>();
            for (Country country : countries.stream().sorted().collect(Collectors.toList())) {
                List<Network> networks = caseRepository.load(date, caseType, country);
                if (networks.isEmpty()) {
                    throw new RuntimeException("Network not found for date " + date + " and country " + country);
                }
                if (optimize) {
                    for (Network network : networks) {
                        LoadFlow loadFlow = loadFlowFactory.create(network, computationManager, loadFlowPriority);
                        LoadFlowResult result = loadFlow.run(loadFlowParameters);
                        if (!result.isOk()) {
                            LOGGER.error("LF divergence on network " + network.getId());
                        }
                    }
                }
                networksToMerge.addAll(networks);
            }
            if (networksToMerge.isEmpty()) {
                throw new RuntimeException("Empty network list to merge");
            }

            // topological merge
            Network merge = networksToMerge.get(0);
            if (networksToMerge.size() > 1) {
                for (int i = 1; i < networksToMerge.size(); i++) {
                    Network otherNetwork = networksToMerge.get(i);
                    merge.merge(otherNetwork);
                }

                if (optimize) {
                    // global optimization to build a consistent case
                    MergeOptimizer optimizer = mergeOptimizerFactory.newMergeOptimizer(merge, computationManager);
                    if (!optimizer.optimize()) {
                        throw new RuntimeException("Merge optimization failed");
                    }
                }

                LoadFlow loadFlow = loadFlowFactory.create(merge, computationManager, loadFlowPriority);
                LoadFlowResult result = loadFlow.run(loadFlowParameters);
                if (!result.isOk()) {
                    throw new RuntimeException("Merge LF divergence");
                }
            }

            return merge;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
