/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import java.util.OptionalInt;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class CgmesTapChangerImpl implements CgmesTapChanger {

    private final String id;
    private final String combinedTapChangerId;
    private String type;
    private final boolean hidden;
    private final Integer step;
    private String controlId;

    CgmesTapChangerImpl(String id, String combinedTapChangerId, String type, boolean hidden, Integer step, String controlId, CgmesTapChangersImpl<?> mapping) {
        this.id = id;
        this.combinedTapChangerId = combinedTapChangerId;
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
    public String getCombinedTapChangerId() {
        return combinedTapChangerId;
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

    @Override
    public void setControlId(String controlId) {
        this.controlId = controlId;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }
}
