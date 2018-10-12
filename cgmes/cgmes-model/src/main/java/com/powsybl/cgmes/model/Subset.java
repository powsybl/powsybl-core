/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public enum Subset {
    EQUIPMENT("EQ"),
    TOPOLOGY("TP"),
    STATE_VARIABLES("SV"),
    STEADY_STATE_HYPOTHESIS("SSH"),
    DYNAMIC("DY"),
    DIAGRAM_LAYOUT("DL"),
    GEOGRAPHICAL_LOCATION("GL");

    private final String identifier;

    Subset(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Get the identifier of a subset
     */
    public String getIdentifier() {
        return identifier;
    }
}
