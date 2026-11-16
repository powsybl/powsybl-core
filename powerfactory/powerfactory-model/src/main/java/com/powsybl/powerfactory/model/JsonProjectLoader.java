/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.model;

import com.google.auto.service.AutoService;

import java.io.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(PowerFactoryDataLoader.class)
public class JsonProjectLoader implements PowerFactoryDataLoader<Project> {

    @Override
    public Class<Project> getDataClass() {
        return Project.class;
    }

    @Override
    public String getExtension() {
        return "json";
    }

    @Override
    public boolean test(InputStream is) {
        try {
            is.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return true;
    }

    @Override
    public Project doLoad(String fileName, InputStream is) {
        try (Reader reader = new InputStreamReader(is)) {
            return Project.parseJson(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
