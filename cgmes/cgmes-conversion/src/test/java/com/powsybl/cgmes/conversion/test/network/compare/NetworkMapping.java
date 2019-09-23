/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test.network.compare;

import java.util.HashMap;
import java.util.Map;

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
        this.calculatedExpecteds = computeCalculated(this.expected);
        this.calculatedActuals = computeCalculated(this.actual);
    }

    public Identifiable findExpected(Identifiable a) {
        Identifiable e = explicit(a, explicitExpecteds, expected);
        if (e != null) {
            return e;
        }
        String eid = applyPrefixToActual(a.getId());
        if (isCalculated(a, this.actual)) {
            return calculatedExpecteds.get(eid);
        }
        return expected.getIdentifiable(eid);
    }

    public Identifiable findActual(Identifiable e) {
        Identifiable a = explicit(e, explicitActuals, actual);
        if (a != null) {
            return a;
        }
        String aid = applyPrefixToExpected(e.getId());
        if (isCalculated(e, this.expected)) {
            return calculatedActuals.get(aid);
        }
        return actual.getIdentifiable(aid);
    }

    public String applyPrefixToExpected(String eid) {
        return applyPrefix(eid, expectedPrefixed, actualPrefixed);
    }

    public String applyPrefixToActual(String aid) {
        return applyPrefix(aid, actualPrefixed, expectedPrefixed);
    }

    public boolean equivalent(Identifiable expected, Identifiable actual) {
        Identifiable expected1 = findExpected(actual);
        if (expected1 == null) {
            return false;
        } else {
            return expected1.getId().equals(expected.getId());
        }
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

    private boolean isCalculated(Identifiable i, Network network) {
        // An identifiable is considered as calculated
        // if it can not be found in the list of identifiables of the Network
        return network.getIdentifiable(i.getId()) == null;
    }

    private Map<String, Identifiable> computeCalculated(Network n) {
        Map<String, Identifiable> calculated = new HashMap<>();
        addCalculatedBuses(calculated, n);
        return calculated;
    }

    private void addCalculatedBuses(Map<String, Identifiable> calculated, Network n) {
        // Add all calculated buses in the bus-breaker view
        n.getBusBreakerView().getBuses().forEach(b -> {
            if (isCalculated(b, n)) {
                calculated.put(b.getId(), b);
            }
        });
    }

    private final Network expected;
    private final Network actual;
    private boolean expectedPrefixed;
    private boolean actualPrefixed;
    private Map<String, String> explicitExpecteds;
    private Map<String, String> explicitActuals;
    private final Map<String, Identifiable> calculatedExpecteds;
    private final Map<String, Identifiable> calculatedActuals;
}
