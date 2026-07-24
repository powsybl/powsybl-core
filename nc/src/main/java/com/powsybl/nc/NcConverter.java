/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc;

import com.powsybl.action.Action;
import com.powsybl.action.ActionList;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.list.ContingencyList;
import com.powsybl.contingency.list.DefaultContingencyList;
import com.powsybl.contingency.strategy.OperatorStrategy;
import com.powsybl.contingency.strategy.OperatorStrategyList;
import com.powsybl.iidm.network.Network;
import com.powsybl.nc.reader.ContingencyReader;
import com.powsybl.nc.reader.GridStateAlterationRemedialActionReader;
import com.powsybl.nc.reader.RotatingMachineActionReader;
import com.powsybl.nc.reader.ShuntCompensatorModificationReader;
import com.powsybl.nc.reader.TopologyActionReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public final class NcConverter {
    public static final Logger LOGGER = LoggerFactory.getLogger(NcConverter.class);

    private NcConverter() {
    }

    public static NcData read(String ncArchivePath) {
        Network network = Network.read(ncArchivePath);
        return read(network, ncArchivePath);
    }

    public static NcData read(Network network, String ncArchivePath) {
        QueryManager queryManager = new QueryManager();
        queryManager.read(ncArchivePath);

        // read contingencies
        Set<Contingency> contingencies = new ContingencyReader(queryManager, network).readFromProfiles();

        // read elementary actions
        Set<Action> allActions = new HashSet<>();
        allActions.addAll(new TopologyActionReader(queryManager, network).readFromProfiles());
        allActions.addAll(new ShuntCompensatorModificationReader(queryManager, network).readFromProfiles());
        allActions.addAll(new RotatingMachineActionReader(queryManager, network).readFromProfiles());

        // read operator strategies
        Set<OperatorStrategy> operatorStrategies = new GridStateAlterationRemedialActionReader(
            queryManager, network, allActions, contingencies
        ).readFromProfiles();

        return new NcData(
            new DefaultContingencyList("NC Contingencies", contingencies.stream().toList()),
            new OperatorStrategyList(operatorStrategies.stream().toList()),
            new ActionList(allActions.stream().toList())
        );
    }

    public record NcData(ContingencyList contingencyList, OperatorStrategyList operatorStrategyList, ActionList actionList) {
    }
}
