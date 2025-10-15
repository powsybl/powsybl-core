package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.SynchronizedGeneratorProperties;
import com.powsybl.iidm.network.extensions.SynchronizedGeneratorPropertiesAdder;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class SynchronizedGeneratorPropertiesAdderImpl extends AbstractExtensionAdder<Generator, SynchronizedGeneratorProperties> implements SynchronizedGeneratorPropertiesAdder {

    private String type;

    private boolean rpcl2;

    public SynchronizedGeneratorPropertiesAdderImpl(Generator generator) {
        super(generator);
    }

    @Override
    protected SynchronizedGeneratorProperties createExtension(Generator extendable) {
        return new SynchronizedGeneratorPropertiesImpl(extendable, type, rpcl2);
    }

    @Override
    public SynchronizedGeneratorPropertiesAdderImpl withType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public SynchronizedGeneratorPropertiesAdderImpl withRpcl2(boolean rpcl2) {
        this.rpcl2 = rpcl2;
        return this;
    }

}
