/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.tools.test {
    requires com.google.common.jimfs;
    requires transitive com.powsybl.commons;
    requires com.powsybl.commons.test;
    requires com.powsybl.computation.api;
    requires transitive com.powsybl.tools;
    requires transitive org.apache.commons.cli;
    requires transitive org.junit.jupiter.api;
    requires org.mockito;
    requires transitive re2j; // No Automatic-Module-Name

    exports com.powsybl.tools.test;
}
