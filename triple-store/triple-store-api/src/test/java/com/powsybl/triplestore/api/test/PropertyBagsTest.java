/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.triplestore.api.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class PropertyBagsTest {
    @BeforeClass
    public static void setup() {
        bags = new PropertyBags();
        List<String> properties = Arrays.asList("key0", "key1");
        PropertyBag b0 = new PropertyBag(properties);
        PropertyBag b1 = new PropertyBag(properties);
        bags.add(b0);
        bags.add(b1);
        b0.put("key0", "http://example.com/#key0-value0");
        b1.put("key0", "http://example.com/#key0-value1");
        b0.put("key1", "http://example.com/#key1-value0");
        b1.put("key1", "http://example.com/#key1-value1");
    }

    @Test
    public void testPluck() {
        List<String> expectedLocalValues0 = Arrays.asList("key0-value0", "key0-value1");
        List<String> expectedLocalValues1 = Arrays.asList("key1-value0", "key1-value1");
        assertEquals(expectedLocalValues0, bags.pluckLocals("key0"));
        assertEquals(expectedLocalValues1, bags.pluckLocals("key1"));

        String base = "http://example.com/#";
        List<String> expectedValues0 = Arrays.asList(base + "key0-value0", base + "key0-value1");
        List<String> expectedValues1 = Arrays.asList(base + "key1-value0", base + "key1-value1");
        assertEquals(expectedValues0, bags.pluck("key0"));
        assertEquals(expectedValues1, bags.pluck("key1"));
    }

    @Test
    public void testTabulateLocals() {
        String expected = String.join(System.lineSeparator(),
                "key0 \t key1",
                "key0-value0 \t key1-value0",
                "key0-value1 \t key1-value1");
        assertEquals(expected, bags.tabulateLocals());
    }

    @Test
    public void testTabulate() {
        String expected = String.join(System.lineSeparator(),
                "key0 \t key1",
                "http://example.com/#key0-value0 \t http://example.com/#key1-value0",
                "http://example.com/#key0-value1 \t http://example.com/#key1-value1");
        assertEquals(expected, bags.tabulate());
    }

    @Test
    public void testPivot() {
        PropertyBags bs = new PropertyBags();
        List<String> properties = Arrays.asList("id", "key", "value");
        String propertyp = "http://example.com/#p";
        String propertyq = "http://example.com/#q";

        PropertyBag b0p = new PropertyBag(properties);
        PropertyBag b0q = new PropertyBag(properties);
        b0p.put("id", "http://example.com/#id0");
        b0p.put("key", propertyp);
        b0p.put("value", "http://example.com/#id0-p-value");
        b0q.put("id", "http://example.com/#id0");
        b0q.put("key", propertyq);
        b0q.put("value", "http://example.com/#id0-q-value");
        bs.add(b0p);
        bs.add(b0q);

        PropertyBag b1p = new PropertyBag(properties);
        PropertyBag b1q = new PropertyBag(properties);
        b1p.put("id", "http://example.com/#id1");
        b1p.put("key", propertyp);
        b1p.put("value", "http://example.com/#id1-p-value");
        b1q.put("id", "http://example.com/#id1");
        b1q.put("key", propertyq);
        b1q.put("value", "http://example.com/#id1-q-value");
        bs.add(b1p);
        bs.add(b1q);

        PropertyBags bs1 = bs.pivot("id", "key", Arrays.asList(propertyp, propertyq), "value");
        List<String> expectedIds = Arrays.asList("id0", "id1");
        List<String> expectedps = Arrays.asList("id0-p-value", "id1-p-value");
        List<String> expectedqs = Arrays.asList("id0-q-value", "id1-q-value");
        assertEquals(expectedIds, bs1.pluckLocals("id"));
        assertEquals(expectedps, bs1.pluckLocals(propertyp));
        assertEquals(expectedqs, bs1.pluckLocals(propertyq));

        PropertyBags bs2 = bs.pivotLocalNames("id", "key", Arrays.asList("p", "q"), "value");
        assertEquals(expectedIds, bs2.pluckLocals("id"));
        assertEquals(expectedps, bs2.pluckLocals("p"));
        assertEquals(expectedqs, bs2.pluckLocals("q"));
    }

    private static PropertyBags bags;
}
