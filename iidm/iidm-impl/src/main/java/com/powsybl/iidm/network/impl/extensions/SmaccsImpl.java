package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.Smacc;
import com.powsybl.iidm.network.extensions.Smaccs;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class SmaccsImpl extends AbstractExtension<Network> implements Smaccs {

    private final List<Smacc> smaccs;

    SmaccsImpl(List<Smacc> smaccs) {
        this.smaccs = Objects.requireNonNull(smaccs);
    }

    @Override
    public List<Smacc> getSmaccs() {
        return Collections.unmodifiableList(smaccs);
    }

    @Override
    public Optional<Smacc> getSmacc(String name) {
        Objects.requireNonNull(name);
        return smaccs.stream().filter(z -> z.getName().equals(name)).findFirst();
    }
}
