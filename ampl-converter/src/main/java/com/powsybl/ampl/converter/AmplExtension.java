/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import java.util.Objects;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;

/**
*
* @author Ferrari Giovanni <giovanni.ferrari@techrain.eu>
*/
public class AmplExtension<A extends Extension<B>, B> {

    private final int extendedNum;
    private final Extendable<B> extendable;
    private final A extension;

    public AmplExtension(int extended, Extendable<B> extendable, A extension) {
        this.extendedNum = extended;
        this.extendable = Objects.requireNonNull(extendable);
        this.extension = Objects.requireNonNull(extension);
    }

    public int getExtendedNum() {
        return extendedNum;
    }

    public Extendable<B> getExtendable() {
        return extendable;
    }

    public A getExtension() {
        return extension;
    }

}
