#!/bin/bash


yum update -y
yum -y install telnet

yum install -y java-17-openjdk

aws s3 cp "s3://${bucket_name}/your-java-app.jar" /path/to/your-java-app.jar

cat << EOF > /etc/systemd/system/sample-java-app.service

[Unit]
Description=Sample Java Application
After=network.target

[Service]
Type=simple
Restart=always
RestartSec=2
ExecStart=/usr/bin/java -jar /path/to/your-java-app.jar

[Install]
WantedBy=multi-user.target

EOF

systemctl daemon-reload
systemctl enable your-java-app
systemctl start your-java-app



