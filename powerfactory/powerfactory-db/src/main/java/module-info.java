/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.powerfactory.db {
    requires static com.google.auto.service;
    requires com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.powerfactory.model;
    requires commons.math3; // No Automatic-Module-Name
    requires org.apache.commons.lang3;
    requires org.scijava.nativelib;
    requires org.slf4j;
    requires re2j; // No Automatic-Module-Name

    exports com.powsybl.powerfactory.db;

    provides com.powsybl.powerfactory.model.PowerFactoryDataLoader with
        com.powsybl.powerfactory.db.DbProjectLoader,
        com.powsybl.powerfactory.db.DbStudyCaseLoader;
}
