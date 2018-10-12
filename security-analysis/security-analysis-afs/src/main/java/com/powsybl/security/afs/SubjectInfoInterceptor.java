/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.afs;

import com.powsybl.iidm.network.*;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyResult;
import com.powsybl.security.interceptors.DefaultSecurityAnalysisInterceptor;
import com.powsybl.security.interceptors.RunningContext;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SubjectInfoInterceptor extends DefaultSecurityAnalysisInterceptor {

    private void addSubjectInfo(RunningContext context, LimitViolationsResult result) {
        for (LimitViolation violation : result.getLimitViolations()) {
            Identifiable identifiable = context.getNetwork().getIdentifiable(violation.getSubjectId());
            if (identifiable instanceof Branch) {
                Branch branch = (Branch) identifiable;

                Set<Country> countries = new TreeSet<>();
                countries.add(branch.getTerminal1().getVoltageLevel().getSubstation().getCountry());
                countries.add(branch.getTerminal2().getVoltageLevel().getSubstation().getCountry());

                Set<Double> nominalVoltages = new TreeSet<>();
                nominalVoltages.add(branch.getTerminal1().getVoltageLevel().getNominalV());
                nominalVoltages.add(branch.getTerminal2().getVoltageLevel().getNominalV());

                violation.addExtension(SubjectInfoExtension.class, new SubjectInfoExtension(countries, nominalVoltages));
            } else if (identifiable instanceof VoltageLevel) {
                VoltageLevel vl = (VoltageLevel) identifiable;
                violation.addExtension(SubjectInfoExtension.class, new SubjectInfoExtension(vl.getSubstation().getCountry(), vl.getNominalV()));
            }
        }
    }

    @Override
    public void onPreContingencyResult(RunningContext context, LimitViolationsResult preContingencyResult) {
        addSubjectInfo(context, preContingencyResult);
    }

    @Override
    public void onPostContingencyResult(RunningContext context, PostContingencyResult postContingencyResult) {
        addSubjectInfo(context, postContingencyResult.getLimitViolationsResult());
    }
}
