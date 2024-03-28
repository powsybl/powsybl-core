/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.model;

import com.powsybl.commons.test.AbstractSerDeTest;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractPowerFactoryTest extends AbstractSerDeTest {

    protected DataObjectIndex index;
    protected DataObject objBar;
    protected DataObject objBaz;
    protected DataObject objFoo;
    protected DataObject elmNet;

    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        DataClass clsFoo = DataClass.init("ElmFoo")
                .addAttribute(new DataAttribute("i", DataAttributeType.INTEGER))
                .addAttribute(new DataAttribute("l", DataAttributeType.INTEGER64))
                .addAttribute(new DataAttribute("f", DataAttributeType.FLOAT))
                .addAttribute(new DataAttribute("d", DataAttributeType.DOUBLE))
                .addAttribute(new DataAttribute("o", DataAttributeType.OBJECT))
                .addAttribute(new DataAttribute("iv", DataAttributeType.INTEGER_VECTOR))
                .addAttribute(new DataAttribute("lv", DataAttributeType.INTEGER64_VECTOR))
                .addAttribute(new DataAttribute("fv", DataAttributeType.FLOAT_VECTOR))
                .addAttribute(new DataAttribute("dv", DataAttributeType.DOUBLE_VECTOR))
                .addAttribute(new DataAttribute("ov", DataAttributeType.OBJECT_VECTOR))
                .addAttribute(new DataAttribute("sv", DataAttributeType.STRING_VECTOR))
                .addAttribute(new DataAttribute("dm", DataAttributeType.DOUBLE_MATRIX));
        DataClass clsBar = DataClass.init("ElmBar");
        DataClass clsBaz = DataClass.init("ElmBaz");
        DataClass clsElmNet = DataClass.init("ElmNet");

        index = new DataObjectIndex();

        elmNet = new DataObject(0L, clsElmNet, index)
                .setLocName("net");
        objFoo = new DataObject(1L, clsFoo, index)
                .setLocName("foo")
                .setIntAttributeValue("i", 3)
                .setLongAttributeValue("l", 49494L)
                .setFloatAttributeValue("f", 3.4f)
                .setDoubleAttributeValue("d", 3494.93939d)
                .setObjectAttributeValue("o", 2L)
                .setIntVectorAttributeValue("iv", List.of(1, 2, 3))
                .setFloatVectorAttributeValue("fv", List.of(1.3f, 2.3f, 3.5f))
                .setLongVectorAttributeValue("lv", List.of(4L, 5L, 6943953495493593L))
                .setDoubleVectorAttributeValue("dv", List.of(1.3949d, 2.34d, 3.1223d))
                .setObjectVectorAttributeValue("ov", List.of(3L))
                .setStringVectorAttributeValue("sv", List.of("AA", "BBB"))
                .setDoubleMatrixAttributeValue("dm", new BlockRealMatrix(new double[][] {{1d, 2d, 3d}, {4d, 5d, 6d}}));
        objFoo.setParent(elmNet);
        objBar = new DataObject(2L, clsBar, index)
                .setLocName("bar");
        objBar.setParent(objFoo);
        objBaz = new DataObject(3L, clsBaz, index)
                .setLocName("baz");
        objBaz.setParent(objFoo);
    }
}
