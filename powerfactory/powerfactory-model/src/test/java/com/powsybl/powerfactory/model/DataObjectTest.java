/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DataObjectTest {

    @Test
    public void test() throws IOException {
        DataClass clsFoo = new DataClass("ElmFoo");
        assertEquals("ElmFoo", clsFoo.getName());
        clsFoo.addAttribute(new DataAttribute(DataAttribute.LOC_NAME, DataAttributeType.STRING));
        clsFoo.addAttribute(new DataAttribute("i", DataAttributeType.INTEGER));
        clsFoo.addAttribute(new DataAttribute("f", DataAttributeType.FLOAT));
        clsFoo.addAttribute(new DataAttribute("d", DataAttributeType.DOUBLE));
        clsFoo.addAttribute(new DataAttribute("l", DataAttributeType.INTEGER64));
        clsFoo.addAttribute(new DataAttribute("iv", DataAttributeType.INTEGER_VECTOR));
        clsFoo.addAttribute(new DataAttribute("fv", DataAttributeType.FLOAT_VECTOR));
        clsFoo.addAttribute(new DataAttribute("dv", DataAttributeType.DOUBLE_VECTOR));
        clsFoo.addAttribute(new DataAttribute("m", DataAttributeType.MATRIX));
        assertEquals("DataClass(name=ElmFoo)", clsFoo.toString());
        assertEquals(9, clsFoo.getAttributes().size());

        DataObject objFoo = new DataObject(0L, clsFoo);
        assertEquals(0L, objFoo.getId());
        assertSame(clsFoo, objFoo.getDataClass());
        assertEquals("ElmFoo", objFoo.getDataClassName());
        assertEquals(List.of(DataAttribute.LOC_NAME, "i", "f", "d", "l", "iv", "fv", "dv", "m"), objFoo.getAttributeNames());
        assertNull(objFoo.getParent());
        assertTrue(objFoo.getChildren().isEmpty());

        assertFalse(objFoo.findStringAttributeValue(DataAttribute.LOC_NAME).isPresent());
        objFoo.setStringAttributeValue(DataAttribute.LOC_NAME, "foo");
        assertEquals("foo", objFoo.getName());
        assertEquals("foo.ElmFoo", objFoo.getFullName());
        assertEquals("foo.ElmFoo", objFoo.toString());
        assertTrue(objFoo.findStringAttributeValue(DataAttribute.LOC_NAME).isPresent());
        assertEquals("foo", objFoo.findStringAttributeValue(DataAttribute.LOC_NAME).orElseThrow());
        assertFalse(objFoo.findIntAttributeValue("s").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getStringAttributeValue("s"));
        assertEquals("foo", objFoo.getStringAttributeValue(DataAttribute.LOC_NAME));

        assertFalse(objFoo.findIntAttributeValue("i").isPresent());
        objFoo.setIntAttributeValue("i", 3);
        assertTrue(objFoo.findIntAttributeValue("i").isPresent());
        assertEquals(3, objFoo.findIntAttributeValue("i").orElseThrow());
        assertFalse(objFoo.findIntAttributeValue("ii").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getIntAttributeValue("ii"));
        assertEquals(3, objFoo.getIntAttributeValue("i"));

        assertFalse(objFoo.findLongAttributeValue("l").isPresent());
        objFoo.setLongAttributeValue("l", 4L);
        assertTrue(objFoo.findLongAttributeValue("l").isPresent());
        assertEquals(4L, objFoo.findLongAttributeValue("l").orElseThrow());
        assertFalse(objFoo.findLongAttributeValue("ll").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getLongAttributeValue("ll"));
        assertEquals(4L, objFoo.getLongAttributeValue("l"));

        assertFalse(objFoo.findFloatAttributeValue("f").isPresent());
        objFoo.setFloatAttributeValue("f", 3.14f);
        assertTrue(objFoo.findFloatAttributeValue("f").isPresent());
        assertEquals(3.14f, objFoo.findFloatAttributeValue("f").orElseThrow(), 0f);
        assertFalse(objFoo.findFloatAttributeValue("ff").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getFloatAttributeValue("ff"));
        assertEquals(3.14f, objFoo.getFloatAttributeValue("f"), 0f);

        assertFalse(objFoo.findDoubleAttributeValue("d").isPresent());
        objFoo.setDoubleAttributeValue("d", 3.14d);
        assertTrue(objFoo.findDoubleAttributeValue("d").isPresent());
        assertEquals(3.14d, objFoo.findDoubleAttributeValue("d").orElseThrow(), 0d);
        assertFalse(objFoo.findDoubleAttributeValue("dd").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getDoubleAttributeValue("dd"));
        assertEquals(3.14d, objFoo.getDoubleAttributeValue("d"), 0d);

        assertFalse(objFoo.findIntVectorAttributeValue("iv").isPresent());
        objFoo.setIntVectorAttributeValue("iv", List.of(3, 4));
        assertTrue(objFoo.findIntVectorAttributeValue("iv").isPresent());
        assertEquals(List.of(3, 4), objFoo.findIntVectorAttributeValue("iv").orElseThrow());
        assertFalse(objFoo.findIntVectorAttributeValue("iv2").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getIntVectorAttributeValue("iv2"));
        assertEquals(List.of(3, 4), objFoo.getIntVectorAttributeValue("iv"));

        assertFalse(objFoo.findFloatVectorAttributeValue("fv").isPresent());
        objFoo.setFloatVectorAttributeValue("fv", List.of(3.1f, 4.1f));
        assertTrue(objFoo.findFloatVectorAttributeValue("fv").isPresent());
        assertEquals(List.of(3.1f, 4.1f), objFoo.findFloatVectorAttributeValue("fv").orElseThrow());
        assertFalse(objFoo.findFloatVectorAttributeValue("fv2").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getFloatVectorAttributeValue("fv2"));
        assertEquals(List.of(3.1f, 4.1f), objFoo.getFloatVectorAttributeValue("fv"));

        assertFalse(objFoo.findDoubleVectorAttributeValue("dv").isPresent());
        objFoo.setDoubleVectorAttributeValue("dv", List.of(3.2d, 4.2d));
        assertTrue(objFoo.findDoubleVectorAttributeValue("dv").isPresent());
        assertEquals(List.of(3.2d, 4.2d), objFoo.findDoubleVectorAttributeValue("dv").orElseThrow());
        assertFalse(objFoo.findDoubleVectorAttributeValue("dv2").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getDoubleVectorAttributeValue("dv2"));
        assertEquals(List.of(3.2d, 4.2d), objFoo.getDoubleVectorAttributeValue("dv"));

        assertFalse(objFoo.findMatrixAttributeValue("m").isPresent());
        objFoo.setMatrixAttributeValue("m", new BlockRealMatrix(2, 2));
        assertTrue(objFoo.findMatrixAttributeValue("m").isPresent());
        assertEquals(new BlockRealMatrix(2, 2), objFoo.findMatrixAttributeValue("m").orElseThrow());
        assertFalse(objFoo.findMatrixAttributeValue("mm").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getMatrixAttributeValue("mm"));
        assertEquals(new BlockRealMatrix(2, 2), objFoo.getMatrixAttributeValue("m"));

        DataClass clsBar = new DataClass("ElmBar");
        clsBar.addAttribute(new DataAttribute(DataAttribute.LOC_NAME, DataAttributeType.STRING));
        DataObject objBar = new DataObject(1L, clsBar);
        objBar.setStringAttributeValue(DataAttribute.LOC_NAME, "bar");
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
    }
}
