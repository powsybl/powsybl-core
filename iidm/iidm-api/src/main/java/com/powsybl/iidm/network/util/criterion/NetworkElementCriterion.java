/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util.criterion;

public interface NetworkElementCriterion {

    enum NetworkElementCriterionType {
        LINE("lineCriterion"),
        TWO_WINDINGS_TRANSFORMER("twoWindingsTransformerCriterion"),
        THREE_WINDINGS_TRANSFORMER("threeWindingsTransformerCriterion"),
        IDENTIFIERS("identifierCriterion");

        private final String name;

        NetworkElementCriterionType(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    String VERSION = "1.0";

    static String getVersion() {
        return VERSION;
    }

    NetworkElementCriterionType getNetworkElementCriterionType();

    boolean accept(NetworkElementVisitor networkElementVisitor);

    String getName();
}
