package com.powsybl.cgmes.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.update.elements14.BusToTopologicalNode;
import com.powsybl.cgmes.update.elements14.GeneratorToSynchronousMachine;
import com.powsybl.cgmes.update.elements14.LineToACLineSegment;
import com.powsybl.cgmes.update.elements14.LoadToEnergyConsumer;
import com.powsybl.cgmes.update.elements14.ShuntCompensatorToShuntCompensator;
import com.powsybl.cgmes.update.elements14.SubstationToSubstation;
import com.powsybl.cgmes.update.elements14.TwoWindingsTransformerToPowerTransformer;
import com.powsybl.cgmes.update.elements14.VoltageLevelToVoltageLevel;

public class IidmToCgmes14 extends AbstractIidmToCgmes {

    public IidmToCgmes14(IidmChange change, CgmesModel cgmes) {
        super(change, cgmes);
    }

    public IidmToCgmes14(IidmChange change) {
        super(change);
    }

    @Override
    protected Multimap<String, CgmesPredicateDetails> switcher() {
        LOG.info("IIDM instance is: " + getIidmInstanceName());
        switch (getIidmInstanceName()) {
            case SUBSTATION_IMPL:
                SubstationToSubstation sb = new SubstationToSubstation(change);
                mapIidmToCgmesPredicates = sb.mapIidmToCgmesPredicates();
                break;
            case BUSBREAKER_VOLTAGELEVEL:
                VoltageLevelToVoltageLevel vl = new VoltageLevelToVoltageLevel(change, cgmes);
                mapIidmToCgmesPredicates = vl.mapIidmToCgmesPredicates();
                break;
            case CONFIGUREDBUS_IMPL:
                BusToTopologicalNode btn = new BusToTopologicalNode(change, cgmes);
                mapIidmToCgmesPredicates = btn.mapIidmToCgmesPredicates();
                break;
            case TWOWINDINGS_TRANSFORMER_IMPL:
                TwoWindingsTransformerToPowerTransformer twpt = new TwoWindingsTransformerToPowerTransformer(change,
                    cgmes);
                mapIidmToCgmesPredicates = twpt.mapIidmToCgmesPredicates();
                break;
            case GENERATOR_IMPL:
                GeneratorToSynchronousMachine gsm = new GeneratorToSynchronousMachine(change, cgmes);
                mapIidmToCgmesPredicates = gsm.mapIidmToCgmesPredicates();
                break;
            case LOAD_IMPL:
                LoadToEnergyConsumer lec = new LoadToEnergyConsumer(change, cgmes);
                mapIidmToCgmesPredicates = lec.mapIidmToCgmesPredicates();
                break;
            case LINE_IMPL:
                LineToACLineSegment lac = new LineToACLineSegment(change, cgmes);
                mapIidmToCgmesPredicates = lac.mapIidmToCgmesPredicates();
                break;
            case SHUNTCOMPENSATOR_IMPL:
                ShuntCompensatorToShuntCompensator sc = new ShuntCompensatorToShuntCompensator(change);
                mapIidmToCgmesPredicates = sc.mapIidmToCgmesPredicates();
                break;
            default:
                LOG.info("This element is not convertable to CGMES");
        }

        return mapIidmToCgmesPredicates;
    }

    private static final Logger LOG = LoggerFactory.getLogger(IidmToCgmes14.class);
}
