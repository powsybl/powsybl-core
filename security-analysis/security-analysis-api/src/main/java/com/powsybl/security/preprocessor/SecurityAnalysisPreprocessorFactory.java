/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.preprocessor;

import com.google.common.io.ByteSource;

/**
 *
 * In charge of building instances of {@link SecurityAnalysisPreprocessor} based on an arbitrary
 * configuration provided as a source of bytes.
 *
 * <p>In particular, will be used to preprocess security analysis inputs with a file provided
 * on the command line {@literal itools security-analysis}. For that purpose, only one factory
 * must be provided at runtime as a service.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public interface SecurityAnalysisPreprocessorFactory {

    String getName();

    SecurityAnalysisPreprocessor newPreprocessor(ByteSource configSource);

}
