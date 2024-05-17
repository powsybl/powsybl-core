/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.dto;

import com.powsybl.iidm.network.extensions.Coordinate;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Chamseddine Benhamed {@literal <chamseddine.benhamed at rte-france.com>}
 */
public class LineGeoData {

    private String id;
    private String country1;
    private String country2;
    private String substationStart;
    private String substationEnd;
    private List<Coordinate> coordinates;

    public LineGeoData(String id, String country1, String country2, String substationStart, String substationEnd, List<Coordinate> coordinates) {
        this.id = id;
        this.country1 = country1;
        this.country2 = country2;
        this.substationStart = substationStart;
        this.substationEnd = substationEnd;
        this.coordinates = coordinates;
    }

    public String getId() {
        return id;
    }

    public String getCountry1() {
        return country1;
    }

    public String getCountry2() {
        return country2;
    }

    public String getSubstationStart() {
        return substationStart;
    }

    public String getSubstationEnd() {
        return substationEnd;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }
}
