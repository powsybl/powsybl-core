/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import java.util.Map;

import com.powsybl.cgmes.conversion.elements.areainterchange.CgmesControlArea;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class CgmesControlAreaMappingImpl extends AbstractExtension<Network> implements CgmesControlAreaMapping {

    private final Map<String, CgmesControlArea> cgmesControlAreas;

    CgmesControlAreaMappingImpl(Map<String, CgmesControlArea> cgmesControlAreas) {
        this.cgmesControlAreas = cgmesControlAreas;
    }

    @Override
    public Map<String, CgmesControlArea> getCgmesControlAreas() {
        return cgmesControlAreas;
    }

}
