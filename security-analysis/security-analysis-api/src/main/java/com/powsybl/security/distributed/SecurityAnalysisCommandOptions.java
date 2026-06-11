/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.distributed;

import java.nio.file.Path;

/**
 * Utility class to programmatically generate an {@literal itools security-analysis} command with its various options.
 * Currently supported options are :
 *  - the {@link Path} to case file
 *  - an optional {@link Path} to contingencies file
 *  - an optional {@link Path} to parameters file
 *  - an optional {@link Path} to output file
 *  - an optional format for the output file
 *  - a list of requested result extensions
 *  - a list of violation types of interest
 *  - an optional task count
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class SecurityAnalysisCommandOptions extends AbstractSecurityAnalysisCommandOptions<SecurityAnalysisCommandOptions> {

    private static final String ITOOLS_COMMAND_NAME = "security-analysis";

    public SecurityAnalysisCommandOptions() {
        super(ITOOLS_COMMAND_NAME);
    }

    @Override
    protected String getCommandName() {
        return ITOOLS_COMMAND_NAME;
    }

    @Override
    protected SecurityAnalysisCommandOptions self() {
        return this;
    }
}
