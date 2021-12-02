/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import java.util.OptionalInt;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class CgmesTapChangerImpl implements CgmesTapChanger {

    private final String id;
    private final String iidmId;
    private final String type;
    private final boolean hidden;
    private final Integer step;
    private final String controlId;

    CgmesTapChangerImpl(String id, String iidmId, String type, boolean hidden, Integer step, String controlId, CgmesTapChangersImpl<?> mapping) {
        this.id = id;
        this.iidmId = iidmId;
        this.type = type;
        this.hidden = hidden;
        this.step = step;
        this.controlId = controlId;
        attach(mapping);
    }

    private void attach(CgmesTapChangersImpl<?> mapping) {
        mapping.putTapChanger(this);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDifferentIidmId() {
        return iidmId;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    @Override
    public OptionalInt getStep() {
        return step != null ? OptionalInt.of(step) : OptionalInt.empty();
    }

    @Override
    public String getControlId() {
        return controlId;
    }
}
