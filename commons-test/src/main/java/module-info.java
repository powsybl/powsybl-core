/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.commons.test {
    requires com.google.common;
    requires com.google.common.jimfs;
    requires transitive org.junit.jupiter.api;
    requires org.slf4j;
    requires org.xmlunit;

    exports com.powsybl.commons.test;
}
