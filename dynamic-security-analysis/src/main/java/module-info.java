/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.security.analysis.dynamic {
    requires com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires static com.google.auto.service;
    requires transitive com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.computation.api;
    requires transitive com.powsybl.contingency.api;
    requires transitive com.powsybl.dynamic.simulation.api;
    requires com.powsybl.dynamic.simulation.dsl;
    requires transitive com.powsybl.iidm.api;
    requires transitive com.powsybl.security.analysis.api;
    requires transitive com.powsybl.tools;
    requires transitive org.apache.commons.cli;
    requires org.slf4j;

    exports com.powsybl.security.dynamic;
    exports com.powsybl.security.dynamic.distributed;
    exports com.powsybl.security.dynamic.execution;
    exports com.powsybl.security.dynamic.json;
    exports com.powsybl.security.dynamic.tools;

    opens com.powsybl.security.dynamic to com.fasterxml.jackson.databind;
    opens com.powsybl.security.dynamic.json to com.fasterxml.jackson.databind;

    uses com.powsybl.security.dynamic.DynamicSecurityAnalysisProvider;

    provides com.powsybl.tools.Tool with
        com.powsybl.security.dynamic.tools.DynamicSecurityAnalysisTool;
}
