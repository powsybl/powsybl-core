/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StudyCase {

    private final String name;

    private final Instant time;

    private final List<DataObject> elmNets;

    public StudyCase(String name, Instant time, List<DataObject> elmNets) {
        this.name = Objects.requireNonNull(name);
        this.time = Objects.requireNonNull(time);
        if (elmNets.isEmpty()) {
            throw new IllegalArgumentException("Empty ElmNet list");
        }
        this.elmNets = Objects.requireNonNull(elmNets);
    }

    public String getName() {
        return name;
    }

    public Instant getTime() {
        return time;
    }

    public List<DataObject> getElmNets() {
        return elmNets;
    }
}
