/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

/**
 * <p>Interface for criterion classes used to filter network elements (represented as
 * {@link com.powsybl.iidm.criteria.translation.NetworkElement} objects).</p>
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public interface NetworkElementCriterion {

    enum NetworkElementCriterionType {
        LINE("lineCriterion"),
        TIE_LINE("tieLineCriterion"),
        DANGLING_LINE("danglingLineCriterion"),
        TWO_WINDINGS_TRANSFORMER("twoWindingsTransformerCriterion"),
        THREE_WINDINGS_TRANSFORMER("threeWindingsTransformerCriterion"),
        IDENTIFIABLE("identifiableCriterion"),
        IDENTIFIER("identifierCriterion");

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
