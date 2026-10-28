/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.scripting {
    requires static com.google.auto.service;
    requires com.powsybl.computation.api;
    requires transitive com.powsybl.tools;
    requires transitive java.desktop;
    requires transitive org.apache.commons.cli;
    requires transitive org.apache.groovy;

    exports com.powsybl.scripting;
    exports com.powsybl.scripting.groovy;

    provides com.powsybl.scripting.groovy.GroovyScriptExtension with
        com.powsybl.scripting.groovy.LogsGroovyScriptExtension;
    provides com.powsybl.tools.Tool with
        com.powsybl.scripting.RunScriptTool;
}
