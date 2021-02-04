/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.powsybl.iidm.network.Terminal;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
class CgmesControlAreaImpl implements CgmesControlArea {
    private final String id;
    private final String name;
    private final String energyIdentCodeEic;
    private final Set<Terminal> terminals = new HashSet<>();
    private final double netInterchange;

    CgmesControlAreaImpl(String id, String name, String energyIdentCodeEic, double netInterchange, CgmesControlAreasImpl mapping) {
        this.id = id;
        this.name = name;
        this.energyIdentCodeEic = energyIdentCodeEic;
        this.netInterchange = netInterchange;
        attach(mapping);
    }

    private void attach(CgmesControlAreasImpl mapping) {
        mapping.putCgmesControlArea(this);
    }

    @Override
    public void addTerminal(Terminal terminal) {
        terminals.add(terminal);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getEnergyIdentCodeEic() {
        return energyIdentCodeEic;
    }

    @Override
    public Set<Terminal> getTerminals() {
        return terminals;
    }

    @Override
    public double getNetInterchange() {
        return netInterchange;
    }
}
