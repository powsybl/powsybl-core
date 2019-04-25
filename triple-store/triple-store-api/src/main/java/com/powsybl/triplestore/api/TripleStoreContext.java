/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.triplestore.api;

import java.util.Objects;

/**
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 * @author Luma Zamarreño <zamarrenolm@aia.es>
 */
public class TripleStoreContext {

    private final String shortName;
    private final String longName;

    public TripleStoreContext(String shortName) {
        this.shortName = Objects.requireNonNull(shortName);
        this.longName = null;
    }

    public TripleStoreContext(String shortName, String longName) {
        this.shortName = Objects.requireNonNull(shortName);
        this.longName = Objects.requireNonNull(longName);
    }

    public String shortName() {
        return shortName;
    }

    public String longName() {
        return longName;
    }
}
