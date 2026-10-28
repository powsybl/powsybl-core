/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.timeseries.dsl {
    requires com.powsybl.commons;
    requires transitive com.powsybl.dsl;
    requires transitive com.powsybl.timeseries.api;
    requires transitive java.desktop;
    requires transitive org.apache.groovy;
    requires org.slf4j;

    exports com.powsybl.timeseries.dsl;

    provides com.powsybl.timeseries.CalculatedTimeSeriesDslLoader with
        com.powsybl.timeseries.dsl.CalculatedTimeSeriesGroovyDslLoader;
}
