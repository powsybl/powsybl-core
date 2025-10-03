package com.powsybl.iidm.geodata.geojson;

import com.powsybl.iidm.geodata.utils.InputUtils;
import com.powsybl.iidm.network.extensions.Coordinate;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.powsybl.iidm.geodata.geojson.GeoJsonDataParser.parseLines;
import static com.powsybl.iidm.geodata.geojson.GeoJsonDataParser.parseSubstations;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class GeoJsonDataLoader {

    protected GeoJsonDataLoader() {
    }

    public static Map<String, Coordinate> getSubstationsCoordinates(Path path) throws IOException {
        try (Reader reader = InputUtils.toReader(path)) {
            return parseSubstations(reader);
        }
    }

    public static Map<String, List<Coordinate>> getLinesCoordinates(Path path) throws IOException {
        try (Reader reader = InputUtils.toReader(path)) {
            return parseLines(reader);
        }
    }
}
