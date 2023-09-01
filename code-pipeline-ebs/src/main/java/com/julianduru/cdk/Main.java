package com.julianduru.cdk;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * created by Julian Dumebi Duru on 19/07/2023
 */
public class Main {


    private static String appName;


    public static void main(String[] args) {
        App app = new App();

        Map<String, String> variableMap = getVariableMap(
            app,
            Arrays.asList(
                "appName",
                "account",
                "region",
                "githubOwner",
                "githubRepo",
                "githubBranch",
                "db_username"
            )
        );

        setAppName(variableMap.get("appName"));

        new CodePipelineStack(
            app,
            prefixApp("CodePipelineStack"),
            StackProps.builder()
                .env(
                    Environment.builder()
                        .account(variableMap.get("account"))
                        .region(variableMap.get("region"))
                        .build()
                )
                .build(),
            variableMap
        );

        app.synth();
    }


    private static void setAppName(String appName) {
        Main.appName = appName;
    }


    public static String getAppName() {
        return appName;
    }


    public static String prefixApp(String name) {
        return String.format("%s-%s", appName, name);
    }


    public static Map<String, String> getVariableMap(App app, List<String> variableNames) {
        Map<String, String> variableMap = new HashMap<>();

        for (String variableName : variableNames) {
            variableMap.put(variableName, app.getNode().tryGetContext(variableName).toString());
        }

        return variableMap;
    }


}
