/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.PowsyblException;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class CgmesTapChangerAdderImpl implements CgmesTapChangerAdder {

    private final CgmesTapChangersImpl<?> mapping;

    private String id = null;
    private String combinedTapChangerId = null;
    private String type = null;
    private boolean hidden = false;
    private Integer step = null;
    private String controlId = null;

    CgmesTapChangerAdderImpl(CgmesTapChangersImpl<?> mapping) {
        this.mapping = mapping;
    }

    @Override
    public CgmesTapChangerAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public CgmesTapChangerAdder setCombinedTapChangerId(String combinedTapChangerId) {
        this.combinedTapChangerId = combinedTapChangerId;
        return this;
    }

    @Override
    public CgmesTapChangerAdder setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public CgmesTapChangerAdder setHiddenStatus(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    @Override
    public CgmesTapChangerAdder setStep(int step) {
        this.step = step;
        return this;
    }

    @Override
    public CgmesTapChangerAdder setControlId(String controlId) {
        this.controlId = controlId;
        return this;
    }

    @Override
    public CgmesTapChanger add() {
        if (id == null) {
            throw new PowsyblException("Tap changer ID should not be null");
        }
        if (!hidden) {
            // step is used to record the normalStep of the tapChanger when it is not hidden
            if (combinedTapChangerId != null) {
                throw new PowsyblException("Non-hidden tap changers do not have a different ID for the combined tap changer");
            }
        }
        if (hidden) {
            if (step == null) { // normalStep of the hidden tapChanger
                throw new PowsyblException("Hidden tap changers step positions should be explicit");
            }
            if (combinedTapChangerId == null) {
                throw new PowsyblException("Hidden tap changers should have an ID for the combined tap changer");
            }
        }
        return new CgmesTapChangerImpl(id, combinedTapChangerId, type, hidden, step, controlId, mapping);
    }
}
