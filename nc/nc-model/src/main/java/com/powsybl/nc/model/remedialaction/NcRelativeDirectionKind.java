/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.model.remedialaction;

import java.util.Arrays;

/**
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
public enum NcRelativeDirectionKind {
    NONE("http://entsoe.eu/ns/nc#RelativeDirectionKind.none"),
    UP("http://entsoe.eu/ns/nc#RelativeDirectionKind.up"),
    DOWN("http://entsoe.eu/ns/nc#RelativeDirectionKind.down"),
    UNKNOWN(null);

    private final String uri;

    NcRelativeDirectionKind(String uri) {
        this.uri = uri;
    }

    public static NcRelativeDirectionKind fromUri(String uri) {
        return Arrays.stream(values())
            .filter(value -> value.uri != null && value.uri.equals(uri))
            .findFirst()
            .orElse(UNKNOWN);
    }
}
