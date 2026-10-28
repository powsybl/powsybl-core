/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.computation.api {
    requires com.google.common;
    requires transitive com.powsybl.commons;
    requires static org.jspecify;
    requires org.slf4j;
    requires re2j; // No Automatic-Module-Name

    exports com.powsybl.computation;
    exports com.powsybl.computation.util;
}
