pipeline {
    agent any

    options {
        // Tự động xóa các bản build cũ để tiết kiệm dung lượng Jenkins server
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // Giới hạn thời gian pipeline chạy tối đa 30 phút
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }

    environment {
        // ID của Credential SSH đã lưu trong Jenkins (loại: SSH Username with private key)
        SSH_CREDENTIAL_ID = 'pentava-vps-ssh-key'

        // Cấu hình thông tin máy chủ VPS của bạn
        VPS_HOST          = '42.96.2.76'        // IP máy chủ VPS
        VPS_USER          = 'root'              // User SSH (thường là root hoặc ubuntu)
        VPS_PORT          = '22'                // Cổng SSH của VPS
        VPS_DEPLOY_PATH   = '/opt/pentava'      // Thư mục lưu mã nguồn backend trên VPS
        GIT_REPO_URL      = 'https://github.com/lamhoanghai-dang1911/pentava-backend.git'
    }

    triggers {
        // Tự động kích hoạt khi có commit push lên nhánh main từ GitHub Webhook
        githubPush()
    }

    stages {
        stage('Checkout Code') {
            steps {
                echo "📥 [Step 1] Đang lấy mã nguồn mới nhất từ GitHub..."
                checkout scm
            }
        }

        stage('Deploy & Build to VPS') {
            steps {
                echo "🚀 [Step 2] Kết nối SSH vào VPS để kéo code, build và cập nhật hệ thống..."
                // Yêu cầu Jenkins cài đặt plugin: "SSH Agent"
                sshagent(credentials: [env.SSH_CREDENTIAL_ID]) {
                    withCredentials([usernamePassword(credentialsId: 'github-token', usernameVariable: 'GH_USER', passwordVariable: 'GH_TOKEN')]) {
                        sh '''
                            # Bỏ qua kiểm tra StrictHostKeyChecking để tránh dừng pipeline vì prompt xác nhận fingerprint
                            SSH_CMD="ssh -o StrictHostKeyChecking=no -p ${VPS_PORT} ${VPS_USER}@${VPS_HOST}"

                            echo "--> Đồng bộ mã nguồn trên VPS..."
                            $SSH_CMD "
                                mkdir -p '${VPS_DEPLOY_PATH}'
                                cd '${VPS_DEPLOY_PATH}'

                                # Nếu chưa có repo git thì init
                                if [ ! -d .git ]; then
                                    echo 'Khởi tạo Git repo trong thư mục...'
                                    git init
                                    git remote add origin https://${GH_TOKEN}@github.com/lamhoanghai-dang1911/pentava-backend.git
                                else
                                    git remote set-url origin https://${GH_TOKEN}@github.com/lamhoanghai-dang1911/pentava-backend.git
                                fi

                                echo 'Cập nhật mã nguồn mới nhất...'
                                git fetch origin main
                                git reset --hard origin/main

                                echo 'Cấp quyền và chạy script deploy...'
                                chmod +x scripts/deploy.sh
                                ./scripts/deploy.sh
                            "
                        '''
                    }
                }
            }
        }

        stage('Verify Healthcheck') {
            steps {
                echo "🩺 [Step 3] Kiểm tra sức khỏe API Gateway sau triển khai..."
                sshagent(credentials: [env.SSH_CREDENTIAL_ID]) {
                    sh '''
                        SSH_CMD="ssh -o StrictHostKeyChecking=no -p ${VPS_PORT} ${VPS_USER}@${VPS_HOST}"
                        $SSH_CMD "
                            echo '--> Kiểm tra trạng thái containers:'
                            cd '${VPS_DEPLOY_PATH}'
                            docker compose ps
                        "
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "✅ [SUCCESS] CI/CD Pipeline hoàn tất! Backend PENTAVA đã được triển khai thành công trên VPS."
        }
        failure {
            echo "❌ [FAILURE] Pipeline thất bại! Vui lòng kiểm tra log chi tiết ở trên."
        }
        always {
            // Dọn dẹp không gian làm việc trên máy chủ Jenkins
            cleanWs()
        }
    }
}
