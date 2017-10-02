/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network.ext;

import com.powsybl.ucte.network.UcteNodeCode;
import java.util.Collection;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UcteVoltageLevel {

    private final String name;

    private final UcteSubstation substation;

    private final Collection<UcteNodeCode> nodes;

    public UcteVoltageLevel(String name, UcteSubstation substation, Collection<UcteNodeCode> nodes) {
        this.name = Objects.requireNonNull(name);
        this.substation = Objects.requireNonNull(substation);
        this.nodes = Objects.requireNonNull(nodes);
    }

    public String getName() {
        return name;
    }

    public UcteSubstation getSubstation() {
        return substation;
    }

    public Collection<UcteNodeCode> getNodes() {
        return nodes;
    }

}
