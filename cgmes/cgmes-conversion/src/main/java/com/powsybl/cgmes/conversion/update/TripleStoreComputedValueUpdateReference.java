package com.powsybl.cgmes.conversion.update;

import java.util.function.Function;

import com.powsybl.iidm.network.Identifiable;

public class TripleStoreComputedValueUpdateReference extends TripleStoreSimpleUpdateReference {

    public TripleStoreComputedValueUpdateReference(String predicate, String contextReference, Function<Identifiable, String> valueComputation) {
        super(predicate, contextReference);
        this.valueComputation = valueComputation;
    }

    @Override
    public String value(IidmChangeUpdate change) {
        return valueComputation.apply(change.getIdentifiable());
    }

    private final Function<Identifiable, String> valueComputation;
}
