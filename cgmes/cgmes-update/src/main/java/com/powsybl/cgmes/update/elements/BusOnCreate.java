package com.powsybl.cgmes.update.elements;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.iidm.network.Bus;

public class BusOnCreate extends IidmToCgmes implements ConversionOnCreate {

    public BusOnCreate(IidmChange change) {
        super(change);
    }

    @Override
    public Map<CgmesPredicateDetails, String> getIdentifiableAttributes() {

        Map<CgmesPredicateDetails, String> mapCgmesPredicateDetails = new HashMap<CgmesPredicateDetails, String>();
        Bus newBus = (Bus) change.getIdentifiable();
        Map<String, Object> iidmToCgmesMapper = busToTopologicalNode();

        mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("rdfType"),
            "cim:TopologicalNode");

        String name = newBus.getName();
        if (name != null) {
            mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("name"),
                name);
        }
        return mapCgmesPredicateDetails;
    }

}
