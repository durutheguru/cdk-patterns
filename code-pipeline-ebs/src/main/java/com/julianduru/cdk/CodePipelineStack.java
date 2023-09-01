package com.julianduru.cdk;

import com.julianduru.cdk.stages.test.EbsStack;
import software.amazon.awscdk.RemovalPolicy;
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
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketProps;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public class CodePipelineStack extends Stack {

    private String githubOwner;

    private String githubRepo;

    private String githubBranch;


    public CodePipelineStack(final Construct scope, final String id, final StackProps props, final Map<String, String> variableMap) {
        super(scope, id, props);

        // GitHub source repository information
//        String githubOwner = "durutheguru";
//        String githubRepo = "cdk-patterns";
//        String githubBranch = "main";

        this.githubOwner = variableMap.get("githubOwner");
        this.githubRepo = variableMap.get("githubRepo");
        this.githubBranch = variableMap.get("githubBranch");

        Bucket codePipelineBucket = createCodePipelineBucket();
        Project buildProject = createBuildProject();

        createPipeline(codePipelineBucket, buildProject, variableMap);
    }


    private Bucket createCodePipelineBucket() {
        return new Bucket(this,
            Main.prefixApp("code-pipeline-ebs-resource-bucket").toLowerCase(),
            BucketProps.builder()
                .bucketName(Main.prefixApp("code-pipeline-bucket-18938817843").toLowerCase())
                .removalPolicy(RemovalPolicy.DESTROY)
                .autoDeleteObjects(true)
                .build()
        );
    }

    private Project createBuildProject() {
        return PipelineProject.Builder.create(this, Main.prefixApp("EBS-CodePipelineProject"))
            .environment(BuildEnvironment.builder().buildImage(LinuxBuildImage.STANDARD_7_0).build())
            .buildSpec(BuildSpec.fromSourceFilename("buildspec.yml"))
            .build();
    }


    private Pipeline createPipeline(Bucket codePipelineBucket, Project buildProject, Map<String, String> variableMap) {
        return new Pipeline(this, Main.prefixApp("EBS-CodePipeline"), PipelineProps.builder()
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
                                    .output(
                                        Artifact.artifact(
                                            Main.prefixApp("EBS-SourceArtifact")
                                        )
                                    )
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
                                    .input(
                                        Artifact.artifact(
                                            Main.prefixApp("EBS-SourceArtifact")
                                        )
                                    )
                                    .outputs(
                                        Collections.singletonList(
                                            Artifact.artifact(
                                                Main.prefixApp("EBS-BuildArtifact")
                                            )
                                        )
                                    )
                                    .build()
                            )
                        )
                        .build(),

                    StageProps.builder()
                        .stageName("Test-Deploy")
                        .actions(Collections.singletonList(getEBSDeployAction(variableMap)))
                        .build()
                )
            )
            .artifactBucket(codePipelineBucket)
            .build()
        );
    }


    private ElasticBeanstalkDeployAction getEBSDeployAction(Map<String, String> variableMap) {
        EbsStack ebsStack = new EbsStack(
            this, Main.prefixApp("ebsStackId"), null, variableMap
        );

        return ElasticBeanstalkDeployAction.Builder.create()
            .actionName("ElasticBeanstalkDeployAction")
            .applicationName(ebsStack.getApplication().getApplicationName())
            .environmentName(ebsStack.getEbsEnvironment().getEnvironmentName())
            .input(
                Artifact.artifact(
                    Main.prefixApp("EBS-BuildArtifact")
                )
            )
            .build();
    }


}

