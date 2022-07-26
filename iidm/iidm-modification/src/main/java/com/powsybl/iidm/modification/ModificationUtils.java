/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.TypedValue;
import org.slf4j.LoggerFactory;

import java.util.logging.Logger;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class ModificationUtils {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ModificationUtils.class);

    public static void throwExceptionOrLogError(String message, String key, boolean throwException, Reporter reporter) {
        LOGGER.error(message);
        reporter.report(Report.builder()
                .withKey(key)
                .withDefaultMessage(message)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
        if (throwException) {
            throw new PowsyblException(message);
        }
    }
    
    private ModificationUtils() {
        
    }
}
