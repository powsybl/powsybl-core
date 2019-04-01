/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.google.auto.service.AutoService;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.test.LoadFooExt;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
@AutoService(ImmutableWrapperExtension.class)
public class ImmutableLoadFooExtension extends AbstractImmutableWrapperExtension<Load, LoadFooExt> {

    @Override
    public String getExtensionName() {
        return "loadFoo";
    }

    @Override
    public Class getExtensionClass() {
        return LoadFooExt.class;
    }

    @Override
    protected LoadFooExt toImmutable(LoadFooExt mutableExt, Load immuExtendable) {
        return new LoadFooExt(immuExtendable) {

            @Override
            public String getUsername() {
                return mutableExt.getUsername();
            }

            @Override
            public void setUsername(String username) {
                throw ImmutableNetwork.UNMODIFIABLE_EXCEPTION;
            }

            @Override
            public void setExtendable(Load extendable) {
                throw ImmutableNetwork.UNMODIFIABLE_EXCEPTION;
            }
        };
    }
}
