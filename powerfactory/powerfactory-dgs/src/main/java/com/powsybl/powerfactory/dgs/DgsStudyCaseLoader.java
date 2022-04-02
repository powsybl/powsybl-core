/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.dgs;

import com.google.auto.service.AutoService;
import com.google.common.io.Files;
import com.powsybl.powerfactory.model.StudyCase;
import com.powsybl.powerfactory.model.StudyCaseLoader;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(StudyCaseLoader.class)
public class DgsStudyCaseLoader implements StudyCaseLoader {

    @Override
    public String getExtension() {
        return "dgs";
    }

    @Override
    public boolean test(InputStream is) {
        return true;
    }

    @Override
    public StudyCase doLoad(String fileName, InputStream is) {
        String studyCaseName = Files.getNameWithoutExtension(fileName);
        return new DgsReader().read(studyCaseName, new InputStreamReader(is));
    }
}
