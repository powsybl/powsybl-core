/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import java.util.List;

import com.powsybl.cgmes.conversion.elements.areainterchange.CgmesControlArea;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public interface CgmesControlAreaMapping extends Extension<Network> {

    List<CgmesControlArea> getCgmesControlAreas();

    @Override
    default String getName() {
        return "cgmesControlArea";
    }
}
