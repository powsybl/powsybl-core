/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.config.classic {
    requires static com.google.auto.service;
    requires transitive com.powsybl.commons;

    exports com.powsybl.config.classic;

    provides com.powsybl.commons.config.PlatformConfigProvider with
        com.powsybl.config.classic.ClassicPlatformConfigProvider;
}
