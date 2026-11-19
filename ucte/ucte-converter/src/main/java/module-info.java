/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.ucte.converter {
    requires static com.google.auto.service;
    requires com.google.common;
    requires transitive com.powsybl.commons;
    requires com.powsybl.entsoe.util;
    requires transitive com.powsybl.iidm.api;
    requires com.powsybl.iidm.extensions;
    requires transitive com.powsybl.ucte.network;
    requires transitive commons.math3; // No Automatic-Module-Name
    requires org.apache.commons.lang3;
    requires org.slf4j;

    exports com.powsybl.ucte.converter;
    exports com.powsybl.ucte.converter.util;

    uses com.powsybl.ucte.converter.NamingStrategy;

    provides com.powsybl.iidm.network.Exporter with
        com.powsybl.ucte.converter.UcteExporter;
    provides com.powsybl.iidm.network.Importer with
        com.powsybl.ucte.converter.UcteImporter;
    provides com.powsybl.ucte.converter.NamingStrategy with
        com.powsybl.ucte.converter.CounterNamingStrategy,
        com.powsybl.ucte.converter.DefaultNamingStrategy;
}
