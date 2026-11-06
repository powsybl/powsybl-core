/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.matpower.converter {
    requires static com.google.auto.service;
    requires com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.iidm.api;
    requires com.powsybl.iidm.extensions;
    requires com.powsybl.matpower.model;
    requires commons.math3; // No Automatic-Module-Name
    requires org.jgrapht.core;
    requires org.slf4j;

    exports com.powsybl.matpower.converter;

    provides com.powsybl.iidm.network.Exporter with
        com.powsybl.matpower.converter.MatpowerExporter;
    provides com.powsybl.iidm.network.Importer with
        com.powsybl.matpower.converter.MatpowerImporter;
}
