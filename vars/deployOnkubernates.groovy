#!/usr/bin/env groovy

// KubernetesCredentialsID can be credentials of service account token or KubeConfig file 
def call(String KubernetesCredentialsID, String kubeConfigFile, String kubeClusterurl, String kubeNamespace, String imageName) {
    
    // Update deployment.yaml with new Docker Hub image
    sh "sed -i 's|image:.*|image: ${imageName}:${BUILD_NUMBER}|g' deployment.yaml"

    // login to Kubernetes Cluster via kubeconfig file
    withCredentials([file(credentialsId: "${KubernetesCredentialsID}", variable: 'KUBECONFIG_FILE')]) {
        sh "export KUBECONFIG=\$KUBECONFIG_FILE && kubectl config set-cluster ${kubeClusterurl}"
        sh "export KUBECONFIG=\$KUBECONFIG_FILE && kubectl config set-context --current --namespace=${kubeNamespace}"
        sh "export KUBECONFIG=\$KUBECONFIG_FILE && kubectl apply -f ."
    }

    // Optional: login to Kubernetes Cluster via service account token
    // withCredentials([string(credentialsId: 'KubernetesServiceAccountToken', variable: 'KUBERNETES_TOKEN')]) {
    //     sh "kubectl --token=${KUBERNETES_TOKEN} --server=${kubeClusterurl} apply -f . -n ${kubeNamespace}"
    // }
}


