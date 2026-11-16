/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class BaseVoltagesConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseVoltagesConfig.class);

    private static final String DEFAULT_CONFIG_FILE_NAME = "base-voltages.yml";

    private List<BaseVoltageConfig> baseVoltages = new ArrayList<>();
    private String defaultProfile;

    public List<BaseVoltageConfig> getBaseVoltages() {
        return baseVoltages;
    }

    public void setBaseVoltages(List<BaseVoltageConfig> baseVoltages) {
        this.baseVoltages = baseVoltages == null ? Collections.emptyList() : baseVoltages;
    }

    public String getDefaultProfile() {
        return defaultProfile;
    }

    public void setDefaultProfile(String defaultProfile) {
        this.defaultProfile = Objects.requireNonNull(defaultProfile);
    }

    public static BaseVoltagesConfig fromPlatformConfig() {
        return fromPlatformConfig(PlatformConfig.defaultConfig());
    }

    public static BaseVoltagesConfig fromPlatformConfig(PlatformConfig platformConfig) {
        return fromPlatformConfig(platformConfig, DEFAULT_CONFIG_FILE_NAME);
    }

    public static BaseVoltagesConfig fromPlatformConfig(PlatformConfig platformConfig, String configFileName) {
        return platformConfig.getConfigDir()
                .map(configDir -> fromPath(configDir, configFileName))
                .orElseGet(() -> {
                    LOGGER.warn("Configuration directory not defined in platform config, trying to load file '{}' from resources", configFileName);
                    return fromResources(configFileName);
                });
    }

    public static BaseVoltagesConfig fromInputStream(InputStream configInputStream) {
        Objects.requireNonNull(configInputStream);
        Yaml yaml = new Yaml(new BaseVoltagesConfigConstructor());
        return yaml.load(configInputStream);
    }

    public static BaseVoltagesConfig fromPath(Path configDir, String configFileName) {
        Objects.requireNonNull(configDir);
        Objects.requireNonNull(configFileName);
        Path configFile = configDir.resolve(configFileName);
        if (Files.exists(configFile)) {
            try (InputStream configInputStream = Files.newInputStream(configFile)) {
                return fromInputStream(configInputStream);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            LOGGER.warn("Base voltage configuration file '{}' not found, trying to load file '{}' from resources", configFile, configFileName);
            return fromResources(configFileName);
        }
    }

    private static BaseVoltagesConfig fromResources(String configFileName) {
        InputStream configInputStream = BaseVoltagesConfig.class.getResourceAsStream("/" + configFileName);
        if (configInputStream != null) {
            return fromInputStream(configInputStream);
        } else {
            throw new PowsyblException("No base voltages configuration found in resources: " + configFileName);
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

    private static class BaseVoltagesConfigConstructor extends Constructor {
        private static final List<String> BASE_VOLTAGES_CONFIG_REQUIRED_FIELDS = Arrays.asList("baseVoltages", "defaultProfile");
        private static final List<String> BASE_VOLTAGE_CONFIG_REQUIRED_FIELDS = Arrays.asList("name", "minValue", "maxValue", "profile");

        BaseVoltagesConfigConstructor() {
            super(BaseVoltagesConfig.class, new LoaderOptions());
        }

        @Override
        protected Object constructObject(Node node) {
            if (node.getTag().equals(rootTag)) {
                checkRequiredFields(node, new LinkedList<>(BASE_VOLTAGES_CONFIG_REQUIRED_FIELDS), BaseVoltagesConfig.class);
            } else if (node.getType().equals(BaseVoltageConfig.class)) {
                checkRequiredFields(node, new LinkedList<>(BASE_VOLTAGE_CONFIG_REQUIRED_FIELDS), node.getType());
            }
            return super.constructObject(node);
        }

        private void checkRequiredFields(Node node, List<String> requiredFields, Class<?> aClass) {
            if (node instanceof MappingNode mappingNode) {
                for (NodeTuple nodeTuple : mappingNode.getValue()) {
                    Node keyNode = nodeTuple.getKeyNode();
                    if (keyNode instanceof ScalarNode scalarNode) {
                        requiredFields.remove(scalarNode.getValue());
                    }
                }
            }
            if (!requiredFields.isEmpty()) {
                throw new YAMLException(aClass + " is missing " + String.join(", ", requiredFields));
            }
        }
    }
}
