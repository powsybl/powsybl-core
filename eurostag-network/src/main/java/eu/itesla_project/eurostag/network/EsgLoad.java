/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag.network;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EsgLoad {

    private final Esg8charName znamlo; // load name
    private final EsgConnectionStatus iloadst; // Status
                                                        //    ‘ ‘	: connected
                                                        //    ‘-’	: not connected
    private final Esg8charName znodlo; // connection node name
    private final float pldsti; // active load [MW]
    private final float pldstz; // active load [MW]
    private final float pldstp; // active load [MW]
    private final float qldsti; // reactive load [Mvar]
    private final float qldstz; // reactive load [Mvar]
    private final float qldstp; // reactive load [Mvar]

    public EsgLoad(EsgConnectionStatus iloadst, Esg8charName znamlo, Esg8charName znodlo, float pldsti, float pldstz, float pldstp, float qldsti, float qldstz, float qldstp) {
        this.iloadst = Objects.requireNonNull(iloadst);
        this.znamlo = Objects.requireNonNull(znamlo);
        this.znodlo = Objects.requireNonNull(znodlo);
        this.pldsti = pldsti;
        this.pldstz = pldstz;
        this.pldstp = pldstp;
        this.qldsti = qldsti;
        this.qldstz = qldstz;
        this.qldstp = qldstp;
    }

    public EsgConnectionStatus getIloadst() {
        return iloadst;
    }

    public float getPldsti() {
        return pldsti;
    }

    public float getPldstp() {
        return pldstp;
    }

    public float getPldstz() {
        return pldstz;
    }

    public float getQldsti() {
        return qldsti;
    }

    public float getQldstp() {
        return qldstp;
    }

    public float getQldstz() {
        return qldstz;
    }

    public Esg8charName getZnamlo() {
        return znamlo;
    }

    public Esg8charName getZnodlo() {
        return znodlo;
    }
}
