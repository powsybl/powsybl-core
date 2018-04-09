/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network.ext;

import com.powsybl.ucte.network.UcteNodeCode;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UcteSubstation {

    private final String name;

    private final List<UcteVoltageLevel> voltageLevels;

    public UcteSubstation(String name, List<UcteVoltageLevel> voltageLevels) {
        this.name = Objects.requireNonNull(name);
        this.voltageLevels = Objects.requireNonNull(voltageLevels);
    }

    public String getName() {
        return name;
    }

    public Collection<UcteVoltageLevel> getVoltageLevels() {
        return voltageLevels;
    }

    public List<UcteNodeCode> getNodes() {
        return voltageLevels.stream()
                .flatMap(vl -> vl.getNodes().stream())
                .collect(Collectors.toList());
    }
}
