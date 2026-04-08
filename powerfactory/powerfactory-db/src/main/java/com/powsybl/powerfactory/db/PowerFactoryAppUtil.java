/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.db;

import com.google.re2j.Pattern;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.powerfactory.model.PowerFactoryException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class PowerFactoryAppUtil {

    static final String DIG_SILENT_DEFAULT_DIR = "C:\\Program Files\\DIgSILENT";

    private static final Pattern POWER_FACTORY_DIR_PATTERN = Pattern.compile("PowerFactory (\\d{4}) SP(\\d+)");

    private PowerFactoryAppUtil() {
    }

    public static Path getHomeDir(PlatformConfig platformConfig) {
        // first read from platform config, then try to autodetect most recent local installation
        String powerFactoryHome = readHomeFromConfig(platformConfig)
                .orElseGet(() -> findHomeDirName(platformConfig));
        return getDigSilentDir(platformConfig).resolve(powerFactoryHome).toAbsolutePath();
    }

    public static Optional<String> readHomeFromConfig(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        return platformConfig.getOptionalModuleConfig("power-factory")
                .flatMap(moduleConfig -> Optional.ofNullable(moduleConfig.getStringProperty("home-dir", null)));
    }

    public static Path getDigSilentDir(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        return platformConfig.getConfigDir()
                .map(cd -> cd.getFileSystem().getPath(DIG_SILENT_DEFAULT_DIR))
                .orElse(Path.of(DIG_SILENT_DEFAULT_DIR));
    }

    public static String findHomeDirName(PlatformConfig platformConfig) {
        Path digSilentDir = getDigSilentDir(platformConfig);
        try {
            if (Files.exists(digSilentDir)) {
                try (var pathStream = Files.list(digSilentDir)) {
                    return pathStream.map(path -> path.getFileName().toString())
                            .filter(fileName -> POWER_FACTORY_DIR_PATTERN.matcher(fileName).matches())
                            .max(Comparator.naturalOrder())
                            .orElseThrow(PowerFactoryAppUtil::createPowerFactoryInstallNotFound);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        throw createPowerFactoryInstallNotFound();
    }

    private static PowerFactoryException createPowerFactoryInstallNotFound() {
        return new PowerFactoryException("PowerFactory installation not found");
    }
}
