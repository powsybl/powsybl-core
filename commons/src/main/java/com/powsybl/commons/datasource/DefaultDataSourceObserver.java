/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

/**
 * This class provides a default empty implementation for the
 * <code>DataSourceObserver</code> interface.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DefaultDataSourceObserver implements DataSourceObserver {

    @Override
    public void opened(String streamName) {
        // empty default implementation
    }

    @Override
    public void closed(String streamName) {
        // empty default implementation
    }

}
