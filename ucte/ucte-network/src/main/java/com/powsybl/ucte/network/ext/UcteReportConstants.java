/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network.ext;

import com.powsybl.commons.reporter.TypedValue;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public final class UcteReportConstants {

    private UcteReportConstants() {
    }

    public static final TypedValue TRACE_SEVERITY = new TypedValue("UCTE_TRACE", TypedValue.TRACE_LOGLEVEL);
    public static final TypedValue DEBUG_SEVERITY = new TypedValue("UCTE_DEBUG", TypedValue.DEBUG_LOGLEVEL);
    public static final TypedValue INFO_SEVERITY = new TypedValue("UCTE_INFO", TypedValue.INFO_LOGLEVEL);
    public static final TypedValue WARN_SEVERITY = new TypedValue("UCTE_WARN", TypedValue.WARN_LOGLEVEL);
    public static final TypedValue ERROR_SEVERITY = new TypedValue("UCTE_ERROR", TypedValue.ERROR_LOGLEVEL);
}
