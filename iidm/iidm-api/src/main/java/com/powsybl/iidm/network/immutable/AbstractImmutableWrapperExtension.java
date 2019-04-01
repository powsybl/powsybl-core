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

/**
 * The subclass should implement the {@link #toImmutable()} method.
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public abstract class AbstractImmutableWrapperExtension<T extends Extendable, E extends Extension<T>> implements ImmutableWrapperExtension<T, E> {

    static final String IMMU_WRAPPER_EXT_CATE_NAME = "ImmutableWrapperExtension";

    Map<E, E> cache = new HashMap<>();

    @Override
    public final String getCategoryName() {
        return IMMU_WRAPPER_EXT_CATE_NAME;
    }

    @Override
    public final E wrap(E extension, T immutableExtendable) {
        return cache.computeIfAbsent(extension, k -> toImmutable(extension, immutableExtendable));
    }

    /**
     * Convert mutable extension to immutable, given the immutable extendable to bind on immutable extension.
     * @param extension the mutable extension to be wrapped
     * @param immutableExtendable the immutable extendable to be binded with the immutable extension
     * @return Returns an immutable extension
     */
    protected abstract E toImmutable(E extension, T immutableExtendable);
}
