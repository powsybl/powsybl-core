/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.iidm.geodata {
    requires com.fasterxml.jackson.core;
    requires static com.google.auto.service;
    requires com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.computation.api;
    requires transitive com.powsybl.iidm.api;
    requires transitive com.powsybl.iidm.extensions;
    requires transitive org.apache.commons.io;
    requires transitive org.jgrapht.core;
    requires org.slf4j;

    exports com.powsybl.iidm.geodata.geojson;
    exports com.powsybl.iidm.geodata.utils;

    provides com.powsybl.iidm.network.ImportPostProcessor with
        com.powsybl.iidm.geodata.geojson.GeoJsonAdderPostProcessor;
}
