# Quick Deployment Checklist

## ✅ Pre-Deployment Checklist

- [ ] Application builds successfully locally (`.\gradlew.bat clean build`)
- [ ] Application runs locally without errors
- [ ] All features tested (login, register, chat, profile)
- [ ] Git installed on your computer
- [ ] GitHub account created
- [ ] Code pushed to GitHub repository

---

## 🚀 Railway.app Deployment (Easiest - 15 minutes)

### Account Setup
- [ ] Sign up at https://railway.app with GitHub
- [ ] Authorize Railway to access your repositories

### Database Setup
- [ ] Create new project in Railway
- [ ] Deploy from GitHub repository
- [ ] Add MySQL database to project
- [ ] Wait for database to provision

### Application Configuration
- [ ] Add environment variable: `SPRING_PROFILES_ACTIVE` = `prod`
- [ ] Add environment variable: `DATABASE_URL` (reference from MySQL)
- [ ] Redeploy application
- [ ] Wait for build to complete (3-5 minutes)

### Domain Setup
- [ ] Generate Railway domain in Settings → Networking
- [ ] Copy the URL (e.g., `your-app.up.railway.app`)
- [ ] Test the URL in browser
- [ ] Verify login works
- [ ] Verify chat works
- [ ] Verify profile upload works

### Optional: Custom Domain
- [ ] Buy domain from Namecheap/GoDaddy
- [ ] Add custom domain in Railway
- [ ] Update DNS records in domain registrar
- [ ] Wait for DNS propagation (15-30 min)
- [ ] Verify SSL certificate is active

---

## 🌐 Alternative: Render.com Deployment

### Account Setup
- [ ] Sign up at https://render.com with GitHub
- [ ] Authorize Render to access repositories

### Service Setup
- [ ] Create new Web Service
- [ ] Select your GitHub repository
- [ ] Set build command: `./gradlew build`
- [ ] Set start command: `java -jar build/libs/demo-0.0.1-SNAPSHOT.jar`
- [ ] Select Free instance type

### Database Setup
- [ ] Create PostgreSQL database (or use external MySQL)
- [ ] Copy database connection URL
- [ ] Add as environment variable in Web Service

### Deploy
- [ ] Click "Create Web Service"
- [ ] Wait for deployment (5-10 minutes)
- [ ] Test your application URL
- [ ] Verify all features work

---

## 🖥️ VPS Deployment (Advanced - 1-2 hours)

### Server Setup
- [ ] Purchase VPS (DigitalOcean/Linode/Vultr)
- [ ] Choose Ubuntu 22.04 LTS
- [ ] Note server IP address
- [ ] Connect via SSH

### Software Installation
- [ ] Update system: `apt update && apt upgrade -y`
- [ ] Install Java 21: `apt install openjdk-21-jdk -y`
- [ ] Install MySQL: `apt install mysql-server -y`
- [ ] Install Nginx: `apt install nginx -y`
- [ ] Install Certbot: `apt install certbot python3-certbot-nginx -y`

### Database Configuration
- [ ] Run MySQL secure installation
- [ ] Create database: `chatdb`
- [ ] Create user: `chatuser`
- [ ] Grant privileges
- [ ] Test connection

### Application Deployment
- [ ] Create application user account
- [ ] Upload JAR file via SCP
- [ ] Create application.properties file
- [ ] Test run application
- [ ] Verify it starts without errors

### System Service
- [ ] Create systemd service file
- [ ] Enable service
- [ ] Start service
- [ ] Verify service is running
- [ ] Check logs for errors

### Web Server Configuration
- [ ] Create Nginx configuration
- [ ] Enable site
- [ ] Test Nginx configuration
- [ ] Restart Nginx
- [ ] Test HTTP access

### SSL Certificate
- [ ] Run Certbot for domain
- [ ] Verify SSL certificate installed
- [ ] Test HTTPS access
- [ ] Enable auto-renewal

### Firewall
- [ ] Allow SSH (port 22)
- [ ] Allow HTTP (port 80)
- [ ] Allow HTTPS (port 443)
- [ ] Enable firewall
- [ ] Verify rules

### Domain Configuration
- [ ] Point domain A record to server IP
- [ ] Add www subdomain
- [ ] Wait for DNS propagation
- [ ] Test domain access

---

## 📧 Email Configuration (Optional)

### Gmail Setup
- [ ] Enable 2-Step Verification in Google Account
- [ ] Generate App Password
- [ ] Note the 16-character password

### Application Configuration
- [ ] Add `SPRING_MAIL_HOST` variable
- [ ] Add `SPRING_MAIL_PORT` variable
- [ ] Add `SPRING_MAIL_USERNAME` variable
- [ ] Add `SPRING_MAIL_PASSWORD` variable (App Password)
- [ ] Enable SMTP auth variables
- [ ] Redeploy application
- [ ] Test forgot password feature

---

## 🧪 Post-Deployment Testing

### Functionality Tests
- [ ] Open the deployed URL
- [ ] Register a new user
- [ ] Verify email validation works
- [ ] Login with registered user
- [ ] Send chat messages
- [ ] Verify messages appear for other users
- [ ] Upload profile picture
- [ ] Update status and description
- [ ] Test forgot password (if configured)
- [ ] Logout and login again
- [ ] Test as guest user
- [ ] Check online/offline user status

### Performance Tests
- [ ] Check page load speed
- [ ] Test with multiple users simultaneously
- [ ] Verify WebSocket connections stable
- [ ] Check image upload speed
- [ ] Monitor database response time

### Mobile Tests
- [ ] Open on mobile browser
- [ ] Test responsive design
- [ ] Verify chat works on mobile
- [ ] Test profile upload on mobile
- [ ] Check all buttons are clickable

---

## 🔒 Security Checklist

- [ ] HTTPS/SSL enabled
- [ ] Strong database passwords set
- [ ] Session timeout configured (30 minutes)
- [ ] HTTP-only cookies enabled
- [ ] No sensitive data in logs
- [ ] CORS configured properly
- [ ] Input validation working
- [ ] SQL injection protection (JPA)
- [ ] XSS protection in place
- [ ] File upload size limits set (5MB)

---

## 📊 Monitoring Setup

### Railway/Render
- [ ] Enable email notifications for deploys
- [ ] Check metrics dashboard regularly
- [ ] Monitor error logs
- [ ] Set up uptime monitoring (UptimeRobot)

### VPS
- [ ] Install monitoring tools (htop, netdata)
- [ ] Set up log rotation
- [ ] Configure backup schedule
- [ ] Monitor disk space
- [ ] Monitor CPU/memory usage
- [ ] Set up automatic updates

---

## 🆘 Troubleshooting Reference

### Build Fails
- [ ] Check Java version is 21
- [ ] Verify gradlew has correct permissions
- [ ] Check build logs for errors
- [ ] Ensure all dependencies download

### Application Won't Start
- [ ] Verify database connection string
- [ ] Check environment variables are set
- [ ] Review application logs
- [ ] Ensure port is available
- [ ] Check database is running

### Database Connection Issues
- [ ] Verify database service is running
- [ ] Check connection URL format
- [ ] Verify username/password
- [ ] Test database connectivity
- [ ] Check firewall rules

### WebSocket Not Working
- [ ] Verify WebSocket proxy in Nginx
- [ ] Check CORS configuration
- [ ] Ensure /chat endpoint accessible
- [ ] Test with different browser
- [ ] Check firewall allows WebSocket

### Images Not Uploading
- [ ] Check file size limit (5MB)
- [ ] Verify LONGTEXT column type
- [ ] Check server disk space
- [ ] Test with smaller image
- [ ] Review server logs

---

## 📝 Maintenance Schedule

### Daily
- [ ] Check application is accessible
- [ ] Review error logs
- [ ] Monitor user reports

### Weekly
- [ ] Check disk space usage
- [ ] Review database size
- [ ] Monitor performance metrics
- [ ] Check SSL certificate status

### Monthly
- [ ] Update dependencies (security patches)
- [ ] Backup database
- [ ] Review and archive old logs
- [ ] Check server updates available
- [ ] Review user growth

### Quarterly
- [ ] Performance optimization review
- [ ] Security audit
- [ ] Update documentation
- [ ] Review and update pricing tier

---

## 📞 Support Resources

- Railway Docs: https://docs.railway.app
- Render Docs: https://render.com/docs
- Spring Boot: https://spring.io/guides
- DigitalOcean: https://www.digitalocean.com/community
- Stack Overflow: https://stackoverflow.com

---

## 🎉 Success Criteria

Your deployment is successful when:

✅ Application is accessible via public URL
✅ Users can register and login
✅ Chat messages send and receive in real-time
✅ Profile pictures upload and display
✅ Users list shows online/offline status
✅ Application is stable for 24+ hours
✅ HTTPS/SSL is working
✅ Mobile devices can access and use the app
✅ No errors in application logs
✅ Database is persisting data correctly

---

**Current Date:** November 7, 2025
**Application:** GK Tamil Chat
**Version:** 1.0

Good luck with your deployment! 🚀
