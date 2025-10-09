/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.extensions.PilotPointAdder;

import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PilotPointAdderImpl implements PilotPointAdder {

    private final ControlZoneAdderImpl parent;

    private List<String> busbarSectionsOrBusesIds;

    private double targetV = Double.NaN;

    PilotPointAdderImpl(ControlZoneAdderImpl parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public PilotPointAdderImpl withBusbarSectionsOrBusesIds(List<String> busbarSectionsOrBusesIds) {
        this.busbarSectionsOrBusesIds = Objects.requireNonNull(busbarSectionsOrBusesIds);
        return this;
    }

    @Override
    public PilotPointAdderImpl withTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public ControlZoneAdderImpl add() {
        if (busbarSectionsOrBusesIds.isEmpty()) {
            throw new PowsyblException("Empty busbar section or bus ID list");
        }
        for (String busbarSectionsOrBusesId : busbarSectionsOrBusesIds) {
            if (busbarSectionsOrBusesId == null) {
                throw new PowsyblException("Null busbar section or bus ID");
            }
        }
        if (Double.isNaN(targetV)) {
            throw new PowsyblException("Invalid target voltage");
        }
        parent.setPilotPoint(new PilotPointImpl(busbarSectionsOrBusesIds, targetV, parent.getParent().getNetwork()));
        return parent;
    }
}
