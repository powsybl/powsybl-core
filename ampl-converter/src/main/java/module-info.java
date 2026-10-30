/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.ampl.converter {
    requires static com.google.auto.service;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.iidm.api;
    requires com.powsybl.iidm.extensions;
    requires static org.jspecify;
    requires org.slf4j;
    requires re2j; // No Automatic-Module-Name

    exports com.powsybl.ampl.converter;
    exports com.powsybl.ampl.converter.util;
    exports com.powsybl.ampl.converter.version;

    uses com.powsybl.ampl.converter.AmplExtensionWriter;

    provides com.powsybl.iidm.network.Exporter with
        com.powsybl.ampl.converter.AmplExporter;
}
