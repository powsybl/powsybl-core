package com.powsybl.iidm.geodata.dto;

import com.powsybl.iidm.network.extensions.Coordinate;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
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
