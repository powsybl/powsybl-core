/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.dsl {
    requires transitive com.powsybl.commons;
    requires transitive java.desktop;
    requires transitive org.apache.groovy;
    requires transitive org.slf4j;

    exports com.powsybl.dsl;
    exports com.powsybl.dsl.ast;
}
