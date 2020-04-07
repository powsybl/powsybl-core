package com.powsybl.ucte.converter;

import com.powsybl.iidm.network.Network;
import com.powsybl.ucte.network.UcteNetwork;

/**
 *
 * @author Jérémy LABOUS <jlabous@silicom.fr>
 */
public interface UcteExportPostProcessor {

    String getName();

    void process(Network network, UcteNetwork ucteNetwork) throws Exception;

}
