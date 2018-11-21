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
import com.powsybl.security.PostContingencyResult;
import com.powsybl.security.SecurityAnalysisResult;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class SecurityAnalysisResultEquivalence extends Equivalence<SecurityAnalysisResult> {

    private final double threshold;
    private SecurityAnalysisResultComparisonWriter comparisonWriter;

    public SecurityAnalysisResultEquivalence(double threshold, Writer writer) {
        this.threshold = threshold;
        this.comparisonWriter = new SecurityAnalysisResultComparisonWriter(writer);
    }

    @Override
    protected boolean doEquivalent(SecurityAnalysisResult result1, SecurityAnalysisResult result2) {
        LimitViolationsResultEquivalence violationsResultEquivalence = new LimitViolationsResultEquivalence(threshold, comparisonWriter);
        PostContingencyResultComparator postContingencyResultComparator = new PostContingencyResultComparator();

        // compare precontingency results
        boolean equivalent = violationsResultEquivalence.equivalent(result1.getPreContingencyResult(), result2.getPreContingencyResult());

        // I still carry on the comparison even if equivalent is already false because I need to print the violations of the post contingency results
        // compare postcontingency results
        List<PostContingencyResult> postContingencyResults1 = result1.getPostContingencyResults();
        List<PostContingencyResult> postContingencyResults2 = result2.getPostContingencyResults();
        Collections.sort(postContingencyResults1, postContingencyResultComparator);
        Collections.sort(postContingencyResults2, postContingencyResultComparator);
        int index1 = 0;
        int index2 = 0;
        while (index1 < postContingencyResults1.size() && index2 < postContingencyResults2.size()) {
            PostContingencyResult postContingencyResult1 = postContingencyResults1.get(index1);
            PostContingencyResult postContingencyResult2 = postContingencyResults2.get(index2);
            int postContingencyResultComparison = postContingencyResultComparator.compare(postContingencyResult1, postContingencyResult2);
            if (postContingencyResultComparison == 0) { // both results for the same contingency
                comparisonWriter.setContingency(postContingencyResult1.getContingency().getId());
                equivalent &= violationsResultEquivalence.equivalent(postContingencyResult1.getLimitViolationsResult(), postContingencyResult2.getLimitViolationsResult());
                index1++;
                index2++;
            } else if (postContingencyResultComparison < 0) { // contingency only in result1
                equivalent &= onlySmallViolations(postContingencyResult1, false);
                index1++;
            } else { // contingency only in result2
                equivalent &= onlySmallViolations(postContingencyResult2, true);
                index2++;
            }
        }
        while (index1 < postContingencyResults1.size()) { // possibly remaining post contingency results in result1
            PostContingencyResult postContingencyResult1 = postContingencyResults1.get(index1);
            equivalent &= onlySmallViolations(postContingencyResult1, false);
            index1++;
        }
        while (index2 < postContingencyResults2.size()) { // possibly remaining post contingency results in result1
            PostContingencyResult postContingencyResult2 = postContingencyResults2.get(index2);
            equivalent &= onlySmallViolations(postContingencyResult2, true);
            index2++;
        }
        return equivalent;
    }

    private boolean onlySmallViolations(PostContingencyResult postContingencyResult, boolean missingResult1) {
        comparisonWriter.setContingency(postContingencyResult.getContingency().getId());
        boolean equivalent = postContingencyResult.getLimitViolationsResult()
                                                  .getLimitViolations()
                                                  .stream()
                                                  .sorted(new LimitViolationComparator())
                                                  .map(violation -> isSmallViolation(violation, missingResult1))
                                                  .reduce(Boolean::logicalAnd)
                                                  .orElse(false);
        comparisonWriter = missingResult1 ?
                           comparisonWriter.write(null, postContingencyResult.getLimitViolationsResult().isComputationOk(), equivalent) :
                           comparisonWriter.write(postContingencyResult.getLimitViolationsResult().isComputationOk(), null, equivalent);
        comparisonWriter = missingResult1 ?
                           comparisonWriter.write(null, postContingencyResult.getLimitViolationsResult().getActionsTaken(), equivalent) :
                           comparisonWriter.write(postContingencyResult.getLimitViolationsResult().getActionsTaken(), null, equivalent);
        return equivalent;
    }

    private boolean isSmallViolation(LimitViolation violation, boolean missingResult1) {
        boolean smallViolation = Math.abs(violation.getValue() - (violation.getLimit() * violation.getLimitReduction())) <= threshold;
        comparisonWriter = missingResult1 ? comparisonWriter.write(null, violation, smallViolation) : comparisonWriter.write(violation, null, smallViolation);
        return smallViolation;
    }

    @Override
    protected int doHash(SecurityAnalysisResult result) {
        return Objects.hashCode(result);
    }

}
