package com.powsybl.iidm.geodata.geojson;

import com.powsybl.iidm.geodata.utils.InputUtils;
import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static com.powsybl.iidm.geodata.geojson.GeoJsonDataParser.parseLines;
import static com.powsybl.iidm.geodata.geojson.GeoJsonDataParser.parseSubstations;
import static com.powsybl.iidm.geodata.utils.NetworkGeoDataExtensionsAdder.fillLineGeoData;
import static com.powsybl.iidm.geodata.utils.NetworkGeoDataExtensionsAdder.fillSubstationGeoData;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class GeoJsonDataAdder {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoJsonDataAdder.class);

    protected GeoJsonDataAdder() {
    }

    public static void fillNetworkSubstationsGeoDataFromFile(Network network, Path path, boolean forceGeoDataComputation) throws IOException {
        AtomicInteger substationsWithNewData = new AtomicInteger();
        AtomicInteger substationsWithOldData = new AtomicInteger();
        AtomicInteger unknownSubstations = new AtomicInteger();
        try (Reader reader = InputUtils.toReader(path)) {
            parseSubstations(reader, (id, coordinates) ->
                fillSubstationGeoData(network, id, coordinates, forceGeoDataComputation, substationsWithNewData, substationsWithOldData, unknownSubstations));
        }
        LOGGER.info("{} substations with data added - {} substations with data updated - {} unknown substations", substationsWithNewData.get(), substationsWithOldData.get(), unknownSubstations.get());
    }

    public static void fillNetworkLinesGeoDataFromFile(Network network, Path path, boolean forceGeoDataComputation) throws IOException {
        AtomicInteger linesWithNewData = new AtomicInteger();
        AtomicInteger linesWithOldData = new AtomicInteger();
        AtomicInteger unknownLines = new AtomicInteger();
        try (Reader reader = InputUtils.toReader(path)) {
            parseLines(reader, (id, coordinates) ->
                fillLineGeoData(network, id, coordinates, forceGeoDataComputation, linesWithNewData, linesWithOldData, unknownLines));
        }
        LOGGER.info("{} lines with data added - {} lines with data updated - {} unknown lines", linesWithNewData.get(), linesWithOldData.get(), unknownLines.get());
    }
}
