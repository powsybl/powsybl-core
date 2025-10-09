/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.preprocessor;

import com.powsybl.security.SecurityAnalysisInputInterface;

/**
 * A preprocessor which may be called before the execution of a security analysis,
 * in order to customize its {@link SecurityAnalysisInputInterface}, in particular
 * contingencies and limit violations detection.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public interface SecurityAnalysisPreprocessor {

    /**
     * Customize the security analysis configuration.
     *
     * @param input The configuration to be customized.
     */
    void preprocess(SecurityAnalysisInputInterface input);
}
