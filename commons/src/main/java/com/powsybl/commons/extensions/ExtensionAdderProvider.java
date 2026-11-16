/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

/**
 * A provider used through java's {@link java.util.ServiceLoader}. It will
 * provide an {@link ExtensionAdder} to add an extension to an extendable.
 * {@link #getImplementationName} is used to find providers corresponding to the
 * implementation of the {@link Extendable}. {@link #getAdderClass} is used to
 * specify the adder class.
 *
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 *
 * @param <T> The extendable
 * @param <E> The extension
 * @param <B> The extensionBuilder
 */
public interface ExtensionAdderProvider<T extends Extendable<T>, E extends Extension<T>, B extends ExtensionAdder<T, E>> {

    /**
     * Returns a name that is used to select this provider when searching for
     * implementations of extension builders in {@link Extendable#newExtension}.
     *
     * @return the name
     */
    String getImplementationName();

    /**
     * Returns extension name.
     *
     * @return the extension name
     */
    default String getExtensionName() {
        return null;
    }

    /**
     * Returns the builder class provided by this Provider.
     *
     * @return the class
     */
    // Class<? super B> to allow providers of generic builders to return the class
    // of the raw type. This has the bad effect of allowing the super classes but we
    // consider the trade-off acceptable.
    Class<? super B> getAdderClass();

    /**
     * returns an new empty ExtensionAdder for this extendable.
     *
     * @param extendable the extendable on which the adder will add the extension
     */
    B newAdder(T extendable);
}
