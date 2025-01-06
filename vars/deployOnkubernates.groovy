#!/usr/bin/env groovy

/**
 * Deploys an application to a Kubernetes cluster using KubeConfig as Secret Text.
 *
 * @param k8sCredentialsID Jenkins credentials ID for Kubernetes KubeConfig (Secret Text)
 * @param imageName Docker image name with tag
 */
def call(String k8sCredentialsID, String imageName) {
    
    // Update deployment.yaml with the new Docker image
    echo " Updating deployment.yaml with image: ${imageName}:${BUILD_NUMBER}"
    sh "sed -i 's|image:.*|image: ${imageName}:${BUILD_NUMBER}|g' deployment.yaml"

    // Authenticate using Kubernetes KubeConfig as Secret Text
    withCredentials([string(credentialsId: "${k8sCredentialsID}", variable: 'KUBECONFIG_CONTENT')]) {
        echo " Using Kubernetes KubeConfig credentials: ${k8sCredentialsID}"
        
        // Save the secret text into a temporary kubeconfig file
        sh '''
        echo "$KUBECONFIG_CONTENT" > /tmp/kubeconfig.yaml
        export KUBECONFIG=/tmp/kubeconfig.yaml
        
        echo " Validating Kubernetes Cluster Connection..."
        kubectl cluster-info
        kubectl get nodes
        
        echo " Applying Kubernetes manifests..."
        kubectl apply -f .
        
        echo " Checking Deployment Rollout Status..."
        kubectl rollout status deployment/ivolve1
        
        '''
    }

    echo "Kubernetes Deployment completed successfully!"
}

