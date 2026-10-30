/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.powerfactory.dgs {
    requires static com.google.auto.service;
    requires com.google.common;
    requires com.powsybl.commons;
    requires transitive com.powsybl.powerfactory.model;
    requires transitive commons.math3; // No Automatic-Module-Name
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires re2j; // No Automatic-Module-Name

    exports com.powsybl.powerfactory.dgs;

    provides com.powsybl.powerfactory.model.PowerFactoryDataLoader with
        com.powsybl.powerfactory.dgs.DgsStudyCaseLoader;
}
