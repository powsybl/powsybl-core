/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.extensions.*;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.regulation.RegulationList;
import com.powsybl.iidm.network.regulation.RegulationListAdder;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class RegulationListAdderImpl<C extends Connectable<C>> extends AbstractExtensionAdder<C, RegulationList<C>> implements RegulationListAdder<C> {

    RegulationListAdderImpl(C extendable) {
        super(extendable);
    }

    @Override
    protected RegulationList<C> createExtension(C extendable) {
        return new RegulationListImpl<>(extendable);
    }
}
