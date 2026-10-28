/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.action.ial.simulator {
    requires static com.google.auto.service;
    requires com.google.common;
    requires transitive com.powsybl.action.ial.dsl;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.computation.api;
    requires com.powsybl.computation.local;
    requires transitive com.powsybl.contingency.api;
    requires com.powsybl.dsl;
    requires transitive com.powsybl.iidm.api;
    requires com.powsybl.iidm.modification;
    requires com.powsybl.iidm.serde;
    requires transitive com.powsybl.loadflow.api;
    requires transitive com.powsybl.security.analysis.api;
    requires transitive com.powsybl.tools;
    requires transitive org.apache.commons.cli;
    requires org.apache.groovy;
    requires org.slf4j;

    exports com.powsybl.action.ial.simulator;
    exports com.powsybl.action.ial.simulator.loadflow;
    exports com.powsybl.action.ial.simulator.tools;

    provides com.powsybl.tools.Tool with
        com.powsybl.action.ial.simulator.tools.ActionSimulatorTool;
}
