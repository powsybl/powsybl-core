/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.IdentifiableAdder;
import com.powsybl.iidm.network.util.Identifiables;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
abstract class AbstractIdentifiableAdderAdapter<I extends IdentifiableAdder<I>> extends AbstractAdapter<I> implements IdentifiableAdder<I>  {

    private String id;

    private boolean ensureIdUnicity = false;

    AbstractIdentifiableAdderAdapter(I delegate, MergingViewIndex index) {
        super(delegate, index);
    }

    protected String getId() {
        return id;
    }

    @Override
    public I setId(final String id) {
        this.id = id;
        getDelegate().setId(id);
        return (I) this;
    }

    protected boolean isEnsureIdUnicity() {
        return ensureIdUnicity;
    }

    @Override
    public I setEnsureIdUnicity(final boolean ensureIdUnicity) {
        this.ensureIdUnicity = ensureIdUnicity;
        getDelegate().setEnsureIdUnicity(ensureIdUnicity);
        return (I) this;
    }

    @Override
    public I setName(final String name) {
        getDelegate().setName(name);
        return (I) this;
    }

    @Override
    public I setFictitious(boolean fictitious) {
        getDelegate().setFictitious(fictitious);
        return (I) this;
    }

    protected void checkAndSetUniqueId() {
        if (this.id == null) {
            throw new PowsyblException(getClass().getSimpleName() + " id is not set");
        }
        if (ensureIdUnicity) {
            setId(Identifiables.getUniqueId(id, getIndex()::contains));
        } else {
            // Check Id is unique in all merging view
            if (getIndex().contains(id)) {
                throw new PowsyblException("The network already contains an object '"
                                           + getDelegate().getClass().getSimpleName()
                                           + "' with the id '"
                                           + id
                                           + "'");
            }
        }
    }
}
