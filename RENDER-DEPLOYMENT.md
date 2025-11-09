# GK Tamil Chat - Render Deployment Guide

## Overview
This guide will help you deploy your GK Tamil Chat application to Render, a modern cloud platform with excellent support for Spring Boot applications.

## Prerequisites
1. GitHub account with your code repository
2. Render account (free tier available)
3. Your application code pushed to GitHub

## Step-by-Step Deployment

### 1. Create Render Account
1. Go to [render.com](https://render.com)
2. Sign up with your GitHub account
3. Authorize Render to access your repositories

### 2. Create Database Service (Option A: PostgreSQL - Recommended)
1. In Render Dashboard, click "New +"
2. Select "PostgreSQL"
3. Configure:
   - **Name**: `gk-tamil-chat-db`
   - **Database Name**: `gktamil_chat`
   - **User**: `gktamil_user`
   - **Region**: Choose closest to your users
   - **Plan**: Free (for testing) or Starter
4. Click "Create Database"
5. **Save the connection details** (you'll need them)

### 3. Create Database Service (Option B: MySQL via Docker)
If you prefer MySQL, you can use the included Dockerfile.mysql:
1. Create a "Private Service" in Render
2. Use the Docker environment
3. Point to your repository and Dockerfile.mysql

### 4. Deploy Web Service
1. In Render Dashboard, click "New +"
2. Select "Web Service"
3. Connect your GitHub repository (`gk-tamil-chat`)
4. Configure:
   - **Name**: `gk-tamil-chat`
   - **Environment**: `Java`
   - **Region**: Same as your database
   - **Branch**: `main`
   - **Root Directory**: Leave blank (if at repo root)
   - **Build Command**: `./gradlew build -x test`
   - **Start Command**: `java -Dserver.port=$PORT -jar build/libs/demo-0.0.1-SNAPSHOT.jar`

### 5. Configure Environment Variables
In your web service settings, add these environment variables:

#### Required Variables:
```bash
SPRING_PROFILES_ACTIVE=render
DATABASE_URL=[Your database connection string from step 2]
DB_USERNAME=[Your database username]
DB_PASSWORD=[Your database password]
```

#### Optional Optimizations:
```bash
JAVA_TOOL_OPTIONS=-Xmx512m -Xms256m
TZ=Asia/Kolkata
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

#### For PostgreSQL (if using Option A):
```bash
# Example DATABASE_URL format:
# postgresql://username:password@hostname:port/database_name
```

#### For MySQL (if using Option B):
```bash
# Example DATABASE_URL format:
# mysql://username:password@hostname:port/database_name
```

### 6. Deploy
1. Click "Create Web Service"
2. Render will automatically build and deploy your application
3. Monitor the build logs for any issues
4. Once deployed, you'll get a URL like: `https://gk-tamil-chat.onrender.com`

## Database Configuration

### Using PostgreSQL (Recommended)
If using PostgreSQL, update your `application-render.properties`:
```properties
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

And add PostgreSQL dependency to `build.gradle`:
```gradle
implementation 'org.postgresql:postgresql'
```

### Using MySQL
Keep the current MySQL configuration in `application-render.properties`.

## File Storage
For file uploads, Render provides ephemeral storage. For production, consider:
1. AWS S3
2. Cloudinary
3. Render's persistent disks (paid plans)

## Custom Domain (Optional)
1. In your web service settings
2. Go to "Settings" → "Custom Domains"
3. Add your domain and configure DNS

## Monitoring & Logs
1. View real-time logs in Render dashboard
2. Set up log retention (paid plans)
3. Configure health checks at `/actuator/health`

## Cost Optimization
- **Free Tier**: Includes web service + PostgreSQL database
- **Sleep Mode**: Free services sleep after 15min inactivity
- **Upgrade**: Consider paid plans for production use

## Troubleshooting

### Common Issues:
1. **Build Fails**: Check Java version compatibility
2. **Database Connection**: Verify DATABASE_URL format
3. **Memory Issues**: Adjust JAVA_TOOL_OPTIONS
4. **SSL Issues**: Ensure database SSL configuration

### Health Checks:
Your app will be available at: `https://your-app-name.onrender.com`
Health check endpoint: `https://your-app-name.onrender.com/actuator/health`

## Advantages of Render vs Railway
- **Reliability**: Better uptime and stability
- **Database**: Managed PostgreSQL included in free tier
- **SSL**: Automatic HTTPS certificates
- **Build System**: More reliable build process
- **Support**: Better documentation and support

## Next Steps After Deployment
1. Test all functionality on Render URL
2. Configure custom domain if needed
3. Set up monitoring and alerts
4. Consider upgrading to paid plan for production
5. Set up CI/CD for automatic deployments

## Support
- Render Documentation: [render.com/docs](https://render.com/docs)
- Community: [community.render.com](https://community.render.com)
- GitHub Issues: Use your repository issues for app-specific problems