/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.action;

import java.util.Objects;

/**
 * An action opening or closing one side or both sides of a line.
 *
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class LineConnectionAction extends AbstractAction {

    public static final String NAME = "LINE_CONNECTION";

    private final String lineId;
    private final boolean openSide1;
    private final boolean openSide2;

    public LineConnectionAction(String id, String lineId, boolean openSide1, boolean openSide2) {
        super(id);
        this.lineId = Objects.requireNonNull(lineId);
        this.openSide1 = openSide1;
        this.openSide2 = openSide2;
    }

    public LineConnectionAction(String id, String lineId, boolean open) {
        super(id);
        this.lineId = Objects.requireNonNull(lineId);
        this.openSide1 = open;
        this.openSide2 = open;
    }

    @Override
    public String getType() {
        return NAME;
    }

    public String getLineId() {
        return lineId;
    }

    /**
     * If {@code true}, applying the action will open the terminal 1 of the line,
     * else it will close it.
     */
    public boolean isOpenSide1() {
        return openSide1;
    }

    /**
     * If {@code true}, applying the action will open the terminal 2 of the line,
     * else it will close it.
     */
    public boolean isOpenSide2() {
        return openSide2;
    }
}
