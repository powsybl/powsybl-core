/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Network;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface NetworkModification {

    void apply(Network network);

    boolean apply(Network network, boolean dryRun);

    void apply(Network network, ComputationManager computationManager);

    boolean apply(Network network, ComputationManager computationManager, boolean dryRun);

    void apply(Network network, ComputationManager computationManager, ReportNode reportNode);

    boolean apply(Network network, ComputationManager computationManager, ReportNode reportNode, boolean dryRun);

    void apply(Network network, ReportNode reportNode);

    boolean apply(Network network, ReportNode reportNode, boolean dryRun);

    /**
     * Applies the modification to the given network. If <code>throwException</code> is set to <code>true</code>, then in case of error, an
     * exception will be thrown. Otherwise, computation will continue but the injection will not be added to the network
     * in case of error.
     */
    void apply(Network network, boolean throwException, ReportNode reportNode);

    boolean apply(Network network, boolean throwException, ReportNode reportNode, boolean dryRun);

    /**
     * Applies the modification to the given network. If <code>throwException</code> is set to <code>true</code>, then in case of error, an
     * exception will be thrown. Otherwise, computation will continue but the injection will not be added to the network
     * in case of error.
     */
    void apply(Network network, boolean throwException, ComputationManager computationManager, ReportNode reportNode);

    boolean apply(Network network, boolean throwException, ComputationManager computationManager, ReportNode reportNode, boolean dryRun);

    void apply(Network network, NamingStrategy namingStrategy);

    boolean apply(Network network, NamingStrategy namingStrategy, boolean dryRun);

    void apply(Network network, NamingStrategy namingStrategy, ComputationManager computationManager);

    boolean apply(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, boolean dryRun);

    void apply(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, ReportNode reportNode);

    boolean apply(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, ReportNode reportNode, boolean dryRun);

    void apply(Network network, NamingStrategy namingStrategy, ReportNode reportNode);

    boolean apply(Network network, NamingStrategy namingStrategy, ReportNode reportNode, boolean dryRun);

    /**
     * Applies the modification to the given network. If <code>throwException</code> is set to <code>true</code>, then in case of error, an
     * exception will be thrown. Otherwise, computation will continue but the injection will not be added to the network
     * in case of error.
     */
    void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ReportNode reportNode);

    /**
     * <p>Applies the modification to the given network. If <code>throwException</code> is set to <code>true</code>, then in case of error, an
     * exception will be thrown. Otherwise, computation will continue but the injection will not be added to the network
     * in case of error.</p>
     * <p>When <code>dryRun</code> is set to <code>true</code>, the modification is applied on a copy of the network and
     * no exception is thrown if an application error is encountered. Instead, the error is logged in <code>reportNode</code>
     * and the method returns <code>false</code>. If no application error is encountered, the method returns <code>true</code>.</p>
     */
    boolean apply(Network network, NamingStrategy namingStrategy, boolean throwException, ReportNode reportNode, boolean dryRun);

    /**
     * Applies the modification to the given network. If <code>throwException</code> is set to <code>true</code>, then in case of error, an
     * exception will be thrown. Otherwise, computation will continue but the injection will not be added to the network
     * in case of error.
     */
    void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode);

    /**
     * <p>Applies the modification to the given network. If <code>throwException</code> is set to <code>true</code>, then in case of error, an
     * exception will be thrown. Otherwise, computation will continue but the injection will not be added to the network
     * in case of error.</p>
     * <p>When <code>dryRun</code> is set to <code>true</code>, the modification is applied on a copy of the network and
     * no exception is thrown if an application error is encountered. Instead, the error is logged in <code>reportNode</code>
     * and the method returns <code>false</code>. If no application error is encountered, the method returns <code>true</code>.</p>
     */
    boolean apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode, boolean dryRun);

    /**
     * States if the network modification would change the current state of the network. It has no impact on the network.
     * @param network Network that serves as reference for the impact.
     * @return True if the network modification would have an impact on the network.
     */
    NetworkModificationImpact hasImpactOnNetwork(final Network network);
}
