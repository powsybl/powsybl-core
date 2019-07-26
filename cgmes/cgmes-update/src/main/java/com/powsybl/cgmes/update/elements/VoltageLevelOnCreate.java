package com.powsybl.cgmes.update.elements;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.cgmes.update.MapTriplestorePredicateToContext;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.VoltageLevel;

public class VoltageLevelOnCreate extends IidmToCgmes implements ConversionOnCreate {

    public VoltageLevelOnCreate(IidmChange change) {
        super(change);
    }
    
    @Override
    public  Map<MapTriplestorePredicateToContext, String> getIdentifiableAttributes(){
        
        Map<MapTriplestorePredicateToContext, String> mapContextPredicateValue =
            new HashMap<MapTriplestorePredicateToContext, String>();
        
        VoltageLevel newVoltageLevel = (VoltageLevel) change.getIdentifiable();
        Map<String, Object> iidmToCgmesMapper = voltageLevelToVoltageLevel();
        
        mapContextPredicateValue.put((MapTriplestorePredicateToContext) iidmToCgmesMapper.get("rdfType"),
            "cim:VoltageLevel");

        String name = newVoltageLevel.getName();
        if (name != null) {
            mapContextPredicateValue.put((MapTriplestorePredicateToContext) iidmToCgmesMapper.get("name"),
                name);
        }
        
        Double highVoltageLimit = newVoltageLevel.getHighVoltageLimit();
        if (highVoltageLimit != null) {
            mapContextPredicateValue.put((MapTriplestorePredicateToContext) iidmToCgmesMapper.get("highVoltageLimit"),
                highVoltageLimit.toString());
        }
        
        Double lowVoltageLimit = newVoltageLevel.getLowVoltageLimit();
        if (lowVoltageLimit != null) {
            mapContextPredicateValue.put((MapTriplestorePredicateToContext) iidmToCgmesMapper.get("lowVoltageLimit"),
                lowVoltageLimit.toString());
        }
        
        String substation = newVoltageLevel.getSubstation().getId();
        if (lowVoltageLimit != null) {
            mapContextPredicateValue.put((MapTriplestorePredicateToContext) iidmToCgmesMapper.get("lowVoltageLimit"),
                lowVoltageLimit.toString());
        }
        
        return mapContextPredicateValue;
    }

}
