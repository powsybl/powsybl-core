/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public final class DcUtils {

    private DcUtils() {
    }

    public static DcNode checkAndGetDcNode(Network network, Validable validable, String dcNodeId, String attribute) {
        if (dcNodeId == null) {
            throw new ValidationException(validable, attribute + " is not set");
        }
        DcNode dcNode = network.getDcNode(dcNodeId);
        if (dcNode == null) {
            throw new ValidationException(validable, "DcNode '" + dcNodeId + "' not found");
        }
        return dcNode;
    }

    public static void checkSameParentNetwork(Network validableNetwork, Validable validable, DcNode dcNode) {
        if (validableNetwork != dcNode.getParentNetwork()) {
            throw new ValidationException(validable, "DC Node '" + dcNode.getId() +
                    "' is in network '" + dcNode.getParentNetwork().getId() + "' but DC Equipment is in '" + validableNetwork.getId() + "'");
        }
    }

    public static void checkSameParentNetwork(Network validableNetwork, Validable validable, DcNode dcNode1, DcNode dcNode2) {
        if (dcNode1.getParentNetwork() != dcNode2.getParentNetwork()) {
            throw new ValidationException(validable, "DC Nodes '" + dcNode1.getId() + "' and '" + dcNode2.getId() +
                    "' are in different networks '" + dcNode1.getParentNetwork().getId() + "' and '" + dcNode2.getParentNetwork().getId() + "'");
        }
        if (validableNetwork != dcNode1.getParentNetwork()) {
            throw new ValidationException(validable, "DC Nodes '" + dcNode1.getId() + "' and '" + dcNode2.getId() +
                    "' are in network '" + dcNode1.getParentNetwork().getId() + "' but DC Equipment is in '" + validableNetwork.getId() + "'");
        }
    }

}
