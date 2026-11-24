/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.sensitivity.analysis.api {
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires static com.google.auto.service;
    requires com.google.common;
    requires transitive com.powsybl.action;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.computation.api;
    requires com.powsybl.computation.local;
    requires transitive com.powsybl.contingency.api;
    requires transitive com.powsybl.iidm.api;
    requires transitive com.powsybl.loadflow.api;
    requires transitive com.powsybl.tools;
    requires transitive org.apache.commons.cli;
    requires org.jgrapht.core;

    exports com.powsybl.sensitivity;
    exports com.powsybl.sensitivity.json;

    opens com.powsybl.sensitivity.json to com.fasterxml.jackson.databind;

    uses com.powsybl.sensitivity.SensitivityAnalysisProvider;

    provides com.powsybl.tools.Tool with
        com.powsybl.sensitivity.SensitivityAnalysisTool;
}
