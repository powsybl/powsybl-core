/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.adders;

import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.IdentifiableAdder;

import static com.powsybl.dataframe.network.adders.SeriesUtils.applyIfPresent;

/**
 * This hierarchy of classes aims at gathering usual columns on initialization
 * and set the corresponding data into IIDM adders.
 *
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
class IdentifiableSeries {
    protected final StringSeries ids;
    protected final StringSeries names;

    IdentifiableSeries(UpdatingDataframe dataframe) {
        this.ids = dataframe.getStrings("id");
        this.names = dataframe.getStrings("name");
    }

    protected void setIdentifiableAttributes(IdentifiableAdder<?, ?> adder, int row) {
        applyIfPresent(ids, row, adder::setId);
        applyIfPresent(names, row, adder::setName);
    }
}
