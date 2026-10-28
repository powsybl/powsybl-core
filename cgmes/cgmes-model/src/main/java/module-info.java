/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.cgmes.model {
    requires com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive com.powsybl.triplestore.api;
    requires java.xml;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires re2j; // No Automatic-Module-Name

    exports com.powsybl.cgmes.model;
    exports com.powsybl.cgmes.model.triplestore;
}
