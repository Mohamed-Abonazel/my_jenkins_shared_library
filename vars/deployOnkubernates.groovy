#!/usr/bin/env groovy

def call(String kubeconfigCredentialsID, String kubernetesClusterURL, String imageName) {
    sh """
        sed -i 's|image:.*|image: ${imageName}:${BUILD_NUMBER}|g' deployment.yaml
    """
    
    withCredentials([file(credentialsId: kubeconfigCredentialsID, variable: 'KUBECONFIG_FILE')]) {
        sh """
            export KUBECONFIG=${KUBECONFIG_FILE}
            echo "Using Kubernetes Cluster at ${kubernetesClusterURL}"
            kubectl cluster-info
            kubectl apply -f .
            kubectl rollout status deployment/$(kubectl get deployment -o=jsonpath='{.items[0].metadata.name}')
        """
    }
    
    echo "Deployment to Kubernetes Cluster completed successfully."
}


