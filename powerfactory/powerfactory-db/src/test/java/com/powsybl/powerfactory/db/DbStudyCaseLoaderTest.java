/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.db;

import com.powsybl.powerfactory.model.DataAttribute;
import com.powsybl.powerfactory.model.DataAttributeType;
import com.powsybl.powerfactory.model.StudyCase;
import com.powsybl.powerfactory.model.StudyCaseLoader;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DbStudyCaseLoaderTest {

    private static class TestDatabaseReader implements DatabaseReader {

        @Override
        public void read(String projectName, DataObjectBuilder builder) {
            builder.createClass("ElmNet");
            builder.createAttribute("ElmNet", DataAttribute.LOC_NAME, DataAttributeType.STRING.ordinal(), "");
            builder.createObject(0L, "ElmNet", -1);
            builder.setStringAttributeValue(0L, DataAttribute.LOC_NAME, "TestGrid");
        }
    }

    @Test
    public void test() {
        StudyCase studyCase = StudyCaseLoader.load("Test.IntPrj",
            () -> null,
            List.of(new DbStudyCaseLoader(new TestDatabaseReader())))
                .orElseThrow();
        assertEquals("???", studyCase.getName());
    }
}
