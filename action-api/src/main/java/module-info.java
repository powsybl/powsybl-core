/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.action {
    requires com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires com.google.common;
    requires com.powsybl.commons;
    requires transitive com.powsybl.iidm.api;
    requires transitive com.powsybl.iidm.modification;

    exports com.powsybl.action;
    exports com.powsybl.action.json;

    opens com.powsybl.action.json to com.fasterxml.jackson.databind;
}
