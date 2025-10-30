package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Generator;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public interface SynchronizedGeneratorProperties extends Extension<Generator> {

    String NAME = "synchronizedGeneratorProperties";

    @Override
    default String getName() {
        return NAME;
    }

    String getType();

    void setType(String type);

    boolean getRpcl2();

    void setRpcl2(boolean rpcl2);

}
