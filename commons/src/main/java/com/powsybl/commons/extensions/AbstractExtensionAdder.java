/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import java.util.Objects;

/**
 * A base class for implementations of {@link ExtensionAdder} that holds the
 * extendable to be able build and then add the extension to the extendable.
 * This class calls {@link #createExtension} that must be overriden by
 * subclasses to create the extension.
 *
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 */
public abstract class AbstractExtensionAdder<T extends Extendable<T>, E extends Extension<T>>
        implements ExtensionAdder<T, E> {

    protected final T extendable;

    protected AbstractExtensionAdder(T extendable) {
        this.extendable = Objects.requireNonNull(extendable);
    }

    /**
     * Creates the extension.
     *
     * @return the extension
     */
    protected abstract E createExtension(T extendable);

    @Override
    public E add() {
        E extension = createExtension(extendable);
        extendable.addExtension(getExtensionClass(), extension);
        return extension;
    }
}
