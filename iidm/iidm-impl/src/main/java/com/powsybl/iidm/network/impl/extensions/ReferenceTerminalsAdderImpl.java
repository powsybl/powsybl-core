/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.ReferenceTerminals;
import com.powsybl.iidm.network.extensions.ReferenceTerminalsAdder;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class ReferenceTerminalsAdderImpl<C extends Connectable<C>> extends AbstractExtensionAdder<C, ReferenceTerminals<C>> implements ReferenceTerminalsAdder<C> {

    ReferenceTerminalsAdderImpl(C extendable) {
        super(extendable);
    }

    @Override
    protected ReferenceTerminals<C> createExtension(C extendable) {
        return new ReferenceTerminalsImpl<>();
    }
}
