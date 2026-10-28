/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.iidm.reducer {
    requires com.powsybl.commons;
    requires transitive com.powsybl.iidm.api;
    requires com.powsybl.iidm.extensions;
    requires org.slf4j;

    exports com.powsybl.iidm.reducer;
}
