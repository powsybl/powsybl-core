/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

/**
 *
 * @author Nicolas Lhuillier <nicolas.lhuillier at rte-france.com>
 */
public interface ReadOnlyScript {

    default String getScriptLabel() {
        return null;
    }

    ScriptType getScriptType();

    String readScript();

    void addListener(ScriptListener listener);

    void removeListener(ScriptListener listener);
}
