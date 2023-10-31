package com.powsybl.security.detectors.criterion.network;

import com.powsybl.iidm.network.util.criterion.SingleCountryCriterion;
import com.powsybl.iidm.network.util.criterion.SingleNominalVoltageCriterion;

import java.util.Set;

public class BusCriterion extends AbstractNetworkElementCriterion implements NetworkElementCriterion {

    SingleCountryCriterion singleCountryCriterion = new SingleCountryCriterion(null);
    SingleNominalVoltageCriterion singleNominalVoltageCriterion = new SingleNominalVoltageCriterion(null);

    public BusCriterion(Set<String> networkElementIds) {
        super(networkElementIds);
    }

    @Override
    public NetworkElementCriterionType getNetworkElementCriterionType() {
        return NetworkElementCriterionType.BUS;
    }

    public SingleCountryCriterion getSingleCountryCriterion() {
        return singleCountryCriterion;
    }

    public BusCriterion setSingleCountryCriterion(SingleCountryCriterion singleCountryCriterion) {
        this.singleCountryCriterion = singleCountryCriterion;
        return this;
    }

    public SingleNominalVoltageCriterion getSingleNominalVoltageCriterion() {
        return singleNominalVoltageCriterion;
    }

    public BusCriterion setSingleNominalVoltageCriterion(SingleNominalVoltageCriterion singleNominalVoltageCriterion) {
        this.singleNominalVoltageCriterion = singleNominalVoltageCriterion;
        return this;
    }
}
