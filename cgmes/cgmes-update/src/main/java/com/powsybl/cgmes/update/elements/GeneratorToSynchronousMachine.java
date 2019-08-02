package com.powsybl.cgmes.update.elements;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.iidm.network.Generator;

public class GeneratorToSynchronousMachine extends IidmToCgmes implements ConversionMapper {
    public GeneratorToSynchronousMachine(IidmChange change) {
        super(change);
    }

    public static Map<String, Object> mapIidmToCgmesPredicates() {
        return Collections.unmodifiableMap(Stream.of(
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false)),
            entry("minQ", new CgmesPredicateDetails("cim:SynchronousMachine.minQ", "_EQ", false)),
            entry("maxQ", new CgmesPredicateDetails("cim:SynchronousMachine.maxQ", "_EQ", false)),
            entry("qPercent", new CgmesPredicateDetails("cim:SynchronousMachine.qPercent", "_EQ", false)),
            entry("ratedS", new CgmesPredicateDetails("cim:SynchronousMachine.ratedS", "_EQ", false)),
            entry("targetP", new CgmesPredicateDetails("cim:GeneratingUnit.initialP", "_EQ", false)),
            entry("minP", new CgmesPredicateDetails("cim:GeneratingUnit.minOperatingP", "_EQ", false)),
            entry("maxP", new CgmesPredicateDetails("cim:GeneratingUnit.maxOperatingP", "_EQ", false)),
            entry("GeneratingUnit",
                new CgmesPredicateDetails("cim:SynchronousMachine.MemberOf_GeneratingUnit", "_EQ", true)))
            .collect(entriesToMap()));
    }

    @Override
    public Map<CgmesPredicateDetails, String> getAllCgmesDetails() {

        Map<CgmesPredicateDetails, String> allCgmesDetails = new HashMap<CgmesPredicateDetails, String>();

        Generator newGenerator = (Generator) change.getIdentifiable();

        CgmesPredicateDetails rdfType = new CgmesPredicateDetails("rdf:type", "_EQ", false);
        allCgmesDetails.put(rdfType, "cim:SynchronousMachine");

        String name = newGenerator.getName();
        if (name != null) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("name"),
                name);
        }

        double ratedS = newGenerator.getRatedS();
        if (!String.valueOf(ratedS).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("ratedS"),
                String.valueOf(ratedS));
        }

        return allCgmesDetails;
    }

}
