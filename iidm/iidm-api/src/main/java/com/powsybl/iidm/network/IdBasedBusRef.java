/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class IdBasedBusRef extends AbstractVoltageLevelBasedBusRef {

    private final String busId;

    public IdBasedBusRef(VoltageLevel voltageLevel, String busId) {
        super(voltageLevel);
        this.busId = Objects.requireNonNull(busId);
    }

    @Override
    public Optional<Bus> resolve() {
        // TODO busId could be diff in different view???
        // TODO busView always be available??
        final VoltageLevel.BusView busView = voltageLevel.getBusView();
        if (busView != null) {
            return Optional.ofNullable(busView.getBus(busId));
        }
        return Optional.empty();
    }

    public VoltageLevel getVoltageLevel() {
        return voltageLevel;
    }

    public String getBusId() {
        return busId;
    }
}
