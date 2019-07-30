package com.powsybl.cgmes.update.elements;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.iidm.network.LccConverterStation;

public class LccConverterStationOnCreate extends IidmToCgmes implements ConversionOnCreate {

    public LccConverterStationOnCreate(IidmChange change) {
        super(change);
    }

    @Override
    public Map<CgmesPredicateDetails, String> getIdentifiableAttributes() {

        Map<CgmesPredicateDetails, String> mapCgmesPredicateDetails = new HashMap<CgmesPredicateDetails, String>();

        LccConverterStation newLccConverterStation = (LccConverterStation) change.getIdentifiable();
        Map<String, Object> iidmToCgmesMapper = lccConverterStationToAcdcConverter();

        mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("rdfType"),
            "cim:ACDCConverter");

        String name = newLccConverterStation.getName();
        if (name != null) {
            mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("name"),
                name);
        }
        return mapCgmesPredicateDetails;
    }

}
