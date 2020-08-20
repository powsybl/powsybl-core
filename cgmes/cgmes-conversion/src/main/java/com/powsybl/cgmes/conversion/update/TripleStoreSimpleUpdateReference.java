/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import com.powsybl.iidm.network.Identifiable;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TripleStoreSimpleUpdateReference {

    public TripleStoreSimpleUpdateReference(String predicate, String contextReference) {
        this(predicate, contextReference, false);
    }

    public TripleStoreSimpleUpdateReference(String predicate, String contextReference, Function<Identifiable<?>, String> subject) {
        this(predicate, contextReference, false, subject);
    }

    public TripleStoreSimpleUpdateReference(String predicate, String contextReference, boolean valueIsUri) {
        this(predicate, contextReference, valueIsUri, Identifiable::getId);
    }

    public TripleStoreSimpleUpdateReference(String predicate, String contextReference, boolean valueIsUri, Function<Identifiable<?>, String> subject) {
        this.predicate = Objects.requireNonNull(predicate);
        this.contextReference = Objects.requireNonNull(contextReference);
        this.valueIsUri = valueIsUri;
        this.subject = Objects.requireNonNull(subject);
    }

    public String subject(Identifiable<?> identifiable) {
        return subject.apply(identifiable);
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

    private final Function<Identifiable<?>, String> subject;
    private final String predicate;
    private final String contextReference;
    private final boolean valueIsUri;
}
