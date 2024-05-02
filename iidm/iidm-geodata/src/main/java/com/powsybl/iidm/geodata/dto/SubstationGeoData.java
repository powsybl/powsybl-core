/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.dto;

import com.powsybl.iidm.network.extensions.Coordinate;

/**
 * @author Chamseddine Benhamed {@literal <chamseddine.benhamed at rte-france.com>}
 */
public class SubstationGeoData {

    private String id;
    private String country;
    private Coordinate coordinate;

    public SubstationGeoData(String id, String country, Coordinate coordinate) {
        this.id = id;
        this.country = country;
        this.coordinate = coordinate;
    }

    public String getId() {
        return id;
    }

    public String getCountry() {
        return country;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }
}
