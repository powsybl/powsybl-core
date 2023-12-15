package com.powsybl.iidm.network.util.translation;

import com.powsybl.iidm.network.*;

import java.util.Optional;

public interface NetworkElementInterface {

    public String getId();

    Country getCountry1();

    Country getCountry2();

    Country getCountry();

    VoltageLevel getVoltageLevel1();

    VoltageLevel getVoltageLevel2();

    VoltageLevel getVoltageLevel3();

    VoltageLevel getVoltageLevel();

    Optional<? extends LoadingLimits> getLoadingLimits(LimitType limitType, ThreeSides side);
}
