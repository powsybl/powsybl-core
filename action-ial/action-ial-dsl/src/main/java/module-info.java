/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.action.ial.dsl {
    requires static com.google.auto.service;
    requires com.google.common;
    requires transitive com.powsybl.action.ial.dsl.spi;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.computation.api;
    requires transitive com.powsybl.contingency.api;
    requires transitive com.powsybl.contingency.dsl;
    requires transitive com.powsybl.dsl;
    requires transitive com.powsybl.iidm.api;
    requires transitive com.powsybl.iidm.modification;
    requires com.powsybl.scripting;
    requires transitive java.desktop;
    requires transitive org.apache.groovy;
    requires org.slf4j;

    exports com.powsybl.action.ial.dsl;
    exports com.powsybl.action.ial.dsl.ast;
    exports com.powsybl.action.ial.dsl.modification;

    provides com.powsybl.action.ial.dsl.spi.DslModificationExtension with
        com.powsybl.action.ial.dsl.modification.ScriptDslModificationExtension;
    provides com.powsybl.contingency.ContingenciesProviderFactory with
        com.powsybl.action.ial.dsl.GroovyDslContingenciesProviderFactory;
}
