/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.Network;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface NetworkReducer {

    static DefaultNetworkReducerBuilder builder() {
        return new DefaultNetworkReducerBuilder();
    }

    void reduce(Network network);
}
