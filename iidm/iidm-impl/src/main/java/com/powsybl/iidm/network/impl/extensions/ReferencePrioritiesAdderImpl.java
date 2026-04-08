/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.ReferencePriorities;
import com.powsybl.iidm.network.extensions.ReferencePrioritiesAdder;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class ReferencePrioritiesAdderImpl<C extends Connectable<C>> extends AbstractExtensionAdder<C, ReferencePriorities<C>> implements ReferencePrioritiesAdder<C> {

    ReferencePrioritiesAdderImpl(C extendable) {
        super(extendable);
    }

    @Override
    protected ReferencePriorities<C> createExtension(C extendable) {
        return new ReferencePrioritiesImpl<>(extendable);
    }
}
