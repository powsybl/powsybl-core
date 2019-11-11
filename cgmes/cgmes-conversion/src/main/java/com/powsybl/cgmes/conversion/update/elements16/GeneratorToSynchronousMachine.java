package com.powsybl.cgmes.conversion.update.elements16;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.conversion.update.AbstractIidmToCgmes;
import com.powsybl.cgmes.conversion.update.CgmesPredicateDetails;
import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Generator;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public final class GeneratorToSynchronousMachine extends AbstractIidmToCgmes {
    private GeneratorToSynchronousMachine() {
    }

    public static Map<String, CgmesPredicateDetails> mapIidmAtrribute() {
        return Collections.unmodifiableMap(Stream.of(
            entry("generatingUnit",
                new CgmesPredicateDetails("cim:RotatingMachine.GeneratingUnit", "_EQ", true, value)),
            entry("ratedS", new CgmesPredicateDetails("cim:RotatingMachine.ratedS", "_EQ", false, value)),
            entry("minQ", new CgmesPredicateDetails("cim:SynchronousMachine.minQ", "_EQ", false, value)),
            entry("maxQ", new CgmesPredicateDetails("cim:SynchronousMachine.maxQ", "_EQ", false, value)),
            entry("targetP", new CgmesPredicateDetails("cim:RotatingMachine.p", "_SSH", false, value)),
            entry("p", new CgmesPredicateDetails("cim:RotatingMachine.p", "_SSH", false, value)),
            entry("q", new CgmesPredicateDetails("cim:RotatingMachine.q", "_SSH", false, value)),
            entry("targetQ", new CgmesPredicateDetails("cim:RotatingMachine.q", "_SSH", false, value)),
            entry("maxP",
                new CgmesPredicateDetails("cim:GeneratingUnit.maxOperatingP", "_EQ", false, value, newSubject)),
            entry("minP",
                new CgmesPredicateDetails("cim:GeneratingUnit.minOperatingP", "_EQ", false, value, newSubject)))
            .collect(entriesToMap()));
    }

    static Map<String, String> getValues(IidmChange change, CgmesModel cgmes) {
        if (!(change.getIdentifiable() instanceof Generator)) {
            throw new ConversionException("Cannot cast the identifiable into the element");
        }
        Generator generator = (Generator) change.getIdentifiable();
        return ImmutableMap.<String, String>builder()
            .put("rdfType", "cim:SynchronousMachine")
            .put("name", generator.getName())
            .put("ratedS", String.valueOf(generator.getRatedS()))
            .put("minQ", String.valueOf(0.0))
            .put("maxQ", String.valueOf(0.0))
            .put("targetP", String.valueOf(-generator.getTargetP()))
            .put("targetQ", String.valueOf(-generator.getTargetQ()))
            .put("maxP", String.valueOf(generator.getMaxP()))
            .put("minP", String.valueOf(generator.getMinP()))
            .put("newSubject", getGeneratingUnitId(change.getIdentifiableId(), cgmes.synchronousMachines()))
            .build();
    }

    /**
     * Check if GeneratingUnit element already exists in grid, if yes - returns the
     * id
     *
     */
    static String getGeneratingUnitId(String currId, PropertyBags synchronousMachines) {
        for (PropertyBag pb : synchronousMachines) {
            if (pb.getId("SynchronousMachine").equals(currId)) {
                return pb.getId("GeneratingUnit");
            } else {
                continue;
            }
        }
        return UUID.randomUUID().toString();
    }
}
