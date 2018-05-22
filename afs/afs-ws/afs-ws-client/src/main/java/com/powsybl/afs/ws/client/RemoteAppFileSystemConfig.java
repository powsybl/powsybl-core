/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.client;

import com.powsybl.afs.ws.storage.RemoteAppStorage;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.exceptions.UncheckedUriSyntaxException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Ali Tahanout <ali.tahanout at rte-france.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RemoteAppFileSystemConfig {

    private final URI url;
    private final String fileSystemName;

    public RemoteAppFileSystemConfig(URI url, String fileSystemName) {
        this.url = Objects.requireNonNull(url);
        this.fileSystemName = Objects.requireNonNull(fileSystemName);
    }

    public static List<RemoteAppFileSystemConfig> load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static List<RemoteAppFileSystemConfig> load(PlatformConfig platformConfig) {
        List<RemoteAppFileSystemConfig> configs = new ArrayList<>();
        ModuleConfig moduleConfig = platformConfig.getModuleConfigIfExists("rest-app-file-system");
        if (moduleConfig != null) {
            addConfig(moduleConfig, configs, null);

            int maxAdditionalDriveCount = moduleConfig.getIntProperty("max-additional-url-count", 0);
            for (int i = 0; i < maxAdditionalDriveCount; i++) {
                addConfig(moduleConfig, configs, i);
            }
        }
        return configs;
    }

    private static void addConfig(ModuleConfig moduleConfig, List<RemoteAppFileSystemConfig> configs, Integer num) {
        String suffix = num != null ? "- " + num : "";
        if (moduleConfig.hasProperty("url-address" + suffix)) {
            URI url;
            try {
                url = new URI(moduleConfig.getStringProperty("url-address" + suffix));
            } catch (URISyntaxException e) {
                throw new UncheckedUriSyntaxException(e);
            }
            List<String> fileSystemNames = RemoteAppStorage.getFileSystemNames(url);
            for (String fileSystemName : fileSystemNames) {
                configs.add(new RemoteAppFileSystemConfig(url, fileSystemName));
            }
        }
    }

    public URI getUrl() {
        return url;
    }

    public String getFileSystemName() {
        return fileSystemName;
    }
}
