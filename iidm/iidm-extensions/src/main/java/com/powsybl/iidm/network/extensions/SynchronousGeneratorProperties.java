package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Generator;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public interface SynchronousGeneratorProperties extends Extension<Generator> {

    String NAME = "synchronousGeneratorProperties";

    @Override
    default String getName() {
        return NAME;
    }

    int getNumberOfWindings();

    void setNumberOfWindings(int numberOfWindings);

    String getGovernor();

    void setGovernor(String governor);

    String getVoltageRegulator();

    void setVoltageRegulator(String voltageRegulator);

    String getPss();

    void setPss(String pss);

    boolean getAuxiliaries();

    void setAuxiliaries(boolean auxiliaries);

    boolean getInternalTransformer();

    void setInternalTransformer(boolean internalTransformer);

    boolean getRpcl();

    void setRpcl(boolean rpcl);

    boolean getRpcl2();

    void setRpcl2(boolean rpcl2);

    String getUva();

    void setUva(String uva);

    boolean getFictitious();

    void setFictitious(boolean fictitious);

    boolean getQlim();

    void setQlim(boolean qlim);

}
