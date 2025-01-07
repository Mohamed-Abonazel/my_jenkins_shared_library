#!/usr/bin/env groovy

// kubeconfigCredentialsID refers to the Jenkins credential ID for the kubeconfig file.
def call(String kubeconfigCredentialsID, String imageName) {
    
    // Update deployment.yaml with the new Docker image
    sh "sed -i 's|image:.*|image: ${imageName}:${BUILD_NUMBER}|g' deployment.yaml"

    // Use Kubeconfig credentials to authenticate with Kubernetes
    withCredentials([file(credentialsId: "${kubeconfigCredentialsID}", variable: 'KUBECONFIG')]) {
        sh '''
        export KUBECONFIG=${KUBECONFIG}
        
        # Check connection to the Kubernetes cluster
        kubectl cluster-info

        # Apply all YAML files in the current directory to the default namespace
        kubectl apply -f .
        '''
    }
}


