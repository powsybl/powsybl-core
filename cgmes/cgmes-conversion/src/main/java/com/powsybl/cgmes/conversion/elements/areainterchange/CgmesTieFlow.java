/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.elements.areainterchange;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class CgmesTieFlow {

    private final String id;
    private final String terminal;

    public CgmesTieFlow(String id, String terminal) {
        this.id = id;
        this.terminal = terminal;
    }

    public String getId() {
        return id;
    }

    public String getTerminal() {
        return terminal;
    }

}
