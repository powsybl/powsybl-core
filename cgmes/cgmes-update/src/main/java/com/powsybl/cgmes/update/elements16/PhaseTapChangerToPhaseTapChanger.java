package com.powsybl.cgmes.update.elements16;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.iidm.network.TapChanger;

public class PhaseTapChangerToPhaseTapChanger extends IidmToCgmes implements ConversionMapper {

    public PhaseTapChangerToPhaseTapChanger(IidmChange change) {
        super(change);
    }

    @Override
    public Map<String, Object> mapIidmToCgmesPredicates() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<CgmesPredicateDetails, String> getAllCgmesDetailsOnCreate() {

        Map<CgmesPredicateDetails, String> mapCgmesPredicateDetails = new HashMap<CgmesPredicateDetails, String>();

//        TapChanger newPhaseTapChanger = (TapChanger) change.getIdentifiable();
//        Map<String, Object> iidmToCgmesMapper = mapIidmToCgmesPredicates();
//
//        mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("rdfType"),
//            "cim:PhaseTapChangerTabular");

        // String name = newPhaseTapChanger.;
//        if (name != null) {
//            mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("name"),
//                name);
//        }
        return null;
    }

}
