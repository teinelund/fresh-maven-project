package org.teinelund.freshmavenproject;

import org.teinelund.freshmavenproject.action.Action;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * ApplicationTypes stores all sorts of ApplicationType. Its purpose is to store all kind of
 * applications that are possible to create: J2EE_SLIM, J2EE_STRTUS_1, CLA_SLIM, CLA and LIB.
 *
 * Each ApplicationType also stores a list of action names.
 *
 * This class is used in the interactive mode. It is used to list all kinds of application
 * types. When a user selects one, the ApplicationType is stored in the ApplicationContext.
 *
 * The ApplicationType is later used to iterate the action names to fetch Action objects
 * from the ActionRepository.
 */
public class ApplicationTypes {

    Map<Application.TypeOfApplication, List<ApplicationType>> applicationTypes = new HashMap<>();
    Map<String, List<Action>> actionMap = new HashMap<>();

    public ApplicationTypes() {
        List<ApplicationType> list = new LinkedList<>();
        Collection<String> actionNames = new LinkedList<>();
        actionNames.add("J2EE");
        actionNames.add("plugins_basic");
        ApplicationType at = new ApplicationType("J2EE_SLIM", "Slim J2EE (JSP/Servlet)", actionNames);
        list.add(at);
        actionNames = new LinkedList<>();
        actionNames.add("J2EE");
        actionNames.add("Struts1");
        actionNames.add("plugins_basic");
        at = new ApplicationType("J2EE_STRUTS_1", "Struts 1", actionNames);
        list.add(at);
        applicationTypes.put(Application.TypeOfApplication.J2EE, list);
        list = new LinkedList<>();
        actionNames = new LinkedList<>();
        actionNames.add("CLA");
        actionNames.add("plugins_basic");
        at = new ApplicationType("CLA_SLIM", "Slim (Maven Shade Plugin)", actionNames);
        list.add(at);
        actionNames = new LinkedList<>();
        actionNames.add("CLA");
        actionNames.add("jcommander");
        actionNames.add("jansi");
        actionNames.add("commons lang3");
        actionNames.add("commons io");
        actionNames.add("unit test 5");
        actionNames.add("plugins_basic");
        at = new ApplicationType("CLA_BASIC", "Basic (Maven Shade Plugin, JCommander, Jansi, Commons Lang3, Commons IO, Junit Jupiter, AssertJ and Mockito)", actionNames);
        list.add(at);
        applicationTypes.put(Application.TypeOfApplication.COMMAND_LINE_APPLICATION, list);
        actionNames = new LinkedList<>();
        actionNames.add("LIB");
        actionNames.add("plugins_basic");
        at = new ApplicationType("LIB_SLIM", "Slim (no dependencies)", actionNames);
        list.add(at);
        applicationTypes.put(Application.TypeOfApplication.LIBRARY, list);
    }

    public List<ApplicationType> getQueries(Application.TypeOfApplication applicationType) {
        return this.applicationTypes.get(applicationType);
    }
}
