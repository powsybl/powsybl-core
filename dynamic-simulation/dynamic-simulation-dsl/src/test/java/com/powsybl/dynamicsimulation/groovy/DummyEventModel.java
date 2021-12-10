/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.dynamicsimulation.groovy;

import com.powsybl.dynamicsimulation.EventModel;

import java.util.Objects;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class DummyEventModel implements EventModel {

    private final String id;

    public DummyEventModel(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public String getId() {
        return id;
    }

}
