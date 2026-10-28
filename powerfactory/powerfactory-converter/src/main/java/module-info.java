/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.powerfactory.converter {
    requires static com.google.auto.service;
    requires com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.iidm.api;
    requires com.powsybl.iidm.extensions;
    requires com.powsybl.iidm.geodata;
    requires com.powsybl.math;
    requires com.powsybl.powerfactory.model;
    requires commons.math3; // No Automatic-Module-Name
    requires org.apache.commons.lang3;
    requires static org.jspecify;
    requires org.slf4j;

    exports com.powsybl.powerfactory.converter;

    provides com.powsybl.iidm.network.Importer with
        com.powsybl.powerfactory.converter.PowerFactoryImporter;
}
