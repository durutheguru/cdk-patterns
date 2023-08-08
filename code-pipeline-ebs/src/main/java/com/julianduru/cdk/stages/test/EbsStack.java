package com.julianduru.cdk.stages.test;

import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.elasticbeanstalk.CfnApplication;
import software.amazon.awscdk.services.elasticbeanstalk.CfnEnvironment;
import software.amazon.awscdk.services.iam.*;
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * created by Julian Dumebi Duru on 29/07/2023
 */
public class EbsStack extends Stack {


    private final CfnApplication application;


    private final CfnEnvironment environment;


    private Vpc vpc;


    public EbsStack(final Construct scope, final String id) {
        this(scope, id, null);
    }


    public EbsStack(@Nullable Construct scope, @Nullable String id, @Nullable StackProps props) {
        super(scope, id, props);

        String appName = "test-ebs-application";

        application = createApplication(appName);

        createVpc();

        SecurityGroup securityGroup = createSecurityGroup(vpc);

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
                .build(),

            CfnEnvironment.OptionSettingProperty.builder()
                .namespace("aws:autoscaling:launchconfiguration")
                .optionName("SecurityGroups")
                .value(securityGroup.getSecurityGroupId())
                .build(),

            CfnEnvironment.OptionSettingProperty.builder()
                .namespace("aws:ec2:vpc")
                .optionName("VPCId")
                .value(vpc.getVpcId())
                .build(),

            CfnEnvironment.OptionSettingProperty.builder()
                .namespace("aws:ec2:vpc")
                .optionName("Subnets")
                .value(getSubnets(SubnetType.PUBLIC))
                .build()
        );

        environment = CfnEnvironment.Builder.create(this, "TestEBSEnvironment")
            .applicationName(application.getApplicationName())
            .environmentName("TestEBSEnvironment")
            .solutionStackName("64bit Amazon Linux 2 v3.4.9 running Corretto 17")
            .optionSettings(applicationSettings)
            .build();

        environment.addDependency(application);
    }


    private Vpc createVpc() {
        this.vpc = Vpc.Builder.create(this, "Test_Env_Vpc")
            .vpcName("Test Env VPC")
            .ipAddresses(IpAddresses.cidr("10.0.0.0/16"))
            .maxAzs(2)
            .natGateways(1)
            .subnetConfiguration(
                Arrays.asList(
                    SubnetConfiguration.builder()
                        .name("Public")
                        .subnetType(SubnetType.PUBLIC)
                        .cidrMask(24)
                        .build(),

                    SubnetConfiguration.builder()
                        .name("Private")
                        .subnetType(SubnetType.PRIVATE_ISOLATED)
                        .cidrMask(24)
                        .build()
                )
            )
            .build();

        // Output VPC ID
        CfnOutput.Builder.create(this, "Test_Env_Vpc_Id")
            .value(vpc.getVpcId())
            .build();

        return vpc;
    }


    private String getSubnets(SubnetType subnetType) {
        List<ISubnet> subnets = new ArrayList<>();

        switch (subnetType) {
            case PUBLIC:
                subnets = vpc.getPublicSubnets();
                break;
            case PRIVATE_ISOLATED:
                subnets = vpc.getIsolatedSubnets();
                break;
            case PRIVATE_WITH_EGRESS:
                subnets = vpc.getPrivateSubnets();
        }

        List<String> subnetIds = subnets.stream().map(ISubnet::getSubnetId).collect(Collectors.toList());
        return String.join(",", subnetIds);
    }


    private SecurityGroup createSecurityGroup(Vpc vpc) {
        SecurityGroup sg = SecurityGroup.Builder.create(this, "TestEc2SecurityGroup")
            .vpc(vpc)
            .securityGroupName("EC2SecurityGroup")
            .build();
        sg.addIngressRule(Peer.anyIpv4(), Port.tcp(22), "Allow SSH access");
        sg.addIngressRule(Peer.anyIpv4(), Port.tcp(5000), "Allow Application access");

        return sg;
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



