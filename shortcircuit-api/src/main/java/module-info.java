/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.shortcircuit.api {
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires static com.google.auto.service;
    requires com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.computation.api;
    requires com.powsybl.computation.local;
    requires transitive com.powsybl.contingency.api;
    requires transitive com.powsybl.iidm.api;
    requires com.powsybl.iidm.extensions;
    requires com.powsybl.math;
    requires transitive com.powsybl.tools;
    requires commons.math3; // No Automatic-Module-Name
    requires transitive org.apache.commons.cli;
    requires transitive org.apache.commons.lang3;
    requires static org.jspecify;
    requires org.slf4j;

    exports com.powsybl.shortcircuit;
    exports com.powsybl.shortcircuit.converter;
    exports com.powsybl.shortcircuit.json;
    exports com.powsybl.shortcircuit.tools;

    opens com.powsybl.shortcircuit.json to com.fasterxml.jackson.databind;

    uses com.powsybl.shortcircuit.ShortCircuitAnalysisProvider;
    uses com.powsybl.shortcircuit.converter.ShortCircuitAnalysisResultExporter;

    provides com.powsybl.shortcircuit.converter.ShortCircuitAnalysisResultExporter with
        com.powsybl.shortcircuit.converter.AsciiShortCircuitAnalysisResultExporter,
        com.powsybl.shortcircuit.converter.CsvShortCircuitAnalysisResultExporter,
        com.powsybl.shortcircuit.converter.JsonShortCircuitAnalysisResultExporter;
    provides com.powsybl.tools.Tool with
        com.powsybl.shortcircuit.tools.ShortCircuitAnalysisTool;
}
