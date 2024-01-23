/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ControlZone;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SecondaryVoltageControlImpl extends AbstractExtension<Network> implements SecondaryVoltageControl {

    private final List<ControlZone> controlZones;

    SecondaryVoltageControlImpl(Network network, List<ControlZone> controlZones) {
        super(network);
        this.controlZones = Objects.requireNonNull(controlZones);
    }

    @Override
    public List<ControlZone> getControlZones() {
        return Collections.unmodifiableList(controlZones);
    }

    @Override
    public Optional<ControlZone> getControlZone(String name) {
        Objects.requireNonNull(name);
        return controlZones.stream().filter(z -> z.getName().equals(name)).findFirst();
    }
}
