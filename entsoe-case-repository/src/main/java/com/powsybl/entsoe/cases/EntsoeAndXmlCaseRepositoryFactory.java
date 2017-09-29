/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.entsoe.cases;

import com.powsybl.cases.CaseRepository;
import com.powsybl.cases.CaseRepositoryFactory;
import com.powsybl.computation.ComputationManager;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class EntsoeAndXmlCaseRepositoryFactory implements CaseRepositoryFactory {
    @Override
    public CaseRepository create(ComputationManager computationManager) {
        return EntsoeAndXmlCaseRepository.create(computationManager);
    }
}
