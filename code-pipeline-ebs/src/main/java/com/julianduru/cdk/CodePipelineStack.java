package com.julianduru.cdk;

import com.julianduru.cdk.stages.TestStage;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.StageProps;
import software.amazon.awscdk.pipelines.CodePipeline;
import software.amazon.awscdk.pipelines.CodePipelineSource;
import software.amazon.awscdk.pipelines.ShellStep;
import software.amazon.awscdk.services.iam.*;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CodePipelineStack extends Stack {


    public CodePipelineStack(final Construct scope, final String id) {
        this(scope, id, null);
    }


    public CodePipelineStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        CodePipeline pipeline = CodePipeline.Builder.create(this, "pipeline")
            .pipelineName("Pipeline")
            .synth(
                ShellStep.Builder.create("Synth")
                    .input(CodePipelineSource.gitHub("durutheguru/cdk-patterns", "main"))
                    .commands(
                        Arrays.asList(
                            "npm install -g aws-cdk",
                            "cd code-pipeline-ebs",
                            "cdk deploy --all --verbose --require-approval never"
                        )
                    )
                    .primaryOutputDirectory("code-pipeline-ebs/cdk.out")
                    .build()
            )
            .build();

        pipeline.addStage(
            new TestStage(
                this, "testStageId",
                StageProps.builder()
                    .env(
                        Environment.builder()
                            .account("058486276453")
                            .region("us-east-1")
                            .build()
                    )
                    .build()
            )
        );
    }


}



