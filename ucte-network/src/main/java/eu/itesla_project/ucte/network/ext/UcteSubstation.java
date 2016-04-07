/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.ucte.network.ext;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import eu.itesla_project.ucte.network.UcteNodeCode;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UcteSubstation {

    private final String name;

    private final List<UcteVoltageLevel> voltageLevels;

    public UcteSubstation(String name, List<UcteVoltageLevel> voltageLevels) {
        this.name = name;
        this.voltageLevels = voltageLevels;
    }

    public String getName() {
        return name;
    }

    public Collection<UcteVoltageLevel> getVoltageLevels() {
        return voltageLevels;
    }

    public Iterable<UcteNodeCode> getNodes() {
        return FluentIterable.from(voltageLevels).transformAndConcat(new Function<UcteVoltageLevel, Iterable<UcteNodeCode>>() {
            @Override
            public Iterable<UcteNodeCode> apply(UcteVoltageLevel ucteVoltageLevel) {
                return ucteVoltageLevel.getNodes();
            }
        });
    }

}
