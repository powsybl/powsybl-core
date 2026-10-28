/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.powerfactory.model {
    requires transitive com.fasterxml.jackson.core;
    requires static com.google.auto.service;
    requires com.powsybl.commons;
    requires transitive commons.math3; // No Automatic-Module-Name
    requires org.apache.commons.lang3;

    exports com.powsybl.powerfactory.model;

    uses com.powsybl.powerfactory.model.PowerFactoryDataLoader;

    provides com.powsybl.powerfactory.model.PowerFactoryDataLoader with
        com.powsybl.powerfactory.model.JsonProjectLoader,
        com.powsybl.powerfactory.model.JsonStudyCaseLoader;
}
