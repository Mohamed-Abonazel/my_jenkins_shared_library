#!usr/bin/env groovy
def call(String dockerHubCredentialsID, String imageName) {
    // Log in to DockerHub
    withCredentials([usernamePassword(credentialsId: "${dockerHubCredentialsID}", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        sh "echo ${PASSWORD} | docker login -u ${USERNAME} --password-stdin"
    }
    
    // Ensure the project is built
    sh './gradlew clean build'  // This will generate the JAR file in build/libs

    // Build and push Docker image
    echo "Building and Pushing Docker image..."
    sh "docker build -t ${imageName}:${BUILD_NUMBER} ."
    sh "docker push ${imageName}:${BUILD_NUMBER}"
}

