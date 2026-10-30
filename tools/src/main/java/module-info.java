/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.tools {
    requires ST4; // No Automatic-Module-Name
    requires static com.google.auto.service;
    requires com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.computation.api;
    requires transitive org.apache.commons.cli;
    requires org.apache.commons.io;

    exports com.powsybl.tools;
    exports com.powsybl.tools.autocompletion;

    uses com.powsybl.tools.Tool;
    uses com.powsybl.tools.Version;

    provides com.powsybl.tools.Tool with
        com.powsybl.tools.PluginsInfoTool,
        com.powsybl.tools.VersionTool,
        com.powsybl.tools.autocompletion.BashCompletionTool;
    provides com.powsybl.tools.Version with
        com.powsybl.tools.PowsyblCoreVersion;
}
