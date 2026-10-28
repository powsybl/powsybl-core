/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.matpower.model {
    requires transitive com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.google.common;
    requires com.powsybl.commons;
    requires org.slf4j;
    requires us.hebi.matlab.mat.mfl.core;

    exports com.powsybl.matpower.model;

    opens com.powsybl.matpower.model to com.fasterxml.jackson.databind;
}
