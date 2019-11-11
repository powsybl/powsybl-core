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
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Line;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public class LineToACLineSegment extends AbstractIidmToCgmes {

    private LineToACLineSegment() {
    }

    public static Map<String, CgmesPredicateDetails> mapIidmAtrribute() {
        return Collections.unmodifiableMap(Stream.of(
            entry("r", new CgmesPredicateDetails("cim:ACLineSegment.r", "_EQ", false, value)),
            entry("x", new CgmesPredicateDetails("cim:ACLineSegment.x", "_EQ", false, value)),
            entry("b1", new CgmesPredicateDetails("cim:ACLineSegment.bch", "_EQ", false, value)),
            entry("b2", new CgmesPredicateDetails("cim:ACLineSegment.bch", "_EQ", false, value)),
            entry("g1", new CgmesPredicateDetails("cim:ACLineSegment.gch", "_EQ", false, value)),
            entry("g2", new CgmesPredicateDetails("cim:ACLineSegment.gch", "_EQ", false, value)),
            entry("BaseVoltage", new CgmesPredicateDetails("cim:ConductingEquipment.BaseVoltage", "_EQ", true, value, newSubject)))
            .collect(entriesToMap()));
    }

    static Map<String, String> getValues(IidmChange change, CgmesModel cgmes) {
        if (!(change.getIdentifiable() instanceof Line)) {
            throw new ConversionException("Cannot cast the identifiable into the element");
        }
        Line line = (Line) change.getIdentifiable();
        String voltageLevelId = line.getTerminal(Branch.Side.ONE).getVoltageLevel().getId();
        double b1 = !String.valueOf(line.getB1()).equals("NaN") ? line.getB1() : 0.0;
        double b2 = !String.valueOf(line.getB2()).equals("NaN") ? line.getB2() : 0.0;
        double g1 = !String.valueOf(line.getG1()).equals("NaN") ? line.getG1() : 0.0;
        double g2 = !String.valueOf(line.getG2()).equals("NaN") ? line.getG2() : 0.0;
        return ImmutableMap.<String, String>builder()
            .put("rdfType", "cim:ACLineSegment")
            .put("name", line.getName())
            .put("r", String.valueOf(line.getR()))
            .put("x", String.valueOf(line.getX()))
            .put("b1", String.valueOf(b1 * 2))
            .put("b2", String.valueOf(b2 * 2))
            .put("g1", String.valueOf(g1 * 2))
            .put("g2", String.valueOf(g2 * 2))
            .put("baseVoltageId", getBaseVoltageId(voltageLevelId, cgmes.voltageLevels()))
            .build();
    }

    /**
     * @return the base voltage id
     */
    private static String getBaseVoltageId(String voltageLevelId, PropertyBags voltageLevels) {
        for(PropertyBag pb : voltageLevels) {
            if (pb.getId("VoltageLevel").equals(voltageLevelId)) {
                return pb.getId("BaseVoltage");
            } else {
                continue;
            }
        }
        return UUID.randomUUID().toString();
    }
}
