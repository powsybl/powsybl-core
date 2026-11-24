/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.triplestore.api {
    requires transitive com.powsybl.commons;
    requires org.slf4j;

    exports com.powsybl.triplestore.api;

    uses com.powsybl.triplestore.api.TripleStoreFactoryService;
}
