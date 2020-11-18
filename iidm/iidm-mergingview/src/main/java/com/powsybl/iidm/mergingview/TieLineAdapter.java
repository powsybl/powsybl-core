/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TieLineAdapter extends LineAdapter implements TieLine {

    TieLineAdapter(final TieLine delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public String getUcteXnodeCode() {
        return ((TieLine) getDelegate()).getUcteXnodeCode();
    }

    @Override
    public HalfLine getHalf1() {
        return ((TieLine) getDelegate()).getHalf1();
    }

    @Override
    public HalfLine getHalf2() {
        return ((TieLine) getDelegate()).getHalf2();
    }

    @Override
    public HalfLine getHalf(Side side) {
        return ((TieLine) getDelegate()).getHalf(side);
    }
}
