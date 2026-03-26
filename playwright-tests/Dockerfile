# Playwright official image ships with all browser dependencies pre-installed.
# Using a pinned version for reproducibility.
FROM mcr.microsoft.com/playwright:v1.44.1-jammy

WORKDIR /app

# Copy package files first to leverage Docker layer cache
COPY package.json package-lock.json ./

# Install Node dependencies (devDependencies included — we need Playwright types)
RUN npm ci

# Copy the rest of the test suite
COPY . .

# Default: keep the container alive so Jenkins can exec into it
CMD ["tail", "-f", "/dev/null"]
