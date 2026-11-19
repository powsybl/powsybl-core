/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.triplestore.impl.rdf4j {
    requires static com.google.auto.service;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.triplestore.api;
    requires org.slf4j;
    requires rdf4j.common.transaction; // No Automatic-Module-Name
    requires rdf4j.common.xml; // No Automatic-Module-Name
    requires rdf4j.model; // No Automatic-Module-Name
    requires rdf4j.model.api; // No Automatic-Module-Name
    requires rdf4j.model.vocabulary; // No Automatic-Module-Name
    requires rdf4j.query; // No Automatic-Module-Name
    requires rdf4j.queryalgebra.evaluation; // No Automatic-Module-Name
    requires rdf4j.repository.api; // No Automatic-Module-Name
    requires rdf4j.repository.sail; // No Automatic-Module-Name
    requires rdf4j.rio.api; // No Automatic-Module-Name
    requires rdf4j.rio.rdfxml; // No Automatic-Module-Name
    requires rdf4j.sail.memory; // No Automatic-Module-Name

    exports com.powsybl.triplestore.impl.rdf4j;

    provides com.powsybl.triplestore.api.TripleStoreFactoryService with
        com.powsybl.triplestore.impl.rdf4j.TripleStoreFactoryServiceRDF4J;
}
