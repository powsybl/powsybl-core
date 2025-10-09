/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;

import java.util.Set;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface ReferenceTerminalsAdder extends ExtensionAdder<Network, ReferenceTerminals> {

    ReferenceTerminalsAdder withTerminals(Set<Terminal> terminals);

    @Override
    default Class<ReferenceTerminals> getExtensionClass() {
        return ReferenceTerminals.class;
    }
}
