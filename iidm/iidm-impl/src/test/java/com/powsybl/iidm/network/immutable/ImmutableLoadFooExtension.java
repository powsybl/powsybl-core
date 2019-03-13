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

import java.util.Objects;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
@AutoService(ImmutableWrapperExtension.class)
public class ImmutableLoadFooExtension extends AbstractImmutableWrapperExtension<Load, LoadFooExt> {

    private Immu immu;

    @Override
    public String getExtensionName() {
        return "loadFoo";
    }

    @Override
    public Class getExtensionClass() {
        return LoadFooExt.class;
    }

    @Override
    public LoadFooExt wrap(LoadFooExt extension) {
        if (immu == null) {
            immu = new Immu(extension);
        }
        return immu;
    }

    private class Immu extends LoadFooExt {

        private LoadFooExt delegate;

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public String getUsername() {
            return delegate.getUsername();
        }

        @Override
        public void setUsername(String username) {
            throw ImmutableNetwork.UNMODIFIABLE_EXCEPTION;
        }

        public Immu(LoadFooExt delegate) {
            super(delegate.getExtendable());
            this.delegate = Objects.requireNonNull(delegate);
        }
    }
}
