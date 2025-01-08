def call(String kubeconfigCredentialsID, String kubernetesClusterURL, String imageName, String caCertPath, String clientCertPath, String clientKeyPath) {
    // Define the path to deployment.yaml
    def deploymentYamlPath = "Kubernetes/deployment.yaml"
    
    // Check if deployment.yaml exists
    echo "Checking if the deployment.yaml exists at ${deploymentYamlPath}"
    sh """
        if [ ! -f ${deploymentYamlPath} ]; then
            echo "Error: ${deploymentYamlPath} does not exist!"
            exit 1
        fi
    """
    
    // Update the deployment.yaml image tag with the current build number
    sh """
        sed -i 's|image:.*|image: ${imageName}:${BUILD_NUMBER}|g' ${deploymentYamlPath}
    """
    
    // Validate certificate files
    sh """
        if [ ! -f ${caCertPath} ]; then
            echo "Error: ${caCertPath} does not exist!"
            exit 1
        fi
        if [ ! -f ${clientCertPath} ]; then
            echo "Error: ${clientCertPath} does not exist!"
            exit 1
        fi
        if [ ! -f ${clientKeyPath} ]; then
            echo "Error: ${clientKeyPath} does not exist!"
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
        """
        
        // Fetch the deployment name dynamically and monitor the rollout
        def deploymentName = sh(script: "kubectl get deployment -o=jsonpath='{.items[0].metadata.name}'", returnStdout: true).trim()
        echo "Monitoring rollout status for deployment: ${deploymentName}"
        sh "kubectl rollout status deployment/${deploymentName}"
    }
    
    // Echo a success message to indicate deployment completion
    echo "Deployment to Kubernetes Cluster completed successfully."
}

