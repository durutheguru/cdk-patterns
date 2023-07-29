package com.julianduru.cdk.stages.test;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.elasticbeanstalk.CfnApplication;
import software.amazon.awscdk.services.elasticbeanstalk.CfnEnvironment;
import software.amazon.awscdk.services.iam.CfnInstanceProfile;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * created by Julian Dumebi Duru on 29/07/2023
 */
public class EbsStack extends Stack {

    public EbsStack(final Construct scope, final String id) {
        super(scope, id);


        Role serviceRole = Role.Builder.create(this, "EBSServiceRole")
            .assumedBy(new ServicePrincipal("elasticbeanstalk.amazonaws.com"))
            .managedPolicies(
                Arrays.asList(
                    ManagedPolicy.fromAwsManagedPolicyName("AWSElasticBeanstalkFullAccess")
                )
            )
            .build();

        String appName = "TestEBSApplication";

        CfnApplication application = CfnApplication.Builder.create(this, "TestEBSApplication")
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


        Role ec2Role = Role.Builder.create(this, appName + "-aws-elasticbeanstalk-ec2-role`")
            .assumedBy(new ServicePrincipal("ec2.amazonaws.com"))
            .build();
        ec2Role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName("AWSElasticBeanstalkWebTier"));

        CfnInstanceProfile instanceProfile = CfnInstanceProfile.Builder.create(this, appName + "-InstanceProfile")
            .roles(Collections.singletonList(ec2Role.getRoleName()))
            .instanceProfileName(appName + "-InstanceProfile")
            .build();

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

        CfnEnvironment environment = CfnEnvironment.Builder.create(this, "TestEBSEnvironment")
            .applicationName(application.getApplicationName())
            .environmentName("TestEBSEnvironment")
            .solutionStackName("64bit Amazon Linux 2 v3.4.9 running Corretto 17")
            .optionSettings(applicationSettings)
            .build();
    }


}




