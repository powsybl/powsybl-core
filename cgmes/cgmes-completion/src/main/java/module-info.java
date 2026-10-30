/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.cgmes.completion {
    requires static com.google.auto.service;
    requires transitive com.powsybl.cgmes.conversion;
    requires com.powsybl.cgmes.extensions;
    requires transitive com.powsybl.cgmes.model;
    requires transitive com.powsybl.commons;
    requires com.powsybl.iidm.api;
    requires com.powsybl.triplestore.api;
    requires java.xml;
    requires org.slf4j;

    exports com.powsybl.cgmes.completion;

    provides com.powsybl.cgmes.conversion.CgmesImportPreProcessor with
        com.powsybl.cgmes.completion.CreateMissingContainersPreProcessor;
}
