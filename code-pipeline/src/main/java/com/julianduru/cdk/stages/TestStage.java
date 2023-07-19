package com.julianduru.cdk.stages;

import com.julianduru.cdk.stages.test.Ec2Stack;
import com.julianduru.cdk.stages.test.RdsStack;
import com.julianduru.cdk.stages.test.VpcStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Stage;
import software.amazon.awscdk.StageProps;
import software.constructs.Construct;

/**
 * created by Julian Dumebi Duru on 19/07/2023
 */
public class TestStage extends Stage {


    public TestStage(@NotNull Construct scope, @NotNull String id) {
        this(scope, id, null);
    }


    public TestStage(@NotNull Construct scope, @NotNull String id, @Nullable StageProps props) {
        super(scope, id, props);

        VpcStack vpcStack = new VpcStack(scope, "vpcStackId");
        Ec2Stack ec2Stack = new Ec2Stack(scope, "ec2StackId", vpcStack.getVpc());
        RdsStack rdsStack = new RdsStack(scope, "rdsStackId", vpcStack.getVpc());

    }


}


