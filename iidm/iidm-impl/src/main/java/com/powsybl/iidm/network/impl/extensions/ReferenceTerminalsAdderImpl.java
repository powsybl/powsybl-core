/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.ReferenceTerminals;
import com.powsybl.iidm.network.extensions.ReferenceTerminalsAdder;

import java.util.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class ReferenceTerminalsAdderImpl extends AbstractExtensionAdder<Network, ReferenceTerminals> implements ReferenceTerminalsAdder {

    Set<Terminal> terminals;

    public ReferenceTerminalsAdderImpl(Network network) {
        super(network);
    }

    @Override
    public ReferenceTerminalsAdder withTerminals(Set<Terminal> terminals) {
        this.terminals = terminals;
        return this;
    }

    @Override
    protected ReferenceTerminals createExtension(Network network) {
        return new ReferenceTerminalsImpl(network, terminals);
    }
}
