/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl.ControlZone;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControlAdder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecondaryVoltageControlAdderImpl extends AbstractExtensionAdder<Network, SecondaryVoltageControl> implements SecondaryVoltageControlAdder {

    private final List<ControlZone> controlZones = new ArrayList<>();

    public SecondaryVoltageControlAdderImpl(Network network) {
        super(network);
    }

    @Override
    public Class<? super SecondaryVoltageControl> getExtensionClass() {
        return SecondaryVoltageControl.class;
    }

    @Override
    public SecondaryVoltageControlAdderImpl addControlZone(ControlZone controlZone) {
        controlZones.add(Objects.requireNonNull(controlZone));
        return this;
    }

    @Override
    protected SecondaryVoltageControlImpl createExtension(Network network) {
        if (controlZones.isEmpty()) {
            throw new PowsyblException("Empty control zone list");
        }
        return new SecondaryVoltageControlImpl(network, controlZones);
    }
}
