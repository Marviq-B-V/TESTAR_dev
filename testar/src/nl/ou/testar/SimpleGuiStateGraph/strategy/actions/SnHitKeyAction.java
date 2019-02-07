package nl.ou.testar.SimpleGuiStateGraph.strategy.actions;

import nl.ou.testar.SimpleGuiStateGraph.strategy.StrategyNode;
import nl.ou.testar.SimpleGuiStateGraph.strategy.actionTypes.StrategyNodeActiontype;
import org.fruit.alayer.Role;
import org.fruit.alayer.State;
import org.fruit.alayer.actions.ActionRoles;

import java.util.ArrayList;

public class SnHitKeyAction extends StrategyNodeActiontype {
    public SnHitKeyAction(ArrayList<StrategyNode> children) {
        super(children);
    }

    public Role getActionType(State state) {
        return ActionRoles.HitKey;
    }
}
