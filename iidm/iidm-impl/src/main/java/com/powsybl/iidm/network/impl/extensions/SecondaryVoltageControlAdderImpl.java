/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ControlZone;
import com.powsybl.iidm.network.extensions.ControlZoneAdder;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControlAdder;
import com.powsybl.iidm.network.impl.NetworkImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SecondaryVoltageControlAdderImpl extends AbstractIidmExtensionAdder<Network, SecondaryVoltageControl> implements SecondaryVoltageControlAdder {

    private final List<ControlZone> controlZones = new ArrayList<>();

    public SecondaryVoltageControlAdderImpl(Network network) {
        super(network);
    }

    @Override
    public Class<? super SecondaryVoltageControl> getExtensionClass() {
        return SecondaryVoltageControl.class;
    }

    void addControlZone(ControlZoneImpl controlZone) {
        controlZones.add(Objects.requireNonNull(controlZone));
    }

    @Override
    public ControlZoneAdder newControlZone() {
        return new ControlZoneAdderImpl(this);
    }

    @Override
    protected SecondaryVoltageControlImpl createExtension(Network network) {
        if (controlZones.isEmpty()) {
            throw new PowsyblException("Empty control zone list");
        }
        var secondaryVoltageControl = new SecondaryVoltageControlImpl(network, controlZones);
        for (var controlZone : controlZones) {
            ((ControlZoneImpl) controlZone).setSecondaryVoltageControl(secondaryVoltageControl);
        }
        return secondaryVoltageControl;
    }

    NetworkImpl getNetwork() {
        return (NetworkImpl) extendable;
    }
}
