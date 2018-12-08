package nl.ou.testar.StateModel;

import es.upv.staq.testar.CodingManager;
import nl.ou.testar.StateModel.ActionSelection.ActionSelector;
import nl.ou.testar.StateModel.ActionSelection.CompoundFactory;
import nl.ou.testar.StateModel.Persistence.OrientDB.OrientDBManager;
import nl.ou.testar.StateModel.Persistence.PersistenceManager;
import nl.ou.testar.StateModel.Persistence.PersistenceManagerFactory;
import nl.ou.testar.StateModel.Persistence.PersistenceManagerFactoryBuilder;
import org.fruit.alayer.Tag;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StateModelManagerFactory {

    public static StateModelManager getStateModelManager(Settings settings) {
        // check the attributes for the abstract state id
        if (settings.get(ConfigTags.AbstractStateAttributes).isEmpty()) {
            throw new RuntimeException("No Abstract State Attributes were provided in the settings file");
        }

        Set<Tag<?>> abstractTags = new HashSet<>();
        for (String abstractStateAttribute : settings.get(ConfigTags.AbstractStateAttributes)) {
            abstractTags.add(CodingManager.allowedStateTags.get(abstractStateAttribute));
        }

        // get an orientdb persistence manager
        PersistenceManagerFactory persistenceManagerFactory = PersistenceManagerFactoryBuilder.createPersistenceManagerFactory(PersistenceManagerFactoryBuilder.ManagerType.ORIENTDB);
        PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager(settings);

        // create the abstract state model and then the state model manager
        AbstractStateModel abstractStateModel = new AbstractStateModel(CodingManager.getAbstractStateModelHash(), abstractTags, (OrientDBManager)persistenceManager);
        ActionSelector actionSelector = CompoundFactory.getCompoundActionSelector();

        return new StateModelManager(abstractStateModel, actionSelector, persistenceManager);
    }

}