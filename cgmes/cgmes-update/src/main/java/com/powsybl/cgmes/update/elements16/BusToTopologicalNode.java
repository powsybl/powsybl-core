package com.powsybl.cgmes.update.elements16;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes16;
import com.powsybl.iidm.network.Bus;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public class BusToTopologicalNode extends IidmToCgmes16 implements ConversionMapper {

    public BusToTopologicalNode(IidmChange change, CgmesModel cgmes) {
        super(change, cgmes);
    }

    @Override
    public Map<String, Object> mapIidmToCgmesPredicates() {
        return Collections.unmodifiableMap(Stream.of(
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_TP", false)),
            entry("v", new CgmesPredicateDetails("cim:SvVoltage.v", "_SV", false, svVoltageId)),
            entry("angle", new CgmesPredicateDetails("cim:SvVoltage.angle", "_SV", false, svVoltageId)))
            .collect(entriesToMap()));
    }

    @Override
    public Map<CgmesPredicateDetails, String> getAllCgmesDetailsOnCreate() {

        Map<CgmesPredicateDetails, String> allCgmesDetails = new HashMap<CgmesPredicateDetails, String>();
        Bus newBus = (Bus) change.getIdentifiable();

        CgmesPredicateDetails rdfType = new CgmesPredicateDetails("rdf:type", "_TP", false);
        allCgmesDetails.put(rdfType, "cim:TopologicalNode");

        String name = newBus.getName();
        if (name != null) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("name"),
                name);
        }

        String baseVoltageId = getBaseVoltageId(newBus);
        CgmesPredicateDetails baseVoltage = new CgmesPredicateDetails(
            "cim:TopologicalNode.BaseVoltage", "_TP", true);
        if (!baseVoltageId.equals("NaN")) {
            allCgmesDetails.put(baseVoltage, baseVoltageId);
        }

        // TODO elena fix ConnectivityNodeContainer, might be: cim:VoltageLevel cim:Bay
        // cim:Line
        String connectivityNodeContainerId = getVoltageId(newBus);
        CgmesPredicateDetails connectivityNode = new CgmesPredicateDetails(
            "cim:TopologicalNode.ConnectivityNodeContainer", "_TP", true);
        if (!connectivityNodeContainerId.equals("NaN")) {
            allCgmesDetails.put(connectivityNode, connectivityNodeContainerId);
        }
        /**
         * Create SvVoltage element
         */
        CgmesPredicateDetails rdfTypeSvVoltage = new CgmesPredicateDetails("rdf:type", "_SV", false, svVoltageId);
        allCgmesDetails.put(rdfTypeSvVoltage, "cim:SvVoltage");

        CgmesPredicateDetails topologicalNodeSvVoltage = new CgmesPredicateDetails("cim:SvVoltage.TopologicalNode",
            "_SV", true, svVoltageId);
        allCgmesDetails.put(topologicalNodeSvVoltage, currId);

        allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("v"), "0.0");

        allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("angle"), "0.0");

        return allCgmesDetails;
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
                String svVoltageId = (pb.getId("SvVoltageT") != null) ? pb.getId("SvVoltageT")
                    : currId.concat("_SvVoltage");
                return svVoltageId;
            } else {
                continue;
            }
        }
        return currId.concat("_SvVoltage");
    }

    private String currId = change.getIdentifiableId();
    private String svVoltageId = getSvVoltageId();

}
