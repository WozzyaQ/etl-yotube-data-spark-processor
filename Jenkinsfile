pipeline {
    agent any

    environment {
          SBT_HOME = tool name: 'sbt-1.5.4', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'
          PATH = "${env.SBT_HOME}/bin:${env.PATH}"
    }

    stages {

        stage('Build') {
            steps {
                sh 'sbt clean assembly'
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
            }
        }
        stage('Deploy') {
            steps {
                withAWS(region: 'us-east-2',credentials: 'sigma-aws-creds') {
                    s3Upload(bucket: "chaikovskyi-jars-bucket", file: "./jars/crawler.jar");
                    s3Upload(bucket: "chaikovskyi-jars-bucket", file: "./jars/spark.jar");
                }
            }
        }
    }

    post {
      always {
        cleanWs()
        dir("${env.WORKSPACE}@tmp") {
          deleteDir()
        }
        dir("${env.WORKSPACE}@script") {
          deleteDir()
        }
        dir("${env.WORKSPACE}@script@tmp") {
          deleteDir()
        }
      }
    }
}
