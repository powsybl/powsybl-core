package com.powsybl.cgmes.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.update.elements.*;

public class IidmToCgmes14 extends IidmToCgmesAbstract {

    public IidmToCgmes14(IidmChange change, CgmesModel cgmes) {
        super(change, cgmes);
    }

    public IidmToCgmes14(IidmChange change) {
        super(change);
    }

    @Override
    protected TwoMaps switcher() {
        LOG.info("IIDM instance is: " + getIidmInstanceName());
        switch (getIidmInstanceName()) {
            case SUBSTATION_IMPL:
                SubstationToSubstation sb = new SubstationToSubstation(change);
                mapIidmToCgmesPredicates = sb.mapIidmToCgmesPredicates();
                allCgmesDetails = sb.getAllCgmesDetailsOnCreate();
                break;
            case BUSBREAKER_VOLTAGELEVEL:
                VoltageLevelToVoltageLevel vl = new VoltageLevelToVoltageLevel(change, cgmes);
                mapIidmToCgmesPredicates = vl.mapIidmToCgmesPredicates();
                allCgmesDetails = vl.getAllCgmesDetailsOnCreate();
                break;
            case CONFIGUREDBUS_IMPL:
                BusToTopologicalNode btn = new BusToTopologicalNode(change, cgmes);
                mapIidmToCgmesPredicates = btn.mapIidmToCgmesPredicates();
                allCgmesDetails = btn.getAllCgmesDetailsOnCreate();
                break;
            case TWOWINDINGS_TRANSFORMER_IMPL:
                TwoWindingsTransformerToPowerTransformer twpt = new TwoWindingsTransformerToPowerTransformer(change,
                    cgmes);
                mapIidmToCgmesPredicates = twpt.mapIidmToCgmesPredicates();
                allCgmesDetails = twpt.getAllCgmesDetailsOnCreate();
                break;
            case GENERATOR_IMPL:
                GeneratorToSynchronousMachine gsm = new GeneratorToSynchronousMachine(change, cgmes);
                mapIidmToCgmesPredicates = gsm.mapIidmToCgmesPredicates();
                allCgmesDetails = gsm.getAllCgmesDetailsOnCreate();
                break;
            case LOAD_IMPL:
                LoadToEnergyConsumer lec = new LoadToEnergyConsumer(change, cgmes);
                mapIidmToCgmesPredicates = lec.mapIidmToCgmesPredicates();
                allCgmesDetails = lec.getAllCgmesDetailsOnCreate();
                break;
            case LINE_IMPL:
                LineToACLineSegment lac = new LineToACLineSegment(change);
                mapIidmToCgmesPredicates = lac.mapIidmToCgmesPredicates();
                allCgmesDetails = lac.getAllCgmesDetailsOnCreate();
                break;
            case SHUNTCOMPENSATOR_IMPL:
                ShuntCompensatorToShuntCompensator sc = new ShuntCompensatorToShuntCompensator(change);
                mapIidmToCgmesPredicates = sc.mapIidmToCgmesPredicates();
                allCgmesDetails = sc.getAllCgmesDetailsOnCreate();
                break;
            default:
                LOG.info("This element is not convertable to CGMES");
        }
        TwoMaps result = new TwoMaps(mapIidmToCgmesPredicates, allCgmesDetails);
        return result;
    }

    private static final Logger LOG = LoggerFactory.getLogger(IidmToCgmes14.class);
}
