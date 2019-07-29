package com.powsybl.cgmes.update.elements;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Substation;

public class SubstationOnCreate extends IidmToCgmes implements ConversionOnCreate {

    public SubstationOnCreate(IidmChange change) {
        super(change);
    }

    @Override
    public Map<CgmesPredicateDetails, String> getIdentifiableAttributes() {

        Map<CgmesPredicateDetails, String> mapContextPredicateValue =
            new HashMap<CgmesPredicateDetails, String>();
        
        Substation newSubstation = (Substation) change.getIdentifiable();
        Map<String, Object> iidmToCgmesMapper = substationToSubstation();

        mapContextPredicateValue.put((CgmesPredicateDetails) iidmToCgmesMapper.get("rdfType"),
            "cim:Substation");

        String name = newSubstation.getName();
        if (name != null) {
            mapContextPredicateValue.put((CgmesPredicateDetails) iidmToCgmesMapper.get("name"),
                name);
        }

        Optional<Country> country = newSubstation.getCountry();
        if (country.isPresent()) {
            mapContextPredicateValue
                .put((CgmesPredicateDetails) iidmToCgmesMapper.get("country"), country.get().toString());
        }

        return mapContextPredicateValue;
    }

}
