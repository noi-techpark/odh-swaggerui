pipeline {
	agent any

	environment {
		AWS_ACCESS_KEY_ID = credentials('AWS_ACCESS_KEY_ID')
		AWS_SECRET_ACCESS_KEY = credentials('AWS_SECRET_ACCESS_KEY')
		DOCKER_PROJECT_NAME = "odh-swaggerui"
		DOCKER_IMAGE = '755952719952.dkr.ecr.eu-west-1.amazonaws.com/odh-swaggerui'
		DOCKER_TAG = "test-$BUILD_NUMBER"
		SERVER_PORT = "1050"

    SWAGGER_JSON = "/code/default.yml"
	}

	stages {
		stage('Configure') {
			steps {
				sh """
					rm -f .env
					cp .env.example .env
					echo 'COMPOSE_PROJECT_NAME=${DOCKER_PROJECT_NAME}' >> .env
					echo 'DOCKER_IMAGE=${DOCKER_IMAGE}' >> .env
					echo 'DOCKER_TAG=${DOCKER_TAG}' >> .env
					echo 'SERVER_PORT=${SERVER_PORT}' >> .env

          echo 'SWAGGER_JSON=${SWAGGER_JSON}' >> .env
				"""
			}
		}
		stage('Build') {
			steps {
				sh '''
					aws ecr get-login --region eu-west-1 --no-include-email | bash
					docker-compose --no-ansi -f docker-compose.build.yml build --pull
					docker-compose --no-ansi -f docker-compose.build.yml push
				'''
			}
		}
		stage('Deploy') {
			steps {
			   sshagent(['jenkins-ssh-key']) {
					sh """
						ansible-galaxy install --force -r ansible/requirements.yml
						ansible-playbook --limit=docker02.testingmachine.eu ansible/deploy.yml --extra-vars "build_number=${BUILD_NUMBER}"
					"""
				}
			}
		}
	}
}
