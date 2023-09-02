package com.julianduru.cdk.stages.test;

import com.julianduru.cdk.Main;
import com.julianduru.cdk.util.JSONUtil;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.elasticbeanstalk.CfnApplication;
import software.amazon.awscdk.services.elasticbeanstalk.CfnEnvironment;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.amazon.awscdk.services.secretsmanager.SecretStringGenerator;
import software.constructs.Construct;

import java.util.*;
import java.util.stream.Collectors;

/**
 * created by Julian Dumebi Duru on 29/07/2023
 */
public class EbsStack extends Stack {


    private CfnApplication application;

    private CfnEnvironment environment;

    private Vpc vpc;

    private SecurityGroup vpcEndpointsSecurityGroup;

    private SecurityGroup elbSecurityGroup;

    private SecurityGroup ec2SecurityGroup;

    private CfnInstanceProfile instanceProfile;

    private DatabaseInstance database;

    private Secret databaseSecret;

    private List<CfnEnvironment.OptionSettingProperty> applicationSettings;



    public EbsStack(
        @Nullable Construct scope,
        @Nullable String id,
        @Nullable StackProps props,
        final Map<String, String> variableMap
    ) {
        super(scope, id, props);

        initVpc();

        createELBSecurityGroup();

        createEC2Profile();

        createDatabase(variableMap);

        createApplication(Main.getAppName());

        createApplicationSettings();

        createBeanstalkEnvironment();
    }


    private void initVpc() {
        createVpc();
        createVPCEndpointsSecurityGroup();
        createVPCEndpoints();
    }


    private void createVpc() {
        this.vpc = Vpc.Builder.create(this, Main.prefixApp("Test_Env_Vpc"))
            .vpcName(Main.prefixApp("Test Env VPC"))
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
                        .name("PrivateIsolated")
                        .subnetType(SubnetType.PRIVATE_ISOLATED)
                        .cidrMask(24)
                        .build(),

                    SubnetConfiguration.builder()
                        .name("PrivateWithEgress")
                        .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                        .cidrMask(24)
                        .build()
                )
            )
            .build();

        // Output VPC ID
        CfnOutput.Builder.create(this, Main.prefixApp("Test_Env_Vpc_Id"))
            .value(this.vpc.getVpcId())
            .build();
    }


    private void createVPCEndpointsSecurityGroup() {
        this.vpcEndpointsSecurityGroup = SecurityGroup.Builder.create(this, "TestVPCEndpointsSecurityGroup")
            .vpc(vpc)
            .securityGroupName("VPCEndpointsSecurityGroup")
            .build();

        this.vpcEndpointsSecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(443), "Allow HTTPS Inbound traffic to VPC Endpoints");
    }


    private void createVPCEndpoints() {
        createVPCEndpoint(
            "VpcEndpointForSSM", vpc, vpcEndpointsSecurityGroup, InterfaceVpcEndpointAwsService.SSM
        );
        createVPCEndpoint(
            "VpcEndpointForSSMMessages", vpc, vpcEndpointsSecurityGroup, InterfaceVpcEndpointAwsService.SSM_MESSAGES
        );
        createVPCEndpoint(
            "VpcEndpointForEC2", vpc, vpcEndpointsSecurityGroup, InterfaceVpcEndpointAwsService.EC2
        );
        createVPCEndpoint(
            "VpcEndpointForEC2Messages", vpc, vpcEndpointsSecurityGroup, InterfaceVpcEndpointAwsService.EC2_MESSAGES
        );
    }


    private InterfaceVpcEndpoint createVPCEndpoint(
        String id,
        Vpc vpc,
        SecurityGroup vpcEndpointSecurityGroup,
        InterfaceVpcEndpointAwsService service
    ) {
        return InterfaceVpcEndpoint.Builder.create(this, id)
            .vpc(vpc)
            .service(service)
            .securityGroups(Collections.singletonList(vpcEndpointSecurityGroup))
            .subnets(
                SubnetSelection.builder()
                    .subnetType(SubnetType.PRIVATE_ISOLATED)
                    .build()
            )
            .privateDnsEnabled(true)
            .build();
    }


    private void createELBSecurityGroup() {
        this.elbSecurityGroup = SecurityGroup.Builder.create(this, Main.prefixApp("TestELBSecurityGroup"))
            .vpc(vpc)
            .securityGroupName(Main.prefixApp("ELBSecurityGroup"))
            .build();

        this.elbSecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(5000), "Allow Application access");
    }


    private void createEC2Profile() {
        createEC2SecurityGroup();
        createEC2InstanceProfile();
    }


    private void createEC2SecurityGroup() {
        this.ec2SecurityGroup = SecurityGroup.Builder.create(this, Main.prefixApp("TestEc2SecurityGroup"))
            .vpc(vpc)
            .securityGroupName(Main.prefixApp("EC2SecurityGroup"))
            .build();

        this.ec2SecurityGroup.addIngressRule(elbSecurityGroup, Port.tcp(5000), "Allow Application access");
        this.ec2SecurityGroup.addIngressRule(vpcEndpointsSecurityGroup, Port.tcp(433), "Allow VPC Endpoints access");
    }


    private void createEC2InstanceProfile() {
        this.instanceProfile = CfnInstanceProfile.Builder.create(this, Main.prefixApp("InstanceProfile"))
            .roles(Collections.singletonList("AWSSSMEC2DefaultRole"))
            .instanceProfileName(Main.prefixApp("InstanceProfile"))
            .build();
    }


    private void createApplicationSettings() {
        this.applicationSettings = Arrays.asList(
            CfnEnvironment.OptionSettingProperty.builder()
                .namespace("aws:autoscaling:updatepolicy:rollingupdate")
                .optionName("RollingUpdateEnabled")
                .value("true")
                .build(),

            CfnEnvironment.OptionSettingProperty.builder()
                .namespace("aws:autoscaling:updatepolicy:rollingupdate")
                .optionName("RollingUpdateType")
                .value("Health")
                .build(),

            CfnEnvironment.OptionSettingProperty.builder()
                .namespace("aws:autoscaling:asg")
                .optionName("MinSize")
                .value("2")
                .build(),

            CfnEnvironment.OptionSettingProperty.builder()
                .namespace("aws:autoscaling:asg")
                .optionName("MaxSize")
                .value("6")
                .build(),

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
                .value(ec2SecurityGroup.getSecurityGroupId())
                .build(),

            CfnEnvironment.OptionSettingProperty.builder()
                .namespace("aws:ec2:vpc")
                .optionName("VPCId")
                .value(vpc.getVpcId())
                .build(),

            CfnEnvironment.OptionSettingProperty.builder()
                .namespace("aws:ec2:vpc")
                .optionName("Subnets")
                .value(getSubnets(SubnetType.PRIVATE_WITH_EGRESS))
                .build(),

            CfnEnvironment.OptionSettingProperty.builder()
                .namespace("aws:ec2:vpc")
                .optionName("ELBSubnets")
                .value(getSubnets(SubnetType.PUBLIC))
                .build(),

            CfnEnvironment.OptionSettingProperty.builder()
                .namespace("aws:elb:loadbalancer")
                .optionName("CrossZone")
                .value("true")
                .build(),

            CfnEnvironment.OptionSettingProperty.builder()
                .namespace("aws:elbv2:loadbalancer")
                .optionName("SecurityGroups")
                .value(elbSecurityGroup.getSecurityGroupId())
                .build(),

            CfnEnvironment.OptionSettingProperty.builder()
                .namespace("aws:elasticbeanstalk:application:environment")
                .optionName("SPRING_DATASOURCE_URL")
                .value(
                    String.format(
                        "jdbc:mysql://%s:%s/push_notification_db?createDatabaseIfNotExist=true",
                        database.getDbInstanceEndpointAddress(),
                        database.getDbInstanceEndpointPort()
                    )
                )
                .build(),

            CfnEnvironment.OptionSettingProperty.builder()
                .namespace("aws:elasticbeanstalk:application:environment")
                .optionName("SPRING_DATASOURCE_USERNAME")
                .value("duru")
                .build(),

            CfnEnvironment.OptionSettingProperty.builder()
                .namespace("aws:elasticbeanstalk:application:environment")
                .optionName("SPRING_DATASOURCE_PASSWORD")
                .value(databaseSecret.secretValueFromJson("password").unsafeUnwrap())
                .build()
        );
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


    private void createApplication(String appName) {
        Role serviceRole = Role.Builder.create(this, Main.prefixApp("EBSServiceRole"))
            .assumedBy(new ServicePrincipal("elasticbeanstalk.amazonaws.com"))
            .managedPolicies(
                Collections.singletonList(
                    ManagedPolicy.fromAwsManagedPolicyName("AdministratorAccess-AWSElasticBeanstalk")
                )
            )
            .build();

        this.application = CfnApplication.Builder.create(this, Main.prefixApp("TestEBSApplication"))
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


    private void createBeanstalkEnvironment() {
        this.environment = CfnEnvironment.Builder
            .create(this, Main.prefixApp("TestEBSEnvironment").toLowerCase())
            .applicationName(application.getApplicationName())
            .environmentName(Main.prefixApp("TestEBSEnvironment").toLowerCase())
            .solutionStackName("64bit Amazon Linux 2 v3.4.9 running Corretto 17")
            .optionSettings(applicationSettings)
            .build();

        environment.addDependency(application);
    }


    private void createDatabase(Map<String, String> variableMap) {
        Map<String, String> secretsMap = new HashMap<>();
        secretsMap.put("username", variableMap.get("db_username"));

        // Create RDS Database Secret
        // Templated secret with username and password fields
        this.databaseSecret = Secret.Builder.create(this, Main.prefixApp("TemplatedSecret"))
            .generateSecretString(
                SecretStringGenerator.builder()
                    .secretStringTemplate(JSONUtil.asJsonString(secretsMap, ""))
                    .generateStringKey("password")
                    .excludeCharacters("/@\"")
                    .build()
            )
            .build();


        // Create Security Group
        SecurityGroup rdsSecurityGroup = SecurityGroup.Builder.create(this, Main.prefixApp("TestRDSSecurityGroup"))
            .vpc(vpc)
            .build();
        rdsSecurityGroup.addIngressRule(ec2SecurityGroup, Port.tcp(3306), "Allow EC2 access");
//        rdsSecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(3306), "Allow Public access");


        // Create RDS Database Instance
        this.database = DatabaseInstance.Builder.create(this, Main.prefixApp("Database"))
            .engine(
                DatabaseInstanceEngine.mysql(
                    MySqlInstanceEngineProps.builder()
                        .version(MysqlEngineVersion.VER_8_0)
                        .build()
                )
            )
            .instanceIdentifier(Main.prefixApp("rds-database"))
            .credentials(
                Credentials.fromSecret(databaseSecret, secretsMap.get("username"))
            )
            .vpc(vpc)
            .vpcSubnets(
                SubnetSelection.builder()
                    .subnetType(SubnetType.PRIVATE_ISOLATED)
                    .build()
            )
            .securityGroups(Collections.singletonList(rdsSecurityGroup))
            .build();


        // Output RDS Endpoint
        CfnOutput.Builder.create(this, Main.prefixApp("DatabaseEndpoint"))
            .value(this.database.getDbInstanceEndpointAddress())
            .build();
    }


    public CfnApplication getApplication() {
        return application;
    }


    public CfnEnvironment getEbsEnvironment() {
        return environment;
    }


}



