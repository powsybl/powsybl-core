package com.powsybl.iidm.geodata.geojson;

import com.powsybl.iidm.network.Network;

import java.io.IOException;
import java.nio.file.Path;

import static com.powsybl.iidm.geodata.geojson.GeoJsonDataLoader.getLinesCoordinates;
import static com.powsybl.iidm.geodata.geojson.GeoJsonDataLoader.getSubstationsCoordinates;
import static com.powsybl.iidm.geodata.utils.NetworkGeoDataExtensionsAdder.fillNetworkLinesGeoData;
import static com.powsybl.iidm.geodata.utils.NetworkGeoDataExtensionsAdder.fillNetworkSubstationsGeoData;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class GeoJsonDataAdder {

    protected GeoJsonDataAdder() {
    }

    public static void fillNetworkSubstationsGeoDataFromFile(Network network, Path path) throws IOException {
        fillNetworkSubstationsGeoData(network, getSubstationsCoordinates(path));
    }

    public static void fillNetworkLinesGeoDataFromFiles(Network network, Path path) throws IOException {
        fillNetworkLinesGeoData(network, getLinesCoordinates(path));
    }
}
