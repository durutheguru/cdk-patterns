package com.julianduru.cdk.stages.test;

/**
 * created by Julian Dumebi Duru on 19/07/2023
 */
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ec2.*;
import software.constructs.Construct;


public class Ec2Stack extends Stack {


    public Ec2Stack(final Construct scope, final String id, final Vpc vpc) {
        super(scope, id);

        // Create Security Group
        SecurityGroup securityGroup = SecurityGroup.Builder.create(this, "TestEc2SecurityGroup")
            .vpc(vpc)
            .build();

        // Create EC2 Instance
        Instance instance = Instance.Builder.create(this, "Test_Ec2_Instance")
            .vpc(vpc)
            .instanceType(InstanceType.of(InstanceClass.T2, InstanceSize.MICRO))
            .machineImage(MachineImage.latestAmazonLinux2023())
            .securityGroup(securityGroup)
            .build();

        // Output EC2 Instance ID
        CfnOutput.Builder.create(this, "Test_Ec2_Instance_Id")
            .value(instance.getInstanceId())
            .build();
    }


}


