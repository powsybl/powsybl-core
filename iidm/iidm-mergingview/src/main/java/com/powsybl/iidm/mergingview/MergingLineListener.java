/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Identifiable;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class MergingLineListener extends AbstractListener {

    MergingLineListener(final MergingViewIndex index) {
        super(index);
    }

    @Override
    public void onCreation(final Identifiable identifiable) {
        if (identifiable instanceof DanglingLine) {
            // Check DanglingLine creation from Network merged into MergingView
            // in order to create a new MergedLine if it's needed
            index.checkNewDanglingLine((DanglingLine) identifiable);
        }
    }
}
