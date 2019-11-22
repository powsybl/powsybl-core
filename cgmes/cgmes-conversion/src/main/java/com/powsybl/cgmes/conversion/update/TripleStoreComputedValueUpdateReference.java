/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.iidm.network.Identifiable;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TripleStoreComputedValueUpdateReference extends TripleStoreSimpleUpdateReference {

    public TripleStoreComputedValueUpdateReference(String predicate, String contextReference, Function<Identifiable, String> valueComputation) {
        super(predicate, contextReference);
        this.valueComputation = Objects.requireNonNull(valueComputation);
        this.subjectComputation = null;
    }

    public TripleStoreComputedValueUpdateReference(String predicate, String contextReference, Function<Identifiable, String> valueComputation,
        BiFunction<Identifiable, CgmesModelTripleStore, String> subjectComputation) {
        super(predicate, contextReference);
        this.valueComputation = Objects.requireNonNull(valueComputation);
        this.subjectComputation = subjectComputation;
    }

    @Override
    public String value(IidmChangeUpdate change) {
        return valueComputation.apply(change.getIdentifiable());
    }

    private final Function<Identifiable, String> valueComputation;
    private final BiFunction<Identifiable, CgmesModelTripleStore, String> subjectComputation;
}
