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

    boolean apply(Network network);

    boolean apply(Network network, boolean dryRun);

    boolean apply(Network network, ComputationManager computationManager);

    boolean apply(Network network, ComputationManager computationManager, boolean dryRun);

    boolean apply(Network network, ComputationManager computationManager, ReportNode reportNode);

    boolean apply(Network network, ComputationManager computationManager, ReportNode reportNode, boolean dryRun);

    boolean apply(Network network, ReportNode reportNode);

    boolean apply(Network network, ReportNode reportNode, boolean dryRun);

    /**
     * Applies the modification to the given network. If throwException is set to true, then in case of error, an
     * exception will be thrown. Otherwise, computation will continue but the injection will not be added to the network
     * in case of error.
     */
    boolean apply(Network network, boolean throwException, ReportNode reportNode);

    boolean apply(Network network, boolean throwException, ReportNode reportNode, boolean dryRun);

    /**
     * Applies the modification to the given network. If throwException is set to true, then in case of error, an
     * exception will be thrown. Otherwise, computation will continue but the injection will not be added to the network
     * in case of error.
     */
    boolean apply(Network network, boolean throwException, ComputationManager computationManager, ReportNode reportNode);

    boolean apply(Network network, boolean throwException, ComputationManager computationManager, ReportNode reportNode, boolean dryRun);

    boolean apply(Network network, NamingStrategy namingStrategy);

    boolean apply(Network network, NamingStrategy namingStrategy, boolean dryRun);

    boolean apply(Network network, NamingStrategy namingStrategy, ComputationManager computationManager);

    boolean apply(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, boolean dryRun);

    boolean apply(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, ReportNode reportNode);

    boolean apply(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, ReportNode reportNode, boolean dryRun);

    boolean apply(Network network, NamingStrategy namingStrategy, ReportNode reportNode);

    boolean apply(Network network, NamingStrategy namingStrategy, ReportNode reportNode, boolean dryRun);

    /**
     * Applies the modification to the given network. If throwException is set to true, then in case of error, an
     * exception will be thrown. Otherwise, computation will continue but the injection will not be added to the network
     * in case of error.
     */
    boolean apply(Network network, NamingStrategy namingStrategy, boolean throwException, ReportNode reportNode);

    /**
     * Applies the modification to the given network. If throwException is set to true, then in case of error, an
     * exception will be thrown. Otherwise, computation will continue but the injection will not be added to the network
     * in case of error.
     */
    boolean apply(Network network, NamingStrategy namingStrategy, boolean throwException, ReportNode reportNode, boolean dryRun);

    /**
     * Applies the modification to the given network. If throwException is set to true, then in case of error, an
     * exception will be thrown. Otherwise, computation will continue but the injection will not be added to the network
     * in case of error.
     */
    boolean apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode);

    /**
     * Applies the modification to the given network. If throwException is set to true, then in case of error, an
     * exception will be thrown. Otherwise, computation will continue but the injection will not be added to the network
     * in case of error.
     */
    boolean apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode, boolean dryRun);
}
