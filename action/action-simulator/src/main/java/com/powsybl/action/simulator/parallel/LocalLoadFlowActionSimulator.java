/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.parallel;

import com.powsybl.action.dsl.ActionDb;
import com.powsybl.action.simulator.loadflow.LoadFlowActionSimulator;
import com.powsybl.action.simulator.loadflow.LoadFlowActionSimulatorConfig;
import com.powsybl.action.simulator.loadflow.LoadFlowActionSimulatorObserver;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LocalLoadFlowActionSimulator extends LoadFlowActionSimulator {

    private final Filtration filtration;

    public LocalLoadFlowActionSimulator(Network network, Filtration filtration) throws IOException {
        super(network, new LocalComputationManager());
        this.filtration = Objects.requireNonNull(filtration);
    }

    public LocalLoadFlowActionSimulator(Network network, Filtration filtration, LoadFlowActionSimulatorConfig config, LoadFlowActionSimulatorObserver... observers) throws IOException {
        super(network, new LocalComputationManager(), config, observers);
        this.filtration = Objects.requireNonNull(filtration);
    }

    public LocalLoadFlowActionSimulator(Network network, Filtration filtration, LoadFlowActionSimulatorConfig config, List<LoadFlowActionSimulatorObserver> observers) throws IOException {
        super(network, new LocalComputationManager(), config, observers);
        this.filtration = Objects.requireNonNull(filtration);
    }

    @Override
    public void start(ActionDb actionDb, String... contingencyIds) {
        start(actionDb, Arrays.asList(contingencyIds));
    }

    @Override
    public void start(ActionDb actionDb, List<String> contingencyIds) {
        List<String> filteredContingencyIds = filterContingency(contingencyIds);
        super.start(actionDb, filteredContingencyIds);
    }

    private List<String> filterContingency(List<String> contingencyIds) {
        List<String> list = new ArrayList<>();
        int size = contingencyIds.size();
        list = contingencyIds.subList(filtration.from(size), filtration.to(size));
        return list;
    }
}
