package com.julianduru.cdk;

import com.julianduru.cdk.stages.test.EbsStack;
import software.amazon.awscdk.SecretValue;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.codebuild.*;
import software.amazon.awscdk.services.codepipeline.Artifact;
import software.amazon.awscdk.services.codepipeline.Pipeline;
import software.amazon.awscdk.services.codepipeline.PipelineProps;
import software.amazon.awscdk.services.codepipeline.StageProps;
import software.amazon.awscdk.services.codepipeline.actions.CodeBuildAction;
import software.amazon.awscdk.services.codepipeline.actions.ElasticBeanstalkDeployAction;
import software.amazon.awscdk.services.codepipeline.actions.GitHubSourceAction;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.Collections;

public class CodePipelineStack extends Stack {


    public CodePipelineStack(final Construct scope, final String id) {
        this(scope, id, null);
    }


    public CodePipelineStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

//        CodePipeline pipeline = CodePipeline.Builder.create(this, "pipeline")
//            .pipelineName("Pipeline")
//            .synth(
//                ShellStep.Builder.create("Synth")
//                    .input(CodePipelineSource.gitHub("durutheguru/cdk-patterns", "main"))
//                    .commands(
//                        Arrays.asList(
//                            "npm install -g aws-cdk",
//                            "cd code-pipeline-ebs",
//                            "cdk deploy --all --verbose --require-approval never"
//                        )
//                    )
//                    .primaryOutputDirectory("code-pipeline-ebs/cdk.out")
//                    .build()
//            )
//            .build();


//        pipeline.addStage(
//            new TestStage(
//                this, "testStageId",
//                StageProps.builder()
//                    .env(
//                        Environment.builder()
//                            .account("058486276453")
//                            .region("us-east-1")
//                            .build()
//                    )
//                    .build()
//            )
//        );


        // GitHub source repository information
        String githubOwner = "durutheguru";
        String githubRepo = "cdk-patterns";
        String githubBranch = "main";


        // Define the CodeBuild project
//        Project buildProject = new Project(this, "MyProject", ProjectProps.builder()
//            .source(
//                Source.gitHub(
//                    GitHubSourceProps.builder()
//                        .owner(githubOwner)
//                        .repo(githubRepo)
//                        .branchOrRef(githubBranch)
//                        .reportBuildStatus(true)
//                        .webhook(true)
//                        .build()
//                )
//            )
//            .environment(
//                BuildEnvironment.builder().buildImage(LinuxBuildImage.STANDARD_5_0).build()
//            )
//            .build());

        Project buildProject = Project.Builder.create(this, "EBS-Project")
            .source(
                Source.gitHub(
                    GitHubSourceProps.builder()
                        .owner(githubOwner)
                        .repo(githubRepo)
                        .branchOrRef(githubBranch)
                        .reportBuildStatus(true)
                        .webhook(true)
                        .build()
                )
            )
            .buildSpec(
                BuildSpec.fromSourceFilename("buildspec.yml")
            )
            .environment(
                BuildEnvironment.builder().buildImage(LinuxBuildImage.STANDARD_5_0).build()
            )
            .build();



        Pipeline pipeline = new Pipeline(this, "EBS-CodePipeline", PipelineProps.builder()
            .stages(
                Arrays.asList(
                    StageProps.builder()
                        .stageName("Source")
                        .actions(
                            Collections.singletonList(
                                GitHubSourceAction.Builder.create()
                                    .actionName("GitHubSourceAction")
                                    .owner(githubOwner)
                                    .repo(githubRepo)
                                    .branch(githubBranch)
                                    .oauthToken(SecretValue.secretsManager("github-token"))
                                    .output(Artifact.artifact("EBS-SourceArtifact"))
                                    .build()
                            )
                        )
                        .build(),

                    StageProps.builder()
                        .stageName("Build")
                        .actions(
                            Collections.singletonList(
                                CodeBuildAction.Builder.create()
                                    .actionName("CodeBuild")
                                    .project(buildProject)
                                    .input(Artifact.artifact("EBS-SourceArtifact"))
                                    .outputs(
                                        Collections.singletonList(
                                            Artifact.artifact("EBS-BuildArtifact")
                                        )
                                    )
                                    .build()
                            )
                        )
                        .build(),

                    StageProps.builder()
                        .stageName("Deploy")
                        .actions(Collections.singletonList(getEBSDeployAction()))
                        .build()
                )
            )
            .build());

    }


    private ElasticBeanstalkDeployAction getEBSDeployAction() {
        EbsStack ebsStack = new EbsStack(this, "ebsStackId");

        return ElasticBeanstalkDeployAction.Builder.create()
            .actionName("ElasticBeanstalkDeployAction")
            .applicationName(ebsStack.getApplication().getApplicationName())
            .environmentName(ebsStack.getEbsEnvironment().getEnvironmentName())
            .input(Artifact.artifact("EBS-BuildArtifact"))
            .build();
    }


}



