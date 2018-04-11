/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import java.util.Objects;
import java.util.ServiceLoader;

/**
*
* @author Ferrari Giovanni <giovanni.ferrari@techrain.eu>
*/
public final class AmplExtensionWriters {
    private AmplExtensionWriters() {

    }

    public static AmplExtensionWriter getWriter(String name) {
        Objects.requireNonNull(name);
        for (AmplExtensionWriter w : ServiceLoader.load(AmplExtensionWriter.class)) {
            if (w.getName().equals(name)) {
                return w;
            }
        }
        return null;
    }
}
