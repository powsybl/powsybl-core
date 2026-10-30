/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.entsoe.util {
    requires static com.google.auto.service;
    requires com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.iidm.api;
    requires org.apache.poi.poi;
    requires re2j; // No Automatic-Module-Name

    exports com.powsybl.entsoe.util;

    provides com.powsybl.commons.extensions.ExtensionAdderProvider with
        com.powsybl.entsoe.util.EntsoeAreaAdderImplProvider;
    provides com.powsybl.commons.extensions.ExtensionSerDe with
        com.powsybl.entsoe.util.EntsoeAreaSerDe;
}
