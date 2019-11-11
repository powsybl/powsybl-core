package com.powsybl.cgmes.conversion.update;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.powsybl.cgmes.conversion.update.elements16.*;

public class IidmToCgmes16 extends IidmToCgmes {

    public IidmToCgmes16() {
        this.converter = converter();
    }

    public Map<String, Map<String, CgmesPredicateDetails>> converter() {
        // map Identifiable Instance with its mapping method for IIDM - CGMES conversion
        return ImmutableMap.<String, Map<String, CgmesPredicateDetails>>builder()
            .put(GENERATOR_IMPL, GeneratorToSynchronousMachine.mapIidmAtrribute())
            .put(LOAD_IMPL, LoadToEnergyConsumer.mapIidmAtrribute())
            .put(CONFIGUREDBUS_IMPL, BusToTopologicalNode.mapIidmAtrribute())
            .put(LINE_IMPL, LineToACLineSegment.mapIidmAtrribute())
            .put(LCCCONVERTER_STATION_IMPL, LccConverterStationToAcdcConverter.mapIidmAtrribute())
            .put(SUBSTATION_IMPL, SubstationToSubstation.mapIidmAtrribute())
            .put(TWOWINDINGS_TRANSFORMER_IMPL, TwoWindingsTransformerToPowerTransformer.mapIidmAtrribute())
            .put(SHUNTCOMPENSATOR_IMPL, ShuntCompensatorToShuntCompensator.mapIidmAtrribute())
            .build();
    }
}
