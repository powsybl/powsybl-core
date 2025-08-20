package com.powsybl.iidm.geodata.geojson;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.ImportPostProcessor;
import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

import static com.powsybl.iidm.geodata.geojson.GeoJsonDataAdder.fillNetworkLinesGeoDataFromFiles;
import static com.powsybl.iidm.geodata.geojson.GeoJsonDataAdder.fillNetworkSubstationsGeoDataFromFile;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
@AutoService(ImportPostProcessor.class)
public class GeoJsonAdderPostProcessor implements ImportPostProcessor {

    public static final Map<String, String> DEFAULT_FILE_NAMES = Map.of("substations", "substations.geojson",
        "lines", "lines.geojson");
    public static final String NAME = "geoJsonImporter";
    private static final Logger LOGGER = LoggerFactory.getLogger(GeoJsonAdderPostProcessor.class);
    private final Path substationsFilePath;
    private final Path linesFilePath;

    public GeoJsonAdderPostProcessor(PlatformConfig config) {
        this(getEquipmentFileFromConfig(config, "substations"), getEquipmentFileFromConfig(config, "lines"));
    }

    public GeoJsonAdderPostProcessor(Path substationsFilePath, Path linesFilePath) {
        this.substationsFilePath = substationsFilePath;
        this.linesFilePath = linesFilePath;
    }

    private static Path getEquipmentFileFromConfig(PlatformConfig platformConfig, String type) {
        Objects.requireNonNull(platformConfig);
        return platformConfig.getOptionalModuleConfig("geo-json-importer-post-processor")
            .flatMap(config -> config.getOptionalPathProperty(type))
            .or(() -> platformConfig.getConfigDir().map(dir -> dir.resolve(DEFAULT_FILE_NAMES.get(type))))
            .orElseThrow(() -> new PowsyblException("No file path nor configuration directory defined in platform config for " + type));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void process(Network network, ComputationManager computationManager) throws IOException {
        boolean substationsFilePresent = Files.exists(substationsFilePath);
        boolean linesFilePresent = Files.exists(linesFilePath);
        if (substationsFilePresent && linesFilePresent) {
            fillNetworkSubstationsGeoDataFromFile(network, substationsFilePath);
            fillNetworkLinesGeoDataFromFiles(network, linesFilePath);
        } else {
            if (!substationsFilePresent) {
                LOGGER.warn("Could not load substations geographical data, file not found : {}", substationsFilePath);
            }
            if (!linesFilePresent) {
                LOGGER.warn("Could not load lines geographical data, file not found : {}", linesFilePath);
            }
        }
    }
}
