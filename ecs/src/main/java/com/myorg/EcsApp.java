package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;

public class EcsApp {
    public static void main(final String[] args) {
        App app = new App();

        new EcsStack(
            app, "EcsStack",
            StackProps.builder()
                .env(
                    Environment.builder()
                    .account("058486276453")
                    .region("us-east-1")
                    .build()
                )
                .build()
        );

        app.synth();
    }
}

