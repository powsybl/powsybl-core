/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.cgmes.conversion {
    requires transitive com.fasterxml.uuid;
    requires static com.google.auto.service;
    requires transitive com.google.common;
    requires transitive com.powsybl.cgmes.extensions;
    requires transitive com.powsybl.cgmes.model;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.iidm.api;
    requires transitive com.powsybl.iidm.extensions;
    requires com.powsybl.iidm.modification;
    requires com.powsybl.math;
    requires transitive com.powsybl.triplestore.api;
    requires commons.math3; // No Automatic-Module-Name
    requires transitive java.xml;
    requires transitive org.apache.commons.lang3;
    requires org.slf4j;
    requires re2j; // No Automatic-Module-Name

    exports com.powsybl.cgmes.conversion;
    exports com.powsybl.cgmes.conversion.elements;
    exports com.powsybl.cgmes.conversion.elements.dc;
    exports com.powsybl.cgmes.conversion.elements.transformers;
    exports com.powsybl.cgmes.conversion.export;
    exports com.powsybl.cgmes.conversion.export.elements;
    exports com.powsybl.cgmes.conversion.naming;
    exports com.powsybl.cgmes.conversion.naming.providers;

    uses com.powsybl.cgmes.conversion.CgmesImportPostProcessor;
    uses com.powsybl.cgmes.conversion.CgmesImportPreProcessor;
    uses com.powsybl.cgmes.conversion.naming.NamingStrategyProvider;

    provides com.powsybl.cgmes.conversion.CgmesImportPostProcessor with
        com.powsybl.cgmes.conversion.EntsoeCategoryPostProcessor,
        com.powsybl.cgmes.conversion.PhaseAngleClock,
        com.powsybl.cgmes.conversion.RemoveGroundsPostProcessor;
    provides com.powsybl.cgmes.conversion.naming.NamingStrategyProvider with
        com.powsybl.cgmes.conversion.naming.providers.CgmesNamingStrategyProvider,
        com.powsybl.cgmes.conversion.naming.providers.IdentityNamingStrategyProvider;
    provides com.powsybl.commons.extensions.ExtensionAdderProvider with
        com.powsybl.cgmes.conversion.CgmesConversionContextExtensionAdderImplProvider,
        com.powsybl.cgmes.conversion.CgmesModelExtensionAdderImplProvider;
    provides com.powsybl.iidm.network.Exporter with
        com.powsybl.cgmes.conversion.CgmesExport;
    provides com.powsybl.iidm.network.Importer with
        com.powsybl.cgmes.conversion.CgmesImport;
}
