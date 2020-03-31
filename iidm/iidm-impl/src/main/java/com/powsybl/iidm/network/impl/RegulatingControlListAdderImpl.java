/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.regulation.RegulatingControlList;
import com.powsybl.iidm.network.regulation.RegulatingControlListAdder;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class RegulatingControlListAdderImpl extends AbstractExtensionAdder<Network, RegulatingControlList> implements RegulatingControlListAdder {

    RegulatingControlListAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    protected RegulatingControlListImpl createExtension(Network extendable) {
        return new RegulatingControlListImpl(extendable);
    }
}
