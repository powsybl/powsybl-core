/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.dynamicsimulation.groovy;

import java.util.Objects;

import com.powsybl.dynamicsimulation.EventModel;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public class DummyEventModel implements EventModel {

    private final String id;
    private final double startTime;

    public DummyEventModel(String id, double startTime) {
        this.id = Objects.requireNonNull(id);
        this.startTime = startTime;
    }

    public String getId() {
        return id;
    }

    @Override
    public double getStartTime() {
        return startTime;
    }
}
