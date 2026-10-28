/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.timeseries.api {
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive de.siegmar.fastcsv;
    requires org.apache.commons.io;
    requires transitive org.apache.commons.lang3;
    requires static org.jspecify;
    requires transitive org.slf4j;
    requires transitive org.threeten.extra;
    requires transitive re2j; // No Automatic-Module-Name
    requires trove4j; // No Automatic-Module-Name

    exports com.powsybl.timeseries;
    exports com.powsybl.timeseries.ast;
    exports com.powsybl.timeseries.json;

    opens com.powsybl.timeseries.json to com.fasterxml.jackson.databind;

    uses com.powsybl.timeseries.CalculatedTimeSeriesDslLoader;
}
