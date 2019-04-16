/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.google.auto.service.AutoService;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.test.SubstationShutdownExtension;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
@AutoService(ImmutableWrapperExtension.class)
public class ImmutableSubstationShutdownExtension extends AbstractImmutableWrapperExtension<Substation, SubstationShutdownExtension> {

    @Override
    public String getExtensionName() {
        return "subShutdownExt";
    }

    @Override
    public Class<? super SubstationShutdownExtension> getExtensionClass() {
        return SubstationShutdownExtension.class;
    }

    @Override
    protected SubstationShutdownExtension toImmutable(SubstationShutdownExtension mutableExtension, Substation immutableExtendable) {
        return new SubstationShutdownExtension() {
            @Override
            public void shutdown() {
                throw ImmutableNetwork.UNMODIFIABLE_EXCEPTION;
            }

            @Override
            public Substation getExtendable() {
                return immutableExtendable;
            }

            @Override
            public void setExtendable(Substation extendable) {
                throw UNMODIFIABLE_EXCEPTION;
            }
        };
    }
}
