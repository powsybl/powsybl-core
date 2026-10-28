/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.dynamic.simulation.dsl {
    requires transitive com.powsybl.commons;
    requires com.powsybl.dsl;
    requires transitive com.powsybl.dynamic.simulation.api;
    requires transitive com.powsybl.iidm.api;
    requires org.apache.commons.io;
    requires transitive org.apache.groovy;

    exports com.powsybl.dynamicsimulation.groovy;
}
