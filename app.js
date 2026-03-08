const express = require('express');
const os = require('os');
const { execSync } = require('child_process');

const app = express();
const PORT = process.env.PORT || 3000;
const START_TIME = Date.now();

app.use(express.json());

// ─────────────────────────────────────────────
//  HELPER FUNCTIONS
// ─────────────────────────────────────────────

function uptimeSeconds() {
  return Math.floor((Date.now() - START_TIME) / 1000);
}

function formatUptime(seconds) {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = seconds % 60;
  return `${h}h ${m}m ${s}s`;
}

function getMemoryInfo() {
  const total = os.totalmem();
  const free = os.freemem();
  const used = total - free;
  return {
    total_mb: (total / 1024 / 1024).toFixed(2),
    used_mb: (used / 1024 / 1024).toFixed(2),
    free_mb: (free / 1024 / 1024).toFixed(2),
    usage_percent: ((used / total) * 100).toFixed(2),
  };
}

function getCpuInfo() {
  const cpus = os.cpus();
  return {
    count: cpus.length,
    model: cpus[0]?.model || 'unknown',
    load_avg: os.loadavg().map(v => v.toFixed(2)),
  };
}

function isRunningInDocker() {
  try {
    const cgroup = require('fs').readFileSync('/proc/1/cgroup', 'utf8');
    return cgroup.includes('docker') || cgroup.includes('kubepods');
  } catch {
    return false;
  }
}

function getHostname() {
  return os.hostname();
}

// ─────────────────────────────────────────────
//  ROUTES
// ─────────────────────────────────────────────

/**
 * GET /
 * Root info
 */
app.get('/', (req, res) => {
  res.json({
    service: 'Docker/K8s Health Check API',
    version: '1.0.0',
    status: 'running',
    endpoints: [
      'GET  /health          → Basic liveness check',
      'GET  /health/live     → Kubernetes liveness probe',
      'GET  /health/ready    → Kubernetes readiness probe',
      'GET  /health/startup  → Kubernetes startup probe',
      'GET  /metrics         → App metrics (Prometheus-style)',
      'GET  /info            → Node/system info',
      'GET  /env             → Safe env variables',
    ],
  });
});

/**
 * GET /health
 * Basic health check — used by Docker HEALTHCHECK
 * Returns 200 if alive, 503 if unhealthy
 */
app.get('/health', (req, res) => {
  const uptime = uptimeSeconds();

  // Simple liveness rule: if uptime > 0 and memory OK → healthy
  const mem = getMemoryInfo();
  const memUsagePct = parseFloat(mem.usage_percent);
  const healthy = uptime > 0 && memUsagePct < 95;

  res.status(healthy ? 200 : 503).json({
    status: healthy ? 'healthy' : 'unhealthy',
    uptime_seconds: uptime,
    uptime_human: formatUptime(uptime),
    timestamp: new Date().toISOString(),
    hostname: getHostname(),
    in_docker: isRunningInDocker(),
  });
});

/**
 * GET /health/live
 * Kubernetes Liveness Probe
 * → "Is the container still alive?"
 * → If this fails, K8s RESTARTS the pod
 */
app.get('/health/live', (req, res) => {
  // A real app would check: DB connection, critical services, etc.
  const alive = true; // Replace with actual checks

  res.status(alive ? 200 : 503).json({
    probe: 'liveness',
    status: alive ? 'alive' : 'dead',
    message: alive
      ? 'Pod is alive and running'
      : 'Pod is unresponsive — Kubernetes will restart it',
    timestamp: new Date().toISOString(),
    pid: process.pid,
    uptime_seconds: uptimeSeconds(),
  });
});

/**
 * GET /health/ready
 * Kubernetes Readiness Probe
 * → "Is the container ready to accept traffic?"
 * → If this fails, K8s STOPS sending traffic to the pod (but doesn't restart)
 */
app.get('/health/ready', (req, res) => {
  const uptime = uptimeSeconds();

  // Ready only after 3 seconds (simulating startup warmup)
  const ready = uptime >= 3;

  res.status(ready ? 200 : 503).json({
    probe: 'readiness',
    status: ready ? 'ready' : 'not_ready',
    message: ready
      ? 'Pod is ready to serve traffic'
      : 'Pod is warming up — not yet ready for traffic',
    uptime_seconds: uptime,
    timestamp: new Date().toISOString(),
  });
});

/**
 * GET /health/startup
 * Kubernetes Startup Probe
 * → "Has the container finished starting up?"
 * → Disables liveness/readiness probes until this passes
 */
app.get('/health/startup', (req, res) => {
  const uptime = uptimeSeconds();

  // Startup complete after 5 seconds
  const started = uptime >= 5;

  res.status(started ? 200 : 503).json({
    probe: 'startup',
    status: started ? 'started' : 'starting',
    message: started
      ? 'Application has fully started'
      : 'Application is still initializing',
    uptime_seconds: uptime,
    timestamp: new Date().toISOString(),
  });
});

/**
 * GET /metrics
 * Prometheus-compatible plain-text metrics
 * Used by Prometheus scraper
 */
app.get('/metrics', (req, res) => {
  const mem = getMemoryInfo();
  const cpu = getCpuInfo();
  const uptime = uptimeSeconds();

  // Prometheus text format
  const metrics = `
# HELP app_uptime_seconds Total uptime of the application in seconds
# TYPE app_uptime_seconds counter
app_uptime_seconds ${uptime}

# HELP app_memory_used_mb Memory used by the host in MB
# TYPE app_memory_used_mb gauge
app_memory_used_mb ${mem.used_mb}

# HELP app_memory_free_mb Free memory on the host in MB
# TYPE app_memory_free_mb gauge
app_memory_free_mb ${mem.free_mb}

# HELP app_memory_usage_percent Memory usage percentage
# TYPE app_memory_usage_percent gauge
app_memory_usage_percent ${mem.usage_percent}

# HELP app_cpu_count Number of CPUs
# TYPE app_cpu_count gauge
app_cpu_count ${cpu.count}

# HELP app_cpu_load_avg_1m CPU load average (1 minute)
# TYPE app_cpu_load_avg_1m gauge
app_cpu_load_avg_1m ${cpu.load_avg[0]}

# HELP app_cpu_load_avg_5m CPU load average (5 minutes)
# TYPE app_cpu_load_avg_5m gauge
app_cpu_load_avg_5m ${cpu.load_avg[1]}

# HELP app_node_version Node.js version info
# TYPE app_node_version gauge
app_node_version{version="${process.version}"} 1
`.trim();

  res.setHeader('Content-Type', 'text/plain; version=0.0.4');
  res.send(metrics);
});

/**
 * GET /info
 * Detailed system + runtime info
 */
app.get('/info', (req, res) => {
  res.json({
    app: {
      name: 'healthcheck-api',
      version: '1.0.0',
      node_version: process.version,
      pid: process.pid,
      uptime_seconds: uptimeSeconds(),
      uptime_human: formatUptime(uptimeSeconds()),
      start_time: new Date(START_TIME).toISOString(),
    },
    host: {
      hostname: getHostname(),
      platform: os.platform(),
      arch: os.arch(),
      os_type: os.type(),
      os_release: os.release(),
      in_docker: isRunningInDocker(),
    },
    cpu: getCpuInfo(),
    memory: getMemoryInfo(),
    network: Object.entries(os.networkInterfaces())
      .map(([name, ifaces]) => ({
        interface: name,
        addresses: ifaces
          .filter(i => !i.internal)
          .map(i => ({ family: i.family, address: i.address })),
      }))
      .filter(n => n.addresses.length > 0),
    timestamp: new Date().toISOString(),
  });
});

/**
 * GET /env
 * Returns safe (non-secret) environment variables
 */
app.get('/env', (req, res) => {
  const safeKeys = ['NODE_ENV', 'PORT', 'HOSTNAME', 'POD_NAME', 'POD_NAMESPACE', 'POD_IP'];
  const safeEnv = {};
  for (const key of safeKeys) {
    safeEnv[key] = process.env[key] || 'not set';
  }
  res.json({
    environment: safeEnv,
    note: 'Only safe, non-secret variables are exposed here.',
    timestamp: new Date().toISOString(),
  });
});

// ─────────────────────────────────────────────
//  404 HANDLER
// ─────────────────────────────────────────────
app.use((req, res) => {
  res.status(404).json({
    error: 'Route not found',
    path: req.originalUrl,
    available_routes: ['/', '/health', '/health/live', '/health/ready', '/health/startup', '/metrics', '/info', '/env'],
  });
});

// ─────────────────────────────────────────────
//  START SERVER
// ─────────────────────────────────────────────
app.listen(PORT, () => {
  console.log(`✅ Health Check API running on port ${PORT}`);
  console.log(`🐳 Docker: ${isRunningInDocker() ? 'YES' : 'NO'}`);
  console.log(`🖥  Hostname: ${getHostname()}`);
  console.log(`📦 Node: ${process.version}`);
});

module.exports = app;