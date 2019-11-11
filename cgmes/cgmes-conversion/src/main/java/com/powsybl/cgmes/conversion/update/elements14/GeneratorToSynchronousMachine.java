package com.powsybl.cgmes.conversion.update.elements14;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.conversion.update.AbstractIidmToCgmes;
import com.powsybl.cgmes.conversion.update.CgmesPredicateDetails;
import com.powsybl.cgmes.conversion.update.ConversionMapper;
import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Generator;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public final class GeneratorToSynchronousMachine extends AbstractIidmToCgmes implements ConversionMapper {
    private GeneratorToSynchronousMachine() {
    }

    public static Map<String, CgmesPredicateDetails> converter() {
        return  Collections.unmodifiableMap(Stream.of(
            entry("generatingUnit", new CgmesPredicateDetails("cim:SynchronousMachine.MemberOf_GeneratingUnit", "_EQ", true, value)),
            entry("ratedS", new CgmesPredicateDetails("cim:SynchronousMachine.ratedS", "_EQ", false, value)),
            entry("minQ", new CgmesPredicateDetails("cim:SynchronousMachine.minQ", "_EQ", false, value)),
            entry("maxQ", new CgmesPredicateDetails("cim:SynchronousMachine.maxQ", "_EQ", false, value)),
            entry("targetP", new CgmesPredicateDetails("cim:GeneratingUnit.nominalP", "_EQ", false, value, newSubject)),
            entry("maxP", new CgmesPredicateDetails("cim:GeneratingUnit.maxOperatingP", "_EQ", false, value, newSubject)),
            entry("minP",new CgmesPredicateDetails("cim:GeneratingUnit.minOperatingP", "_EQ", false, value, newSubject)))
            .collect(entriesToMap()));
    }

    static Map<String, String> getValues(IidmChange change, CgmesModel cgmes) {
        if (!(change.getIdentifiable() instanceof Generator)) {
            throw new ConversionException("Cannot cast the identifiable into the element");
        }
        Generator generator = (Generator) change.getIdentifiable();
        return ImmutableMap.of(
            "rdfType", "cim:SynchronousMachine",
            "name", generator.getName(),
            "ratedS", String.valueOf(generator.getRatedS()),
            "minQ", String.valueOf(0.0),
            "maxQ", String.valueOf(0.0));
//            "targetP", String.valueOf(-generator.getTargetP()),
//            "targetQ", String.valueOf(-generator.getTargetQ()),
//            "maxP", String.valueOf(generator.getMaxP()),
//            "minP", String.valueOf(generator.getMinP()),
//            "newSubject", getGeneratingUnitId(change.getIdentifiableId(), cgmes.synchronousMachines()));
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
