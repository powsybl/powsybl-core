package com.powsybl.cgmes.update.elements14;

import java.util.Iterator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.iidm.network.Load;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public class LoadToEnergyConsumer implements ConversionMapper {

    public LoadToEnergyConsumer(IidmChange change, CgmesModel cgmes) {
        this.change = change;
        this.cgmes = cgmes;
        this.loadResponseCharacteristicId = getLoadResponseCharacteristicId();
    }

    @Override
    public Multimap<String, CgmesPredicateDetails> mapIidmToCgmesPredicates() {

        final Multimap<String, CgmesPredicateDetails> map = ArrayListMultimap.create();
        Load newLoad = (Load) change.getIdentifiable();

        map.put("rdfType", new CgmesPredicateDetails("rdf:type", "_EQ", false, "cim:EnergyConsumer"));

        String name = newLoad.getName();
        if (name != null) {
            map.put("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false, name));
        }

        String voltageLevelId = newLoad.getTerminal().getVoltageLevel().getId();
        if (!voltageLevelId.equals("NaN")) {
            map.put("equipmentContainer", new CgmesPredicateDetails(
                "cim:Equipment.MemberOf_EquipmentContainer", "_EQ", true, voltageLevelId));
        }

        map.put("energyConsumerLoadResponse", new CgmesPredicateDetails("cim:EnergyConsumer.LoadResponse",
            "_EQ", true, loadResponseCharacteristicId));

        /**
         * Create LoadResponseCharacteristic element
         */
        map.put("rdfTypeLRC", new CgmesPredicateDetails("rdf:type", "_EQ", false, "cim:LoadResponseCharacteristic",
            loadResponseCharacteristicId));

        if (name != null) {
            map.put("nameLRC", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false, name.concat("_LRC"),
                loadResponseCharacteristicId));
        }

        map.put("exponentModelLRC", new CgmesPredicateDetails(
            "cim:LoadResponseCharacteristic.exponentModel", "_EQ", false, "false", loadResponseCharacteristicId));

        double p0 = newLoad.getP0();
        if (!String.valueOf(p0).equals("NaN")) {
            map.put("p0",
                new CgmesPredicateDetails("cim:LoadResponseCharacteristic.pConstantPower", "_EQ", false,
                    String.valueOf(p0), loadResponseCharacteristicId));
        }

        double q0 = newLoad.getQ0();
        if (!String.valueOf(q0).equals("NaN")) {
            map.put("q0",
                new CgmesPredicateDetails("cim:LoadResponseCharacteristic.qConstantPower", "_EQ", false,
                    String.valueOf(q0), loadResponseCharacteristicId));
        }

        return map;
    }

    /**
     * Check if EnergyConsumer.LoadResponse element already exists in grid, if yes -
     * returns the id
     *
     */
    private String getLoadResponseCharacteristicId() {
        String currId = change.getIdentifiableId();
        PropertyBags energyConsumers = cgmes.energyConsumers();
        Iterator i = energyConsumers.iterator();
        while (i.hasNext()) {
            PropertyBag pb = (PropertyBag) i.next();
            if (pb.getId("EnergyConsumer").equals(currId)) {
                return pb.getId("LoadResponse");
            } else {
                continue;
            }
        }
        return currId.concat("_LRC");
    }

    private IidmChange change;
    private CgmesModel cgmes;
    private String loadResponseCharacteristicId;

}
