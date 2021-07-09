/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Injection;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public interface InjectionObservability<I extends Injection<I>> extends Extension<I> {

    @Override
    default String getName() {
        return "injectionObservability";
    }

    boolean isObservable();

    InjectionObservability<I> setObservable(boolean observable);
}
