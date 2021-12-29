package org.teinelund.freshmavenproject.action;

import java.util.HashMap;
import java.util.Map;

public class ActionRepository {

    private Map<String, Action> actionMap = new HashMap<>();

    public ActionRepository() {
        // J2EE
        // Struts1

        // CLA
        // jcommander
        // jansi
        // commons lang3
        // commons io
        // unit test 5

        // LIB
        StringBuilder content = new StringBuilder();
        content.append("            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-jar-plugin</artifactId>\n" +
                "                <version>3.2.0</version>\n" +
                "                <configuration>\n" +
                "                    <finalName>${project.name}</finalName>\n" +
                "                    <archive>\n" +
                "                        <manifest>\n" +
                "                            <mainClass>${packageName}.Application</mainClass>\n" +
                "                            <addDefaultImplementationEntries>\n" +
                "                                true\n" +
                "                            </addDefaultImplementationEntries>\n" +
                "                        </manifest>\n" +
                "                    </archive>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-shade-plugin</artifactId>\n" +
                "                <version>3.2.4</version>\n" +
                "                <executions>\n" +
                "                    <execution>\n" +
                "                        <phase>package</phase>\n" +
                "                        <goals>\n" +
                "                            <goal>shade</goal>\n" +
                "                        </goals>\n" +
                "                        <configuration>\n" +
                "                            <transformers>\n" +
                "                                <transformer\n" +
                "                                        implementation=\"org.apache.maven.plugins.shade.resource.ManifestResourceTransformer\">\n" +
                "                                    <!-- Main class -->\n" +
                "                                    <mainClass>${packageName}.Application</mainClass>\n" +
                "                                </transformer>\n" +
                "                            </transformers>\n" +
                "                        </configuration>\n" +
                "                    </execution>\n" +
                "                </executions>\n" +
                "            </plugin>\n");
        PomFilePluginAction cla = new PomFilePluginAction(content.toString());
        actionMap.put("CLA", cla);

        content = new StringBuilder();
        content.append("        <dependency>\n" +
                "            <groupId>com.beust</groupId>\n" +
                "            <artifactId>jcommander</artifactId>\n" +
                "            <version>1.81</version>\n" +
                "        </dependency>\n");
        PomFileDependencyAction jcommander = new PomFileDependencyAction(content.toString());
        actionMap.put("jcommander", jcommander);

        content = new StringBuilder();
        content.append("        <dependency>\n" +
                "            <groupId>org.fusesource.jansi</groupId>\n" +
                "            <artifactId>jansi</artifactId>\n" +
                "            <version>2.4.0</version>\n" +
                "        </dependency>\n");
        PomFileDependencyAction jansi = new PomFileDependencyAction(content.toString());
        actionMap.put("jansi", jansi);

        content = new StringBuilder();
        content.append("        <dependency>\n" +
                "            <groupId>org.apache.commons</groupId>\n" +
                "            <artifactId>commons-lang3</artifactId>\n" +
                "            <version>3.11</version>\n" +
                "        </dependency>\n");
        PomFileDependencyAction commons_lang3 = new PomFileDependencyAction(content.toString());
        actionMap.put("commons lang3", commons_lang3);

        content = new StringBuilder();
        content.append("        <dependency>\n" +
                "            <groupId>commons-io</groupId>\n" +
                "            <artifactId>commons-io</artifactId>\n" +
                "            <version>2.11.0</version>\n" +
                "        </dependency>\n");
        PomFileDependencyAction commons_io = new PomFileDependencyAction(content.toString());
        actionMap.put("commons io", commons_io);

        ListOfAction unit_test_5 = new ListOfAction();
        content = new StringBuilder();
        content.append("        <dependency>\n" +
                "            <groupId>org.junit.jupiter</groupId>\n" +
                "            <artifactId>junit-jupiter-api</artifactId>\n" +
                "            <version>${junit.jupiter.version}</version>\n" +
                "            <scope>test</scope>\n" +
                "        </dependency>\n");
        PomFileDependencyAction junit = new PomFileDependencyAction(content.toString());
        unit_test_5.addAction(junit);

        content = new StringBuilder();
        content.append("        <dependency>\n" +
                "            <groupId>org.junit.jupiter</groupId>\n" +
                "            <artifactId>junit-jupiter-engine</artifactId>\n" +
                "            <version>${junit.jupiter.version}</version>\n" +
                "            <scope>test</scope>\n" +
                "        </dependency>\n");
        junit = new PomFileDependencyAction(content.toString());
        unit_test_5.addAction(junit);

        content = new StringBuilder();
        content.append("        <dependency>\n" +
                "            <groupId>org.assertj</groupId>\n" +
                "            <artifactId>assertj-core</artifactId>\n" +
                "            <!-- use 2.9.1 for Java 7 projects -->\n" +
                "            <version>3.21.0</version>\n" +
                "            <scope>test</scope>\n" +
                "        </dependency>\n");
        junit = new PomFileDependencyAction(content.toString());
        unit_test_5.addAction(junit);

        content = new StringBuilder();
        content.append("        <dependency>\n" +
                "            <groupId>org.mockito</groupId>\n" +
                "            <artifactId>mockito-core</artifactId>\n" +
                "            <version>4.0.0</version>\n" +
                "            <scope>test</scope>\n" +
                "        </dependency>\n");
        junit = new PomFileDependencyAction(content.toString());
        unit_test_5.addAction(junit);

        actionMap.put("unit test 5", unit_test_5);
    }

    public Action getAction(String actionName) {
        return this.actionMap.get(actionName);
    }
}
