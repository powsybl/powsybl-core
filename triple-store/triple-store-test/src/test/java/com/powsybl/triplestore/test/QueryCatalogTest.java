package com.powsybl.triplestore.test;

import com.powsybl.triplestore.api.QueryCatalog;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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
        assertEquals(q1, q2);
        assertNotEquals(q1, q3);
        assertNotEquals(q1, q4);
    }
}
