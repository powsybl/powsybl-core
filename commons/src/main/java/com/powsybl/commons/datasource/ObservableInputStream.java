/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import com.powsybl.commons.io.ForwardingInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ObservableInputStream extends ForwardingInputStream<InputStream> {

    private final String streamName;

    private final DataSourceObserver observer;

    ObservableInputStream(InputStream is, String streamName, DataSourceObserver observer) {
        super(is);
        this.streamName = streamName;
        this.observer = observer;
        if (observer != null) {
            observer.opened(streamName);
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (observer != null) {
            observer.closed(streamName);
        }
    }

}
