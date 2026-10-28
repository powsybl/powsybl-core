/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.cgmes.measurements {
    requires static com.google.auto.service;
    requires transitive com.powsybl.cgmes.conversion;
    requires transitive com.powsybl.cgmes.model;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.iidm.api;
    requires com.powsybl.iidm.extensions;
    requires transitive com.powsybl.triplestore.api;
    requires org.slf4j;

    exports com.powsybl.cgmes.measurements;

    provides com.powsybl.cgmes.conversion.CgmesImportPostProcessor with
        com.powsybl.cgmes.measurements.CgmesMeasurementsPostProcessor;
}
