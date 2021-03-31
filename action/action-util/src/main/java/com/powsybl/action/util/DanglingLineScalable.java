package com.powsybl.action.util;

import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static com.powsybl.action.util.Scalable.ScalingConvention.*;

public class DanglingLineScalable extends AbstractInjectionScalable{
    private static final Logger LOGGER = LoggerFactory.getLogger(DanglingLineScalable.class);

    DanglingLineScalable(String id, double minValue, double maxValue) {
        super(id, minValue, maxValue);
    }

    @Override
    public void reset(Network n) {
        Objects.requireNonNull(n);

        DanglingLine dl = n.getDanglingLine(id);
        if (dl != null) {
            dl.setP0(0); //is it legit?
        }
    }

    /**
     * {@inheritDoc}
     *
     * Default value is Double.MAX_VALUE for LoadScalable, what is default value for DanglingLine?
     */
    @Override
    public double maximumValue(Network n, Scalable.ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);

        DanglingLine dl = n.getDanglingLine(id);
        if (dl != null) {
            return scalingConvention == LOAD ? maxValue : -minValue; //Quid? Is it legit to consider the same convention for a dangling line and a load?
        } else {
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Default value is 0 for LoadScalable, what is default value for Dangling Line?
     */
    @Override
    public double minimumValue(Network n, Scalable.ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);

        DanglingLine dl = n.getDanglingLine(id);
        if (dl != null) {
            return scalingConvention == LOAD ? minValue : -maxValue; //I really don't understand this line of code
        } else {
            return 0;
        }

    }

    @Override //Physical explanation? What are we filtering here? (I know injections, but what does it mean?)
    public void filterInjections(Network n, List<Injection> injections, List<String> notFoundInjections) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(injections);

        DanglingLine dl = n.getDanglingLine(id);
        if (dl != null) {
            injections.add(dl);
        } else if (notFoundInjections != null) { //Why is the id added if notFoundInjections is not null?
            notFoundInjections.add(id);
        }
    }

    /**
     * {@inheritDoc}
     *
     * If scalingConvention is LOAD, the load active power increases for positive "asked" and decreases inversely
     * If scalingConvention is GENERATOR, the load active power decreases for positive "asked" and increases inversely
     */
    @Override
    public double scale(Network n, double asked, Scalable.ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);

        DanglingLine dl = n.getDanglingLine(id);

        double done = 0;
        if (dl == null) {
            LOGGER.warn("Dangling line {} not found", id);
            return done;
        }

        Terminal t = dl.getTerminal(); //what exactly is a terminal?
        if (!t.isConnected()) {
            t.connect();
            LOGGER.info("Connecting {}", dl.getId());
        }

        double oldP0 = dl.getP0();
        if (oldP0 < minValue || oldP0 > maxValue) {
            LOGGER.error("Error scaling DanglingLineScalable {}: Initial P is not in the range [Pmin, Pmax]", id);
            return 0.;
        }

        // We use natural load convention to compute the limits.
        // The actual convention is taken into account afterwards.
        double availableDown = oldP0 - minValue;
        double availableUp = maxValue - oldP0;

        if (scalingConvention == LOAD) { //LOAD or something else?
            done = asked > 0 ? Math.min(asked, availableUp) : -Math.min(-asked, availableDown);
            dl.setP0(oldP0 + done);
        } else {
            done = asked > 0 ? Math.min(asked, availableDown) : -Math.min(-asked, availableUp);
            dl.setP0(oldP0 - done);
        }

        LOGGER.info("Change active power setpoint of {} from {} to {} ",
                dl.getId(), oldP0, dl.getP0());

        return done;
    }

}
