/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.db;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.powerfactory.model.PowerFactoryException;
import com.powsybl.powerfactory.model.StudyCase;
import com.powsybl.powerfactory.model.StudyCaseLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Study case loader that is able to connect to PowerFactory DB using C++ API.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(StudyCaseLoader.class)
public class DbStudyCaseLoader implements StudyCaseLoader {

    private static final String DIG_SILENT_DEFAULT_DIR = "C:\\Program Files\\DIgSILENT";
    private static final Pattern POWER_FACTORY_DIR_PATTERN = Pattern.compile("PowerFactory (\\d{4}) SP(\\d+)");

    private final PlatformConfig platformConfig;

    private final DatabaseReader dbReader;

    public DbStudyCaseLoader() {
        this(PlatformConfig.defaultConfig(), new JniDatabaseReader());
    }

    public DbStudyCaseLoader(PlatformConfig platformConfig, DatabaseReader dbReader) {
        this.platformConfig = Objects.requireNonNull(platformConfig);
        this.dbReader = Objects.requireNonNull(dbReader);
    }

    @Override
    public String getExtension() {
        return "IntPrj";
    }

    @Override
    public boolean test(InputStream is) {
        return true;
    }

    private static Optional<String> readPowerFactoryHomeFromConfig(PlatformConfig platformConfig) {
        return platformConfig.getOptionalModuleConfig("power-factory")
                .flatMap(moduleConfig -> Optional.ofNullable(moduleConfig.getStringProperty("home-dir", null)));
    }

    private static String findPowerFactoryHome() {
        Path digSilentDir = Paths.get(DIG_SILENT_DEFAULT_DIR);
        try {
            if (Files.exists(digSilentDir)) {
                try (var pathStream = Files.list(digSilentDir)) {
                    return pathStream.map(path -> path.getFileName().toString())
                            .filter(fileName -> POWER_FACTORY_DIR_PATTERN.matcher(fileName).matches())
                            .max(Comparator.naturalOrder())
                            .orElseThrow(DbStudyCaseLoader::createPowerFactoryInstallNotFound);
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

    @Override
    public StudyCase doLoad(String fileName, InputStream is) {
        String projectName = com.google.common.io.Files.getNameWithoutExtension(fileName);

        // first read from platform config, then try to autodetect
        String powerFactoryHome = readPowerFactoryHomeFromConfig(platformConfig)
                .orElseGet(DbStudyCaseLoader::findPowerFactoryHome);

        // read study cases objects from PowerFactory DB using C++ API
        DataObjectBuilder builder = new DataObjectBuilder();
        dbReader.read(powerFactoryHome, projectName, builder);

        Instant time = Instant.now(); // FIXME get from study case object
        String studyCaseName = "???";
        return new StudyCase(studyCaseName, time, builder.getIndex());
    }
}
