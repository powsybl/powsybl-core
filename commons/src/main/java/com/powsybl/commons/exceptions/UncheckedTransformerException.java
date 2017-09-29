/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.exceptions;

import javax.xml.transform.TransformerException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UncheckedTransformerException extends RuntimeException {

    private static final long serialVersionUID = 8386645257112988838L;

    public UncheckedTransformerException(TransformerException cause) {
        super(cause);
    }

    @Override
    public TransformerException getCause() {
        return (TransformerException) super.getCause();
    }
}
