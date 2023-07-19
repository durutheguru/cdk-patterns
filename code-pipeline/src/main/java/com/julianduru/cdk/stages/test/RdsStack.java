package com.julianduru.cdk.stages.test;

/**
 * created by Julian Dumebi Duru on 19/07/2023
 */
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.secretsmanager.*;
import software.constructs.Construct;

public class RdsStack extends Stack {


    public RdsStack(final Construct scope, final String id, final Vpc vpc) {
        super(scope, id);

        // Create RDS Database Secret
        ISecret secret = Secret.Builder.create(this, "DatabaseSecret")
            .secretName("mydatabase/credentials")
            .build();

        // Create RDS Database Instance
        DatabaseInstance database = DatabaseInstance.Builder.create(this, "Database")
            .engine(
                DatabaseInstanceEngine.mysql(
                    MySqlInstanceEngineProps.builder()
                        .version(MysqlEngineVersion.VER_8_0_23)
                        .build()
                )
            )
            .instanceIdentifier("mydatabase")
            .credentials(Credentials.fromSecret(secret))
            .vpc(vpc)
            .vpcSubnets(
                SubnetSelection.builder()
                    .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                    .build()
            )
            .build();

        // Output RDS Endpoint
        CfnOutput.Builder.create(this, "DatabaseEndpoint")
            .value(database.getDbInstanceEndpointAddress())
            .build();
    }


}
