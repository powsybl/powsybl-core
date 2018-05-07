/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Substation;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EntsoeArea extends AbstractExtension<Substation> {

    private final Substation substation;

    private EntsoeGeographicalCode code;

    public EntsoeArea(Substation substation, EntsoeGeographicalCode code) {
        this.substation = Objects.requireNonNull(substation);
        this.code = Objects.requireNonNull(code);
    }

    @Override
    public String getName() {
        return "entsoeArea";
    }

    @Override
    public Substation getExtendable() {
        return substation;
    }

    public EntsoeGeographicalCode getCode() {
        return code;
    }

    public EntsoeArea setCode(EntsoeGeographicalCode code) {
        this.code = Objects.requireNonNull(code);
        return this;
    }
}
