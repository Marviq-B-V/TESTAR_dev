package nl.ou.testar.SimpleGuiStateGraph.strategy.actions;

import nl.ou.testar.SimpleGuiStateGraph.strategy.StrategyNode;
import nl.ou.testar.SimpleGuiStateGraph.strategy.actionTypes.StrategyNodeNumber;
import org.fruit.alayer.State;
import org.fruit.alayer.actions.ActionRoles;

import java.util.ArrayList;

public class SnNumberOfUnexecutedDragActions extends StrategyNodeNumber {

    public SnNumberOfUnexecutedDragActions(ArrayList<StrategyNode> children) {
        super(children);
    }

    @Override
    public int getValue(State state) {
        return state.getNumberOfActions(ActionRoles.Drag, "UNEX");
    }

}
