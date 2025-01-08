#!/usr/bin/env groovy

def call (String kubeconfigCredentialsID, String kubernetesClusterURL, String imageName) {
    // Define the path to deployment.yaml
    def deploymentYamlPath = "Kubernetes/deployment.yaml"
    
    // Update the deployment.yaml image tag with the current build number
    sh """
        if [ -f ${deploymentYamlPath} ]; then
            sed -i 's|image:.*|image: ${imageName}:${BUILD_NUMBER}|g' ${deploymentYamlPath}
        else
            echo "Error: ${deploymentYamlPath} does not exist!"
            exit 1
        fi
    """
    
    // Use Kubernetes credentials to apply the deployment
    withCredentials([file(credentialsId: kubeconfigCredentialsID, variable: 'KUBECONFIG_FILE')]) {
        sh """
            export KUBECONFIG=\$KUBECONFIG_FILE
            echo "Using Kubernetes Cluster at ${kubernetesClusterURL}"
            kubectl cluster-info --server=${kubernetesClusterURL}
            kubectl apply -f ${deploymentYamlPath}
            kubectl rollout status deployment/\$(kubectl get deployment -o=jsonpath='{.items[0].metadata.name}')
        """
    }
    
    // Echo a success message to indicate deployment completion
    echo "Deployment to Kubernetes Cluster completed successfully."
}

