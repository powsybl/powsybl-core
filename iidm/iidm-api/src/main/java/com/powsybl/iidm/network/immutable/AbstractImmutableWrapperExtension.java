/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public abstract class AbstractImmutableWrapperExtension<T extends Extendable, E extends Extension<T>> implements ImmutableWrapperExtension<T, E> {

    static final String IMMU_WRAPPER_EXT_CATE_NAME = "ImmutableWrapperExtension";

    protected BiFunction<E, T, E> immuter;

    Map<E, E> cache = new HashMap<>();

    @Override
    public final String getCategoryName() {
        return IMMU_WRAPPER_EXT_CATE_NAME;
    }

    protected abstract void setImmuter();

    @Override
    public final E wrap(E extension, T extendable) {
        if (immuter == null) {
            setImmuter();
        }
        return cache.computeIfAbsent(extension, k -> immuter.apply(extension, extendable));
    }
}
