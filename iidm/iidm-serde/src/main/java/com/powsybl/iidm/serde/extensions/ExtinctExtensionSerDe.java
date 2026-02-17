/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.serde.ExportOptions;
import com.powsybl.iidm.serde.IidmVersion;

/**
 * <p>Interface for the (de)serialization of extensions that no longer exists.</p>
 * <p>It is useful for backward compatibility purposes.</p>
 *
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public interface ExtinctExtensionSerDe<T extends Extendable, E extends Extension<T>> extends ExtensionSerDe<T, E> {

    default boolean isExtensionNeeded(Network network, ExportOptions options) {
        return isExtensionExportable(options) && isExtensionNeeded(network);
    }

    private boolean isExtensionExportable(ExportOptions options) {
        return isExtensionExportable(options, getName(), getLastSupportedVersion());
    }

    static boolean isExtensionExportable(ExportOptions options, String extensionName, IidmVersion lastSupportedVersion) {
        return options.withExtension(extensionName) && options.getVersion().compareTo(lastSupportedVersion) <= 0;
    }

    IidmVersion getLastSupportedVersion();

    boolean isExtensionNeeded(Network network);
}
