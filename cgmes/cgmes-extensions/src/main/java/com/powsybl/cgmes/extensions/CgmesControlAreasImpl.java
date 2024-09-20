/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import java.util.*;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class CgmesControlAreasImpl extends AbstractExtension<Network> implements CgmesControlAreas {

    private final Map<String, CgmesControlArea> cgmesControlAreas = new HashMap<>();

    CgmesControlAreasImpl(Network network) {
        super(network);
    }

    @Override
    public CgmesControlAreaAdder newCgmesControlArea() {
        return new CgmesControlAreaAdderImpl(this);
    }

    @Override
    public Collection<CgmesControlArea> getCgmesControlAreas() {
        return Collections.unmodifiableCollection(cgmesControlAreas.values());
    }

    @Override
    public CgmesControlArea getCgmesControlArea(String controlAreaId) {
        return cgmesControlAreas.get(controlAreaId);
    }

    @Override
    public boolean containsCgmesControlAreaId(String controlAreaId) {
        return cgmesControlAreas.containsKey(controlAreaId);
    }

    void putCgmesControlArea(CgmesControlAreaImpl cgmesControlArea) {
        Objects.requireNonNull(cgmesControlArea);
        if (cgmesControlAreas.containsKey(cgmesControlArea.getId())) {
            throw new PowsyblException(String.format("CGMES control area %s has already been added", cgmesControlArea.getId()));
        }
        cgmesControlAreas.put(cgmesControlArea.getId(), cgmesControlArea);
    }
}
