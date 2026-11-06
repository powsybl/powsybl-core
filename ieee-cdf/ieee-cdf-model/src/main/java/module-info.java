/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.ieeecdf.model {
    requires com.powsybl.commons;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires re2j; // No Automatic-Module-Name

    exports com.powsybl.ieeecdf.model;
    exports com.powsybl.ieeecdf.model.conversion;
    exports com.powsybl.ieeecdf.model.elements;
    exports com.powsybl.ieeecdf.model.reader;
    exports com.powsybl.ieeecdf.model.writer;
}
