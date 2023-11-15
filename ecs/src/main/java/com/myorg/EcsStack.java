package com.myorg;

import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.CpuArchitecture;
import software.amazon.awscdk.services.ecs.OperatingSystemFamily;
import software.amazon.awscdk.services.ecs.RuntimePlatform;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import java.util.Collections;

public class EcsStack extends Stack {
    public EcsStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

//    public EcsStack(final Construct scope, final String id, final StackProps props) {
//        super(scope, id, props);
//
//        // Fetch the default VPC
//        IVpc defaultVpc = Vpc.fromLookup(this, "DefaultVPC", VpcLookupOptions.builder()
//            .isDefault(true)
//            .build());
//
//        SecurityGroup securityGroup = SecurityGroup.Builder.create(this, "ECSFargateServiceSecurityGroup")
//            .vpc(defaultVpc)
//            .securityGroupName("ECSFargateServiceSecurityGroup")
//            .allowAllOutbound(true)
//            .build();
//
//        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(80));
//        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(443));
//        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(10101));
////        securityGroup.addEgressRule(Peer.anyIpv4(), Port.allTraffic());
//
//
//        ApplicationLoadBalancedFargateService.Builder.create(this, "ECSFargateService")
//            .serviceName("ECSFargateService")
//            .taskImageOptions(
//                ApplicationLoadBalancedTaskImageOptions.builder()
//                    .image(
//                        ContainerImage.fromEcrRepository(
//                            Repository.fromRepositoryName(
//                                this,
//                                "ECSFargateServiceRepository",
//                                "test-repository"
//                            ),
//                            "latest"
//                        )
//                    )
//                    .containerPort(10101)
//                    .build()
//            )
//            .publicLoadBalancer(true)
//            .assignPublicIp(true)
//            .runtimePlatform(
//                RuntimePlatform.builder()
//                    .cpuArchitecture(CpuArchitecture.ARM64)
//                    .operatingSystemFamily(OperatingSystemFamily.LINUX)
//                    .build()
//            )
//            .securityGroups(Collections.singletonList(securityGroup))
//            .vpc(defaultVpc)
//            .build();
//    }


    public EcsStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        ApplicationLoadBalancedFargateService.Builder.create(this, "ECSFargateService")
            .taskImageOptions(
                ApplicationLoadBalancedTaskImageOptions.builder()
                .image(
                    ContainerImage.fromRegistry("amazon/amazon-ecs-sample")
                )
                .build()
            )
            .publicLoadBalancer(true)
            .build();
    }


}


