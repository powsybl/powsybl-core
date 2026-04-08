/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.tools.autocompletion;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at gmail.com>}
 */
public interface OptionType {

    OptionType FILE = File.INSTANCE;
    OptionType DIRECTORY = Directory.INSTANCE;
    OptionType HOSTNAME = Hostname.INSTANCE;

    Kind getKind();

    static OptionType enumeration(Class<? extends Enum<?>> clazz) {
        return new Enumeration(clazz);
    }

    enum Kind {
        FILE,
        DIRECTORY,
        HOSTNAME,
        ENUMERATION
    }

    enum File implements OptionType {
        INSTANCE;

        @Override
        public Kind getKind() {
            return Kind.FILE;
        }
    }

    enum Directory implements OptionType {
        INSTANCE;

        @Override
        public Kind getKind() {
            return Kind.DIRECTORY;
        }
    }

    enum Hostname implements OptionType {
        INSTANCE;

        @Override
        public Kind getKind() {
            return Kind.HOSTNAME;
        }
    }

    class Enumeration implements OptionType {
        private final Class<? extends Enum<?>> clazz;

        public Enumeration(Class<? extends Enum<?>> clazz) {
            this.clazz = clazz;
        }

        public Class<? extends Enum<?>> getClazz() {
            return clazz;
        }

        @Override
        public Kind getKind() {
            return Kind.ENUMERATION;
        }
    }

}
