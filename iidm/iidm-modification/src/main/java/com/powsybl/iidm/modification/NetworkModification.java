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

    void apply(Network network);

    void apply(Network network, ComputationManager computationManager);

    void apply(Network network, ComputationManager computationManager, Reporter reporter);

    void apply(Network network, Reporter reporter);

    /**
     * Applies the modification to the given network. If throwException is set to true, then in case of error, an
     * exception will be thrown. Otherwise, computation will continue but the injection will not be added to the network
     * in case of error.
     */
    void apply(Network network, boolean throwException, Reporter reporter);

    /**
     * Applies the modification to the given network. If throwException is set to true, then in case of error, an
     * exception will be thrown. Otherwise, computation will continue but the injection will not be added to the network
     * in case of error.
     */
    void apply(Network network, boolean throwException, ComputationManager computationManager, Reporter reporter);
}
