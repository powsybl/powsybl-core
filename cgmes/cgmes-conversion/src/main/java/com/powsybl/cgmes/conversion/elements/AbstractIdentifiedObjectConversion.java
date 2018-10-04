package com.powsybl.cgmes.conversion.elements;

/*
 * #%L
 * CGMES conversion
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.triplestore.PropertyBag;
import com.powsybl.triplestore.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public abstract class AbstractIdentifiedObjectConversion extends AbstractObjectConversion {

    public AbstractIdentifiedObjectConversion(
            String type,
            PropertyBag properties,
            Conversion.Context context) {
        super(type, properties, context);

        String idPropertyName = type;
        this.id = properties.getId(idPropertyName);
        this.name = p.get("name");
    }

    public AbstractIdentifiedObjectConversion(
            String type,
            PropertyBags propertiess,
            Conversion.Context context) {
        super(type, propertiess, context);

        String idPropertyName = type;
        this.id = ps.get(0).getId(idPropertyName);
        this.name = ps.get(0).get("name");
    }

    public String id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public String iidmId() {
        return context.namingStrategy().getId(type, id);
    }

    public String iidmName() {
        return context.namingStrategy().getName(type, name);
    }

    @Override
    protected String what() {
        if (name != null) {
            return String.format("%s %s (%s)", type, name, id);
        } else {
            return String.format("%s %s", type, id);
        }
    }

    @Override
    protected String complete(String what) {
        if (name != null) {
            return String.format("%s at %s %s (%s)", what, type, name, id);
        } else {
            return String.format("%s at %s %s", what, type, id);
        }
    }

    protected final String id;
    protected final String name;
}
