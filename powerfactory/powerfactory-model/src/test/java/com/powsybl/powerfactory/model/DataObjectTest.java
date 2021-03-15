/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DataObjectTest {

    @Test
    public void test() {
        DataClass clsFoo = new DataClass("ElmFoo");
        assertEquals("ElmFoo", clsFoo.getName());
        clsFoo.addAttribute(new DataAttribute(DataAttribute.LOC_NAME, DataAttributeType.STRING));
        clsFoo.addAttribute(new DataAttribute("i", DataAttributeType.INTEGER));
        clsFoo.addAttribute(new DataAttribute("f", DataAttributeType.FLOAT));
        assertEquals("DataClass(name=ElmFoo)", clsFoo.toString());
        assertEquals(3, clsFoo.getAttributes().size());

        DataObject objFoo = new DataObject(0L, clsFoo);
        assertEquals(0L, objFoo.getId());
        assertSame(clsFoo, objFoo.getDataClass());
        assertEquals("ElmFoo", objFoo.getDataClassName());
        assertNull(objFoo.getParent());
        assertTrue(objFoo.getChildren().isEmpty());

        objFoo.setStringAttributeValue(DataAttribute.LOC_NAME, "foo");
        assertEquals("foo", objFoo.getName());
        assertEquals("foo.ElmFoo", objFoo.getFullName());
        assertTrue(objFoo.findStringAttributeValue(DataAttribute.LOC_NAME).isPresent());
        assertEquals("foo", objFoo.findStringAttributeValue(DataAttribute.LOC_NAME).orElseThrow());
        assertFalse(objFoo.findIntAttributeValue("s").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getStringAttributeValue("s"));
        assertEquals("foo", objFoo.getStringAttributeValue(DataAttribute.LOC_NAME));

        objFoo.setIntAttributeValue("i", 3);
        assertTrue(objFoo.findIntAttributeValue("i").isPresent());
        assertEquals(3, objFoo.findIntAttributeValue("i").orElseThrow());
        assertFalse(objFoo.findIntAttributeValue("ii").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getIntAttributeValue("ii"));
        assertEquals(3, objFoo.getIntAttributeValue("i"));

        objFoo.setFloatAttributeValue("f", 3.14f);
        assertTrue(objFoo.findFloatAttributeValue("f").isPresent());
        assertEquals(3.14f, objFoo.findFloatAttributeValue("f").orElseThrow(), 0f);
        assertFalse(objFoo.findFloatAttributeValue("ff").isPresent());
        assertThrows(PowerFactoryException.class, () -> objFoo.getFloatAttributeValue("ff"));
        assertEquals(3.14f, objFoo.getFloatAttributeValue("f"), 0f);

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
        assertEquals(List.of(objBar, objFoo), objFoo.getPath());
    }
}
