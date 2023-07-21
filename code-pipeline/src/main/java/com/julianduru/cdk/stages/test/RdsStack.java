package com.julianduru.cdk.stages.test;

/**
 * created by Julian Dumebi Duru on 19/07/2023
 */
import com.julianduru.cdk.util.JSONUtil;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.secretsmanager.*;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class RdsStack extends Stack {


    public RdsStack(final Construct scope, final String id, final Vpc vpc) {
        super(scope, id);

        Map<String, String> secretsMap = new HashMap<>();
        secretsMap.put("username", "postgres");

        // Create RDS Database Secret
        // Templated secret with username and password fields
        Secret secret = Secret.Builder.create(this, "TemplatedSecret")
            .generateSecretString(
                SecretStringGenerator.builder()
                    .secretStringTemplate(JSONUtil.asJsonString(secretsMap, ""))
                    .generateStringKey("password")
                    .excludeCharacters("/@\"")
                    .build()
            )
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
            .instanceIdentifier("my_rds_database")
            .credentials(Credentials.fromSecret(secret))
            .vpc(vpc)
            .vpcSubnets(
                SubnetSelection.builder()
                    .subnetType(SubnetType.PRIVATE_ISOLATED)
                    .build()
            )
            .build();

        // Output RDS Endpoint
        CfnOutput.Builder.create(this, "DatabaseEndpoint")
            .value(database.getDbInstanceEndpointAddress())
            .build();
    }


}

