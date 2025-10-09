/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Substation;

import java.util.Objects;

/**
 * @author Jérémy Labous {@literal <jlabous at silicom.fr>}
 */
public class EntsoeAreaImpl extends AbstractExtension<Substation> implements EntsoeArea {

    private EntsoeGeographicalCode code;

    public EntsoeAreaImpl(Substation substation, EntsoeGeographicalCode code) {
        super(substation);
        this.code = Objects.requireNonNull(code);
    }

    @Override
    public EntsoeGeographicalCode getCode() {
        return code;
    }

    @Override
    public EntsoeAreaImpl setCode(EntsoeGeographicalCode code) {
        this.code = Objects.requireNonNull(code);
        return this;
    }
}
