/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.dynamic.simulation.tool {
    requires static com.google.auto.service;
    requires com.powsybl.commons;
    requires com.powsybl.computation.api;
    requires com.powsybl.dynamic.simulation.api;
    requires com.powsybl.dynamic.simulation.dsl;
    requires com.powsybl.iidm.api;
    requires transitive com.powsybl.tools;
    requires transitive org.apache.commons.cli;
    requires org.apache.commons.lang3;

    exports com.powsybl.dynamicsimulation.tool;

    provides com.powsybl.tools.Tool with
        com.powsybl.dynamicsimulation.tool.DynamicSimulationTool,
        com.powsybl.dynamicsimulation.tool.ListDynamicSimulationModelsTool;
}
