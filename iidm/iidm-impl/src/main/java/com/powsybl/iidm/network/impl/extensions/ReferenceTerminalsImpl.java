/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.ReferenceTerminal;
import com.powsybl.iidm.network.extensions.ReferenceTerminalAdder;
import com.powsybl.iidm.network.extensions.ReferenceTerminals;

import java.util.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class ReferenceTerminalsImpl<C extends Connectable<C>> extends AbstractExtension<C> implements ReferenceTerminals<C> {

    private final Map<Terminal, ReferenceTerminal> referenceTerminals = new LinkedHashMap<>();

    ReferenceTerminalsImpl<C> add(ReferenceTerminal referenceTerminal) {
        if (!getExtendable().getTerminals().contains(referenceTerminal.getTerminal())) {
            throw new PowsyblException("The provided terminal does not belong to this connectable");
        }
        referenceTerminals.put(referenceTerminal.getTerminal(), referenceTerminal);
        return this;
    }

    @Override
    public ReferenceTerminalAdder newReferenceTerminal() {
        return new ReferenceTerminalAdderImpl(this);
    }

    @Override
    public List<ReferenceTerminal> getReferenceTerminals() {
        return referenceTerminals.values().stream().toList();
    }
}
