/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;

import java.nio.file.FileSystem;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class InMemoryModuleConfigRepository extends AbstractModuleConfigRepository {

    private final FileSystem fileSystem;

    public InMemoryModuleConfigRepository(FileSystem fileSystem) {
        this.fileSystem = Objects.requireNonNull(fileSystem);
    }

    public MapModuleConfig createModuleConfig(String name) {
        MapModuleConfig config = configs.get(name);
        if (config != null) {
            throw new PowsyblException("Module " + name + " already exists");
        }
        config = new MapModuleConfig(fileSystem);
        configs.put(name, config);
        return config;
    }
}
