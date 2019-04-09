/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.triplestore.api;

import java.util.Objects;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class CgmesContext {

    private final CgmesProfile profile;
    private String name;

    public CgmesContext(CgmesProfile profile) {
        this.profile = Objects.requireNonNull(profile);
    }

    public CgmesContext(CgmesProfile profile, String name) {
        this(profile);
        this.name = Objects.requireNonNull(name);
    }

    public CgmesProfile getProfile() {
        return profile;
    }

    public String getName() {
        return name;
    }

}
