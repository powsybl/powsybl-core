/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.LoadAction;

/**
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class LoadActionSerializer extends AbstractLoadActionSerializer<LoadAction> {

    public LoadActionSerializer() {
        super(LoadAction.class);
    }

    @Override
    protected String getElementIdAttributeName() {
        return "loadId";
    }

    @Override
    protected String getElementId(LoadAction action) {
        return action.getLoadId();
    }
}
