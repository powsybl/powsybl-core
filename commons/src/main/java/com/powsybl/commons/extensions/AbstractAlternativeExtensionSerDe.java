/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractAlternativeExtensionSerDe<T extends Extendable, E extends Extension<T>>
        extends AbstractExtensionSerDe<T, E>
        implements AlternativeExtensionSerDe<T, E> {

    protected AbstractAlternativeExtensionSerDe(String extensionName, String categoryName, Class<? super E> extensionClass,
                                                String xsdFileName, String namespaceUri, String namespacePrefix) {
        super(extensionName, categoryName, extensionClass, xsdFileName, namespaceUri, namespacePrefix);
    }
}
