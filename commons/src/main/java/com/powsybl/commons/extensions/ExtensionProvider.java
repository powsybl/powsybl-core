/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public interface ExtensionProvider<T extends Extendable, E extends Extension<T>> {

    /**
     * Return the name of the extensions provided by this provider.
     * @return the name of the extensions provided by this provider.
     */
    String getExtensionName();

    /**
     * Return the category of the extensions provided by this provider.
     * @return the category of the extensions provided by this provider.
     */
    String getCategoryName();

    /**
     * Return the type of extensions provided by this provider.
     * @return the type of extensions provided by this provider.
     */
    Class<? super E> getExtensionClass();
}
