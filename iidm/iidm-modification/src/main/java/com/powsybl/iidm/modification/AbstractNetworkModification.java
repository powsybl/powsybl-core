/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.modification.topology.DefaultNamingStrategy;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public abstract class AbstractNetworkModification implements NetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNetworkModification.class);

    @Override
    public void apply(Network network) {
        apply(network, new DefaultNamingStrategy(), false, LocalComputationManager.getDefault(), ReportNode.NO_OP);
    }

    @Override
    public void apply(Network network, ComputationManager computationManager) {
        apply(network, new DefaultNamingStrategy(), false, computationManager, ReportNode.NO_OP);
    }

    @Override
    public void apply(Network network, ComputationManager computationManager, ReportNode reportNode) {
        apply(network, new DefaultNamingStrategy(), false, computationManager, reportNode);
    }

    @Override
    public void apply(Network network, ReportNode reportNode) {
        apply(network, new DefaultNamingStrategy(), false, LocalComputationManager.getDefault(), reportNode);
    }

    @Override
    public void apply(Network network, boolean throwException, ReportNode reportNode) {
        apply(network, new DefaultNamingStrategy(), throwException, LocalComputationManager.getDefault(), reportNode);
    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        apply(network, new DefaultNamingStrategy(), throwException, computationManager, reportNode);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy) {
        apply(network, namingStrategy, false, LocalComputationManager.getDefault(), ReportNode.NO_OP);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, ComputationManager computationManager) {
        apply(network, namingStrategy, false, computationManager, ReportNode.NO_OP);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, ReportNode reportNode) {
        apply(network, namingStrategy, false, computationManager, reportNode);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, ReportNode reportNode) {
        apply(network, namingStrategy, false, LocalComputationManager.getDefault(), reportNode);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ReportNode reportNode) {
        apply(network, namingStrategy, throwException, LocalComputationManager.getDefault(), reportNode);
    }

    /**
     * Utility during apply functions, logs or throw the message.
     *
     * @param throwException if true will throw {@link com.powsybl.commons.PowsyblException} with the given message
     */
    protected void logOrThrow(boolean throwException, String message) {
        if (throwException) {
            throw new PowsyblException(message);
        } else {
            LOGGER.warn("Error while applying modification : {}", message);
        }
    }
}
