/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import java.util.List;
import java.util.Objects;

import com.powsybl.commons.util.ServiceLoaderCache;

/**
*
* @author Ferrari Giovanni {@literal <giovanni.ferrari@techrain.eu>}
*/
public final class AmplExtensionWriters {

    private static final ServiceLoaderCache<AmplExtensionWriter> WRITERS_LOADER = new ServiceLoaderCache<>(AmplExtensionWriter.class);

    private AmplExtensionWriters() {
    }

    public static AmplExtensionWriter getWriter(String name) {
        Objects.requireNonNull(name);
        for (AmplExtensionWriter w : WRITERS_LOADER.getServices()) {
            if (w.getName().equals(name)) {
                return w;
            }
        }
        return null;
    }

    public static List<String> getWriterNames() {
        return WRITERS_LOADER.getServices().stream().map(AmplExtensionWriter::getName).toList();
    }

}
