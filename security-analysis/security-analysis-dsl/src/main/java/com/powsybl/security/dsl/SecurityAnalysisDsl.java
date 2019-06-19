/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.dsl;

import com.powsybl.dsl.GroovyDsl;
import com.powsybl.security.SecurityAnalysisInput;

/**
 * Defines a DSL to customize {@link SecurityAnalysisInput},
 * used by the {@link SecurityAnalysisDslPreprocessor}.
 * You can define your own DSL by providing en implementation of that interface
 * as a service, possibly using {@link com.google.auto.service.AutoService}.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public interface SecurityAnalysisDsl extends GroovyDsl<SecurityAnalysisInput> {

}
