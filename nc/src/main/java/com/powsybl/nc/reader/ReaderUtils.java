/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.nc.reader;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public final class ReaderUtils {

    private ReaderUtils() {
    }

    public static String getElementIdFromResourceUri(String resourceUri) {
        // expected format is http://entsoe.eu/#_
        // +s need to be replaced by spaces to account for networks imported from UCTE
        return resourceUri.substring(resourceUri.lastIndexOf('#') + 2).replace('+', ' ');
    }
}
