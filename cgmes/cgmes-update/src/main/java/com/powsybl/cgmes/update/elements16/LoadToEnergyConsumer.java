package com.powsybl.cgmes.update.elements16;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.iidm.network.Load;

public class LoadToEnergyConsumer extends IidmToCgmes implements ConversionMapper {

    public LoadToEnergyConsumer(IidmChange change) {
        super(change);
    }

    @Override
    public Map<String, Object> mapIidmToCgmesPredicates() {
        return Collections.unmodifiableMap(Stream.of(
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false)),
            entry("p0", new CgmesPredicateDetails("cim:EnergyConsumer.pfixed", "_EQ", false)),
            entry("q0", new CgmesPredicateDetails("cim:EnergyConsumer.qfixed", "_EQ", false)),
            entry("p", new CgmesPredicateDetails("cim:EnergyConsumer.p", "_SSH", false)),
            entry("q", new CgmesPredicateDetails("cim:EnergyConsumer.q", "_SSH", false)),
            entry("VoltageLevel", new CgmesPredicateDetails("cim:Equipment.MemberOf_EquipmentContainer", "_EQ", true)))
            .collect(entriesToMap()));
    }

    @Override
    public Map<CgmesPredicateDetails, String> getAllCgmesDetailsOnCreate() {

        Map<CgmesPredicateDetails, String> allCgmesDetails = new HashMap<CgmesPredicateDetails, String>();
        Load newLoad = (Load) change.getIdentifiable();

        CgmesPredicateDetails rdfType = new CgmesPredicateDetails("rdf:type", "_EQ", false);
        allCgmesDetails.put(rdfType, "cim:EnergyConsumer");

        String name = newLoad.getName();
        if (name != null) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("name"),
                name);
        }

        double p0 = newLoad.getP0();
        if (!String.valueOf(p0).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("p0"),
                String.valueOf(p0));
        }

        double q0 = newLoad.getQ0();
        if (!String.valueOf(q0).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("q0"),
                String.valueOf(q0));
        }

        return allCgmesDetails;
    }

}
