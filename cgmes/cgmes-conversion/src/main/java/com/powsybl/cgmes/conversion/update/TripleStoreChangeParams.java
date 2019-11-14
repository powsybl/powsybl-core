/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

/**
 * This class contains details we need to know to construct a triple for a given
 * subject: the predicate name, the triplestore context, if the value is node or
 * Literal, and the value
 *
 * @author Elena Kaltakova <kaltakovae at aia.es>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TripleStoreChangeParams {

    public TripleStoreChangeParams(TripleStoreSimpleUpdateReference ref, String value) {
        this.predicate = ref.predicate();
        this.contextReference = ref.contextReference();
        this.valueIsNode = ref.valueIsNode();
        this.value = value;
    }

    public String predicate() {
        return predicate;
    }

    public String contextReference() {
        return contextReference;
    }

    public boolean valueIsNode() {
        return valueIsNode;
    }

    public String value() {
        return value;
    }

    private final String predicate;
    private final String contextReference;
    private final boolean valueIsNode;
    private final String value;
}
