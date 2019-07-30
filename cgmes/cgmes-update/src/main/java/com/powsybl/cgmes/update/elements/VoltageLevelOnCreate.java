package com.powsybl.cgmes.update.elements;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.iidm.network.VoltageLevel;

public class VoltageLevelOnCreate extends IidmToCgmes implements ConversionOnCreate {

    public VoltageLevelOnCreate(IidmChange change) {
        super(change);
    }

    @Override
    public Map<CgmesPredicateDetails, String> getIdentifiableAttributes() {

        Map<CgmesPredicateDetails, String> mapCgmesPredicateDetails = new HashMap<CgmesPredicateDetails, String>();

        VoltageLevel newVoltageLevel = (VoltageLevel) change.getIdentifiable();
        Map<String, Object> iidmToCgmesMapper = voltageLevelToVoltageLevel();

        mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("rdfType"),
            "cim:VoltageLevel");

        String name = newVoltageLevel.getName();
        if (name != null) {
            mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("name"),
                name);
        }

        double highVoltageLimit = newVoltageLevel.getHighVoltageLimit();
        if (!String.valueOf(highVoltageLimit).equals("NaN")) {
            mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("highVoltageLimit"),
                String.valueOf(highVoltageLimit));
        }

        double lowVoltageLimit = newVoltageLevel.getLowVoltageLimit();
        if (!String.valueOf(lowVoltageLimit).equals("NaN")) {
            mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("lowVoltageLimit"),
                String.valueOf(lowVoltageLimit));
        }

        String substation = newVoltageLevel.getSubstation().getId();
        if (substation != null) {
            mapCgmesPredicateDetails.put((CgmesPredicateDetails) iidmToCgmesMapper.get("Substation"),
                substation.toString());
        }

        return mapCgmesPredicateDetails;
    }

}
