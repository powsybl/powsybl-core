/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class RemoveFeederBayBuilder {

    private String connectableId = null;

    public RemoveFeederBay build() {
        return new RemoveFeederBay(connectableId);
    }

    /**
     * @param connectableId the non-null ID of the connectable
     */
    public RemoveFeederBayBuilder withConnectableId(String connectableId) {
        this.connectableId = connectableId;
        return this;
    }
}
