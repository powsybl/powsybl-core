/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import java.util.Collection;
import java.util.Map;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public interface ReportSeeker {

    String getDefaultName();

    Collection<ReportSeeker> getChildReporters();

    Collection<Report> getReports();

    String getTaskKey();

    Map<String, Object> getTaskValues();
}
