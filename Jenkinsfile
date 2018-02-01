pipeline {
  agent {
    node {
      label 'docker.ci.uktrade.io'
    }
  }

  stages {
    stage('prep') {
      steps {
        script {
          deleteDir()
          checkout scm
          deployer = docker.image("ukti/lite-image-builder")
          deployer.pull()
          deployer.inside {
            sh 'chmod 777 gradlew'
          }
          testFailure = false
        }
      }
    }

    stage("test: jersey-correlation-id") {
      steps {
        script {
          dir("jersey-correlation-id") {
            deployer.inside {
              try {
                sh "../gradlew test"
              }
              catch (e) {
                testFailure = true
              }
            }
          }
        }
      }
    }

    stage("test: json-console-appender") {
      steps {
        script {
          dir("json-console-appender") {
            deployer.inside {
              try {
                sh "../gradlew test"
              }
              catch (e) {
                testFailure = true
              }
            }
          }
        }
      }
    }

    stage("test: paas-utils") {
      steps {
        script {
          dir("paas-utils") {
            deployer.inside {
              try {
                sh "../gradlew test"
              }
              catch (e) {
                testFailure = true
              }
            }
          }
        }
      }
    }

    stage("test: readiness-metric") {
      steps {
        script {
          dir("readiness-metric") {
            deployer.inside {
              try {
                sh "../gradlew test"
              }
              catch (e) {
                testFailure = true
              }
            }
          }
        }
      }
    }

    stage("test: spire-client") {
      steps {
        script {
          dir("spire-client") {
            deployer.inside {
              try {
                sh "../gradlew test"
              }
              catch (e) {
                testFailure = true
              }
            }
          }
        }
      }
    }

    stage("test: jwt") {
      steps {
        script {
          dir("jwt") {
            deployer.inside {
              try {
                sh "../gradlew test"
              }
              catch (e) {
                testFailure = true
              }
            }
          }
        }
      }
    }

    stage("archive results") {
      steps {
        script {
          step([$class: 'JUnitResultArchiver', testResults: '*/build/test-results/**/*.xml'])

          if (testFailure) {
            error("Test failures found, see the test reports for more details")
          }
        }
      }
    }

    stage("check test failures") {
      steps {
        script {
          if (testFailure) {
            error("Test failures found, see the test reports for more details")
          }
        }
      }
    }
  }
}