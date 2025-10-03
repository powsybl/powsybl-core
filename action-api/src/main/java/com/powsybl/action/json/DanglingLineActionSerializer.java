/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.BoundaryLineAction;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */

// TODO: Manage versioning to chang DanglingLine to BoundaryLine

public class DanglingLineActionSerializer extends AbstractLoadActionSerializer<BoundaryLineAction> {
    public DanglingLineActionSerializer() {
        super(BoundaryLineAction.class);
    }

    @Override
    protected String getElementIdAttributeName() {
        return "danglingLineId";
    }

    @Override
    protected String getElementId(BoundaryLineAction action) {
        return action.getDanglingLineId();
    }
}
