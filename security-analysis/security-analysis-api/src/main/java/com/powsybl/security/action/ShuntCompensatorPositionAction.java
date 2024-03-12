/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.action;

import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;

import java.util.List;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public class ShuntCompensatorPositionAction extends AbstractAction {

    public static final String NAME = "SHUNT_COMPENSATOR_POSITION";

    private final int sectionCount;

    ShuntCompensatorPositionAction(String id, List<NetworkElementIdentifier> shuntIdentifiers, int sectionCount) {
        super(id, shuntIdentifiers);
        this.sectionCount = sectionCount;
    }

    @Override
    public String getType() {
        return NAME;
    }

    public int getSectionCount() {
        return sectionCount;
    }
}
