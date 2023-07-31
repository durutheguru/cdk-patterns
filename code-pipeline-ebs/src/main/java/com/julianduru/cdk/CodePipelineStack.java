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
import software.amazon.awscdk.services.codestar.CfnGitHubRepository;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.Collections;

public class CodePipelineStack extends Stack {


    public CodePipelineStack(final Construct scope, final String id) {
        this(scope, id, null);
    }


    public CodePipelineStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // GitHub source repository information
        String githubOwner = "durutheguru";
        String githubRepo = "cdk-patterns";
        String githubBranch = "main";


        Project buildProject = PipelineProject.Builder.create(this, "EBS-CodePipelineProject")
            .environment(BuildEnvironment.builder().buildImage(LinuxBuildImage.STANDARD_7_0).build())
            .buildSpec(BuildSpec.fromSourceFilename("buildspec.yml"))
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
                                    .outputs(Collections.singletonList(Artifact.artifact("EBS-BuildArtifact")))
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
            .build()
        );

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



