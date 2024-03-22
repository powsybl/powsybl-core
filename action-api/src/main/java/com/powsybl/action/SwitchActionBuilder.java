/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.commons.PowsyblException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class SwitchActionBuilder implements ActionBuilder<SwitchActionBuilder> {

    private String id;
    private String switchId;
    private Boolean open;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public SwitchAction build() {
        if (open == null) {
            throw new PowsyblException("for switch action open field can't be null");
        }
        return new SwitchAction(id, switchId, open);
    }

    @Override
    public SwitchActionBuilder withNetworkElementId(String switchId) {
        this.switchId = switchId;
        return this;
    }

    public SwitchActionBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public SwitchActionBuilder withOpen(boolean open) {
        this.open = open;
        return this;
    }

    public SwitchActionBuilder withSwitchId(String switchId) {
        this.switchId = switchId;
        return this;
    }
}
