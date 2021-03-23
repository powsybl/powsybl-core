/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
class CgmesControlAreasAdderImpl extends AbstractExtensionAdder<Network, CgmesControlAreas> implements CgmesControlAreasAdder {

    CgmesControlAreasAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    protected CgmesControlAreas createExtension(Network extendable) {
        return new CgmesControlAreasImpl(extendable);
    }

}
