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

    /**
     * <p>Return the `Ref` object corresponding to `network`.</p>
     * <ul>
     *     <li>if `network` is `rootNetwork`, return its reference;</li>
     *     <li>if `network` is a subnetwork, return the reference of the subnetwork, which is stored in `rootNetwork`.</li>
     * </ul>
     * @param rootNetwork the root network
     * @param network the root network or one of its subnetwork
     * @return the `ref` object corresponding to `network`
     */
    public static Ref<NetworkImpl> getRef(NetworkImpl rootNetwork, Network network) {
        if (rootNetwork == network) {
            return rootNetwork.getRef();
        }
        return rootNetwork.getRef(network.getId());
    }

}
