/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.psse.model {
    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive de.siegmar.fastcsv;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires transitive re2j; // No Automatic-Module-Name

    exports com.powsybl.psse.model;
    exports com.powsybl.psse.model.io;
    exports com.powsybl.psse.model.pf;
    exports com.powsybl.psse.model.pf.internal;
    exports com.powsybl.psse.model.pf.io;

    opens com.powsybl.psse.model to com.fasterxml.jackson.databind;
    opens com.powsybl.psse.model.pf to com.fasterxml.jackson.databind;
}
