package com.powsybl.cgmes.conversion.elements.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerHolder;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

public class RatioTapChangerExtension
        extends AbstractExtension<ThreeWindingsTransformer>
        implements RatioTapChangerHolder {

    private RatioTapChanger ratioTapChanger;

    @Override
    public String getName() {
        return "RatioTapChangerExtension";
    }

    @Override
    public RatioTapChangerAdder newRatioTapChanger() {
        return new RatioTapChangerExtensionAdder(this);
    }

    @Override
    public RatioTapChanger getRatioTapChanger() {
        return ratioTapChanger;
    }

    // Protected, only our adder can set it
    void setRatioTapChanger(RatioTapChanger rtc) {
        this.ratioTapChanger = rtc;
    }
}
