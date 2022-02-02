/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.modification;

import com.powsybl.iidm.network.Network;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkModificationList implements NetworkModification {

    private final List<NetworkModification> modificationList;

    public NetworkModificationList(List<NetworkModification> modificationList) {
        this.modificationList = Objects.requireNonNull(modificationList);
    }

    public NetworkModificationList(NetworkModification... modificationList) {
        this(Arrays.asList(modificationList));
    }

    @Override
    public void apply(Network network) {
        for (NetworkModification modification : modificationList) {
            modification.apply(network);
        }
    }

}
