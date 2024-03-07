/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;

/**
 *
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface ImportPostProcessor {

    String getName();

    default void process(Network network, ComputationManager computationManager) throws Exception {
        process(network, computationManager, ReportNode.NO_OP);
    }

    default void process(Network network, ComputationManager computationManager, ReportNode reportNode) throws Exception {
        process(network, computationManager);
    }

}
