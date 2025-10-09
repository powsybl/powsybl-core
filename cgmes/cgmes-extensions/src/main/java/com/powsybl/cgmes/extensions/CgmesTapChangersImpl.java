/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class CgmesTapChangersImpl<C extends Connectable<C>> extends AbstractExtension<C> implements CgmesTapChangers<C> {

    private final Map<String, CgmesTapChanger> tapChangers = new HashMap<>();

    CgmesTapChangersImpl(C transformer) {
        super(transformer);
    }

    @Override
    public Set<CgmesTapChanger> getTapChangers() {
        return tapChangers.values().stream().sorted(Comparator.comparing(CgmesTapChanger::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new)); // If not sorted, hard to debug in SSH files
    }

    @Override
    public CgmesTapChanger getTapChanger(String id) {
        return tapChangers.get(id);
    }

    @Override
    public CgmesTapChangerAdder newTapChanger() {
        return new CgmesTapChangerAdderImpl(this);
    }

    void putTapChanger(CgmesTapChangerImpl tapChanger) {
        String tapChangerId = tapChanger.getId();
        if (tapChangers.containsKey(tapChangerId)) {
            throw new PowsyblException(String.format("Tap changer %s has already been added", tapChangerId));
        }
        tapChangers.put(tapChangerId, tapChanger);
    }
}
