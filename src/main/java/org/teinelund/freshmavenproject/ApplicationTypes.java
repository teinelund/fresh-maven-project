package org.teinelund.freshmavenproject;

import org.teinelund.freshmavenproject.action.Action;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ApplicationTypes {

    Map<Application.TypeOfApplication, List<ApplicationType>> applicationTypes = new HashMap<>();
    Map<String, List<Action>> actionMap = new HashMap<>();

    public ApplicationTypes() {
        List<ApplicationType> list = new LinkedList<>();
        Collection<String> actions = new LinkedList<>();
        actions.add("J2EE");
        ApplicationType at = new ApplicationType("J2EE_SLIM", "Slim J2EE (JSP/Servlet)", actions);
        list.add(at);
        actions = new LinkedList<>();
        actions.add("J2EE");
        actions.add("Struts1");
        at = new ApplicationType("J2EE_STRUTS_1", "Struts 1", actions);
        list.add(at);
        applicationTypes.put(Application.TypeOfApplication.J2EE, list);
        list = new LinkedList<>();
        actions = new LinkedList<>();
        actions.add("CLA");
        at = new ApplicationType("CLA_SLIM", "Slim (Maven Shade Plugin)", actions);
        list.add(at);
        actions = new LinkedList<>();
        actions.add("CLA");
        actions.add("jcommander");
        actions.add("jansi");
        actions.add("commons lang3");
        actions.add("commons io");
        actions.add("unit test 5");
        at = new ApplicationType("CLA_BASIC", "Basic (Maven Shade Plugin, JCommander, Jansi, Commons Lang3, Commons IO, Junit Jupiter, AssertJ and Mockito)", actions);
        list.add(at);
        applicationTypes.put(Application.TypeOfApplication.COMMAND_LINE_APPLICATION, list);
        actions = new LinkedList<>();
        actions.add("LIB");
        at = new ApplicationType("LIB_SLIM", "Slim (no dependencies)", actions);
        list.add(at);
        applicationTypes.put(Application.TypeOfApplication.LIBRARY, list);
    }

    public List<ApplicationType> getQueries(Application.TypeOfApplication applicationType) {
        return this.applicationTypes.get(applicationType);
    }
}
