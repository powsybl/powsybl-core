/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.impl.util.Ref;

/**
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
public final class NetworkUtil {
    private NetworkUtil() {
    }

    public static Ref<NetworkImpl> getRef(NetworkImpl network, Network parentNetwork) {
        if (network == parentNetwork) {
            return network.getRef();
        }
        return network.getRef(parentNetwork.getId());
    }

}
