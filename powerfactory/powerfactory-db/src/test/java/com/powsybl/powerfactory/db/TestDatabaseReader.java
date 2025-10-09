/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.db;

import com.powsybl.powerfactory.model.DataAttribute;
import com.powsybl.powerfactory.model.DataAttributeType;

import java.time.Instant;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class TestDatabaseReader implements DatabaseReader {

    @Override
    public boolean isOk() {
        return true;
    }

    @Override
    public void read(String powerFactoryHome, String projectName, DataObjectBuilder builder) {
        builder.createClass("IntPrj");
        builder.createAttribute("IntPrj", DataAttribute.LOC_NAME, DataAttributeType.STRING.ordinal(), "");
        builder.createAttribute("IntPrj", "pCase", DataAttributeType.OBJECT.ordinal(), "");

        builder.createClass("IntPrjfolder");
        builder.createAttribute("IntPrjfolder", DataAttribute.LOC_NAME, DataAttributeType.STRING.ordinal(), "");

        builder.createClass("IntCase");
        builder.createAttribute("IntCase", DataAttribute.LOC_NAME, DataAttributeType.STRING.ordinal(), "");
        builder.createAttribute("IntCase", "iStudyTime", DataAttributeType.INTEGER64.ordinal(), "");

        builder.createClass("ElmNet");
        builder.createAttribute("ElmNet", DataAttribute.LOC_NAME, DataAttributeType.STRING.ordinal(), "");
        builder.createAttribute("ElmNet", "aInt", DataAttributeType.INTEGER.ordinal(), "");
        builder.createAttribute("ElmNet", "aInt64", DataAttributeType.INTEGER64.ordinal(), "");
        builder.createAttribute("ElmNet", "aDouble", DataAttributeType.DOUBLE.ordinal(), "");
        builder.createAttribute("ElmNet", "aObj", DataAttributeType.OBJECT.ordinal(), "");
        builder.createAttribute("ElmNet", "aIntVec", DataAttributeType.INTEGER_VECTOR.ordinal(), "");
        builder.createAttribute("ElmNet", "aInt64Vec", DataAttributeType.INTEGER64_VECTOR.ordinal(), "");
        builder.createAttribute("ElmNet", "aDoubleVec", DataAttributeType.DOUBLE_VECTOR.ordinal(), "");
        builder.createAttribute("ElmNet", "aStrVec", DataAttributeType.STRING_VECTOR.ordinal(), "");
        builder.createAttribute("ElmNet", "aObjVec", DataAttributeType.OBJECT_VECTOR.ordinal(), "");
        builder.createAttribute("ElmNet", "aMat", DataAttributeType.DOUBLE_MATRIX.ordinal(), "");

        builder.createClass("ElmSubstat");

        builder.createObject(0L, "IntPrj");
        builder.setStringAttributeValue(0L, DataAttribute.LOC_NAME, "TestProject");
        builder.setObjectAttributeValue(0L, "pCase", 2L);

        builder.createObject(1L, "IntPrjfolder");
        builder.setStringAttributeValue(1L, DataAttribute.LOC_NAME, "Study Cases");
        builder.setObjectParent(1L, 0L);

        builder.createObject(2L, "IntCase");
        builder.setStringAttributeValue(2L, DataAttribute.LOC_NAME, "TestStudyCase");
        Instant studyTime = Instant.parse("2021-10-30T09:35:25Z");
        builder.setLongAttributeValue(2L, "iStudyTime", studyTime.toEpochMilli());
        builder.setObjectParent(2L, 1L);

        builder.createObject(3L, "IntPrjfolder");
        builder.setStringAttributeValue(3L, DataAttribute.LOC_NAME, "Network Model");
        builder.setObjectParent(3L, 0L);

        builder.createObject(4L, "IntPrjfolder");
        builder.setStringAttributeValue(4L, DataAttribute.LOC_NAME, "Network Data");
        builder.setObjectParent(4L, 3L);

        builder.createObject(5L, "ElmNet");
        builder.setStringAttributeValue(5L, DataAttribute.LOC_NAME, "TestNetwork");
        builder.setIntAttributeValue(5L, "aInt", 3);
        builder.setLongAttributeValue(5L, "aInt64", 494949L);
        builder.setDoubleAttributeValue(5L, "aDouble", 3.34d);
        builder.setObjectAttributeValue(5L, "aObj", 1L);
        builder.setIntVectorAttributeValue(5L, "aIntVec", List.of(67));
        builder.setLongVectorAttributeValue(5L, "aInt64Vec", List.of(6772437L));
        builder.setDoubleVectorAttributeValue(5L, "aDoubleVec", List.of(4.54828d));
        builder.setObjectVectorAttributeValue(5L, "aObjVec", List.of(2L));
        builder.setStringVectorAttributeValue(5L, "aStrVec", List.of("a", "b"));
        builder.setDoubleMatrixAttributeValue(5L, "aMat", 2, 3, List.of(0d, 1d, 2d, 3d, 4d, 5d));
        builder.setObjectParent(5L, 4L);

        builder.createObject(6L, "ElmSubstat");
        builder.setObjectParent(6L, 5L);
    }
}
