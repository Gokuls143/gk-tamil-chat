# Step-by-Step Deployment Guide to Railway.app

## Complete Instructions for Beginners

This guide will walk you through deploying your GK Tamil Chat application to the internet using Railway.app (Free to start).

---

## Part 1: Prepare Your Code (10 minutes)

### Step 1: Test Your Application Locally

Before deploying, make sure everything works:

1. **Open PowerShell in your project folder:**
   ```powershell
   cd "C:\Users\Admin\Documents\New Project\demo"
   ```

2. **Build the application:**
   ```powershell
   .\gradlew.bat clean build
   ```
   
   ✅ Wait for "BUILD SUCCESSFUL" message

3. **Test it runs:**
   ```powershell
   java -jar build\libs\demo-0.0.1-SNAPSHOT.jar
   ```
   
   ✅ Open browser to http://localhost:8080
   ✅ Test login, chat, profile
   ✅ Press Ctrl+C to stop the server

---

### Step 2: Create Production Configuration

1. **Create a new file:** `src/main/resources/application-prod.properties`

2. **Add this content:**
   ```properties
   # Production Database Configuration
   spring.datasource.url=${DATABASE_URL}
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=false
   
   # Server Configuration
   server.port=${PORT:8080}
   
   # Session Configuration
   server.servlet.session.timeout=30m
   server.servlet.session.cookie.http-only=true
   server.servlet.session.cookie.secure=false
   
   # Logging
   logging.level.root=INFO
   logging.level.com.example.demo=INFO
   ```

---

### Step 3: Set Up Git Repository

1. **Install Git if you don't have it:**
   - Download from: https://git-scm.com/download/win
   - Install with default settings

2. **Initialize Git in your project:**
   ```powershell
   cd "C:\Users\Admin\Documents\New Project\demo"
   git init
   ```

3. **Create `.gitignore` file:**
   ```powershell
   notepad .gitignore
   ```
   
   Add this content:
   ```
   .gradle
   build/
   !gradle/wrapper/gradle-wrapper.jar
   !**/src/main/**/build/
   !**/src/test/**/build/
   
   ### STS ###
   .apt_generated
   .classpath
   .factorypath
   .project
   .settings
   .springBeans
   .sts4-cache
   bin/
   !**/src/main/**/bin/
   !**/src/test/**/bin/
   
   ### IntelliJ IDEA ###
   .idea
   *.iws
   *.iml
   *.ipr
   out/
   !**/src/main/**/out/
   !**/src/test/**/out/
   
   ### NetBeans ###
   /nbproject/private/
   /nbbuild/
   /dist/
   /nbdist/
   /.nb-gradle/
   
   ### VS Code ###
   .vscode/
   
   ### Environment Variables ###
   .env
   *.env
   
   ### Logs ###
   *.log
   
   ### Compiled files ###
   *.class
   
   ### Database ###
   *.db
   ```

4. **Configure Git user (first time only):**
   ```powershell
   git config --global user.name "Your Name"
   git config --global user.email "your-email@example.com"
   ```

5. **Commit your code:**
   ```powershell
   git add .
   git commit -m "Initial commit - GK Tamil Chat application"
   ```

---

### Step 4: Push to GitHub

1. **Create GitHub account:**
   - Go to https://github.com
   - Click "Sign up" if you don't have an account
   - Verify your email

2. **Create a new repository:**
   - Click the "+" icon in top right
   - Select "New repository"
   - Name: `gk-tamil-chat`
   - Description: "Community chat application with user profiles"
   - Select: **Public** or **Private** (your choice)
   - ❌ **DO NOT** check "Initialize with README"
   - Click "Create repository"

3. **Push your code to GitHub:**
   
   Copy the commands from GitHub (they look like this):
   ```powershell
   git remote add origin https://github.com/YOUR-USERNAME/gk-tamil-chat.git
   git branch -M main
   git push -u origin main
   ```
   
   Replace `YOUR-USERNAME` with your actual GitHub username.
   
   ✅ You may be asked to login to GitHub - follow the prompts

4. **Verify upload:**
   - Refresh your GitHub repository page
   - You should see all your files

---

## Part 2: Deploy to Railway.app (10 minutes)

### Step 1: Sign Up for Railway

1. **Go to Railway.app:**
   - Open https://railway.app in your browser

2. **Sign up with GitHub:**
   - Click "Login"
   - Select "Login with GitHub"
   - Authorize Railway to access your GitHub
   - ✅ This connects Railway to your repositories

---

### Step 2: Create New Project

1. **Create a new project:**
   - Click "New Project" button
   - Select "Deploy from GitHub repo"
   
2. **Select your repository:**
   - Find and click `gk-tamil-chat` from the list
   - Click "Deploy Now"

3. **Wait for initial deployment:**
   - Railway will detect it's a Spring Boot application
   - You'll see build logs appearing
   - ⏱️ Wait 3-5 minutes for the build to complete

---

### Step 3: Add MySQL Database

1. **In your Railway project dashboard:**
   - Click "New" button (or "+ New")
   - Select "Database"
   - Choose "Add MySQL"

2. **MySQL will be created automatically:**
   - Railway provisions a MySQL 8.0 instance
   - Connection details are generated automatically

3. **Connect database to your app:**
   - Click on your application service (gk-tamil-chat)
   - Go to "Variables" tab
   - Click "New Variable"
   
   Add these variables one by one:

   **Variable 1:**
   - Name: `SPRING_PROFILES_ACTIVE`
   - Value: `prod`

   **Variable 2:**
   - Name: `DATABASE_URL`
   - Value: Click "Add Reference" → Select MySQL → Select `DATABASE_URL`
   
   Railway will automatically inject: `jdbc:mysql://[host]:[port]/railway?user=[user]&password=[password]`

4. **Redeploy the application:**
   - Click "Deploy" or "Redeploy"
   - Wait for deployment to complete

---

### Step 4: Configure Domain and Access

1. **Generate public URL:**
   - Click on your application service
   - Go to "Settings" tab
   - Scroll to "Networking" section
   - Click "Generate Domain"
   - You'll get a URL like: `gk-tamil-chat-production.up.railway.app`

2. **Test your application:**
   - Click on the generated URL
   - You should see your index.html page
   - Try to register a new user
   - Try to login
   - Test the chat functionality

---

### Step 5: Configure Environment Variables (Optional)

If you want email functionality for forgot password:

1. **In Variables tab, add:**
   
   ```
   SPRING_MAIL_HOST=smtp.gmail.com
   SPRING_MAIL_PORT=587
   SPRING_MAIL_USERNAME=your-email@gmail.com
   SPRING_MAIL_PASSWORD=your-app-password
   SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
   SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
   ```

2. **For Gmail App Password:**
   - Go to Google Account settings
   - Security → 2-Step Verification → App passwords
   - Generate a password for "Mail"
   - Use that password (not your regular Gmail password)

---

## Part 3: Using Custom Domain (Optional, 15 minutes)

### Step 1: Buy a Domain

1. **Purchase domain from:**
   - Namecheap.com (recommended, ~$10/year)
   - GoDaddy.com
   - Google Domains
   - Cloudflare

2. **Choose a name like:**
   - gktamilchat.com
   - tamilchat.online
   - mychatapp.com

---

### Step 2: Configure Domain in Railway

1. **In Railway project settings:**
   - Go to "Settings" → "Networking"
   - Click "Custom Domain"
   - Enter your domain: `gktamilchat.com`

2. **Railway will show DNS records:**
   ```
   Type: CNAME
   Name: @
   Value: [railway-generated-value].railway.app
   ```

3. **Add DNS records in your domain registrar:**
   - Login to Namecheap/GoDaddy
   - Go to DNS settings
   - Add the CNAME record shown by Railway
   - Save changes

4. **Wait for DNS propagation:**
   - Takes 5-30 minutes
   - Check status: https://dnschecker.org

5. **Enable SSL:**
   - Railway automatically provisions SSL certificate
   - Your site will be accessible via HTTPS

---

## Part 4: Alternative - Deploy to Render.com

If Railway doesn't work, try Render.com:

### Step 1: Sign Up

1. Go to https://render.com
2. Sign up with GitHub
3. Authorize Render

### Step 2: Create Web Service

1. Click "New +"
2. Select "Web Service"
3. Connect your GitHub repository
4. Configure:
   - Name: `gk-tamil-chat`
   - Environment: `Docker` or `Java`
   - Build Command: `./gradlew build`
   - Start Command: `java -jar build/libs/demo-0.0.1-SNAPSHOT.jar`
   - Instance Type: Free

### Step 3: Add Database

1. Click "New +"
2. Select "PostgreSQL" (or use external MySQL)
3. Copy connection string
4. Add as environment variable: `DATABASE_URL`

### Step 4: Deploy

- Click "Create Web Service"
- Wait for deployment
- You'll get a URL like: `gk-tamil-chat.onrender.com`

---

## Part 5: Alternative - Deploy to Your Own Server

If you have a VPS or want full control:

### Step 1: Get a VPS

**Recommended providers:**
- DigitalOcean: $6/month (use referral for $200 credit)
- Linode: $5/month
- Vultr: $5/month
- AWS EC2: Free tier for 1 year

### Step 2: Server Setup

1. **Create Ubuntu 22.04 server**

2. **Connect via SSH:**
   ```powershell
   ssh root@your-server-ip
   ```

3. **Run setup script:**
   ```bash
   # Update system
   apt update && apt upgrade -y
   
   # Install Java 21
   apt install openjdk-21-jdk -y
   
   # Install MySQL
   apt install mysql-server -y
   
   # Install Nginx
   apt install nginx -y
   
   # Install Certbot for SSL
   apt install certbot python3-certbot-nginx -y
   ```

4. **Setup MySQL:**
   ```bash
   mysql -u root -p
   ```
   ```sql
   CREATE DATABASE chatdb;
   CREATE USER 'chatuser'@'localhost' IDENTIFIED BY 'StrongPassword123!';
   GRANT ALL PRIVILEGES ON chatdb.* TO 'chatuser'@'localhost';
   FLUSH PRIVILEGES;
   EXIT;
   ```

5. **Create user for application:**
   ```bash
   adduser chatapp
   usermod -aG sudo chatapp
   su - chatapp
   ```

### Step 3: Deploy Application

1. **Upload JAR file:**
   
   From your Windows machine:
   ```powershell
   scp build\libs\demo-0.0.1-SNAPSHOT.jar chatapp@your-server-ip:/home/chatapp/
   ```

2. **Create application.properties:**
   ```bash
   nano /home/chatapp/application.properties
   ```
   
   Add:
   ```properties
   server.port=8080
   spring.datasource.url=jdbc:mysql://localhost:3306/chatdb
   spring.datasource.username=chatuser
   spring.datasource.password=StrongPassword123!
   spring.jpa.hibernate.ddl-auto=update
   ```

3. **Test run:**
   ```bash
   java -jar demo-0.0.1-SNAPSHOT.jar --spring.config.location=application.properties
   ```

### Step 4: Create Systemd Service

1. **Create service file:**
   ```bash
   sudo nano /etc/systemd/system/gktamilchat.service
   ```

2. **Add configuration:**
   ```ini
   [Unit]
   Description=GK Tamil Chat Application
   After=network.target mysql.service

   [Service]
   Type=simple
   User=chatapp
   WorkingDirectory=/home/chatapp
   ExecStart=/usr/bin/java -jar /home/chatapp/demo-0.0.1-SNAPSHOT.jar --spring.config.location=/home/chatapp/application.properties
   Restart=always
   RestartSec=10

   [Install]
   WantedBy=multi-user.target
   ```

3. **Enable and start:**
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable gktamilchat
   sudo systemctl start gktamilchat
   sudo systemctl status gktamilchat
   ```

### Step 5: Configure Nginx

1. **Create Nginx config:**
   ```bash
   sudo nano /etc/nginx/sites-available/gktamilchat
   ```

2. **Add configuration:**
   ```nginx
   server {
       listen 80;
       server_name your-domain.com www.your-domain.com;

       client_max_body_size 10M;

       location / {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
           proxy_redirect off;
       }

       # WebSocket support
       location /chat {
           proxy_pass http://localhost:8080/chat;
           proxy_http_version 1.1;
           proxy_set_header Upgrade $http_upgrade;
           proxy_set_header Connection "upgrade";
           proxy_set_header Host $host;
           proxy_cache_bypass $http_upgrade;
       }
   }
   ```

3. **Enable site:**
   ```bash
   sudo ln -s /etc/nginx/sites-available/gktamilchat /etc/nginx/sites-enabled/
   sudo nginx -t
   sudo systemctl restart nginx
   ```

### Step 6: Setup SSL Certificate

1. **Get SSL certificate:**
   ```bash
   sudo certbot --nginx -d your-domain.com -d www.your-domain.com
   ```

2. **Follow prompts:**
   - Enter email address
   - Agree to terms
   - Choose to redirect HTTP to HTTPS

3. **Test auto-renewal:**
   ```bash
   sudo certbot renew --dry-run
   ```

### Step 7: Configure Firewall

```bash
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
sudo ufw status
```

### Step 8: Point Domain to Server

1. **In your domain registrar (Namecheap, etc.):**
   - Go to DNS settings
   - Add A record:
     - Type: A
     - Host: @
     - Value: your-server-ip
     - TTL: Automatic
   - Add A record:
     - Type: A
     - Host: www
     - Value: your-server-ip
     - TTL: Automatic

2. **Wait for DNS propagation (15-30 minutes)**

3. **Test your site:**
   - https://your-domain.com

---

## Troubleshooting

### Railway Deployment Issues

**Build fails:**
```
Check build logs in Railway dashboard
Ensure gradlew has execute permissions
Verify Java version is 21
```

**Application won't start:**
```
Check environment variables are set correctly
Verify DATABASE_URL is properly configured
Check application logs in Railway
```

**Database connection fails:**
```
Ensure MySQL service is running
Verify DATABASE_URL format is correct
Check if application and database are in same project
```

### Server Deployment Issues

**Port 8080 already in use:**
```bash
sudo lsof -i :8080
sudo kill -9 <PID>
```

**Application crashes:**
```bash
sudo journalctl -u gktamilchat -f
```

**MySQL connection refused:**
```bash
sudo systemctl status mysql
sudo systemctl restart mysql
```

**Nginx errors:**
```bash
sudo nginx -t
sudo tail -f /var/log/nginx/error.log
```

---

## Monitoring Your Application

### Railway Dashboard

- View logs: Click on service → "Logs" tab
- Check metrics: CPU, memory, network usage
- View deployments: History of all deployments

### Server Monitoring

**Check application status:**
```bash
sudo systemctl status gktamilchat
```

**View logs:**
```bash
sudo journalctl -u gktamilchat -f
```

**Check resource usage:**
```bash
htop
df -h
free -m
```

---

## Updating Your Application

### Railway (Automatic)

1. Make changes to your code locally
2. Commit and push to GitHub:
   ```powershell
   git add .
   git commit -m "Update description"
   git push
   ```
3. Railway automatically detects changes and redeploys
4. Monitor deployment in Railway dashboard

### Server (Manual)

1. Build new JAR:
   ```powershell
   .\gradlew.bat clean build
   ```

2. Upload to server:
   ```powershell
   scp build\libs\demo-0.0.1-SNAPSHOT.jar chatapp@your-server-ip:/home/chatapp/
   ```

3. Restart service:
   ```bash
   sudo systemctl restart gktamilchat
   ```

---

## Cost Summary

### Railway.app
- Free tier: $5 credit/month (good for small apps)
- Hobby plan: $5/month per service
- Pro plan: $20/month with higher limits

### Render.com
- Free tier: Available (with limitations)
- Starter: $7/month per service

### Self-Hosted VPS
- Server: $5-10/month (DigitalOcean, Linode)
- Domain: $10-15/year
- SSL: Free (Let's Encrypt)
- Total: ~$60-120/year

---

## Security Best Practices

1. **Change default passwords**
2. **Enable HTTPS/SSL** (automatic on Railway)
3. **Keep dependencies updated**
4. **Set strong database passwords**
5. **Enable firewall** (on VPS)
6. **Regular backups** of database
7. **Monitor application logs**
8. **Use environment variables** for secrets

---

## Support Resources

- **Railway Documentation:** https://docs.railway.app
- **Spring Boot Docs:** https://spring.io/guides
- **DigitalOcean Tutorials:** https://www.digitalocean.com/community/tutorials
- **Stack Overflow:** https://stackoverflow.com/questions/tagged/spring-boot

---

## Quick Reference Commands

### Railway CLI (Optional)
```powershell
# Install Railway CLI
npm install -g @railway/cli

# Login
railway login

# Link project
railway link

# View logs
railway logs

# Open dashboard
railway open
```

### Git Commands
```powershell
# Check status
git status

# Add changes
git add .

# Commit changes
git commit -m "Your message"

# Push to GitHub
git push

# Pull latest changes
git pull
```

---

**Congratulations!** Your GK Tamil Chat application is now live on the internet! 🎉

Share your URL with friends and start chatting!
