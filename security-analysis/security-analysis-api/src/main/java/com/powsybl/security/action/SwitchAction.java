/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.action;

import java.util.Objects;

/**
 *
 * an action activating a switch
 *
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class SwitchAction extends AbstractAction {

    public static final String NAME = "SWITCH";

    private final String switchId;
    private final boolean open;

    public SwitchAction(String id, String switchId, boolean open) {
        super(id);
        this.switchId = Objects.requireNonNull(switchId);
        this.open = open;
    }

    @Override
    public String getType() {
        return NAME;
    }

    public String getSwitchId() {
        return switchId;
    }

    public boolean isOpen() {
        return open;
    }
}
