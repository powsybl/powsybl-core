package com.powsybl.action.json;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.action.*;
import com.powsybl.iidm.network.identifiers.NetworkElementIdentifier;
import com.powsybl.iidm.network.identifiers.json.IdentifierDeserializer;
import com.powsybl.iidm.network.identifiers.json.IdentifierSerializer;

public class ActionJsonModule extends SimpleModule {

    public ActionJsonModule() {
        addSerializer(ActionList.class, new ActionListSerializer());
        addSerializer(NetworkElementIdentifier.class, new IdentifierSerializer());
        addDeserializer(ActionList.class, new ActionListDeserializer());
        addDeserializer(NetworkElementIdentifier.class, new IdentifierDeserializer());
        configureActionsSerialization();
    }

    /**
     * Deserializer for actions will be chosen based on the "type" property.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY, visible = true)
    interface ActionMixIn {
    }

    private <T> void registerActionType(Class<T> actionClass, String typeName, JsonSerializer<T> serializer, JsonDeserializer<T> deserializer) {
        registerSubtypes(new NamedType(actionClass, typeName));
        addDeserializer(actionClass, deserializer);
        addSerializer(actionClass, serializer);
    }

    private void configureActionsSerialization() {
        setMixInAnnotation(Action.class, ActionJsonModule.ActionMixIn.class);
        registerActionType(SwitchAction.class, SwitchAction.NAME,
                new SwitchActionSerializer(), new SwitchActionDeserializer());
        registerActionType(TerminalsConnectionAction.class, TerminalsConnectionAction.NAME,
                new TerminalsConnectionActionSerializer(), new TerminalsConnectionActionDeserializer());
        registerActionType(MultipleActionsAction.class, MultipleActionsAction.NAME,
                new MultipleActionsActionSerializer(), new MultipleActionsActionDeserializer());
        registerActionType(PhaseTapChangerTapPositionAction.class, PhaseTapChangerTapPositionAction.NAME,
                new PhaseTapChangerTapPositionActionSerializer(), new PhaseTapChangerTapPositionActionDeserializer());
        registerActionType(RatioTapChangerTapPositionAction.class, RatioTapChangerTapPositionAction.NAME,
                new RatioTapChangerTapPositionActionSerializer(), new RatioTapChangerTapPositionActionDeserializer());
        registerActionType(PhaseTapChangerRegulationAction.class, PhaseTapChangerRegulationAction.NAME,
                new PhaseTapChangerRegulationActionSerializer(), new PhaseTapChangerRegulationActionDeserializer());
        registerActionType(RatioTapChangerRegulationAction.class, RatioTapChangerRegulationAction.NAME,
                new RatioTapChangerRegulationActionSerializer(), new RatioTapChangerRegulationActionDeserializer());
        registerActionType(LoadAction.class, LoadAction.NAME, new LoadActionSerializer(), new LoadActionDeserializer());
        registerActionType(DanglingLineAction.class, DanglingLineAction.NAME, new DanglingLineActionSerializer(), new DanglingLineActionDeserializer());
        registerActionType(HvdcAction.class, HvdcAction.NAME, new HvdcActionSerializer(), new HvdcActionDeserializer());
        registerActionType(GeneratorAction.class, GeneratorAction.NAME,
                new GeneratorActionSerializer(), new GeneratorActionDeserializer());
        registerActionType(ShuntCompensatorPositionAction.class, ShuntCompensatorPositionAction.NAME,
                new ShuntCompensatorPositionActionSerializer(), new ShuntCompensatorPositionActionDeserializer());
        registerActionType(StaticVarCompensatorAction.class, StaticVarCompensatorAction.NAME,
                new StaticVarCompensatorActionSerializer(), new StaticVarCompensatorActionDeserializer());
    }
}
