/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import com.powsybl.commons.io.ForwardingOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream wrapper that allows notification when the stream is opened
 * and closed.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ObservableOutputStream extends ForwardingOutputStream<OutputStream> {

    private final String streamName;

    private final DataSourceObserver observer;

    public ObservableOutputStream(OutputStream os, String streamName, DataSourceObserver observer) {
        super(os);
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
