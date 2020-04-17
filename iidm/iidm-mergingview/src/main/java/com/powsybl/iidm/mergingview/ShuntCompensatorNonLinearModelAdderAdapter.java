/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.ShuntCompensatorAdder;
import com.powsybl.iidm.network.ShuntCompensatorNonLinearModelAdder;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class ShuntCompensatorNonLinearModelAdderAdapter implements ShuntCompensatorNonLinearModelAdder {

    private final ShuntCompensatorNonLinearModelAdder delegate;
    private final ShuntCompensatorAdderAdapter parent;

    ShuntCompensatorNonLinearModelAdderAdapter(final ShuntCompensatorNonLinearModelAdder delegate, ShuntCompensatorAdderAdapter parent) {
        this.delegate = Objects.requireNonNull(delegate);
        this.parent = Objects.requireNonNull(parent);
    }

    class SectionAdderAdapter implements SectionAdder {

        private final SectionAdder delegate;
        private final ShuntCompensatorNonLinearModelAdderAdapter parent;

        SectionAdderAdapter(final SectionAdder delegate, ShuntCompensatorNonLinearModelAdderAdapter parent) {
            this.delegate = Objects.requireNonNull(delegate);
            this.parent = Objects.requireNonNull(parent);
        }

        @Override
        public SectionAdder setSectionIndex(int sectionIndex) {
            delegate.setSectionIndex(sectionIndex);
            return this;
        }

        @Override
        public SectionAdder setB(double b) {
            delegate.setB(b);
            return this;
        }

        @Override
        public SectionAdder setG(double g) {
            delegate.setG(g);
            return this;
        }

        @Override
        public ShuntCompensatorNonLinearModelAdder endSection() {
            delegate.endSection();
            return parent;
        }
    }

    @Override
    public SectionAdder beginSection() {
        return new SectionAdderAdapter(delegate.beginSection(), this);
    }

    @Override
    public ShuntCompensatorAdder add() {
        delegate.add();
        return parent;
    }
}
