/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.security.analysis.api {
    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires static com.google.auto.service;
    requires transitive com.google.common;
    requires transitive com.powsybl.action;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.computation.api;
    requires com.powsybl.computation.local;
    requires transitive com.powsybl.contingency.api;
    requires transitive com.powsybl.iidm.api;
    requires transitive com.powsybl.iidm.criteria;
    requires com.powsybl.iidm.serde;
    requires transitive com.powsybl.loadflow.api;
    requires transitive com.powsybl.tools;
    requires transitive org.apache.commons.cli;
    requires org.apache.commons.lang3;
    requires transitive org.slf4j;

    exports com.powsybl.security;
    exports com.powsybl.security.comparator;
    exports com.powsybl.security.converter;
    exports com.powsybl.security.detectors;
    exports com.powsybl.security.distributed;
    exports com.powsybl.security.execution;
    exports com.powsybl.security.extensions;
    exports com.powsybl.security.interceptors;
    exports com.powsybl.security.json;
    exports com.powsybl.security.json.limitscaling;
    exports com.powsybl.security.limitscaling;
    exports com.powsybl.security.limitscaling.computation;
    exports com.powsybl.security.limitscaling.result;
    exports com.powsybl.security.monitor;
    exports com.powsybl.security.preprocessor;
    exports com.powsybl.security.results;
    exports com.powsybl.security.tools;

    opens com.powsybl.security to com.fasterxml.jackson.databind;
    opens com.powsybl.security.json to com.fasterxml.jackson.databind;
    opens com.powsybl.security.json.limitscaling to com.fasterxml.jackson.databind;
    opens com.powsybl.security.monitor to com.fasterxml.jackson.databind;

    uses com.powsybl.security.SecurityAnalysisProvider;
    uses com.powsybl.security.converter.SecurityAnalysisResultExporter;
    uses com.powsybl.security.interceptors.SecurityAnalysisInterceptorExtension;
    uses com.powsybl.security.json.SecurityAnalysisJsonPlugin;
    uses com.powsybl.security.preprocessor.SecurityAnalysisPreprocessorFactory;

    provides com.powsybl.commons.extensions.ExtensionJsonSerializer with
        com.powsybl.security.json.ActivePowerExtensionSerializer,
        com.powsybl.security.json.CurrentExtensionSerializer,
        com.powsybl.security.json.VoltageExtensionSerializer;
    provides com.powsybl.security.converter.SecurityAnalysisResultExporter with
        com.powsybl.security.converter.JsonSecurityAnalysisResultExporter;
    provides com.powsybl.tools.Tool with
        com.powsybl.security.comparator.CompareSecurityAnalysisResultsTool,
        com.powsybl.security.tools.SecurityAnalysisTool;
}
