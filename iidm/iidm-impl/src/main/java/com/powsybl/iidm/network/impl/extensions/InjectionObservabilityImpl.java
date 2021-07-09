/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.InjectionObservability;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class InjectionObservabilityImpl<T extends Injection<T>> extends AbstractExtension<T>
        implements InjectionObservability<T> {

    private boolean observable;

    public InjectionObservabilityImpl(T component, boolean observable) {
        super(component);
        this.observable = observable;
    }

    public boolean isObservable() {
        return observable;
    }

    @Override
    public InjectionObservability<T> setObservable(boolean observable) {
        this.observable = observable;
        return this;
    }
}
