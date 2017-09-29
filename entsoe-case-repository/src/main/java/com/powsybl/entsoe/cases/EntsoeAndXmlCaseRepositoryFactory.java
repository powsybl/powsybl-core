/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package eu.itesla_project.entsoe.cases;

import eu.itesla_project.cases.CaseRepository;
import eu.itesla_project.cases.CaseRepositoryFactory;
import eu.itesla_project.computation.ComputationManager;

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
