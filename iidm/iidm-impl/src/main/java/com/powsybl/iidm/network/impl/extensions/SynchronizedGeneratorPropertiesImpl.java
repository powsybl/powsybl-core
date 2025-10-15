package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.SynchronizedGeneratorProperties;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class SynchronizedGeneratorPropertiesImpl extends AbstractExtension<Generator> implements SynchronizedGeneratorProperties {

    private String type;

    private boolean rpcl2;

    public SynchronizedGeneratorPropertiesImpl(Generator generator,
                                              String type, boolean rpcl2) {
        super(generator);
        this.type = type;
        this.rpcl2 = rpcl2;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean getRpcl2() {
        return rpcl2;
    }

}
