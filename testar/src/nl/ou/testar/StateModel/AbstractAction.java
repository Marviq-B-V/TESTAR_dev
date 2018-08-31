package nl.ou.testar.StateModel;

import java.util.HashSet;
import java.util.Set;

public class AbstractAction {

    // identifier for this action
    private String actionId;

    // collection of concrete actions that are abstracted by this action
    private Set<String> concreteActionIds;

    /**
     * Constructor
     * @param actionId
     */
    public AbstractAction(String actionId) {
        this.actionId = actionId;
        concreteActionIds = new HashSet<>();
    }

    /**
     * This method returns the action id
     * @return id for this action
     */
    public String getActionId() {
        return actionId;
    }

    /**
     * This method adds a concrete action id to this abstract action
     * @param concreteActionId the concrete action id to add
     */
    public void addConcreteActionId(String concreteActionId) {
        concreteActionIds.add(concreteActionId);
    }

    /**
     * This method returns the set of concrete action ids that are abstracted by this action
     * @return concrete action ids
     */
    public Set<String> getConcreteActionIds() {
        return concreteActionIds;
    }
}