/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.action;

import com.powsybl.contingency.contingency.list.identifier.IdBasedNetworkElementIdentifier;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;

import java.util.Collections;
import java.util.List;

/**
 * An action opening or closing a switch.
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class SwitchAction extends AbstractAction {

    public static final String NAME = "SWITCH";
    private final boolean open;

    public SwitchAction(String id, List<NetworkElementIdentifier> switchIdentifiers, boolean open) {
        super(id, switchIdentifiers);
        this.open = open;
    }

    public SwitchAction(String id, String switchId, boolean open) {
        this(id, Collections.singletonList(new IdBasedNetworkElementIdentifier(switchId)), open);
    }

    @Override
    public String getType() {
        return NAME;
    }

    /**
     * If {@code true}, applying the action will open the switch,
     * else it will close it.
     */
    public boolean isOpen() {
        return open;
    }
}
