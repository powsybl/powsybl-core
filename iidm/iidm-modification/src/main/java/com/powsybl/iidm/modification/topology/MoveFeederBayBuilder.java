/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.Terminal;

/**
 * @author  Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */

public class MoveFeederBayBuilder {
    private String connectableId = null;
    private String targetBusOrBusBarSectionId = null;
    private String targetVoltageLevelId = null;
    private Terminal terminal = null;

    public MoveFeederBay build() {
        return new MoveFeederBay(connectableId, targetBusOrBusBarSectionId, targetVoltageLevelId, terminal);
    }

    /**
     * @param connectableId the non-null ID of the connectable
     */
    public MoveFeederBayBuilder withConnectableId(String connectableId) {
        this.connectableId = connectableId;
        return this;
    }

    public MoveFeederBayBuilder withTargetBusOrBusBarSectionId(String busOrBbsId) {
        this.targetBusOrBusBarSectionId = busOrBbsId;
        return this;
    }

    public MoveFeederBayBuilder withTargetVoltageLevelId(String voltageLevelId) {
        this.targetVoltageLevelId = voltageLevelId;
        return this;
    }

    public MoveFeederBayBuilder withTerminal(Terminal terminal) {
        this.terminal = terminal;
        return this;
    }
}

