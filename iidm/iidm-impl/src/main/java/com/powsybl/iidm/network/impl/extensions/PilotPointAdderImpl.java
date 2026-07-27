/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.extensions.PilotPoint;
import com.powsybl.iidm.network.extensions.PilotPointAdder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PilotPointAdderImpl implements PilotPointAdder {

    private final ControlZoneAdderImpl parent;

    private List<PilotPoint.BusRef> buses = Collections.emptyList();

    private List<String> busbarSectionIds = Collections.emptyList();

    private double targetV = Double.NaN;

    PilotPointAdderImpl(ControlZoneAdderImpl parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public PilotPointAdderImpl withBuses(List<PilotPoint.BusRef> buses) {
        this.buses = Objects.requireNonNull(buses);
        return this;
    }

    @Override
    public PilotPointAdderImpl withBusbarSectionIds(List<String> busbarSectionIds) {
        this.busbarSectionIds = Objects.requireNonNull(busbarSectionIds);
        return this;
    }

    @Override
    public PilotPointAdderImpl withTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public ControlZoneAdderImpl add() {
        if (buses.isEmpty() && busbarSectionIds.isEmpty()) {
            throw new PowsyblException("Empty pilot point bus and busbar section ID list");
        }
        for (PilotPoint.BusRef bus : buses) {
            if (bus == null) {
                throw new PowsyblException("Null pilot point bus");
            }
        }
        for (String busbarSectionId : busbarSectionIds) {
            if (busbarSectionId == null) {
                throw new PowsyblException("Null pilot point busbar section ID");
            }
        }
        if (Double.isNaN(targetV)) {
            throw new PowsyblException("Invalid target voltage");
        }
        parent.setPilotPoint(new PilotPointImpl(buses, busbarSectionIds, targetV, parent.getParent().getNetwork()));
        return parent;
    }
}
