package com.powsybl.security.detectors.criterion.network;

import com.powsybl.iidm.network.util.criterion.SingleCountryCriterion;
import com.powsybl.iidm.network.util.criterion.TwoNominalVoltageCriterion;

import java.util.Set;

public class TwoWindingsTransformerCriterion extends AbstractNetworkElementCriterion {

    SingleCountryCriterion singleCountryCriterion;
    TwoNominalVoltageCriterion twoNominalVoltageCriterion = new TwoNominalVoltageCriterion(null, null);

    public TwoWindingsTransformerCriterion(Set<String> networkElementIds) {
        super(networkElementIds);
    }

    @Override
    public NetworkElementCriterionType getNetworkElementCriterionType() {
        return NetworkElementCriterionType.TWO_WINDING_TRANSFORMER;
    }

    @Override
    public boolean accept(NetworkElementVisitor networkElementVisitor) {
        return networkElementVisitor.visitTwoWindingsTransformerCriterion(this);
    }

    public SingleCountryCriterion getSingleCountryCriterion() {
        return singleCountryCriterion;
    }

    public TwoWindingsTransformerCriterion setSingleCountryCriterion(SingleCountryCriterion singleCountryCriterion) {
        this.singleCountryCriterion = singleCountryCriterion;
        return this;
    }

    public TwoNominalVoltageCriterion getTwoNominalVoltageCriterion() {
        return twoNominalVoltageCriterion;
    }

    public TwoWindingsTransformerCriterion setTwoNominalVoltageCriterion(TwoNominalVoltageCriterion twoNominalVoltageCriterion) {
        this.twoNominalVoltageCriterion = twoNominalVoltageCriterion;
        return this;
    }

}
