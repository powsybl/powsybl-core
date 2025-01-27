package com.powsybl.ucte.converter.export.elements;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Load;
import com.powsybl.ucte.network.UcteNode;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public final class LoadUcteExport {

    private LoadUcteExport() {
    }

    /**
     * Initialize the power consumption fields from the loads connected to the specified bus.
     *
     * @param ucteNode The UCTE node to fill
     * @param bus The bus the loads are connected to
     */
    public static void convertLoads(UcteNode ucteNode, Bus bus) {
        double activeLoad = 0.0;
        double reactiveLoad = 0.0;
        for (Load load : bus.getLoads()) {
            activeLoad += load.getP0();
            reactiveLoad += load.getQ0();
        }
        ucteNode.setActiveLoad(activeLoad);
        ucteNode.setReactiveLoad(reactiveLoad);
    }
}
