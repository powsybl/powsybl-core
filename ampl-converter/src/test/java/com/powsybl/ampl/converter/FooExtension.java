package com.powsybl.ampl.converter;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.HvdcLine;

public class FooExtension extends AbstractExtension<HvdcLine> {

    @Override
    public String getName() {
        return "Foo";
    }

}
