pipeline {
  agent { label 'built-in' }

  options {
    timestamps()
    timeout(time: 30, unit: 'MINUTES')
    disableConcurrentBuilds()
  }

  tools {
    jdk   'Temurin-21'
    maven 'Maven-3.9'
  }

  environment {
    REGISTRY           = 'rezervasyon-cd.b3lab.org'
    REGISTRY_NAMESPACE = 'b3lab'
    IMAGE_REPO         = 'reserve-docker-reserve-backend'

    SERVICE_NAME = 'reserve-backend'
    COMPOSE_YML  = "${WORKSPACE}/docker-compose.yml"
  }

  stages {
    stage('Checkout(auto)') {
      steps {
        sh 'set -eu; git rev-parse --abbrev-ref HEAD || true; git log -1 --oneline || true'
      }
    }

    stage('Build & Test') {
      steps {
        sh 'set -eu; mvn -B clean package -Dmaven.test.skip=true'
      }
    }

    stage('Docker Build') {
      steps {
        sh '''
          set -eu
          IMAGE_BASE="${REGISTRY}/${REGISTRY_NAMESPACE}/${IMAGE_REPO}"
          GIT_SHA="$(git rev-parse --short HEAD || echo dev)"
          IMAGE_TAG="${IMAGE_BASE}:${GIT_SHA}"
          echo "[build] ${IMAGE_TAG}"
          docker build -t "${IMAGE_TAG}" .
          echo "${IMAGE_TAG}" > image.tag
        '''
      }
    }

    stage('Push Image (Harbor)') {
      when { expression { return fileExists('image.tag') } }
      steps {
        withCredentials([usernamePassword(credentialsId: 'registry-creds', usernameVariable: 'REG_USER', passwordVariable: 'REG_PASS')]) {
          sh '''
            set -eu
            printf "%s" "$REG_PASS" | docker login "${REGISTRY}" -u "$REG_USER" --password-stdin
            IMG="$(cat image.tag)"
            docker push "$IMG"
            docker tag "$IMG" "${REGISTRY}/${REGISTRY_NAMESPACE}/${IMAGE_REPO}:latest"
            docker push "${REGISTRY}/${REGISTRY_NAMESPACE}/${IMAGE_REPO}:latest"
            docker logout "${REGISTRY}" || true
          '''
        }
      }
    }

    stage('Deploy backend (compose)') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'registry-creds', usernameVariable: 'REG_USER', passwordVariable: 'REG_PASS')]) {
          sh '''
            set -eu
            export HOME="${HOME:-/var/jenkins_home}"
            printf "%s" "$REG_PASS" | docker login "${REGISTRY}" -u "$REG_USER" --password-stdin

            ensure_compose() {
              if docker compose version >/dev/null 2>&1; then
                compose() { docker compose "$@"; }; return
              elif command -v docker-compose >/dev/null 2>&1; then
                compose() { docker-compose "$@"; }; return
              fi
              for p in /usr/local/bin/docker-compose /usr/lib/docker/cli-plugins/docker-compose /usr/local/lib/docker/cli-plugins/docker-compose "${HOME}/.docker/cli-plugins/docker-compose"; do
                [ -x "$p" ] && compose() { "$p" "$@"; } && return
              done
              echo "[compose] not found"
              exit 11
            }
            ensure_compose

            [ -f "${COMPOSE_YML}" ] || { echo "Missing ${COMPOSE_YML}"; exit 2; }
            [ -f image.tag ] || { echo "image.tag missing"; exit 1; }
            BACKEND_IMAGE="$(tr -d ' \t\r\n' < image.tag)"
            docker network inspect ${NETWORK_NAME:-dev-network} >/dev/null 2>&1 || docker network create ${NETWORK_NAME:-dev-network}
            docker rm -f "${SERVICE_NAME}" 2>/dev/null || true

            BACKEND_IMAGE="${BACKEND_IMAGE}" compose -f "${COMPOSE_YML}" up -d --no-deps --force-recreate "${SERVICE_NAME}"
            BACKEND_IMAGE="${BACKEND_IMAGE}" compose -f "${COMPOSE_YML}" ps
          '''
        }
      }
    }
  }

  post {
    success { echo '✅ Backend build & deploy tamam.' }
    failure { echo '❌ Hata oluştu. Logları kontrol et.' }
  }
}
