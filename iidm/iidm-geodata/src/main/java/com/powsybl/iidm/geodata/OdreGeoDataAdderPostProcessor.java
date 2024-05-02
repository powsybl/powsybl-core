package com.powsybl.iidm.geodata;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.ImportPostProcessor;
import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
@AutoService(ImportPostProcessor.class)
public class OdreGeoDataAdderPostProcessor implements ImportPostProcessor {

    public static final String NAME = "odreGeoDataImporter";
    public static final Map<String, String> DEFAULT_FILE_NAMES = Map.of("substations", "postes-electriques-rte.csv",
            "aerial-lines", "lignes-aeriennes-rte-nv.csv",
            "underground-lines", "lignes-souterraines-rte-nv.csv");

    private static final Logger LOGGER = LoggerFactory.getLogger(OdreGeoDataAdderPostProcessor.class);
    private final Path substationsFilePath;
    private final Path aerialLinesFilePath;
    private final Path undergroundLinesFilePath;

    public OdreGeoDataAdderPostProcessor() {
        this(PlatformConfig.defaultConfig());
    }

    public OdreGeoDataAdderPostProcessor(PlatformConfig config) {
        this(getEquipmentFileFromConfig(config, "substations"),
                getEquipmentFileFromConfig(config, "aerial-lines"),
                getEquipmentFileFromConfig(config, "underground-lines"));
    }

    public OdreGeoDataAdderPostProcessor(Path substationsFilePath, Path aerialLinesFilePath,
                                         Path undergroundLinesFilePath) {
        this.substationsFilePath = substationsFilePath;
        this.aerialLinesFilePath = aerialLinesFilePath;
        this.undergroundLinesFilePath = undergroundLinesFilePath;
    }

    private static Path getEquipmentFileFromConfig(PlatformConfig platformConfig, String type) {
        Objects.requireNonNull(platformConfig);
        return platformConfig.getOptionalModuleConfig("odre-geodata-post-processor")
                .flatMap(config -> config.getOptionalPathProperty(type))
                .or(() -> platformConfig.getConfigDir().map(dir -> dir.resolve(DEFAULT_FILE_NAMES.get(type))))
                .orElseThrow(() -> new PowsyblException("No file path nor configuration directory defined in platform config for " + type));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void process(Network network, ComputationManager computationManager) throws Exception {
        if (Files.exists(substationsFilePath)) {
            OdreGeoDataAdder.fillNetworkSubstationsGeoDataFromFile(network, substationsFilePath);
            boolean aerialLinesPresent = Files.exists(aerialLinesFilePath);
            boolean undergroundLinesPresent = Files.exists(undergroundLinesFilePath);
            if (aerialLinesPresent && undergroundLinesPresent) {
                OdreGeoDataAdder.fillNetworkLinesGeoDataFromFiles(network,
                        aerialLinesFilePath, undergroundLinesFilePath, substationsFilePath);
            } else {
                String missingFiles = aerialLinesPresent ? "" : aerialLinesFilePath + " ";
                missingFiles.concat(undergroundLinesPresent ? "" : undergroundLinesFilePath.toString());
                LOGGER.warn("Could not load lines geographical data, file(s) not found : {}", missingFiles);
            }
        } else {
            LOGGER.warn("Could not load substations geographical data, file not found : {}", substationsFilePath);
        }
    }
}
