/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.Discretes;
import com.powsybl.iidm.network.extensions.DiscretesAdder;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class DiscretesAdderImpl<I extends Identifiable<I>> extends AbstractExtensionAdder<I, Discretes<I>> implements DiscretesAdder<I> {

    DiscretesAdderImpl(I extendable) {
        super(extendable);
    }

    @Override
    protected Discretes<I> createExtension(I extendable) {
        return new DiscretesImpl<>();
    }
}
