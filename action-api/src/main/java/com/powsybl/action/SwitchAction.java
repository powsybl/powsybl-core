/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.modification.CloseSwitch;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.OpenSwitch;

import java.util.Objects;

/**
 * An action opening or closing a switch.
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
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

    /**
     * If {@code true}, applying the action will open the switch,
     * else it will close it.
     */
    public boolean isOpen() {
        return open;
    }

    public NetworkModification toModification() {
        if (isOpen()) {
            return new OpenSwitch(getSwitchId());
        } else {
            return new CloseSwitch(getSwitchId());
        }
    }

}
