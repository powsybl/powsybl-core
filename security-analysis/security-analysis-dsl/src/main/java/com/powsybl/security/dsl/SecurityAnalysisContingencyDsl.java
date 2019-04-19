/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.dsl;

import com.google.auto.service.AutoService;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.dsl.ContingencyDslLoader;
import com.powsybl.security.SecurityAnalysisInput;
import groovy.lang.Binding;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Enable the definition of contingencies in the same script as limit factors.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
@AutoService(SecurityAnalysisDsl.class)
public class SecurityAnalysisContingencyDsl implements SecurityAnalysisDsl {


    /**
     * Enable the contingency DSL, which will collect contingencies into the {@link com.powsybl.contingency.ContingenciesProvider ContingenciesProvider}
     * of the specified input.
     */
    @Override
    public void enable(Binding binding, SecurityAnalysisInput input) {
        List<Contingency> contingencies = new ArrayList<>();
        input.setContingencies(network -> contingencies);
        ContingencyDslLoader.loadDsl(binding, input.getNetworkVariant().getNetwork(), contingencies::add, null);
    }
}
