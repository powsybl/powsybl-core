/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util.immutable;

import com.google.auto.service.AutoService;
import com.powsybl.entsoe.util.EntsoeArea;
import com.powsybl.entsoe.util.EntsoeGeographicalCode;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.immutable.AbstractImmutableWrapperExtension;
import com.powsybl.iidm.network.immutable.ImmutableWrapperExtension;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
@AutoService(ImmutableWrapperExtension.class)
public class ImmutableEntsoeArea extends AbstractImmutableWrapperExtension<Substation, EntsoeArea> {
    @Override
    protected EntsoeArea toImmutable(EntsoeArea extension, Substation immutableExtendable) {
        return new EntsoeArea(immutableExtendable, extension.getCode()) {
            @Override
            public EntsoeArea setCode(EntsoeGeographicalCode code) {
                throw UNMODIFIABLE_EXCEPTION;
            }

            @Override
            public void setExtendable(Substation extendable) {
                throw UNMODIFIABLE_EXCEPTION;
            }
        };
    }

    @Override
    public String getExtensionName() {
        return "entsoeArea";
    }

    @Override
    public Class<? super EntsoeArea> getExtensionClass() {
        return EntsoeArea.class;
    }
}
