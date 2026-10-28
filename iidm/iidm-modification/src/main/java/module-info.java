/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.iidm.modification {
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires static com.google.auto.service;
    requires com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.computation.api;
    requires com.powsybl.computation.local;
    requires transitive com.powsybl.iidm.api;
    requires transitive com.powsybl.iidm.extensions;
    requires com.powsybl.iidm.serde;
    requires com.powsybl.loadflow.api;
    requires transitive com.powsybl.math;
    requires transitive commons.math3; // No Automatic-Module-Name
    requires transitive org.apache.commons.lang3;
    requires transitive org.jgrapht.core;
    requires static org.jspecify;
    requires org.slf4j;

    exports com.powsybl.iidm.modification;
    exports com.powsybl.iidm.modification.scalable;
    exports com.powsybl.iidm.modification.scalable.json;
    exports com.powsybl.iidm.modification.tapchanger;
    exports com.powsybl.iidm.modification.topology;
    exports com.powsybl.iidm.modification.tripping;
    exports com.powsybl.iidm.modification.util;

    opens com.powsybl.iidm.modification.scalable.json to com.fasterxml.jackson.databind;

    uses com.powsybl.iidm.modification.topology.NamingStrategy;

    provides com.powsybl.iidm.modification.topology.NamingStrategy with
        com.powsybl.iidm.modification.topology.DefaultNamingStrategy;
    provides com.powsybl.iidm.network.ImportPostProcessor with
        com.powsybl.iidm.modification.ReplaceTieLinesByLinesPostProcessor;
}
