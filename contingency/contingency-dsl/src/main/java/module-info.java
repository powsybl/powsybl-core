/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.contingency.dsl {
    requires static com.google.auto.service;
    requires com.powsybl.commons;
    requires transitive com.powsybl.contingency.api;
    requires transitive com.powsybl.dsl;
    requires transitive com.powsybl.iidm.api;
    requires transitive java.desktop;
    requires transitive org.apache.groovy;
    requires org.slf4j;

    exports com.powsybl.contingency.dsl;

    provides com.powsybl.contingency.ContingenciesProviderFactory with
        com.powsybl.contingency.dsl.GroovyDslContingenciesProviderFactory;
    provides com.powsybl.contingency.list.ContingencyListLoader with
        com.powsybl.contingency.dsl.GroovyContingencyListLoader;
}
