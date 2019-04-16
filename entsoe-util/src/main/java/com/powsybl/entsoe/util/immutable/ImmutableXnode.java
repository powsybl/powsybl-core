/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util.immutable;

import com.google.auto.service.AutoService;
import com.powsybl.entsoe.util.Xnode;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.immutable.AbstractImmutableWrapperExtension;
import com.powsybl.iidm.network.immutable.ImmutableWrapperExtension;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
@AutoService(ImmutableWrapperExtension.class)
public class ImmutableXnode extends AbstractImmutableWrapperExtension<DanglingLine, Xnode> {
    @Override
    protected Xnode toImmutable(Xnode extension, DanglingLine immutableExtendable) {
        return new Xnode(immutableExtendable, extension.getCode()) {
            @Override
            public Xnode setCode(String code) {
                throw UNMODIFIABLE_EXCEPTION;
            }

            @Override
            public void setExtendable(DanglingLine extendable) {
                throw UNMODIFIABLE_EXCEPTION;
            }
        };
    }

    @Override
    public String getExtensionName() {
        return "xnode";
    }

    @Override
    public Class<? super Xnode> getExtensionClass() {
        return Xnode.class;
    }
}
