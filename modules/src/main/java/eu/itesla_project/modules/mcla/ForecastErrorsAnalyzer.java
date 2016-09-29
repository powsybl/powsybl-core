/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.mcla;

import eu.itesla_project.commons.Versionable;
import eu.itesla_project.modules.online.TimeHorizon;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public interface ForecastErrorsAnalyzer extends Versionable {

    void init(ForecastErrorsAnalyzerParameters parameters);
    
    void run(TimeHorizon timeHorizon) throws Exception;
    
}
