#!/usr/bin/env groovy

def call(String kubeconfigCredentialsID, String kubernetesClusterURL, String imageName) {
    // Update the deployment.yaml image tag with the current build number
    sh """
        sed -i 's|image:.*|image: ${imageName}:${BUILD_NUMBER}|g' deployment.yaml
    """
    
    // Use Kubernetes credentials to apply the deployment
    withCredentials([file(credentialsId: kubeconfigCredentialsID, variable: 'KUBECONFIG_FILE')]) {
        sh """
            export KUBECONFIG=${KUBECONFIG_FILE}
            echo "Using Kubernetes Cluster at ${kubernetesClusterURL}"
            kubectl cluster-info --server=${kubernetesClusterURL}
            kubectl apply -f .
            kubectl rollout status deployment/$(kubectl get deployment -o=jsonpath='{.items[0].metadata.name}')
        """
    }
    
    // Echo a success message to indicate deployment completion
    echo "Deployment to Kubernetes Cluster completed successfully."
}

