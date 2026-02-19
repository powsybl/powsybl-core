/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.config.test;

import com.google.auto.service.AutoService;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.ModuleConfigRepository;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigProvider;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Provides a PlatformConfig reading config from the classpath only.
 *
 * The files must be listed manually in a "filelist.txt" file. The "filelist.txt" and
 * the test files are read from the classpath relative to this class.
 *
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 */
@AutoService(PlatformConfigProvider.class)
public class TestPlatformConfigProvider implements PlatformConfigProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestPlatformConfigProvider.class);

    private static final String NAME = "test";

    private static final String FILELIST_PATH = "filelist.txt";
    //Using a static for the FileSystem to show that it is a singleton
    //and won't be closed until the jvm is shut down.
    private static final FileSystem JIMFS = Jimfs.newFileSystem(Configuration.unix());

    private static final String CONFIG_NAME = "config";
    static final String CONFIG_DIR = "unittests";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public PlatformConfig getPlatformConfig() {

        InputStream resourceList = TestPlatformConfigProvider.class.getResourceAsStream(FILELIST_PATH);
        List<String> resources;
        if (resourceList != null) {
            resources = IOUtils.readLines(resourceList, StandardCharsets.UTF_8);
        } else {
            resources = Collections.emptyList();
        }

        Path cfgDir;
        try {
            cfgDir = Files.createDirectories(JIMFS.getPath(CONFIG_DIR).toAbsolutePath());
            for (String resource : resources) {
                // The resources have relative paths (no leading slash) with full package path.
                Path dest = cfgDir.resolve(resource);
                LOGGER.info("Copying classpath resource: {} -> {}", resource, dest);
                Files.createDirectories(dest.getParent());
                Files.copy(TestPlatformConfigProvider.class.getResourceAsStream(resource), dest);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to initialize test config", e);
        }
        ModuleConfigRepository repository = PlatformConfig.loadModuleRepository(cfgDir, CONFIG_NAME);
        return new PlatformConfig(repository, cfgDir);
    }

}
