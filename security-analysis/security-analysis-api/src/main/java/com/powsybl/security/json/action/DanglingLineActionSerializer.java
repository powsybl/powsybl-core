/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.action;

import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;
import com.powsybl.security.action.DanglingLineAction;

import java.util.List;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class DanglingLineActionSerializer extends AbstractLoadActionSerializer<DanglingLineAction> {
    public DanglingLineActionSerializer() {
        super(DanglingLineAction.class);
    }

    @Override
    protected List<NetworkElementIdentifier> getElementId(DanglingLineAction action) {
        return action.getNetworkElementIdentifiers();
    }
}
