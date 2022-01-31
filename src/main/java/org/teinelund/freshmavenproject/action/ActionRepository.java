package org.teinelund.freshmavenproject.action;

import java.util.HashMap;
import java.util.Map;

/**
 * ActionRepository contains all Action objects. Given an action name, the repository gives
 * the corresponding Action object. An action can be a specific pom file dependency,
 * a file, a folder path and more.
 *
 * Which actions to fetch is governed by ApplicationTypes and the ApplicationType a user
 * wants. The ApplicationType is stored in the ApplicationContext and is set in the
 * interactiveMode() method in Application class.
 */
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


        //
        // Plugins
        //

        ListOfAction cla = new ListOfAction();
        FolderPathAction srcMainJavaPath = new FolderPathAction("src/main/java", "srcMainJavaFolderName");
        cla.addAction(srcMainJavaPath);
        FolderPathAction srcTestJavaPath = new FolderPathAction("src/test/java", "srcTestJavaFolderName");
        cla.addAction(srcTestJavaPath);
        FolderPathAction mainPackageFolderPath = new FolderPathAction("${srcMainJavaFolderName}/${packageFolderPathName}",
                "mainPackageFolderPathName");
        cla.addAction(mainPackageFolderPath);
        FolderPathAction testPackageFolderPath = new FolderPathAction("${srcTestJavaFolderName}/${packageFolderPathName}",
                "testPackageFolderPathName");
        cla.addAction(testPackageFolderPath);
        FileAction pomFile = new FileAction("pom.vtl", "pom.xml", "projectFolderPathName");
        cla.addAction(pomFile);
        FileAction applicationFile = new FileAction("Application.vtl", "Application.java", "mainPackageFolderPathName");
        cla.addAction(applicationFile);
        FileAction applicationTestFile = new FileAction("ApplicationTest.vtl", "ApplicationTest.java", "testPackageFolderPathName");
        cla.addAction(applicationTestFile);
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
        PomFilePluginAction shade_jar_plugins = new PomFilePluginAction(content.toString());
        cla.addAction(shade_jar_plugins);
        actionMap.put("CLA", cla);

        ListOfAction plugins_basic = new ListOfAction();

        content = new StringBuilder();
        content.append("            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.8.1</version>\n" +
                "                <configuration>\n" +
                "                    <source>14</source>\n" +
                "                    <target>14</target>\n" +
                "                </configuration>\n" +
                "            </plugin>\n");
        Action maven_compiler_plugin = new PomFilePluginAction(content.toString());
        plugins_basic.addAction(maven_compiler_plugin);

        content = new StringBuilder();
        content.append("            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-surefire-plugin</artifactId>\n" +
                "                <version>2.22.2</version>\n" +
                "            </plugin>\n");
        Action maven_surefire_plugin = new PomFilePluginAction(content.toString());
        plugins_basic.addAction(maven_surefire_plugin);

        content = new StringBuilder();
        content.append("            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-help-plugin</artifactId>\n" +
                "                <version>3.2.0</version>\n" +
                "            </plugin>\n");
        Action maven_help_plugin = new PomFilePluginAction(content.toString());
        plugins_basic.addAction(maven_help_plugin);

        content = new StringBuilder();
        content.append("            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-clean-plugin</artifactId>\n" +
                "                <version>3.1.0</version>\n" +
                "            </plugin>\n");
        Action maven_clean_plugin = new PomFilePluginAction(content.toString());
        plugins_basic.addAction(maven_clean_plugin);

        content = new StringBuilder();
        content.append("            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-install-plugin</artifactId>\n" +
                "                <version>2.5.2</version>\n" +
                "            </plugin>\n");
        Action maven_install_plugin = new PomFilePluginAction(content.toString());
        plugins_basic.addAction(maven_install_plugin);

        actionMap.put("plugins_basic", plugins_basic);


        //
        // Dependencies
        //

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
        Action junit = new PomFileDependencyAction(content.toString());
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
        content.append("        <junit.jupiter.version>5.8.1</junit.jupiter.version>\n");
        junit = new PomFilePropertyAction(content.toString());
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
