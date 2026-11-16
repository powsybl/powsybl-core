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
public class JsonStudyCaseLoader implements PowerFactoryDataLoader<StudyCase> {

    private final JsonProjectLoader projectLoader = new JsonProjectLoader();

    @Override
    public Class<StudyCase> getDataClass() {
        return StudyCase.class;
    }

    @Override
    public String getExtension() {
        return projectLoader.getExtension();
    }

    @Override
    public boolean test(InputStream is) {
        return projectLoader.test(is);
    }

    @Override
    public StudyCase doLoad(String fileName, InputStream is) {
        return projectLoader.doLoad(fileName, is).getActiveStudyCase();
    }
}
