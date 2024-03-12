/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.action;

import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public abstract class AbstractAction implements Action {

    private final String id;
    private final List<NetworkElementIdentifier> networkElementIdentifiers;

    protected AbstractAction(String id) {
        this(id, Collections.emptyList());
    }

    protected AbstractAction(String id, List<NetworkElementIdentifier> networkElementIdentifiers) {
        this.id = Objects.requireNonNull(id);
        this.networkElementIdentifiers = networkElementIdentifiers;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<NetworkElementIdentifier> getNetworkElementIdentifiers() {
        return networkElementIdentifiers;
    }
}
