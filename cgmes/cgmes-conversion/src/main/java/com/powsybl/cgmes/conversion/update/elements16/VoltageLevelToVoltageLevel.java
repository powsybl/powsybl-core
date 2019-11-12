package com.powsybl.cgmes.conversion.update.elements16;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.conversion.update.AbstractIidmToCgmes;
import com.powsybl.cgmes.conversion.update.CgmesPredicateDetails;

import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public class VoltageLevelToVoltageLevel extends AbstractIidmToCgmes {
    public VoltageLevelToVoltageLevel() {
    }

    public static Map<String, CgmesPredicateDetails> mapIidmAtrribute() {
        /**
         * VoltageLevel and BaseVoltage element
         */
        return Collections.unmodifiableMap(Stream.of(
            entry("highVoltageLimit",
                new CgmesPredicateDetails("cim:VoltageLevel.highVoltageLimit", "_EQ", false, value)),
            entry("lowVoltageLimit",
                new CgmesPredicateDetails("cim:VoltageLevel.lowVoltageLimit", "_EQ", false, value)),
            entry("Substation", new CgmesPredicateDetails("cim:VoltageLevel.MemberOf_Substation", "_EQ", true, value)),
            entry("voltageLevelBaseVoltage",
                new CgmesPredicateDetails("cim:VoltageLevel.BaseVoltage", "_EQ", true, value)),
            entry("rdfTypeBV", new CgmesPredicateDetails("rdf:type", "_EQ", false, "cim:BaseVoltage", newSubject)),
            entry("nominalV", new CgmesPredicateDetails("cim:BaseVoltage.nominalVoltage", "_EQ", false,
                value, newSubject)))
            .collect(entriesToMap()));
    }

    public Map<String, String> getValues(IidmChange change, CgmesModel cgmes) {
        if (!(change.getIdentifiable() instanceof VoltageLevel)) {
            throw new ConversionException("Cannot cast the identifiable into the element");
        }
        VoltageLevel voltageLevel = (VoltageLevel) change.getIdentifiable();
        return ImmutableMap.<String, String>builder()
            .put("rdfType", "cim:VoltageLevel")
            .put("name", voltageLevel.getName())
            .put("highVoltageLimit", String.valueOf(voltageLevel.getHighVoltageLimit()))
            .put("lowVoltageLimit", String.valueOf(voltageLevel.getLowVoltageLimit()))
            .put("Substation", voltageLevel.getSubstation().getId())
            .put("nominalV", String.valueOf(voltageLevel.getNominalV()))
            .put("newSubject", getBaseVoltageId(change.getIdentifiableId(), cgmes.voltageLevels()))
            .build();
    }

    /**
     * Check if BaseVoltage element already exists in grid, if yes - returns its id,
     * otherwise new id if new element is created
     *
     * @return the base voltage id
     */
    private String getBaseVoltageId(String currId, PropertyBags voltageLevels) {
        for (PropertyBag pb : voltageLevels) {
            if (pb.getId("VoltageLevel").equals(currId)) {
                return pb.getId("BaseVoltage");
            } else {
                continue;
            }
        }
        return UUID.randomUUID().toString();
    }
}
