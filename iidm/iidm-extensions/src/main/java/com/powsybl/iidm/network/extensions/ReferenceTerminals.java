/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;

import java.util.Set;

/**
 * ReferenceTerminals iIDM extension allows storing the angle references that were chosen by a load flow, for
 * example (but not necessarily) chosen using {@link ReferencePriorities} inputs.<br/>
 * There should be one reference terminal per SynchronousComponent calculated.
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface ReferenceTerminals extends Extension<Network> {

    String NAME = "referenceTerminals";

    @Override
    default String getName() {
        return NAME;
    }

    Set<Terminal> getReferenceTerminals();

    void setReferenceTerminals(Set<Terminal> terminals);

    ReferenceTerminals clear();

    ReferenceTerminals addReferenceTerminal(Terminal terminal);
}
