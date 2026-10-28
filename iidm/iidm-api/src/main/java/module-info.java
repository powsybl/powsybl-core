/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.iidm.api {
    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires static com.google.auto.service;
    requires com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.computation.api;
    requires com.powsybl.computation.local;
    requires transitive com.powsybl.math;
    requires transitive com.powsybl.tools;
    requires transitive commons.math3; // No Automatic-Module-Name
    requires transitive org.apache.commons.cli;
    requires transitive org.apache.commons.lang3;
    requires transitive org.jgrapht.core;
    requires transitive org.jgrapht.io;
    requires static org.jspecify;
    requires transitive org.slf4j;
    requires trove4j; // No Automatic-Module-Name

    exports com.powsybl.iidm.network;
    exports com.powsybl.iidm.network.components;
    exports com.powsybl.iidm.network.dot;
    exports com.powsybl.iidm.network.events;
    exports com.powsybl.iidm.network.identifiers;
    exports com.powsybl.iidm.network.identifiers.json;
    exports com.powsybl.iidm.network.limitmodification;
    exports com.powsybl.iidm.network.limitmodification.result;
    exports com.powsybl.iidm.network.regulation;
    exports com.powsybl.iidm.network.tools;
    exports com.powsybl.iidm.network.util;

    opens com.powsybl.iidm.network to com.fasterxml.jackson.databind;
    opens com.powsybl.iidm.network.identifiers.json to com.fasterxml.jackson.databind;

    uses com.powsybl.iidm.network.Exporter;
    uses com.powsybl.iidm.network.ImportPostProcessor;
    uses com.powsybl.iidm.network.Importer;

    provides com.powsybl.commons.plugins.PluginInfo with
        com.powsybl.iidm.network.ExporterPluginInfo,
        com.powsybl.iidm.network.ImportPostProcessorPluginInfo,
        com.powsybl.iidm.network.ImporterPluginInfo;
    provides com.powsybl.tools.Tool with
        com.powsybl.iidm.network.tools.ConversionTool;
}
