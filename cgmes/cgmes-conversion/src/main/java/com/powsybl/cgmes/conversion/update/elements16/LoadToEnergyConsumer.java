package com.powsybl.cgmes.conversion.update.elements16;

import java.util.Iterator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.powsybl.cgmes.conversion.update.CgmesPredicateDetails;
import com.powsybl.cgmes.conversion.update.ConversionMapper;
import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Load;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public class LoadToEnergyConsumer implements ConversionMapper {

	public LoadToEnergyConsumer(IidmChange change, CgmesModel cgmes) {
        this.change = change;
		this.cgmes = cgmes;
    }

    @Override
    public Multimap<String, CgmesPredicateDetails> mapIidmToCgmesPredicates() {

        final Multimap<String, CgmesPredicateDetails> map = ArrayListMultimap.create();
        Load newLoad = (Load) change.getIdentifiable();

		map.put("rdfType", new CgmesPredicateDetails("rdf:type", "_EQ", false, getRdfTypeValue()));

		map.put("rdfType", new CgmesPredicateDetails("rdf:type", "_SSH", false, getRdfTypeValue()));

        String name = newLoad.getName();
        if (name != null) {
            map.put("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false, name));
        }

        String voltageLevelId = newLoad.getTerminal().getVoltageLevel().getId();
        if (!voltageLevelId.equals("NaN")) {
            map.put("equipmentContainer", new CgmesPredicateDetails(
                "cim:Equipment.EquipmentContainer", "_EQ", true, voltageLevelId));
        }

//      double p0 = newLoad.getP0();
//      if (!String.valueOf(p0).equals("NaN")) {
//          map.put("p0", new CgmesPredicateDetails(
//              "cim:EnergyConsumer.pfixed", "_EQ", false, String.valueOf(p0)));
//      }
//
//      double q0 = newLoad.getQ0();
//      if (!String.valueOf(q0).equals("NaN")) {
//          map.put("q0", new CgmesPredicateDetails(
//              "cim:EnergyConsumer.qfixed", "_EQ", false, String.valueOf(q0)));
//      }

        double p = newLoad.getP0();
        if (!String.valueOf(p).equals("NaN")) {
            map.put("p0", new CgmesPredicateDetails(
                "cim:EnergyConsumer.p", "_SSH", false, String.valueOf(p)));
        }

        double q = newLoad.getQ0();
        if (!String.valueOf(q).equals("NaN")) {
            map.put("q0", new CgmesPredicateDetails(
                "cim:EnergyConsumer.q", "_SSH", false, String.valueOf(q)));
        }

        return map;
    }

	private String getRdfTypeValue() {
		String currId = change.getIdentifiableId();
		PropertyBags energyConsumers = cgmes.energyConsumers();
		Iterator i = energyConsumers.iterator();
		while (i.hasNext()) {
			PropertyBag pb = (PropertyBag) i.next();
			if (pb.getId("EnergyConsumer").equals(currId)) {
				return pb.getLocal("type");
			} else {
				continue;
			}
		}
		return null;
	}

    private IidmChange change;
	private CgmesModel cgmes;
}
