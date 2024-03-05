package com.powsybl.security.json.action;

import com.powsybl.security.action.DanglingLineAction;
import com.powsybl.security.action.DanglingLineActionBuilder;

public class DanglingLineActionDeserializer extends AbstractLoadActionDeserializer<DanglingLineAction> {

    public DanglingLineActionDeserializer() {
        super(DanglingLineAction.class);
    }

    @Override
    protected DanglingLineAction createAction(ParsingContext context) {
        DanglingLineActionBuilder builder = new DanglingLineActionBuilder();
        builder.withId(context.id).withLoadId(context.loadId).withRelativeValue(context.relativeValue);
        if (context.activePowerValue != null) {
            builder.withActivePowerValue(context.activePowerValue);
        }
        if (context.reactivePowerValue != null) {
            builder.withReactivePowerValue(context.reactivePowerValue);
        }
        return builder.build();
    }
}
