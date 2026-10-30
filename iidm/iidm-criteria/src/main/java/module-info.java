/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.iidm.criteria {
    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires com.google.common;
    requires com.powsybl.commons;
    requires transitive com.powsybl.iidm.api;
    requires transitive org.apache.commons.lang3;
    requires org.slf4j;
    requires re2j; // No Automatic-Module-Name

    exports com.powsybl.iidm.criteria;
    exports com.powsybl.iidm.criteria.duration;
    exports com.powsybl.iidm.criteria.json;
    exports com.powsybl.iidm.criteria.json.duration;
    exports com.powsybl.iidm.criteria.json.util;
    exports com.powsybl.iidm.criteria.translation;
    exports com.powsybl.iidm.criteria.util;

    opens com.powsybl.iidm.criteria to com.fasterxml.jackson.databind;
    opens com.powsybl.iidm.criteria.json to com.fasterxml.jackson.databind;
    opens com.powsybl.iidm.criteria.json.duration to com.fasterxml.jackson.databind;
}
