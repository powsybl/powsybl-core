/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.commons {
    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires com.github.luben.zstd_jni;
    requires static com.google.auto.service;
    requires transitive com.google.common;
    requires de.siegmar.fastcsv;
    requires transitive java.xml;
    requires org.apache.commons.cli;
    requires org.apache.commons.compress;
    requires org.apache.commons.csv;
    requires org.apache.commons.io;
    requires transitive org.apache.commons.lang3;
    requires org.apache.commons.text;
    requires static org.jspecify;
    requires transitive org.slf4j;
    requires org.tukaani.xz;
    requires org.yaml.snakeyaml;
    requires re2j; // No Automatic-Module-Name
    requires sirocco.text.table.formatter; // No Automatic-Module-Name
    requires stax.utils; // No Automatic-Module-Name
    requires trove4j; // No Automatic-Module-Name

    exports com.powsybl.commons;
    exports com.powsybl.commons.binary;
    exports com.powsybl.commons.compress;
    exports com.powsybl.commons.concurrent;
    exports com.powsybl.commons.config;
    exports com.powsybl.commons.datasource;
    exports com.powsybl.commons.exceptions;
    exports com.powsybl.commons.extensions;
    exports com.powsybl.commons.io;
    exports com.powsybl.commons.io.table;
    exports com.powsybl.commons.json;
    exports com.powsybl.commons.net;
    exports com.powsybl.commons.parameters;
    exports com.powsybl.commons.plugins;
    exports com.powsybl.commons.ref;
    exports com.powsybl.commons.report;
    exports com.powsybl.commons.util;
    exports com.powsybl.commons.util.trove;
    exports com.powsybl.commons.xml;

    opens com.powsybl.commons.net to com.fasterxml.jackson.databind;
    opens com.powsybl.commons.report to com.fasterxml.jackson.databind;

    uses com.powsybl.commons.concurrent.CleanableExecutors.ThreadCleaner;
    uses com.powsybl.commons.config.PlatformConfigProvider;
    uses com.powsybl.commons.extensions.ExtensionAdderProvider;
    uses com.powsybl.commons.plugins.PluginInfo;
    uses com.powsybl.commons.report.ReportResourceBundle;

    provides com.powsybl.commons.report.ReportResourceBundle with
        com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
}
