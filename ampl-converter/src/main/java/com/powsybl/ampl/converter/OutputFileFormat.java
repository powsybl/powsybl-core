/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Simple POJO to define some properties of files read after an Ampl solve
 *
 * @author Nicolas Pierre {@literal <nicolas.pierre@artelys.com>}
 */
public interface OutputFileFormat {
    /**
     * @return regex string that will be used to separate tokens
     */
    String getTokenSeparator();

    String getFileExtension();

    Charset getFileEncoding();

    /**
     * Token separator : "( )+"
     * <p>
     * File extension : txt
     * <p>
     * Encoding : UTF-8
     * <p>
     */
    static OutputFileFormat getDefault() {
        return new OutputFileFormat() {

            @Override
            public String getTokenSeparator() {
                return "( )+";
            }

            @Override
            public String getFileExtension() {
                return "txt";
            }

            @Override
            public Charset getFileEncoding() {
                return StandardCharsets.UTF_8;
            }

        };
    }

}
