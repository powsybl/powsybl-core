/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.contingency.api {
    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires static com.google.auto.service;
    requires com.google.common;
    requires com.powsybl.action;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.computation.api;
    requires transitive com.powsybl.iidm.api;
    requires transitive com.powsybl.iidm.criteria;
    requires transitive com.powsybl.iidm.modification;
    requires org.apache.commons.io;
    requires static org.jspecify;
    requires transitive org.slf4j;

    exports com.powsybl.contingency;
    exports com.powsybl.contingency.json;
    exports com.powsybl.contingency.list;
    exports com.powsybl.contingency.strategy;
    exports com.powsybl.contingency.strategy.condition;
    exports com.powsybl.contingency.violations;

    opens com.powsybl.contingency to com.fasterxml.jackson.databind;
    opens com.powsybl.contingency.json to com.fasterxml.jackson.databind;
    opens com.powsybl.contingency.list to com.fasterxml.jackson.databind;

    uses com.powsybl.contingency.list.ContingencyListLoader;

    provides com.powsybl.commons.plugins.PluginInfo with
        com.powsybl.contingency.ContingenciesProviderFactoryPluginInfo;
    provides com.powsybl.contingency.ContingenciesProviderFactory with
        com.powsybl.contingency.EmptyContingencyListProviderFactory,
        com.powsybl.contingency.JsonContingenciesProviderFactory;
    provides com.powsybl.contingency.list.ContingencyListLoader with
        com.powsybl.contingency.json.JsonContingencyListLoader;
}
