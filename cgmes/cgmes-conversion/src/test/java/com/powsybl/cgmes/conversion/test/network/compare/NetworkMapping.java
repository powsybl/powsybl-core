/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test.network.compare;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class NetworkMapping {

    protected NetworkMapping(Network expected, Network actual) {
        this.expected = expected;
        this.actual = actual;
        this.explicitExpecteds = new HashMap<>();
        this.explicitActuals = new HashMap<>();
        this.expectedPrefixed = false;
        this.actualPrefixed = false;
    }

    public Identifiable findExpected(Identifiable a) {
        Identifiable e = explicit(a, explicitExpecteds, expected);
        if (e != null) {
            return e;
        }
        String eid = applyPrefixToActual(a.getId());
        if (eid.contains("+") && expected.getIdentifiable(eid) == null) {
            return expected.getIdentifiable(findByComposedId(eid, expected));
        }
        return expected.getIdentifiable(eid);
    }

    public Identifiable findActual(Identifiable e) {
        Identifiable a = explicit(e, explicitActuals, actual);
        if (a != null) {
            return a;
        }
        String aid = applyPrefixToExpected(e.getId());
        if (aid.contains("+") && actual.getIdentifiable(aid) == null) {
            return actual.getIdentifiable(findByComposedId(aid, actual));
        }
        return actual.getIdentifiable(aid);
    }

    private String findByComposedId(String id, Network n) {
        if (!id.contains("+")) {
            return id;
        } else {
            int index = id.indexOf("+");
            String switchedId = id.substring(index + 2) + " + " + id.substring(0, index - 1);
            if (n.getIdentifiable(switchedId) != null) {
                return switchedId;
            }
        }
        return id;
    }

    public String applyPrefixToExpected(String eid) {
        return applyPrefix(eid, expectedPrefixed, actualPrefixed);
    }

    public String applyPrefixToActual(String aid) {
        return applyPrefix(aid, actualPrefixed, expectedPrefixed);
    }

    public boolean equivalent(Identifiable expected, Identifiable actual) {
        if (expected == null) {
            return actual == null;
        }
        Identifiable expected1 = findExpected(actual);
        if (expected1 == null) {
            return false;
        } else {
            return expected1.getId().equals(expected.getId());
        }
    }

    public boolean equivalent(Identifiable[] expected, Identifiable[] actual) {
        if (expected == null) {
            return actual == null;
        }
        Identifiable check;
        Set<String> expectedId = new HashSet<>();
        Set<String> actualId = new HashSet<>();
        for (Identifiable i : expected) {
            check = findExpected(i);
            if (check == null) {
                return false;
            } else {
                expectedId.add(i.getId());
            }
        }
        for (Identifiable i : actual) {
            check = findActual(i);
            if (check == null) {
                return false;
            } else {
                actualId.add(i.getId());
            }
        }
        return expectedId.equals(actualId);
    }

    protected void addMapping(String expected, String actual) {
        explicitExpecteds.put(actual, expected);
        explicitActuals.put(expected, actual);
    }

    protected Identifiable explicit(Identifiable i, Map<String, String> explicits, Network n) {
        String id = i.getId();
        if (explicits.containsKey(id)) {
            return n.getIdentifiable(explicits.get(id));
        }
        return null;
    }

    private String applyPrefix(String id, boolean sourcePrefixed, boolean targetPrefixed) {
        if (sourcePrefixed == targetPrefixed) {
            return id;
        }
        if (sourcePrefixed && !targetPrefixed) {
            return id.substring(1);
        }
        if (!sourcePrefixed && targetPrefixed) {
            return "_" + id;
        }
        return id;
    }

    private final Network expected;
    private final Network actual;
    private boolean expectedPrefixed;
    private boolean actualPrefixed;
    private Map<String, String> explicitExpecteds;
    private Map<String, String> explicitActuals;
}
