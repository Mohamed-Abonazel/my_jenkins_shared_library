#!/usr/bin/env groovy

def call(String kubeconfigCredentialsID, String kubernetesClusterURL, String imageName, String caCertContent, String clientCertContent, String clientKeyContent) {
    // Define the path to deployment.yaml
    def deploymentYamlPath = "CloudDevOpsProject-iVolve/Kubernetes/deployment.yaml"
    
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
        // Set up the Kubernetes certificates for secure communication using environment variables
        withEnv([
            "KUBERNETES_CA_CERT=${caCertContent}",
            "KUBERNETES_CLIENT_CERT=${clientCertContent}",
            "KUBERNETES_CLIENT_KEY=${clientKeyContent}"
        ]) {
            sh """
                echo "Using Kubernetes Cluster at ${kubernetesClusterURL}"
                kubectl config set-cluster my-cluster --server=${kubernetesClusterURL} --certificate-authority=${KUBERNETES_CA_CERT}
                kubectl config set-credentials my-user --client-certificate=${KUBERNETES_CLIENT_CERT} --client-key=${KUBERNETES_CLIENT_KEY}
                kubectl config set-context my-context --cluster=my-cluster --user=my-user
                kubectl config use-context my-context
                kubectl cluster-info || { echo "Cluster info failed"; exit 1; }
                kubectl apply -f ${deploymentYamlPath} || { echo "Kubectl apply failed"; exit 1; }
                kubectl rollout status deployment/\$(kubectl get deployment -o=jsonpath='{.items[0].metadata.name}') || { echo "Rollout failed"; exit 1; }
            """
        }
    }
    
    // Echo a success message to indicate deployment completion
    echo "Deployment to Kubernetes Cluster completed successfully."
}

