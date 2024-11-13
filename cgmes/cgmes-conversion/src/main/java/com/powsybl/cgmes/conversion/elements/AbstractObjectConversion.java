/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public abstract class AbstractObjectConversion {

    protected AbstractObjectConversion(String type, PropertyBag properties, Context context) {
        this.type = Objects.requireNonNull(type);
        this.p = Objects.requireNonNull(properties);
        this.ps = null;
        this.context = Objects.requireNonNull(context);
    }

    protected AbstractObjectConversion(String type, PropertyBags properties, Context context) {
        this.type = Objects.requireNonNull(type);
        this.p = null;
        this.ps = Objects.requireNonNull(properties);
        this.context = Objects.requireNonNull(context);
    }

    public boolean insideBoundary() {
        return false;
    }

    public void convertInsideBoundary() {
        throw new ConversionException("No implementation at this level");
    }

    public abstract boolean valid();

    public abstract void convert();

    public void update() {
        throw new ConversionException("Missing implementation: update for " + type);
    }

    public abstract String what();

    protected abstract String complete(String what);

    public static int fromContinuous(double value) {
        // ShuntCompensator sections and TapChanger step/position
        // coming from SV or SSH may be continuous,
        // we must read these values as floating point numbers
        // and convert them to integers for IIDM.
        // We should consider storing these attributes in IIDM also as real numbers.
        // This method is used only to clearly identify the affected properties
        // The value is supposed to be rounded to the nearest integer
        return (int) Math.round(value);
    }

    public void invalid(String reason) {
        context.invalid(what(), reason);
    }

    public void ignored(String reason) {
        context.ignored(what(), reason);
    }

    public void ignored(Supplier<String> reason) {
        context.ignored(what(), reason);
    }

    public void ignored(String what, Supplier<String> reason) {
        context.ignored(complete(what), reason);
    }

    public void fixed(String what, String reason) {
        context.fixed(complete(what), reason);
    }

    public void fixed(String what, Supplier<String> reason) {
        context.fixed(complete(what), reason);
    }

    public void fixed(String what, String reason, double wrong, double fixed) {
        context.fixed(complete(what), reason, wrong, fixed);
    }

    public void missing(String what) {
        context.missing(complete(what));
    }

    public void missing(String what, double defaultValue) {
        context.missing(complete(what), defaultValue);
    }

    protected final String type;
    protected final PropertyBag p;
    protected final PropertyBags ps;
    protected final Context context;
}
