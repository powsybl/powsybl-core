/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conformity.test;

import java.util.Collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class ExpectedFlows {

    public static class ExpectedFlow {
        final String elementType;
        final String name;
        final double nominalVoltage;
        final String id;
        final String terminalId;
        public final double p;
        public final double q;

        ExpectedFlow(String elementType, String name, double nominalVoltage, String id, String terminalId, double p,
                double q) {
            this.elementType = elementType;
            this.name = name;
            this.nominalVoltage = nominalVoltage;
            this.id = id;
            this.terminalId = terminalId;
            this.p = p;
            this.q = q;
        }
    }

    // Store expected flows as they are published
    // in the conformity test cases documentation Excel files
    void add(String elementType, String name, double nominalVoltage, String id, String terminalId, double p, double q) {
        flows.put(id, new ExpectedFlow(elementType, name, nominalVoltage, id, terminalId, p, q));
    }

    public Collection<ExpectedFlow> get(String id) {
        return flows.get(id);
    }

    // In IIDM we will not have easy access to CGMES terminal identifiers,
    // so we guess which expected flow corresponds to a particular side of a branch
    // For lines:
    // We select the expected flow for which P has the same sign
    // For transformers:
    // We select the expected flow with a nominal voltage closer to
    // the nominal voltage of the given end

    public ExpectedFlow findBestCandidate(String id, double p) {
        Collection<ExpectedFlow> fs = flows.get(id);
        for (ExpectedFlow f : fs) {
            if (Math.signum(f.p) == Math.signum(p)) {
                return f;
            }
        }
        return null;
    }

    public ExpectedFlow findBestCandidate(Branch<?> b, Branch.Side side) {
        Collection<ExpectedFlow> fs = flows.get(b.getId());
        ExpectedFlow bestCandidate = null;
        for (ExpectedFlow f : fs) {
            if (matchesSide(f, b, side, bestCandidate)) {
                bestCandidate = f;
            }
        }
        if (bestCandidate == null) {
            throw new PowsyblException("expected flow not found for branch " + b.getNameOrId());
        } else {
            return bestCandidate;
        }
    }

    private boolean matchesSide(ExpectedFlow f, Branch<?> b, Branch.Side side, ExpectedFlow bestCandidate) {
        if (f.elementType.equals("Transformer")) {
            if (bestCandidate == null) {
                return true;
            }
            double vl = b.getTerminal(side).getVoltageLevel().getNominalV();
            double dvlBest = Math.abs(bestCandidate.nominalVoltage - vl);
            double dvlThis = Math.abs(f.nominalVoltage - vl);
            if (dvlThis < dvlBest) {
                return true;
            }
        } else if (f.elementType.equals("Line")) {
            if (Math.signum(f.p) == Math.signum(b.getTerminal(side).getP())) {
                return true;
            }
        }
        return false;
    }

    ExpectedFlow get(ThreeWindingsTransformer t, ThreeWindingsTransformer.Side side) {
        Collection<ExpectedFlow> fs = flows.get(t.getId());
        ExpectedFlow bestCandidate = null;
        for (ExpectedFlow f : fs) {
            if (matchesSide(f, t, side, bestCandidate)) {
                bestCandidate = f;
            }
        }
        if (bestCandidate == null) {
            throw new PowsyblException("expected flow not found for 3w " + t.getNameOrId());
        } else {
            return bestCandidate;
        }
    }

    private boolean matchesSide(ExpectedFlow f, ThreeWindingsTransformer t, ThreeWindingsTransformer.Side side,
            ExpectedFlow bestCandidate) {
        if (bestCandidate == null) {
            return true;
        }
        double vl = t.getTerminal(side).getVoltageLevel().getNominalV();
        double dvlBest = Math.abs(bestCandidate.nominalVoltage - vl);
        double dvlThis = Math.abs(f.nominalVoltage - vl);
        return dvlThis < dvlBest;
    }

    Multimap<String, ExpectedFlow> flows = HashMultimap.create(50, 2);
}
