package com.powsybl.iidm.geodata;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.ImportPostProcessor;
import com.powsybl.iidm.network.Network;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public class OdreGeoDataAdderPostProcessor implements ImportPostProcessor {

    public static final String NAME = "odreGeoDataImporter";
    public static final Map<String, String> DEFAULT_FILE_NAMES = Map.of("substations", "postes-electriques-rte.csv",
            "aerial-lines", "lignes-aeriennes-rte-nv.csv",
            "underground-lines", "lignes-souterraines-rte-nv.csv");

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
            NetworkGeoDataFiller.fillNetworkSubstationsGeoDataFromFile(network, substationsFilePath);
            if (Files.exists(aerialLinesFilePath) && Files.exists(undergroundLinesFilePath)) {
                NetworkGeoDataFiller.fillNetworkLinesGeoDataFromFiles(network,
                        aerialLinesFilePath, undergroundLinesFilePath, substationsFilePath);
            }
        }
    }
}
