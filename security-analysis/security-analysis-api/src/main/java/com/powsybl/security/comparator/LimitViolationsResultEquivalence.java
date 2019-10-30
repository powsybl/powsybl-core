/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.comparator;

import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Equivalence;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationsResult;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class LimitViolationsResultEquivalence extends Equivalence<LimitViolationsResult> {

    private final double threshold;
    private SecurityAnalysisResultComparisonWriter comparisonWriter;

    public LimitViolationsResultEquivalence(double threshold, Writer writer) {
        this(threshold, new SecurityAnalysisResultComparisonWriter(writer));
    }

    LimitViolationsResultEquivalence(double threshold, SecurityAnalysisResultComparisonWriter comparisonWriter) {
        this.threshold = threshold;
        this.comparisonWriter = comparisonWriter;
    }

    @Override
    protected boolean doEquivalent(LimitViolationsResult result1, LimitViolationsResult result2) {
        LimitViolationComparator violationComparator = new LimitViolationComparator();
        LimitViolationEquivalence violationEquivalence = new LimitViolationEquivalence(threshold);

        // compare computation
        boolean equivalent = result1.isComputationOk() == result2.isComputationOk();
        comparisonWriter.write(result1.isComputationOk(), result2.isComputationOk(), equivalent);

        // I still carry on the comparison even if equivalent is already false because I need to print the violations
        // compare violations
        List<LimitViolation> violations1 = result1.getLimitViolations();
        List<LimitViolation> violations2 = result2.getLimitViolations();
        Collections.sort(violations1, violationComparator);
        Collections.sort(violations2, violationComparator);
        int index1 = 0;
        int index2 = 0;
        while (index1 < violations1.size() && index2 < violations2.size()) {
            LimitViolation violation1 = violations1.get(index1);
            LimitViolation violation2 = violations2.get(index2);
            int violationsComparison = violationComparator.compare(violation1, violation2);
            if (violationsComparison == 0) { // same violations in both results
                boolean violationsEquivalent = violationEquivalence.equivalent(violation1, violation2);
                comparisonWriter.write(violation1, violation2, violationsEquivalent);
                equivalent &= violationsEquivalent;
                index1++;
                index2++;
            } else if (violationsComparison < 0) { // violation only on result1
                equivalent &= isSmallViolation(violation1, false);
                index1++;
            } else { // violation only on result2
                equivalent &= isSmallViolation(violation2, true);
                index2++;
            }
        }
        while (index1 < violations1.size()) { // possibly remaining result1 violations
            LimitViolation violation1 = violations1.get(index1);
            equivalent &= isSmallViolation(violation1, false);
            index1++;
        }
        while (index2 < violations2.size()) { // possibly remaining result1 violations
            LimitViolation violation2 = violations2.get(index1);
            equivalent &= isSmallViolation(violation2, true);
            index2++;
        }

        // compare actions
        List<String> actions1 = result1.getActionsTaken();
        List<String> actions2 = result2.getActionsTaken();
        Collections.sort(actions1);
        Collections.sort(actions2);
        boolean actionsEquivalent = actions1.equals(actions2);
        comparisonWriter.write(actions1, actions2, actionsEquivalent);
        equivalent &= actionsEquivalent;

        return equivalent;
    }

    private boolean isSmallViolation(LimitViolation violation, boolean missingResult1) {
        boolean smallViolation = Math.abs(violation.getValue() - (violation.getLimit() * violation.getLimitReduction())) <= threshold;
        comparisonWriter = missingResult1 ? comparisonWriter.write(null, violation, smallViolation) : comparisonWriter.write(violation, null, smallViolation);
        return smallViolation;
    }

    @Override
    protected int doHash(LimitViolationsResult result) {
        return Objects.hashCode(result);
    }

}
