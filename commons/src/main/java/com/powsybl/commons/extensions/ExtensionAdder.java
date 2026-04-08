/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

/**
 * An ExtensionAdder is a builder for an extension that is built and then added
 * to an extendable.
 *
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 */
// Can't use "T extends Extendable<T>" here because T is used in Extendable::newExtensionAdder
// to ensure that ExtensionAdder::T is the same as Extendable::O and Extendable doesn't declare
// Extendable<O extends Extendable>.
// TODO: If we can, we should change Extendable to declare Extendable<O extends Extendable> ?
public interface ExtensionAdder<T, E extends Extension<T>> {

    /**
     * Returns the class of the extension. This is expected to be an interface so
     * that multiple implementors can implement the same extensions. This will be
     * the key at which the extension is added on the extendable. This is meant to
     * be implemented by adder interfaces but not by adder implementations.
     *
     * @return the interface of the extension
     */
    // Class<? super E> to allow adders of generic extensions to return the class
    // of the raw type. This has the bad effect of allowing the super classes but we
    // consider the trade-off acceptable.
    Class<? super E> getExtensionClass();

    /**
     * Builds and adds the extension to the extendable which was used to get this
     * extensionAdder. The extendable is returned to allow a fluent style adding of
     * multiple extensions.
     *
     * @return the extendable
     */
    E add();
}
