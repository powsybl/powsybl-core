package com.powsybl.cgmes.update.elements14;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.iidm.network.Bus;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public class BusToTopologicalNode implements ConversionMapper {

    public BusToTopologicalNode(IidmChange change, CgmesModel cgmes) {
        this.change = change;
        this.cgmes = cgmes;
        this.currId = change.getIdentifiableId();
        this.svVoltageId = getSvVoltageId();
    }

    @Override
    public Map<String, CgmesPredicateDetails> mapIidmToCgmesPredicates() {

        final Map<String, CgmesPredicateDetails> map = new HashMap<>();
        Bus newBus = (Bus) change.getIdentifiable();

        map.put("rdfType", new CgmesPredicateDetails("rdf:type", "_TP", false, "cim:TopologicalNode"));

        String name = newBus.getName();
        if (name != null) {
            map.put("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false, name));
        }

        String baseVoltageId = getBaseVoltageId(newBus);
        if (!baseVoltageId.equals("NaN")) {
            map.put("baseVoltage", new CgmesPredicateDetails(
                "cim:TopologicalNode.BaseVoltage", "_TP", true, baseVoltageId));
        }

        String connectivityNodeContainerId = getVoltageId(newBus);
        if (!connectivityNodeContainerId.equals("NaN")) {
            map.put("connectivityNode", new CgmesPredicateDetails(
                "cim:TopologicalNode.ConnectivityNodeContainer", "_TP", true, connectivityNodeContainerId));
        }
        /**
         * Create SvVoltage element
         */
        map.put("rdfTypeSvVoltage", new CgmesPredicateDetails("rdf:type", "_SV", false, "cim:SvVoltage", svVoltageId));

        map.put("topologicalNodeSvVoltage", new CgmesPredicateDetails("cim:SvVoltage.TopologicalNode",
            "_SV", true, currId, svVoltageId));

        double v = !String.valueOf(newBus.getV()).equals("NaN") ? newBus.getV() : 0.0;
        map.put("v", new CgmesPredicateDetails("cim:SvVoltage.v", "_SV", false, String.valueOf(v), svVoltageId));

        double angle = !String.valueOf(newBus.getAngle()).equals("NaN") ? newBus.getAngle() : 0.0;
        map.put("angle",
            new CgmesPredicateDetails("cim:SvVoltage.angle", "_SV", false, String.valueOf(angle), svVoltageId));

        return map;
    }

    private String getVoltageId(Bus bus) {
        return bus.getVoltageLevel().getId();
    }

    private String getBaseVoltageId(Bus bus) {
        String voltageLevelId = getVoltageId(bus);
        PropertyBags voltageLevels = cgmes.voltageLevels();
        Iterator i = voltageLevels.iterator();
        while (i.hasNext()) {
            PropertyBag pb = (PropertyBag) i.next();
            if (pb.getId("VoltageLevel").equals(voltageLevelId)) {
                return pb.getId("BaseVoltage");
            } else {
                continue;
            }
        }
        return "NaN";
    }

    private String getSvVoltageId() {
        PropertyBags topologicalNodes = cgmes.topologicalNodes();
        Iterator i = topologicalNodes.iterator();
        while (i.hasNext()) {
            PropertyBag pb = (PropertyBag) i.next();
            if (pb.getId("TopologicalNode").equals(currId)) {
                String svVoltageId = (pb.getId("SvVoltage") != null) ? pb.getId("SvVoltage")
                    : currId.concat("_SvVoltage");
                return svVoltageId;
            } else {
                continue;
            }
        }
        return currId.concat("_SvVoltage");
    }

    private IidmChange change;
    private CgmesModel cgmes;
    private String currId;
    private String svVoltageId;

}
