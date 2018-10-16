package com.powsybl.triplestore.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.powsybl.triplestore.api.QueryCatalog;

public class QueryCatalogTest {

    @Test
    public void testHashCodeEquals() {
        QueryCatalog q1 = new QueryCatalog("foaf/foaf-graphs.sparql");
        QueryCatalog q2 = new QueryCatalog("foaf/foaf-graphs.sparql");
        QueryCatalog q3 = new QueryCatalog("foaf/foaf-graphs-copy.sparql");
        QueryCatalog q4 = new QueryCatalog("foaf/foaf-optionals.sparql");
        assertEquals(q1.hashCode(), q2.hashCode());
        assertNotEquals(q1.hashCode(), q3.hashCode());
        assertNotEquals(q1.hashCode(), q4.hashCode());
        assertTrue(q1.equals(q2));
        assertFalse(q1.equals(q3));
        assertFalse(q1.equals(q4));
    }
}
