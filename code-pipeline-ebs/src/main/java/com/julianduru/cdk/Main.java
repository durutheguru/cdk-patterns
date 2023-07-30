package com.julianduru.cdk;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

/**
 * created by Julian Dumebi Duru on 19/07/2023
 */
public class Main {


    public static void main(String[] args) {
        App app = new App();

        StackProps stackProps = StackProps.builder()
            .env(
                Environment.builder()
                    .account("058486276453")
                    .region("us-east-1")
                    .build()
            )
            .build();

        new CodePipelineStack(app, "CodePipelineStack", stackProps);

        app.synth();
    }


}


