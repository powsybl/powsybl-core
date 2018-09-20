package com.powsybl.commons.config;

import com.fasterxml.jackson.annotation.JsonValue;
import com.powsybl.commons.PowsyblException;

import java.util.Arrays;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public enum VersionConfig {
    VERSION_1_0("1.0"),
    LATEST_VERSION("1.1");

    private String version;

    VersionConfig(final String version) {
        this.version = version;
    }

    @Override
    @JsonValue
    public String toString() {
        return version;
    }

    public static VersionConfig valueOfByString(String versionString) {
        return Arrays.stream(values())
                .filter(v -> versionString.equals(v.toString()))
                .findFirst()
                .orElseThrow(() -> new PowsyblException("Unexpected version : this version is not supported or does not exist"));
    }

}
