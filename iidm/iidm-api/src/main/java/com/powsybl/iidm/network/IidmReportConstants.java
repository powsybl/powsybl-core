/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.reporter.TypedValue;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class IidmReportConstants {

    public static final TypedValue WARN_SEVERITY = new TypedValue("IIDM_VALIDATION_WARN", TypedValue.WARN_LOGLEVEL);
    public static final TypedValue ERROR_SEVERITY = new TypedValue("IIDM_VALIDATION_ERROR", TypedValue.ERROR_LOGLEVEL);

    private IidmReportConstants() {
    }
}
