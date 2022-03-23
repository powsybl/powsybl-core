/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.db;

import com.google.auto.service.AutoService;
import com.google.common.io.Files;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.StudyCase;
import com.powsybl.powerfactory.model.StudyCaseLoader;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Study case loader that is able to connect to PowerFactory DB using C++ API.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(StudyCaseLoader.class)
public class DbStudyCaseLoader implements StudyCaseLoader {

    @Override
    public String getExtension() {
        return "IntPrj";
    }

    @Override
    public boolean test(InputStream is) {
        return true;
    }

    private native void readDb(String studyCaseName, DataObjectsBuilder builder);

    @Override
    public StudyCase doLoad(String fileName, InputStream is) {
        String studyCaseName = Files.getNameWithoutExtension(fileName);

        // read study cases objects from PowerFactory DB using C++ API
        DataObjectsBuilder builder = new DataObjectsBuilder();
        readDb(studyCaseName, builder);

        Instant time = Instant.now(); // FIXME get from study case object
        List<DataObject> elmNets = builder.getObjects()
                .stream()
                .filter(obj -> obj.getDataClassName().equals("ElmNet"))
                .collect(Collectors.toList());
        return new StudyCase(studyCaseName, time, elmNets);
    }
}
