package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.SynchronousGeneratorProperties;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class SynchronousGeneratorPropertiesImpl extends AbstractExtension<Generator> implements SynchronousGeneratorProperties {

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

    public SynchronousGeneratorPropertiesImpl(Generator generator,
                                              int numberOfWindings,
                                              String governor,
                                              String voltageRegulator,
                                              String pss,
                                              boolean auxiliaries,
                                              boolean internalTransformer,
                                              boolean rpcl,
                                              boolean rpcl2,
                                              String uva,
                                              boolean fictitious,
                                              boolean qlim) {
        super(generator);
        this.numberOfWindings = numberOfWindings;
        this.governor = governor;
        this.voltageRegulator = voltageRegulator;
        this.pss = pss;
        this.auxiliaries = auxiliaries;
        this.internalTransformer = internalTransformer;
        this.rpcl = rpcl;
        this.rpcl2 = rpcl2;
        this.uva = uva;
        this.fictitious = fictitious;
        this.qlim = qlim;
    }

    @Override
    public int getNumberOfWindings() {
        return numberOfWindings;
    }

    @Override
    public String getGovernor() {
        return governor;
    }

    @Override
    public String getVoltageRegulator() {
        return voltageRegulator;
    }

    @Override
    public String getPss() {
        return pss;
    }

    @Override
    public boolean getAuxiliaries() {
        return auxiliaries;
    }

    @Override
    public boolean getInternalTransformer() {
        return internalTransformer;
    }

    @Override
    public boolean getRpcl() {
        return rpcl;
    }

    @Override
    public boolean getRpcl2() {
        return rpcl2;
    }

    @Override
    public String getUva() {
        return uva;
    }

    @Override
    public boolean getFictitious() {
        return fictitious;
    }

    @Override
    public boolean getQlim() {
        return qlim;
    }

}
