/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.cgmes.extensions {
    requires static com.google.auto.service;
    requires com.google.common;
    requires transitive com.powsybl.cgmes.model;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.iidm.api;
    requires transitive com.powsybl.iidm.serde;
    requires org.slf4j;

    exports com.powsybl.cgmes.extensions;
    exports com.powsybl.cgmes.extensions.compatibility;

    provides com.powsybl.commons.extensions.ExtensionAdderProvider with
        com.powsybl.cgmes.extensions.BaseVoltageMappingAdderImplProvider,
        com.powsybl.cgmes.extensions.CgmesBoundaryLineBoundaryNodeAdderImplProvider,
        com.powsybl.cgmes.extensions.CgmesLineBoundaryNodeAdderImplProvider,
        com.powsybl.cgmes.extensions.CgmesMetadataModelsAdderImplProvider,
        com.powsybl.cgmes.extensions.CgmesTapChangersAdderImplProvider,
        com.powsybl.cgmes.extensions.CimCharacteristicsAdderImplProvider;
    provides com.powsybl.commons.extensions.ExtensionSerDe with
        com.powsybl.cgmes.extensions.BaseVoltageMappingSerDe,
        com.powsybl.cgmes.extensions.CgmesBoundaryLineBoundaryNodeSerDe,
        com.powsybl.cgmes.extensions.CgmesLineBoundaryNodeSerDe,
        com.powsybl.cgmes.extensions.CgmesMetadataModelsSerDe,
        com.powsybl.cgmes.extensions.CgmesTapChangersSerDe,
        com.powsybl.cgmes.extensions.CimCharacteristicsSerDe,
        com.powsybl.cgmes.extensions.compatibility.CgmesControlAreasSerDe;
}
