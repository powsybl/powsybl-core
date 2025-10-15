package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.Acmc;
import com.powsybl.iidm.network.extensions.Acmcs;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class AcmcsImpl extends AbstractExtension<Network> implements Acmcs {

    private final List<Acmc> acmcs;

    AcmcsImpl(List<Acmc> acmcs) {
        this.acmcs = Objects.requireNonNull(acmcs);
    }

    @Override
    public List<Acmc> getAcmcs() {
        return Collections.unmodifiableList(acmcs);
    }

    @Override
    public Optional<Acmc> getAcmc(String name) {
        Objects.requireNonNull(name);
        return acmcs.stream().filter(z -> z.getName().equals(name)).findFirst();
    }
}
