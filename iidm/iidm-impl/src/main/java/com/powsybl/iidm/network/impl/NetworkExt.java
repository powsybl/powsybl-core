/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.commons.ref.RefChain;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface NetworkExt extends Network {

    /**
     * Return the reference to the root network which is also used within the network elements.
     * This is used to easily update the root network of network elements when merging networks or detaching subnetworks.
     */
    RefChain<NetworkImpl> getRootNetworkRef();
}
