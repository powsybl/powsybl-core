package com.powsybl.cgmes.update.elements16;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.iidm.network.Bus;

public class BusToTopologicalNode extends IidmToCgmes implements ConversionMapper {

    public BusToTopologicalNode(IidmChange change) {
        super(change);
    }

    @Override
    public Map<String, Object> mapIidmToCgmesPredicatesOnUpdate() {
        return Collections.unmodifiableMap(Stream.of(
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_TP", false)))
            .collect(entriesToMap()));
    }

    @Override
    public Map<CgmesPredicateDetails, String> getAllCgmesDetailsOnCreate() {

        Map<CgmesPredicateDetails, String> allCgmesDetails = new HashMap<CgmesPredicateDetails, String>();
        Bus newBus = (Bus) change.getIdentifiable();

        CgmesPredicateDetails rdfType = new CgmesPredicateDetails("rdf:type", "_EQ", false);
        allCgmesDetails.put(rdfType, "cim:TopologicalNode");

        String name = newBus.getName();
        if (name != null) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicatesOnUpdate().get("name"),
                name);
        }
        return allCgmesDetails;
    }

}
