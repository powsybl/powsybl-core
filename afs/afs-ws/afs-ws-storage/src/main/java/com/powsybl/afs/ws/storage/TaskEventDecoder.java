/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.storage;

import com.powsybl.afs.TaskEvent;
import com.powsybl.afs.ws.utils.JacksonDecoder;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TaskEventDecoder extends JacksonDecoder<TaskEvent> {

    public TaskEventDecoder() {
        super(TaskEvent.class);
    }
}
