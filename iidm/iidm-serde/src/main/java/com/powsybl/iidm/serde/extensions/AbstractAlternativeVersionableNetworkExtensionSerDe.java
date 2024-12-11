/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.extensions.AlternativeExtensionSerDe;
import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.serde.IidmVersion;

import java.util.Map;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractAlternativeVersionableNetworkExtensionSerDe<T extends Extendable, E extends Extension<T>>
        extends AbstractVersionableNetworkExtensionSerDe<T, E>
        implements AlternativeExtensionSerDe<T, E> {

    protected AbstractAlternativeVersionableNetworkExtensionSerDe(String extensionName, Class<? super E> extensionClass, String namespacePrefix,
                                                       Map<IidmVersion, ImmutableSortedSet<String>> extensionVersions, Map<String, String> namespaceUris) {
        super(extensionName, extensionClass, namespacePrefix, extensionVersions, namespaceUris);
    }
}
