#!/usr/bin/env groovy

def call(String k8sCredentialsID, String imageName) {

    // Update deployment.yaml with new Docker Hub image
    sh "sed -i 's|image:.*|image: ${imageName}:${BUILD_NUMBER}|g' deployment.yaml"

    // Login to k8s Cluster via KubeConfig file
    withCredentials([file(credentialsId: "${kubeconfigCredentialsID}", variable: 'KUBECONFIG_FILE')]) {
        sh """
            export KUBECONFIG=${KUBECONFIG_FILE}
            kubectl apply -f .
        """
    }
}



