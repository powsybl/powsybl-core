/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.action;

import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;
import com.powsybl.security.action.LoadAction;

import java.util.List;

/**
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class LoadActionSerializer extends AbstractLoadActionSerializer<LoadAction> {

    public LoadActionSerializer() {
        super(LoadAction.class);
    }

    @Override
    protected List<NetworkElementIdentifier> getElementId(LoadAction action) {
        return action.getNetworkElementIdentifiers();
    }
}
