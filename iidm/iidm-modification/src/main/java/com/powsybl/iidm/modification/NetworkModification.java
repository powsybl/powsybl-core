/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface NetworkModification {

    default void apply(Network network) {
        apply(network, (ComputationManager) null);
    }

    void apply(Network network, ComputationManager computationManager);

    default void apply(Network network, ComputationManager computationManager, Reporter reporter) {
        apply(network);
    }

    default void apply(Network network, Reporter reporter) {
        apply(network);
    }
}
