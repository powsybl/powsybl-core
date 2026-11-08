/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.iidm.twopass.security.analysis {
    requires static com.google.auto.service;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.contingency.api;
    requires transitive com.powsybl.iidm.api;
    requires transitive com.powsybl.security.analysis.api;
    requires com.powsybl.tools;
    requires org.slf4j;

    exports com.powsybl.twopasssecurity.security.analysis;
    exports com.powsybl.twopasssecurity.security.analysis.parameters;

    provides com.powsybl.security.SecurityAnalysisProvider with
        com.powsybl.twopasssecurity.security.analysis.TwoPassSecurityAnalysisProvider;
}
