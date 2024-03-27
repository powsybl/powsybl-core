/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.model;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class DataObjectTest {

    private static DataClass createFooClass() {
        return DataClass.init("ElmFoo")
                .addAttribute(new DataAttribute("i", DataAttributeType.INTEGER))
                .addAttribute(new DataAttribute("f", DataAttributeType.FLOAT))
                .addAttribute(new DataAttribute("d", DataAttributeType.DOUBLE))
                .addAttribute(new DataAttribute("l", DataAttributeType.INTEGER64))
                .addAttribute(new DataAttribute("iv", DataAttributeType.INTEGER_VECTOR))
                .addAttribute(new DataAttribute("fv", DataAttributeType.FLOAT_VECTOR))
                .addAttribute(new DataAttribute("dv", DataAttributeType.DOUBLE_VECTOR))
                .addAttribute(new DataAttribute("m", DataAttributeType.DOUBLE_MATRIX));
    }

    @Test
    void testClass() {
        DataClass clsFoo = createFooClass();
        assertEquals("ElmFoo", clsFoo.getName());
        assertEquals("DataClass(name=ElmFoo)", clsFoo.toString());
        assertEquals(9, clsFoo.getAttributes().size());
        DataAttribute attr = clsFoo.getAttributeByName("i");
        assertEquals("i", attr.getName());
        assertEquals(DataAttributeType.INTEGER, attr.getType());
        assertEquals("", attr.getDescription());
        assertEquals("DataAttribute(name=i, type=INTEGER, description=)", attr.toString());
    }

    @Test
    void testObj() throws IOException {
        DataObjectIndex index = new DataObjectIndex();
        DataClass clsFoo = createFooClass();
        DataObject objFoo = new DataObject(0L, clsFoo, index)
                .setLocName("foo");
        assertEquals(0L, objFoo.getId());
        assertSame(clsFoo, objFoo.getDataClass());
        assertEquals("ElmFoo", objFoo.getDataClassName());
        assertEquals(List.of(DataAttribute.LOC_NAME, "i", "f", "d", "l", "iv", "fv", "dv", "m"), objFoo.getAttributeNames());
        assertNull(objFoo.getParent());
        assertTrue(objFoo.getChildren().isEmpty());

        DataClass clsBar = DataClass.init("ElmBar");
        DataObject objBar = new DataObject(1L, clsBar, index)
                .setLocName("bar");
        objFoo.setParent(objBar);
        assertEquals(1, objBar.getChildren().size());
        assertSame(objBar, objFoo.getParent());
        assertSame(objFoo, objBar.getChild("foo").orElseThrow());
        assertTrue(objBar.getChild("foo2").isEmpty());
        assertEquals(1, objBar.getChildrenByClass("ElmFoo").size());
        assertTrue(objBar.getChildrenByClass("ElmFoo2").isEmpty());
        assertTrue(objBar.findFirstChildByClass("ElmFoo").isPresent());
        assertSame(objFoo, objBar.findFirstChildByClass("ElmFoo").orElseThrow());
        assertEquals(List.of(objBar, objFoo), objFoo.getPath());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(baos)) {
            objBar.print(ps, DataObject::toString);
            ps.flush();
            assertEquals("bar.ElmBar" + System.lineSeparator() + "    bar\\foo.ElmFoo" + System.lineSeparator(), baos.toString());
        }

        List<DataObject> foundObjs = objFoo.search(".*f.*");
        assertEquals(1, foundObjs.size());
        assertSame(objFoo, foundObjs.get(0));
    }

    @Test
    void testStringAttribute() {
        DataObjectIndex index = new DataObjectIndex();
        DataClass clsFoo = createFooClass();
        DataObject objFoo = new DataObject(0L, clsFoo, index);
        assertFalse(objFoo.findStringAttributeValue(DataAttribute.LOC_NAME).isPresent());
        objFoo.setStringAttributeValue(DataAttribute.LOC_NAME, "foo");
        assertEquals("foo", objFoo.getLocName());
        assertEquals("foo.ElmFoo", objFoo.getFullName());
        assertEquals("foo.ElmFoo", objFoo.toString());
        assertTrue(objFoo.findStringAttributeValue(DataAttribute.LOC_NAME).isPresent());
        assertEquals("foo", objFoo.findStringAttributeValue(DataAttribute.LOC_NAME).orElseThrow());
        assertFalse(objFoo.findIntAttributeValue("s").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getStringAttributeValue("s"));
        assertEquals("foo", objFoo.getStringAttributeValue(DataAttribute.LOC_NAME));
        assertEquals(Map.of(DataAttribute.LOC_NAME, "foo"), objFoo.getAttributeValues());
        assertEquals("foo", objFoo.getAttributeValue(DataAttribute.LOC_NAME));
    }

    @Test
    void testIntAttribute() {
        DataObjectIndex index = new DataObjectIndex();
        DataClass clsFoo = createFooClass();
        DataObject objFoo = new DataObject(0L, clsFoo, index);
        assertFalse(objFoo.findIntAttributeValue("i").isPresent());
        objFoo.setIntAttributeValue("i", 3);
        assertTrue(objFoo.findIntAttributeValue("i").isPresent());
        assertEquals(3, objFoo.findIntAttributeValue("i").orElseThrow());
        assertFalse(objFoo.findIntAttributeValue("ii").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getIntAttributeValue("ii"));
        assertEquals(3, objFoo.getIntAttributeValue("i"));
        assertEquals(3L, objFoo.getLongAttributeValue("i"));
    }

    @Test
    void testLongAttribute() {
        DataObjectIndex index = new DataObjectIndex();
        DataClass clsFoo = createFooClass();
        DataObject objFoo = new DataObject(0L, clsFoo, index);
        assertFalse(objFoo.findLongAttributeValue("l").isPresent());
        objFoo.setLongAttributeValue("l", 4L);
        assertTrue(objFoo.findLongAttributeValue("l").isPresent());
        assertEquals(4L, objFoo.findLongAttributeValue("l").orElseThrow());
        assertFalse(objFoo.findLongAttributeValue("ll").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getLongAttributeValue("ll"));
        assertEquals(4L, objFoo.getLongAttributeValue("l"));
    }

    @Test
    void testFloatAttribute() {
        DataObjectIndex index = new DataObjectIndex();
        DataClass clsFoo = createFooClass();
        DataObject objFoo = new DataObject(0L, clsFoo, index);
        assertFalse(objFoo.findFloatAttributeValue("f").isPresent());
        objFoo.setFloatAttributeValue("f", 3.14f);
        assertTrue(objFoo.findFloatAttributeValue("f").isPresent());
        assertEquals(3.14f, objFoo.findFloatAttributeValue("f").orElseThrow(), 0f);
        assertFalse(objFoo.findFloatAttributeValue("ff").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getFloatAttributeValue("ff"));
        assertEquals(3.14f, objFoo.getFloatAttributeValue("f"), 0f);
    }

    @Test
    void testDoubleAttribute() {
        DataObjectIndex index = new DataObjectIndex();
        DataClass clsFoo = createFooClass();
        DataObject objFoo = new DataObject(0L, clsFoo, index);
        assertFalse(objFoo.findDoubleAttributeValue("d").isPresent());
        objFoo.setDoubleAttributeValue("d", 3.14d);
        assertTrue(objFoo.findDoubleAttributeValue("d").isPresent());
        assertEquals(3.14d, objFoo.findDoubleAttributeValue("d").orElseThrow(), 0d);
        assertFalse(objFoo.findDoubleAttributeValue("dd").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getDoubleAttributeValue("dd"));
        assertEquals(3.14d, objFoo.getDoubleAttributeValue("d"), 0d);
        assertEquals(3.14f, objFoo.getFloatAttributeValue("d"), 0f);
    }

    @Test
    void testIntVectorAttribute() {
        DataObjectIndex index = new DataObjectIndex();
        DataClass clsFoo = createFooClass();
        DataObject objFoo = new DataObject(0L, clsFoo, index);
        assertFalse(objFoo.findIntVectorAttributeValue("iv").isPresent());
        objFoo.setIntVectorAttributeValue("iv", List.of(3, 4));
        assertTrue(objFoo.findIntVectorAttributeValue("iv").isPresent());
        assertEquals(List.of(3, 4), objFoo.findIntVectorAttributeValue("iv").orElseThrow());
        assertFalse(objFoo.findIntVectorAttributeValue("iv2").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getIntVectorAttributeValue("iv2"));
        assertEquals(List.of(3, 4), objFoo.getIntVectorAttributeValue("iv"));
    }

    @Test
    void testFloatVectorAttribute() {
        DataObjectIndex index = new DataObjectIndex();
        DataClass clsFoo = createFooClass();
        DataObject objFoo = new DataObject(0L, clsFoo, index);
        assertFalse(objFoo.findFloatVectorAttributeValue("fv").isPresent());
        objFoo.setFloatVectorAttributeValue("fv", List.of(3.1f, 4.1f));
        assertTrue(objFoo.findFloatVectorAttributeValue("fv").isPresent());
        assertEquals(List.of(3.1f, 4.1f), objFoo.findFloatVectorAttributeValue("fv").orElseThrow());
        assertFalse(objFoo.findFloatVectorAttributeValue("fv2").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getFloatVectorAttributeValue("fv2"));
        assertEquals(List.of(3.1f, 4.1f), objFoo.getFloatVectorAttributeValue("fv"));
    }

    @Test
    void testDoubleVectorAttribute() {
        DataObjectIndex index = new DataObjectIndex();
        DataClass clsFoo = createFooClass();
        DataObject objFoo = new DataObject(0L, clsFoo, index);
        assertFalse(objFoo.findDoubleVectorAttributeValue("dv").isPresent());
        objFoo.setDoubleVectorAttributeValue("dv", List.of(3.2d, 4.2d));
        assertTrue(objFoo.findDoubleVectorAttributeValue("dv").isPresent());
        assertEquals(List.of(3.2d, 4.2d), objFoo.findDoubleVectorAttributeValue("dv").orElseThrow());
        assertFalse(objFoo.findDoubleVectorAttributeValue("dv2").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getDoubleVectorAttributeValue("dv2"));
        assertEquals(List.of(3.2d, 4.2d), objFoo.getDoubleVectorAttributeValue("dv"));
    }

    @Test
    void testMatrixVectorAttribute() {
        DataObjectIndex index = new DataObjectIndex();
        DataClass clsFoo = createFooClass();
        DataObject objFoo = new DataObject(0L, clsFoo, index);
        assertFalse(objFoo.findDoubleMatrixAttributeValue("m").isPresent());
        objFoo.setDoubleMatrixAttributeValue("m", new BlockRealMatrix(2, 2));
        assertTrue(objFoo.findDoubleMatrixAttributeValue("m").isPresent());
        assertEquals(new BlockRealMatrix(2, 2), objFoo.findDoubleMatrixAttributeValue("m").orElseThrow());
        assertFalse(objFoo.findDoubleMatrixAttributeValue("mm").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getDoubleMatrixAttributeValue("mm"));
        assertEquals(new BlockRealMatrix(2, 2), objFoo.getDoubleMatrixAttributeValue("m"));
    }
}
