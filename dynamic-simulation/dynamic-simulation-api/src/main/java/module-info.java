/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.dynamic.simulation.api {
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.computation.api;
    requires com.powsybl.computation.local;
    requires transitive com.powsybl.iidm.api;
    requires transitive com.powsybl.timeseries.api;

    exports com.powsybl.dynamicsimulation;
    exports com.powsybl.dynamicsimulation.json;

    opens com.powsybl.dynamicsimulation.json to com.fasterxml.jackson.databind;

    uses com.powsybl.dynamicsimulation.DynamicSimulationProvider;
}
