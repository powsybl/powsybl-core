/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl;

import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecondaryVoltageControlImpl extends AbstractExtension<Network> implements SecondaryVoltageControl {

    private final List<Zone> zones;

    public SecondaryVoltageControlImpl(Network network, List<Zone> zones) {
        super(network);
        this.zones = Objects.requireNonNull(zones);
    }

    @Override
    public List<Zone> getZones() {
        return zones;
    }
}
