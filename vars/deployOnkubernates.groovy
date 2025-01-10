#!/usr/bin/env groovy

def call(String kubeconfigCredentialsID, String kubernetesClusterURL, String imageName, String minikubeCACertID, String minikubeClientCertID, String minikubeClientKeyID) {
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
        // Export the KUBECONFIG and run cluster info
        sh """
            echo "KUBECONFIG path is: ${KUBECONFIG}"

            kubectl cluster-info || { echo "Cluster info failed"; exit 1; }
        """

        // Apply the deployment using kubectl
        sh """
            echo "Using Kubernetes Cluster at ${kubernetesClusterURL}"
            kubectl apply -f ${deploymentYamlPath} || { echo "Kubectl apply failed"; exit 1; }
            kubectl rollout status deployment/\$(kubectl get deployment -o=jsonpath='{.items[0].metadata.name}') || { echo "Rollout failed"; exit 1; }
        """
    }

    // Echo a success message to indicate deployment completion
    echo "Deployment to Kubernetes Cluster completed successfully."
}

