/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.action;

import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;
import com.powsybl.iidm.network.ThreeSides;

import java.util.List;
import java.util.Optional;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public abstract class AbstractTapChangerAction extends AbstractAction {

    private final ThreeSides side;

    protected AbstractTapChangerAction(String id, List<NetworkElementIdentifier> tapChangerIdentifiers, ThreeSides side) {
        super(id, tapChangerIdentifiers);
        this.side = side;
    }

    public Optional<ThreeSides> getSide() {
        return Optional.ofNullable(side);
    }
}
