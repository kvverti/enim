package kvverti.enim.model.multipart;

import com.google.gson.*;

import kvverti.enim.entity.state.RenderState;

/** A structure representing the logical or over conditions. Conditions may be nested. */
public class ConditionOr extends Condition {

    /** The conditions that must not be fulfilled for this not to be fulfilled */
    private final Condition[] conditions;

    private ConditionOr(Condition[] c) { conditions = c; }

    @Override
    public boolean fulfills(RenderState state) {

        for(Condition c : conditions)
            if(c.fulfills(state))
                return true;
        return false;
    }

    /**
     * ConditionOr is in the form of a JsonArray. i.e. [ condition, ... ]
     */
    static ConditionOr deserialize(JsonElement json, JsonDeserializationContext context) {

        assert json.isJsonArray() : json;
        if(json.getAsJsonArray().size() == 0)
            throw new JsonParseException("OR array must be non-empty");
        Condition[] c = context.deserialize(json, Condition[].class);
        return new ConditionOr(c);
    }
}
