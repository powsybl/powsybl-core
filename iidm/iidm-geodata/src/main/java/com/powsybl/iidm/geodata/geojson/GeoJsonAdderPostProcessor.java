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

import static com.powsybl.iidm.geodata.geojson.GeoJsonDataAdder.fillNetworkLinesGeoDataFromFile;
import static com.powsybl.iidm.geodata.geojson.GeoJsonDataAdder.fillNetworkSubstationsGeoDataFromFile;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
@AutoService(ImportPostProcessor.class)
public class GeoJsonAdderPostProcessor implements ImportPostProcessor {

    public static final String SUBSTATIONS = "substations";
    public static final String LINES = "lines";
    public static final Map<String, String> DEFAULT_FILE_NAMES = Map.of(SUBSTATIONS, "substations.geojson",
        LINES, "lines.geojson");
    public static final String NAME = "geoJsonImporter";
    private static final Logger LOGGER = LoggerFactory.getLogger(GeoJsonAdderPostProcessor.class);
    private final Path substationsFilePath;
    private final Path linesFilePath;
    private final boolean forceGeoDataComputation;

    public GeoJsonAdderPostProcessor() {
        this(PlatformConfig.defaultConfig());
    }

    public GeoJsonAdderPostProcessor(PlatformConfig config) {
        this(getEquipmentFileFromConfig(config, SUBSTATIONS), true, getEquipmentFileFromConfig(config, LINES));
    }

    public GeoJsonAdderPostProcessor(PlatformConfig config, boolean forceGeoDataComputation) {
        this(getEquipmentFileFromConfig(config, SUBSTATIONS), forceGeoDataComputation, getEquipmentFileFromConfig(config, LINES));
    }

    public GeoJsonAdderPostProcessor(Path substationsFilePath, boolean forceGeoDataComputation, Path linesFilePath) {
        this.substationsFilePath = substationsFilePath;
        this.linesFilePath = linesFilePath;
        this.forceGeoDataComputation = forceGeoDataComputation;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void process(Network network, ComputationManager computationManager) throws IOException {
        boolean substationsFilePresent = Files.isRegularFile(substationsFilePath);
        boolean linesFilePresent = Files.isRegularFile(linesFilePath);
        if (substationsFilePresent && linesFilePresent) {
            fillNetworkSubstationsGeoDataFromFile(network, substationsFilePath, forceGeoDataComputation);
            fillNetworkLinesGeoDataFromFile(network, linesFilePath, forceGeoDataComputation);
        } else {
            if (!substationsFilePresent) {
                LOGGER.warn("Could not load substations geographical data, file not found : {}", substationsFilePath);
            }
            if (!linesFilePresent) {
                LOGGER.warn("Could not load lines geographical data, file not found : {}", linesFilePath);
            }
        }
    }

    private static Path getEquipmentFileFromConfig(PlatformConfig platformConfig, String type) {
        Objects.requireNonNull(platformConfig);
        return platformConfig.getOptionalModuleConfig("geo-json-importer-post-processor")
            .flatMap(config -> config.getOptionalPathProperty(type))
            .or(() -> platformConfig.getConfigDir().map(dir -> dir.resolve(DEFAULT_FILE_NAMES.get(type))))
            .orElseThrow(() -> new PowsyblException("No file path nor configuration directory defined in platform config for " + type));
    }
}
