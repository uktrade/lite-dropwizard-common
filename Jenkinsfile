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
          deployer.inside {
            try {
              sh "./gradlew jersey-correlation-id:test"
            }
            catch (e) {
              testFailure = true
            }
          }
        }
      }
    }

    stage("test: json-console-appender") {
      steps {
        script {
          deployer.inside {
            try {
              sh "./gradlew json-console-appender:test"
            }
            catch (e) {
              testFailure = true
            }
          }
        }
      }
    }

    stage("test: paas-utils") {
      steps {
        script {
          deployer.inside {
            try {
              sh "./gradlew paas-utils:test"
            }
            catch (e) {
              testFailure = true
            }
          }
        }
      }
    }

    stage("test: readiness-metric") {
      steps {
        script {
          deployer.inside {
            try {
              sh "./gradlew readiness-metric:test"
            }
            catch (e) {
              testFailure = true
            }
          }
        }
      }
    }

    stage("test: spire-client") {
      steps {
        script {
          deployer.inside {
            try {
              sh "./gradlew spire-client:test"
            }
            catch (e) {
              testFailure = true
            }
          }
        }
      }
    }

    stage("test: jwt") {
      steps {
        script {
          deployer.inside {
            try {
              sh "./gradlew jwt:test"
            }
            catch (e) {
              testFailure = true
            }
          }
        }
      }
    }

    stage("archive results") {
      steps {
        script {
          step([$class: 'JUnitResultArchiver', testResults: '*/build/test-results/**/*.xml'])
        }
      }
    }

    stage('sonarqube') {
      steps {
        script {
          deployer.inside {
            withSonarQubeEnv('sonarqube') {
              sh 'chmod 777 gradlew'
              sh './gradlew compileJava compileTestJava -i'
              sh "${env.SONAR_SCANNER_PATH}/sonar-scanner"
            }
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