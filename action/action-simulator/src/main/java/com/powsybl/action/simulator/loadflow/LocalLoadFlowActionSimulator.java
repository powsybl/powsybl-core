/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.powsybl.action.dsl.ActionDb;
import com.powsybl.computation.Partition;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Network;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
public class LocalLoadFlowActionSimulator extends LoadFlowActionSimulator {

    private final Partition partition;

    public LocalLoadFlowActionSimulator(Network network, Partition partition) throws IOException {
        super(network, new LocalComputationManager());
        this.partition = Objects.requireNonNull(partition);
    }

    public LocalLoadFlowActionSimulator(Network network, Partition partition, LoadFlowActionSimulatorConfig config, boolean applyIfSolved, LoadFlowActionSimulatorObserver... observers) throws IOException {
        super(network, new LocalComputationManager(), config, applyIfSolved, observers);
        this.partition = Objects.requireNonNull(partition);
    }

    public LocalLoadFlowActionSimulator(Network network, Partition partition, LoadFlowActionSimulatorConfig config, boolean applyIfSolved, List<LoadFlowActionSimulatorObserver> observers) throws IOException {
        super(network, new LocalComputationManager(), config, applyIfSolved, observers);
        this.partition = Objects.requireNonNull(partition);
    }

    @Override
    public void start(ActionDb actionDb, String... contingencyIds) {
        start(actionDb, Arrays.asList(contingencyIds));
    }

    @Override
    public void start(ActionDb actionDb, List<String> contingencyIds) {
        List<String> partContingencyIds = getPartOfContingency(contingencyIds);
        super.start(actionDb, partContingencyIds);
    }

    private List<String> getPartOfContingency(List<String> contingencyIds) {
        int size = contingencyIds.size();
        return contingencyIds.subList(partition.startIndex(size), partition.endIndex(size));
    }
}
