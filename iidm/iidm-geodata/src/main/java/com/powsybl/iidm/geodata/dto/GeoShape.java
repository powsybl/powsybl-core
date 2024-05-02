package com.powsybl.iidm.geodata.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.powsybl.iidm.geodata.utils.GeoShapeDeserializer;
import com.powsybl.iidm.network.extensions.Coordinate;

import java.util.List;

/**
 * @author Hugo Marcellin <hugo.marcelin at rte-france.com>
 */

@JsonDeserialize(using = GeoShapeDeserializer.class)
public record GeoShape(List<Coordinate> coordinates) { }
