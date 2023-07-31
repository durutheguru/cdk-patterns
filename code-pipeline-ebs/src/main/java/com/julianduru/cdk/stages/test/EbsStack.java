package com.julianduru.cdk.stages.test;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.elasticbeanstalk.CfnApplication;
import software.amazon.awscdk.services.elasticbeanstalk.CfnApplicationVersion;
import software.amazon.awscdk.services.elasticbeanstalk.CfnEnvironment;
import software.amazon.awscdk.services.iam.CfnInstanceProfile;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketProps;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * created by Julian Dumebi Duru on 29/07/2023
 */
public class EbsStack extends Stack {

    private final CfnApplication application;


    private final CfnEnvironment environment;


    public EbsStack(final Construct scope, final String id) {
        this(scope, id, null);
    }


    public EbsStack(@Nullable Construct scope, @Nullable String id, @Nullable StackProps props) {
        super(scope, id, props);

        String appName = "test-ebs-application";

        application = createApplication(appName);


        CfnInstanceProfile instanceProfile = createEc2InstanceProfile(appName);

        List<CfnEnvironment.OptionSettingProperty> applicationSettings = Arrays.asList(
            CfnEnvironment.OptionSettingProperty.builder()
                .namespace("aws:autoscaling:launchconfiguration")
                .optionName("InstanceType")
                .value("t2.micro")
                .build(),

            CfnEnvironment.OptionSettingProperty.builder()
                .namespace("aws:autoscaling:launchconfiguration")
                .optionName("IamInstanceProfile")
                .value(instanceProfile.getInstanceProfileName())
                .build()
        );


//        CfnApplicationVersion applicationVersion = CfnApplicationVersion.Builder.create(this, "TestEBSApplicationVersion")
//            .applicationName(application.getApplicationName())
//            .sourceBundle(
//                CfnApplicationVersion.SourceBundleProperty.builder()
//                    .s3Bucket("julianduru-cdk")
//                    .s3Key("test-eb-app.zip")
//                    .build()
//            )
//            .build();


        environment = CfnEnvironment.Builder.create(this, "TestEBSEnvironment")
            .applicationName(application.getApplicationName())
            .environmentName("TestEBSEnvironment")
            .solutionStackName("64bit Amazon Linux 2 v3.4.9 running Corretto 17")
            .optionSettings(applicationSettings)
//            .ver
            .build();

        environment.addDependency(application);
    }


    private CfnApplication createApplication(String appName) {
        Role serviceRole = Role.Builder.create(this, "EBSServiceRole")
            .assumedBy(new ServicePrincipal("elasticbeanstalk.amazonaws.com"))
            .managedPolicies(
                Collections.singletonList(
                    ManagedPolicy.fromAwsManagedPolicyName("AdministratorAccess-AWSElasticBeanstalk")
                )
            )
            .build();

        return CfnApplication.Builder.create(this, "TestEBSApplication")
            .applicationName(appName)
            .resourceLifecycleConfig(
                CfnApplication.ApplicationResourceLifecycleConfigProperty.builder()
                    .serviceRole(serviceRole.getRoleArn())
                    .versionLifecycleConfig(
                        CfnApplication.ApplicationVersionLifecycleConfigProperty.builder()
                            .maxCountRule(
                                CfnApplication.MaxCountRuleProperty.builder()
                                    .deleteSourceFromS3(true)
                                    .maxCount(3)
                                    .enabled(true)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build();
    }


    private CfnInstanceProfile createEc2InstanceProfile(String appName) {
        Role ec2Role = Role.Builder.create(this, appName + "-aws-elasticbeanstalk-ec2-role`")
            .assumedBy(new ServicePrincipal("ec2.amazonaws.com"))
            .build();
        ec2Role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName("AWSElasticBeanstalkWebTier"));
        ec2Role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName("EC2InstanceConnect"));

        return CfnInstanceProfile.Builder.create(this, appName + "-InstanceProfile")
            .roles(Collections.singletonList(ec2Role.getRoleName()))
            .instanceProfileName(appName + "-InstanceProfile")
            .build();
    }


    public CfnApplication getApplication() {
        return application;
    }


    public CfnEnvironment getEbsEnvironment() {
        return environment;
    }


}




