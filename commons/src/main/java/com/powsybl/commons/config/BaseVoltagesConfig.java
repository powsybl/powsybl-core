/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class BaseVoltagesConfig {

    private static final String CONFIG_FILE = "base-voltages.yml";

    private List<BaseVoltageConfig> baseVoltages;
    private String defaultProfile;

    public List<BaseVoltageConfig> getBaseVoltages() {
        return baseVoltages;
    }

    public void setBaseVoltages(List<BaseVoltageConfig> baseVoltages) {
        this.baseVoltages = baseVoltages;
    }

    public String getDefaultProfile() {
        return defaultProfile;
    }

    public void setDefaultProfile(String defaultProfile) {
        this.defaultProfile = defaultProfile;
    }

    public static BaseVoltagesConfig fromPlatformConfig() {
        return fromPath(PlatformConfig.defaultConfig().getConfigDir().resolve(CONFIG_FILE));
    }

    public static BaseVoltagesConfig fromInputStream(InputStream configInputStream) {
        Objects.requireNonNull(configInputStream);
        Yaml yaml = new Yaml(new Constructor(BaseVoltagesConfig.class));
        return yaml.load(configInputStream);
    }

    public static BaseVoltagesConfig fromPath(Path configFile) {
        Objects.requireNonNull(configFile);
        if (Files.exists(configFile)) {
            try (InputStream configInputStream = Files.newInputStream(configFile)) {
                return fromInputStream(configInputStream);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            InputStream configInputStream = BaseVoltagesConfig.class.getResourceAsStream("/" + CONFIG_FILE);
            if (configInputStream != null) {
                return fromInputStream(configInputStream);
            } else {
                throw new PowsyblException("No base voltages configuration found");
            }
        }
    }

    public List<String> getProfiles() {
        return getBaseVoltages()
                .stream()
                .map(BaseVoltageConfig::getProfile)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> getBaseVoltageNames(String profile) {
        Objects.requireNonNull(profile);
        return getBaseVoltages()
                .stream()
                .filter(baseVoltage -> baseVoltage.getProfile().equals(profile))
                .map(BaseVoltageConfig::getName)
                .collect(Collectors.toList());
    }

    public Optional<String> getBaseVoltageName(double baseVoltage, String profile) {
        Objects.requireNonNull(profile);
        return getBaseVoltages()
                .stream()
                .filter(v -> v.getProfile().equals(profile)
                        && v.getMinValue() <= baseVoltage
                        && v.getMaxValue() > baseVoltage)
                .map(BaseVoltageConfig::getName)
                .findFirst();
    }

}
