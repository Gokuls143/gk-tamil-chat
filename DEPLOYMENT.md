# Deployment Guide - GK Tamil Chat

## Prerequisites
- Java 21 installed on server
- MySQL 8.x database
- Domain name (optional but recommended)
- Server with public IP address

## Option 1: Deploy to VPS (DigitalOcean, Linode, AWS EC2)

### Step 1: Prepare Your Application

1. **Build the application:**
   ```bash
   ./gradlew clean build
   ```
   This creates: `build/libs/demo-0.0.1-SNAPSHOT.jar`

2. **Test locally:**
   ```bash
   java -jar build/libs/demo-0.0.1-SNAPSHOT.jar
   ```

### Step 2: Setup Server

1. **Connect to your server:**
   ```bash
   ssh username@your-server-ip
   ```

2. **Install Java 21:**
   ```bash
   sudo apt update
   sudo apt install openjdk-21-jdk -y
   java -version
   ```

3. **Install MySQL:**
   ```bash
   sudo apt install mysql-server -y
   sudo mysql_secure_installation
   ```

4. **Create database:**
   ```bash
   sudo mysql -u root -p
   ```
   ```sql
   CREATE DATABASE chatdb;
   CREATE USER 'chatuser'@'localhost' IDENTIFIED BY 'your_strong_password';
   GRANT ALL PRIVILEGES ON chatdb.* TO 'chatuser'@'localhost';
   FLUSH PRIVILEGES;
   EXIT;
   ```

### Step 3: Deploy Application

1. **Upload JAR file to server:**
   ```bash
   scp build/libs/demo-0.0.1-SNAPSHOT.jar username@your-server-ip:/home/username/
   ```

2. **Create application.properties on server:**
   ```bash
   nano application.properties
   ```
   Add:
   ```properties
   server.port=8080
   spring.datasource.url=jdbc:mysql://localhost:3306/chatdb
   spring.datasource.username=chatuser
   spring.datasource.password=your_strong_password
   spring.jpa.hibernate.ddl-auto=update
   
   # Email configuration (optional)
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```

3. **Run application:**
   ```bash
   java -jar demo-0.0.1-SNAPSHOT.jar --spring.config.location=application.properties
   ```

### Step 4: Run as Background Service

Create systemd service:
```bash
sudo nano /etc/systemd/system/gktamilchat.service
```

Add:
```ini
[Unit]
Description=GK Tamil Chat Application
After=mysql.service

[Service]
User=username
WorkingDirectory=/home/username
ExecStart=/usr/bin/java -jar /home/username/demo-0.0.1-SNAPSHOT.jar --spring.config.location=/home/username/application.properties
SuccessExitStatus=143
TimeoutStopSec=10
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl daemon-reload
sudo systemctl enable gktamilchat
sudo systemctl start gktamilchat
sudo systemctl status gktamilchat
```

### Step 5: Setup Nginx Reverse Proxy (Recommended)

1. **Install Nginx:**
   ```bash
   sudo apt install nginx -y
   ```

2. **Configure Nginx:**
   ```bash
   sudo nano /etc/nginx/sites-available/gktamilchat
   ```
   
   Add:
   ```nginx
   server {
       listen 80;
       server_name your-domain.com www.your-domain.com;

       location / {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }

       location /chat {
           proxy_pass http://localhost:8080/chat;
           proxy_http_version 1.1;
           proxy_set_header Upgrade $http_upgrade;
           proxy_set_header Connection "upgrade";
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
   }
   ```

3. **Enable site:**
   ```bash
   sudo ln -s /etc/nginx/sites-available/gktamilchat /etc/nginx/sites-enabled/
   sudo nginx -t
   sudo systemctl restart nginx
   ```

### Step 6: Setup SSL (HTTPS) with Let's Encrypt

```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d your-domain.com -d www.your-domain.com
```

### Step 7: Configure Firewall

```bash
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

---

## Option 2: Deploy to Heroku

1. **Create Procfile:**
   ```
   web: java -jar build/libs/demo-0.0.1-SNAPSHOT.jar
   ```

2. **Create system.properties:**
   ```
   java.runtime.version=21
   ```

3. **Deploy:**
   ```bash
   heroku login
   heroku create gk-tamil-chat
   heroku addons:create cleardb:ignite
   git push heroku main
   heroku open
   ```

---

## Option 3: Deploy to Railway.app

1. Push code to GitHub
2. Go to railway.app and sign in
3. Click "New Project" → "Deploy from GitHub repo"
4. Select your repository
5. Add MySQL database from Railway marketplace
6. Railway auto-detects Spring Boot and deploys

---

## Option 4: Docker Deployment

Create `Dockerfile`:
```dockerfile
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY build/libs/demo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Create `docker-compose.yml`:
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: chatdb
      MYSQL_USER: chatuser
      MYSQL_PASSWORD: chatpassword
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/chatdb
      SPRING_DATASOURCE_USERNAME: chatuser
      SPRING_DATASOURCE_PASSWORD: chatpassword
    depends_on:
      - mysql

volumes:
  mysql-data:
```

Deploy:
```bash
docker-compose up -d
```

---

## Domain Setup

1. **Buy a domain** from:
   - Namecheap
   - GoDaddy
   - Google Domains
   - Cloudflare

2. **Point domain to your server:**
   - Add an A record: `@` → Your server IP
   - Add an A record: `www` → Your server IP

3. **Wait for DNS propagation** (5-30 minutes)

---

## Security Checklist

- [ ] Change all default passwords
- [ ] Enable HTTPS/SSL
- [ ] Configure CORS properly
- [ ] Set up regular backups
- [ ] Enable firewall
- [ ] Keep Java and dependencies updated
- [ ] Use strong database passwords
- [ ] Limit SSH access
- [ ] Monitor application logs
- [ ] Set up error monitoring (Sentry, etc.)

---

## Monitoring

View logs:
```bash
sudo journalctl -u gktamilchat -f
```

Check status:
```bash
sudo systemctl status gktamilchat
```

Restart application:
```bash
sudo systemctl restart gktamilchat
```

---

## Cost Estimates

- **VPS (DigitalOcean/Linode)**: $5-10/month
- **Domain name**: $10-15/year
- **SSL Certificate**: Free (Let's Encrypt)
- **Railway.app**: Free tier available, $5-10/month for production
- **Heroku**: Free tier discontinued, starts at $7/month

---

## Quick Deploy Commands

For quick deployment on a fresh Ubuntu server:

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Java 21
sudo apt install openjdk-21-jdk -y

# Install MySQL
sudo apt install mysql-server -y

# Install Nginx
sudo apt install nginx -y

# Configure firewall
sudo ufw allow 22
sudo ufw allow 80
sudo ufw allow 443
sudo ufw enable

# Transfer and run application
# (Upload JAR file first)
java -jar demo-0.0.1-SNAPSHOT.jar
```

---

## Troubleshooting

**Port 8080 already in use:**
```bash
sudo lsof -i :8080
sudo kill -9 <PID>
```

**Check application logs:**
```bash
tail -f /var/log/syslog | grep gktamilchat
```

**MySQL connection issues:**
```bash
sudo systemctl status mysql
sudo mysql -u chatuser -p
```

**WebSocket connection fails:**
- Ensure Nginx WebSocket proxy is configured
- Check firewall allows WebSocket traffic
- Verify CORS settings in application

---

For more help, refer to:
- Spring Boot Documentation: https://spring.io/guides
- Nginx Documentation: https://nginx.org/en/docs/
- Let's Encrypt: https://letsencrypt.org/
