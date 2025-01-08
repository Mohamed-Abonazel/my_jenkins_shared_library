#!/usr/bin/env groovy

def call(String kubeconfigCredentialsID, String kubernetesClusterURL, String imageName, String caCertPath, String clientCertPath, String clientKeyPath) {
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
    
    // Use Kubernetes credentials and certificates to apply the deployment
    withCredentials([file(credentialsId: kubeconfigCredentialsID, variable: 'KUBECONFIG_FILE')]) {
        // Set up the Kubernetes certificates for secure communication
        sh """
            echo "Using Kubernetes Cluster at ${kubernetesClusterURL}"
            kubectl config set-cluster my-cluster --server=${kubernetesClusterURL} --certificate-authority=${caCertPath}
            kubectl config set-credentials my-user --client-certificate=${clientCertPath} --client-key=${clientKeyPath}
            kubectl config set-context my-context --cluster=my-cluster --user=my-user
            kubectl config use-context my-context
            kubectl cluster-info
            kubectl apply -f ${deploymentYamlPath}
            kubectl rollout status deployment/\$(kubectl get deployment -o=jsonpath='{.items[0].metadata.name}')
        """
    }
    
    // Echo a success message to indicate deployment completion
    echo "Deployment to Kubernetes Cluster completed successfully."
}

