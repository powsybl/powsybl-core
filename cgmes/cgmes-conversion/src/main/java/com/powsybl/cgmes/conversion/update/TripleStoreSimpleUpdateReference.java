/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TripleStoreSimpleUpdateReference {

    public TripleStoreSimpleUpdateReference(String predicate, String contextReference) {
        this(predicate, contextReference, false);
    }

    public TripleStoreSimpleUpdateReference(String predicate, String contextReference, boolean valueIsUri) {
        this.predicate = predicate;
        this.contextReference = contextReference;
        this.valueIsUri = valueIsUri;
    }

    public String predicate() {
        return predicate;
    }

    public String contextReference() {
        return contextReference;
    }

    public String value(IidmChangeUpdate change) {
        return change.getNewValue().toString();
    }

    public boolean valueIsUri() {
        return valueIsUri;
    }

    private final String predicate;
    private final String contextReference;
    private final boolean valueIsUri;
}
