/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class CgmesControlAdderImpl implements CgmesControlAreaAdder {

    private final CgmesControlAreasImpl mapping;
    private String id;
    private String name;
    private String energyIdentCodeEic;
    private double netInterchange;

    CgmesControlAdderImpl(CgmesControlAreasImpl mapping) {
        this.mapping = Objects.requireNonNull(mapping);
    }

    @Override
    public CgmesControlAreaAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public CgmesControlAreaAdder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public CgmesControlAreaAdder setEnergyIdentCodeEic(String energyIdentCodeEic) {
        this.energyIdentCodeEic = energyIdentCodeEic;
        return this;
    }

    @Override
    public CgmesControlAreaAdder setNetInterchange(double netInterchange) {
        this.netInterchange = netInterchange;
        return this;
    }

    @Override
    public CgmesControlAreaImpl add() {
        return new CgmesControlAreaImpl(id, name, energyIdentCodeEic, netInterchange, mapping);
    }
}
