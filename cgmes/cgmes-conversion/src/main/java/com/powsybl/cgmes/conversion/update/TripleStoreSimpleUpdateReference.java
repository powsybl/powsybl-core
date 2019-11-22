/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import java.util.Objects;
import java.util.function.BiFunction;

import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.iidm.network.Identifiable;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TripleStoreSimpleUpdateReference {

    public TripleStoreSimpleUpdateReference(String predicate, String contextReference) {
        this(predicate, contextReference, false, null);
    }

    public TripleStoreSimpleUpdateReference(String predicate, String contextReference,
        BiFunction<Identifiable, CgmesModelTripleStore, String> subjectComputation) {
        this.predicate = Objects.requireNonNull(predicate);
        this.contextReference = Objects.requireNonNull(contextReference);
        this.valueIsUri = false;
        this.subjectComputation = subjectComputation;
    }

    public TripleStoreSimpleUpdateReference(String predicate, String contextReference,
        boolean valueIsUri) {
        this.predicate = Objects.requireNonNull(predicate);
        this.contextReference = Objects.requireNonNull(contextReference);
        this.valueIsUri = Objects.requireNonNull(valueIsUri);
        this.subjectComputation = null;
    }


    public TripleStoreSimpleUpdateReference(String predicate, String contextReference,
        boolean valueIsUri,
        BiFunction<Identifiable, CgmesModelTripleStore, String> subjectComputation) {
        this.predicate = Objects.requireNonNull(predicate);
        this.contextReference = Objects.requireNonNull(contextReference);
        this.valueIsUri = Objects.requireNonNull(valueIsUri);
        this.subjectComputation = subjectComputation;
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

    public String subject(IidmChangeUpdate change,CgmesModelTripleStore cgmes) {
        return subjectComputation == null ? change.getIdentifiable().getId()
            : subjectComputation.apply(change.getIdentifiable(), cgmes);
    }

    private final String predicate;
    private final String contextReference;
    private final boolean valueIsUri;
    BiFunction<Identifiable, CgmesModelTripleStore, String> subjectComputation;
}
