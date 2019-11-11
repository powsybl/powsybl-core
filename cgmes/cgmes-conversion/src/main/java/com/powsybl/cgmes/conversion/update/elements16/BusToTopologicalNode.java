package com.powsybl.cgmes.conversion.update.elements16;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.conversion.update.AbstractIidmToCgmes;
import com.powsybl.cgmes.conversion.update.CgmesPredicateDetails;
import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Bus;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public class BusToTopologicalNode extends AbstractIidmToCgmes {

    private BusToTopologicalNode() {
    }

    public static Map<String, CgmesPredicateDetails> mapIidmAtrribute() {
        return Collections.unmodifiableMap(Stream.of(
            entry("baseVoltage", new CgmesPredicateDetails("cim:TopologicalNode.BaseVoltage", "_TP", true, value)),
            entry("connectivityNode",
                new CgmesPredicateDetails("cim:TopologicalNode.ConnectivityNodeContainer", "_TP", true, value)),
            entry("topologicalNodeSvVoltage",
                new CgmesPredicateDetails("cim:SvVoltage.TopologicalNode", "_SV", true, value, newSubject)),
            entry("v", new CgmesPredicateDetails("cim:SvVoltage.v", "_SV", false, value, newSubject)),
            entry("angle", new CgmesPredicateDetails("cim:SvVoltage.angle", "_SV", false, value, newSubject)),
            entry("TerminalTopologicalNode",
                new CgmesPredicateDetails("cim:Terminal.TopologicalNode", "_TP", false, value, newSubject)))
            .collect(entriesToMap()));
        /**
         * SvVoltage element
         */
        /**
         * TP Terminal element
         */
        /**
         * EQ Terminal element
         */
    }

    static Map<String, String> getValues(IidmChange change, CgmesModel cgmes) {
        if (!(change.getIdentifiable() instanceof Bus)) {
            throw new ConversionException("Cannot cast the identifiable into the element");
        }
        Bus bus = (Bus) change.getIdentifiable();
        return ImmutableMap.<String, String>builder()
            .put("rdfType", "cim:TopologicalNode")
            .put("name", bus.getName())
            .put("connectivityNodeontainerId", getVoltageId(bus))
            .put("baseVoltage", getBaseVoltageId(bus, cgmes.voltageLevels()))
            .put("svVoltageId", getSvVoltageId(change.getIdentifiableId(), cgmes.topologicalNodes()))
            .put("v", String.valueOf(bus.getV()))
            .put("angle", String.valueOf(bus.getAngle())).build();
    }

    static String getVoltageId(Bus bus) {
        return bus.getVoltageLevel().getId();
    }

    static String getBaseVoltageId(Bus bus, PropertyBags voltageLevels) {
        for (PropertyBag pb : voltageLevels) {
            if (pb.getId("VoltageLevel").equals(getVoltageId(bus))) {
                return pb.getId("BaseVoltage");
            } else {
                continue;
            }
        }
        return "NaN";
    }

    static String getSvVoltageId(String currId, PropertyBags topologicalNodes) {
        for (PropertyBag pb : topologicalNodes) {
            if (pb.getId("TopologicalNode").equals(currId)) {
                return (pb.getId("SvVoltageT") != null) ? pb.getId("SvVoltageT") : UUID.randomUUID().toString();
            } else {
                continue;
            }
        }
        return UUID.randomUUID().toString();
    }
}
