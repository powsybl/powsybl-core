/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class IeeeCdfModel {

    private final IeeeCdfTitle title;

    private final List<IeeeCdfBus> buses = new ArrayList<>();

    private final List<IeeeCdfBranch> branches = new ArrayList<>();

    private final List<IeeeCdfLossZone> lossZones = new ArrayList<>();

    private final List<IeeeCdfInterchangeData> interchangeData = new ArrayList<>();

    private final List<IeeeCdfTieLine> tieLines = new ArrayList<>();

    public IeeeCdfModel(IeeeCdfTitle title) {
        this.title = Objects.requireNonNull(title);
    }

    public IeeeCdfTitle getTitle() {
        return title;
    }

    public List<IeeeCdfBus> getBuses() {
        return buses;
    }

    public List<IeeeCdfBranch> getBranches() {
        return branches;
    }

    public List<IeeeCdfLossZone> getLossZones() {
        return lossZones;
    }

    public List<IeeeCdfInterchangeData> getInterchangeData() {
        return interchangeData;
    }

    public List<IeeeCdfTieLine> getTieLines() {
        return tieLines;
    }
}
