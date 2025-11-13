# Installing yt-dlp on Your Server

This guide explains how to install yt-dlp for Bloomee-style music integration.

## ✅ Already Configured in Dockerfile

If you're using Docker (which you are), yt-dlp is already configured in the `Dockerfile`. Just rebuild your Docker image:

```bash
docker build -t your-app-name .
```

## Manual Installation Methods

### Method 1: Using Docker (Recommended - Already Done)

The Dockerfile has been updated to automatically install yt-dlp. No manual steps needed!

### Method 2: Install on Linux Server (if not using Docker)

#### For Ubuntu/Debian:
```bash
# Install Python and pip
sudo apt update
sudo apt install -y python3 python3-pip ffmpeg

# Install yt-dlp
sudo pip3 install yt-dlp

# Verify installation
yt-dlp --version
```

#### For Alpine Linux (if running directly):
```bash
# Install Python and pip
apk add --no-cache python3 py3-pip ffmpeg

# Install yt-dlp
pip3 install yt-dlp

# Verify installation
yt-dlp --version
```

#### For CentOS/RHEL:
```bash
# Install Python and pip
sudo yum install -y python3 python3-pip ffmpeg

# Install yt-dlp
sudo pip3 install yt-dlp

# Verify installation
yt-dlp --version
```

### Method 3: Install on Railway (if not using Docker)

If Railway is building your app differently, add a `railway.json` or use Railway's build hooks:

1. Go to your Railway project settings
2. Add a build command:
   ```bash
   apk add --no-cache python3 py3-pip ffmpeg && pip3 install yt-dlp
   ```

Or create a `railway.json`:
```json
{
  "build": {
    "builder": "DOCKERFILE",
    "dockerfilePath": "Dockerfile"
  }
}
```

### Method 4: Using Standalone Binary (No Python Required)

Download the standalone binary:

```bash
# For Linux
wget https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -O /usr/local/bin/yt-dlp
chmod a+rx /usr/local/bin/yt-dlp

# Verify
yt-dlp --version
```

## Verify Installation

After installation, test yt-dlp:

```bash
# Check version
yt-dlp --version

# Test search (should return video info)
yt-dlp --get-title --get-id "ytsearch1:test song"
```

## Testing in Your Application

Once installed, test the music search:

1. Log in as an Admin or Super Admin
2. Open the FM Player
3. Search for a song
4. Check server logs for yt-dlp output

## Troubleshooting

### Issue: "yt-dlp: command not found"
**Solution:** Make sure yt-dlp is in your PATH or use full path: `/usr/local/bin/yt-dlp`

### Issue: "ffmpeg not found"
**Solution:** Install ffmpeg:
```bash
# Alpine
apk add ffmpeg

# Ubuntu/Debian
apt install ffmpeg

# CentOS/RHEL
yum install ffmpeg
```

### Issue: Permission denied
**Solution:** Make sure yt-dlp is executable:
```bash
chmod +x /usr/local/bin/yt-dlp
```

### Issue: Python not found
**Solution:** Install Python 3:
```bash
# Alpine
apk add python3 py3-pip

# Ubuntu/Debian
apt install python3 python3-pip
```

## Railway-Specific Notes

If deploying on Railway:

1. **Using Dockerfile (Recommended):** The Dockerfile already includes yt-dlp installation
2. **Using Nixpacks:** Add to `nixpacks.toml`:
   ```toml
   [phases.setup]
   nixPkgs = ["python3", "ffmpeg"]
   
   [phases.install]
   cmds = ["pip3 install yt-dlp"]
   ```
3. **Using Buildpacks:** Add a build script that installs yt-dlp

## Updating yt-dlp

Keep yt-dlp updated for best compatibility:

```bash
# Update via pip
pip3 install --upgrade yt-dlp

# Or if using standalone binary
yt-dlp -U
```

## Docker Build Command

To rebuild your Docker image with yt-dlp:

```bash
docker build -t your-app-name .
docker run -p 8080:8080 your-app-name
```

## Verification in Code

The `YouTubeMusicService` automatically detects if yt-dlp is available. Check your application logs when searching for music - it will show whether yt-dlp is being used or if it's falling back to basic search.

