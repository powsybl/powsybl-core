/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.ampl.executor {
    requires transitive com.powsybl.ampl.converter;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.computation.api;
    requires transitive com.powsybl.iidm.api;
    requires transitive org.apache.commons.lang3;
    requires org.slf4j;

    exports com.powsybl.ampl.executor;
}
