/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.ThreeSides;

/**
 *
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class TerminalsConnectionActionBuilder implements ActionBuilder<TerminalsConnectionActionBuilder> {

    private String id;
    private String elementId;
    private ThreeSides side;
    private Boolean open;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public TerminalsConnectionAction build() {
        if (open == null) {
            throw new PowsyblException("for terminal connection action open field can't be null");
        }
        return new TerminalsConnectionAction(id, elementId, side, open);
    }

    @Override
    public TerminalsConnectionActionBuilder withNetworkElementId(String elementId) {
        this.elementId = elementId;
        return this;
    }

    public TerminalsConnectionActionBuilder withSide(ThreeSides side) {
        this.side = side;
        return this;
    }

    public TerminalsConnectionActionBuilder withOpen(boolean open) {
        this.open = open;
        return this;
    }

    public TerminalsConnectionActionBuilder withId(String id) {
        this.id = id;
        return this;
    }
}
