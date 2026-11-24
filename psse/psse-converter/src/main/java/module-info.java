/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.psse.converter {
    requires static com.google.auto.service;
    requires com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.iidm.api;
    requires com.powsybl.iidm.extensions;
    requires transitive com.powsybl.psse.model;
    requires commons.math3; // No Automatic-Module-Name
    requires org.jgrapht.core;
    requires org.slf4j;

    exports com.powsybl.psse.converter;
    exports com.powsybl.psse.converter.extensions;

    provides com.powsybl.commons.extensions.ExtensionAdderProvider with
        com.powsybl.psse.converter.extensions.PsseConversionContextExtensionAdderImplProvider,
        com.powsybl.psse.converter.extensions.PsseModelExtensionAdderImplProvider;
    provides com.powsybl.iidm.network.Exporter with
        com.powsybl.psse.converter.PsseExporter;
    provides com.powsybl.iidm.network.Importer with
        com.powsybl.psse.converter.PsseImporter;
}
