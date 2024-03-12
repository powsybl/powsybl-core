/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.action;

import com.powsybl.contingency.contingency.list.identifier.IdBasedNetworkElementIdentifier;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;
import com.powsybl.iidm.network.ThreeSides;

import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class TerminalsConnectionActionBuilder {

    private String id;
    private List<NetworkElementIdentifier> elementIdentifiers;
    private Boolean open;
    private ThreeSides side;

    public TerminalsConnectionActionBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public TerminalsConnectionActionBuilder withIdentifiers(List<NetworkElementIdentifier> elementIdentifiers) {
        this.elementIdentifiers = elementIdentifiers;
        return this;
    }

    public TerminalsConnectionActionBuilder withElementId(String elementId) {
        this.elementIdentifiers = Collections.singletonList(new IdBasedNetworkElementIdentifier(elementId));
        return this;
    }

    public TerminalsConnectionActionBuilder withSide(ThreeSides side) {
        this.side = side;
        return this;
    }

    public TerminalsConnectionActionBuilder withOpen(boolean open) {
        this.open = open;
        return this;
    }

    public TerminalsConnectionAction build() {
        return new TerminalsConnectionAction(id, elementIdentifiers, side, open);
    }
}
