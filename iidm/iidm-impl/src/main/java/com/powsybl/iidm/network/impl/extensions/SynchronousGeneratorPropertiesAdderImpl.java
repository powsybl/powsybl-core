package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.SynchronousGeneratorProperties;
import com.powsybl.iidm.network.extensions.SynchronousGeneratorPropertiesAdder;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class SynchronousGeneratorPropertiesAdderImpl extends AbstractExtensionAdder<Generator, SynchronousGeneratorProperties> implements SynchronousGeneratorPropertiesAdder {

    private int numberOfWindings;

    private String governor;

    private String voltageRegulator;

    private String pss;

    private boolean auxiliaries;

    private boolean internalTransformer;

    private boolean rpcl;

    private boolean rpcl2;

    private String uva;

    private boolean fictitious;

    private boolean qlim;

    public SynchronousGeneratorPropertiesAdderImpl(Generator generator) {
        super(generator);
    }

    @Override
    protected SynchronousGeneratorProperties createExtension(Generator extendable) {
        return new SynchronousGeneratorPropertiesImpl(extendable, numberOfWindings, governor, voltageRegulator, pss,
                auxiliaries, internalTransformer, rpcl, rpcl2, uva, fictitious, qlim);
    }

    @Override
    public SynchronousGeneratorPropertiesAdderImpl withNumberOfWindings(int numberOfWindings) {
        this.numberOfWindings = numberOfWindings;
        return this;
    }

    @Override
    public SynchronousGeneratorPropertiesAdderImpl withGovernor(String governor) {
        this.governor = governor;
        return this;
    }

    @Override
    public SynchronousGeneratorPropertiesAdderImpl withVoltageRegulator(String voltageRegulator) {
        this.voltageRegulator = voltageRegulator;
        return this;
    }

    @Override
    public SynchronousGeneratorPropertiesAdderImpl withPss(String pss) {
        this.pss = pss;
        return this;
    }

    @Override
    public SynchronousGeneratorPropertiesAdderImpl withAuxiliaries(boolean auxiliaries) {
        this.auxiliaries = auxiliaries;
        return this;
    }

    @Override
    public SynchronousGeneratorPropertiesAdderImpl withInternalTransformer(boolean internalTransformer) {
        this.internalTransformer = internalTransformer;
        return this;
    }

    @Override
    public SynchronousGeneratorPropertiesAdderImpl withRpcl(boolean rpcl) {
        this.rpcl = rpcl;
        return this;
    }

    @Override
    public SynchronousGeneratorPropertiesAdderImpl withRpcl2(boolean rpcl2) {
        this.rpcl2 = rpcl2;
        return this;
    }

    @Override
    public SynchronousGeneratorPropertiesAdderImpl withUva(String uva) {
        this.uva = uva;
        return this;
    }

    @Override
    public SynchronousGeneratorPropertiesAdderImpl withFictitious(boolean fictitious) {
        this.fictitious = fictitious;
        return this;
    }

    @Override
    public SynchronousGeneratorPropertiesAdderImpl withQlim(boolean qlim) {
        this.qlim = qlim;
        return this;
    }

}
