/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util.immutable;

import com.google.auto.service.AutoService;
import com.powsybl.entsoe.util.MergedXnode;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.immutable.AbstractImmutableWrapperExtension;
import com.powsybl.iidm.network.immutable.ImmutableWrapperExtension;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
@AutoService(ImmutableWrapperExtension.class)
public class ImmutableMergedXnode extends AbstractImmutableWrapperExtension<Line, MergedXnode> {
    @Override
    protected MergedXnode toImmutable(MergedXnode extension, Line immutableExtendable) {
        return new MergedXnode(immutableExtendable, extension.getRdp(), extension.getXdp(), extension.getXnodeP1(), extension.getXnodeQ1(), extension.getXnodeP2(), extension.getXnodeQ2(), extension.getCode()) {
            @Override
            public MergedXnode setRdp(float rdp) {
                throw UNMODIFIABLE_EXCEPTION;
            }

            @Override
            public MergedXnode setXdp(float xdp) {
                throw UNMODIFIABLE_EXCEPTION;
            }

            @Override
            public MergedXnode setXnodeP1(double xNodeP1) {
                throw UNMODIFIABLE_EXCEPTION;
            }

            @Override
            public MergedXnode setXnodeQ1(double xNodeQ1) {
                throw UNMODIFIABLE_EXCEPTION;
            }

            @Override
            public MergedXnode setXnodeP2(double xNodeP2) {
                throw UNMODIFIABLE_EXCEPTION;
            }

            @Override
            public MergedXnode setXnodeQ2(double xNodeQ2) {
                throw UNMODIFIABLE_EXCEPTION;
            }

            @Override
            public MergedXnode setCode(String xNodeCode) {
                throw UNMODIFIABLE_EXCEPTION;
            }

            @Override
            public void setExtendable(Line extendable) {
                throw UNMODIFIABLE_EXCEPTION;
            }
        };
    }

    @Override
    public String getExtensionName() {
        return "mergedXnode";
    }

    @Override
    public Class<? super MergedXnode> getExtensionClass() {
        return MergedXnode.class;
    }
}
