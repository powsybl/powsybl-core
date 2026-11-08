/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.iidm.extensions {
    requires transitive com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.iidm.api;
    requires transitive com.powsybl.math;
    requires transitive commons.math3; // No Automatic-Module-Name
    requires org.slf4j;

    exports com.powsybl.iidm.network.extensions;
    exports com.powsybl.iidm.network.extensions.removed;
    exports com.powsybl.iidm.network.extensions.util;
}
