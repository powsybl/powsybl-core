/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.iidm.scripting {
    requires static com.google.auto.service;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.computation.api;
    requires com.powsybl.computation.local;
    requires transitive com.powsybl.iidm.api;
    requires transitive com.powsybl.scripting;
    requires transitive java.desktop;
    requires transitive org.apache.groovy;
    requires org.slf4j;

    exports com.powsybl.iidm.network.scripting;

    provides com.powsybl.iidm.network.ImportPostProcessor with
        com.powsybl.iidm.network.scripting.GroovyScriptPostProcessor;
    provides com.powsybl.scripting.groovy.GroovyScriptExtension with
        com.powsybl.iidm.network.scripting.NetworkLoadSaveGroovyScriptExtension;
}
