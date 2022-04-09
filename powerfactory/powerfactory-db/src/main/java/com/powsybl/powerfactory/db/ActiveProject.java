/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ActiveProject {

    private final String name;

    public ActiveProject(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    public static Optional<ActiveProject> read(InputStream is) {
        try (is) {
            var properties = new Properties();
            properties.load(is);
            return Optional.ofNullable(properties.getProperty("projectName"))
                    .map(ActiveProject::new);
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
