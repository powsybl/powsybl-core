/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.cim.anonymiser {
    requires static com.google.auto.service;
    requires com.powsybl.commons;
    requires transitive com.powsybl.tools;
    requires java.xml;
    requires transitive org.apache.commons.cli;
    requires org.apache.commons.compress;
    requires org.slf4j;
    requires stax.utils; // No Automatic-Module-Name

    exports com.powsybl.cim;

    provides com.powsybl.tools.Tool with
        com.powsybl.cim.CimAnonymizerTool;
}
