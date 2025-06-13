/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ControlZone;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SecondaryVoltageControlImpl extends AbstractMultiVariantIdentifiableExtension<Network> implements SecondaryVoltageControl {

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

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        for (ControlZone controlZone : controlZones) {
            ((ControlZoneImpl) controlZone).extendVariantArraySize(number, sourceIndex);
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        for (ControlZone controlZone : controlZones) {
            ((ControlZoneImpl) controlZone).reduceVariantArraySize(number);
        }
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // Does nothing
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (ControlZone controlZone : controlZones) {
            ((ControlZoneImpl) controlZone).allocateVariantArrayElement(indexes, sourceIndex);
        }
    }
}
