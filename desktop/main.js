const { app, BrowserWindow, shell, dialog } = require('electron');
const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');
const http = require('http');

const PORT = process.env.LIFEHUB_PORT || '8791';
const APP_URL = `http://127.0.0.1:${PORT}`;
const JAR_NAME = 'to-do-list-1.0.0.jar';
const HEALTH_URL = `${APP_URL}/actuator/health`;

let serverProcess = null;
let mainWindow = null;

function firstExisting(candidates) {
  return candidates.find((p) => p && fs.existsSync(p));
}

function resolveJarPath() {
  const candidates = app.isPackaged
    ? [path.join(process.resourcesPath, 'app', JAR_NAME)]
    : [
        path.join(__dirname, '..', 'target', JAR_NAME),
        path.join(__dirname, 'app', JAR_NAME),
      ];
  return firstExisting(candidates);
}

function resolveJavaCommand() {
  const javaHome = process.env.JAVA_HOME;
  if (javaHome) {
    const exe = path.join(javaHome, 'bin', process.platform === 'win32' ? 'java.exe' : 'java');
    if (fs.existsSync(exe)) return exe;
  }
  return 'java';
}

function startServer(jarPath) {
  const java = resolveJavaCommand();
  const args = [
    '-jar', jarPath,
    '--spring.profiles.active=desktop',
    `--server.port=${PORT}`,
  ];
  serverProcess = spawn(java, args, {
    stdio: ['ignore', 'pipe', 'pipe'],
    windowsHide: true,
  });
  serverProcess.stdout.on('data', (d) => process.stdout.write(`[server] ${d}`));
  serverProcess.stderr.on('data', (d) => process.stderr.write(`[server] ${d}`));
  serverProcess.on('exit', (code) => {
    serverProcess = null;
    if (mainWindow && !mainWindow.isDestroyed()) {
      mainWindow.webContents.send('server-stopped', code);
    }
  });
}

function pingHealth() {
  return new Promise((resolve) => {
    const req = http.get(HEALTH_URL, { timeout: 1500 }, (res) => {
      res.resume();
      resolve(res.statusCode >= 200 && res.statusCode < 400);
    });
    req.on('error', () => resolve(false));
    req.on('timeout', () => {
      req.destroy();
      resolve(false);
    });
  });
}

async function waitForServer(timeoutMs) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    if (await pingHealth()) return true;
    if (!serverProcess) return false;
    await new Promise((r) => setTimeout(r, 600));
  }
  return false;
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1360,
    height: 900,
    minWidth: 960,
    minHeight: 640,
    backgroundColor: '#09090b',
    title: 'LifeHub',
    autoHideMenuBar: true,
    webPreferences: {
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true,
    },
  });

  mainWindow.loadURL(
    'data:text/html;charset=utf-8,' +
      encodeURIComponent(
        '<html><body style="margin:0;font-family:system-ui,Segoe UI,sans-serif;background:#09090b;color:#e4e4e7;display:flex;align-items:center;justify-content:center;height:100vh"><div style="text-align:center"><h2 style="font-weight:600">LifeHub</h2><p style="color:#a1a1aa">Iniciando o servidor local…</p></div></body></html>'
      )
  );

  // Links externos abrem no navegador padrão, nunca dentro do app.
  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    if (url.startsWith('http://127.0.0.1') || url.startsWith('http://localhost')) {
      return { action: 'allow' };
    }
    shell.openExternal(url);
    return { action: 'deny' };
  });
  mainWindow.webContents.on('will-navigate', (event, url) => {
    if (!url.startsWith(APP_URL)) {
      event.preventDefault();
      shell.openExternal(url);
    }
  });

  mainWindow.on('closed', () => {
    mainWindow = null;
  });

  return mainWindow;
}

function stopServer() {
  if (!serverProcess) return;
  try {
    serverProcess.kill();
  } catch (e) {
    // ignore
  }
  serverProcess = null;
}

app.whenReady().then(async () => {
  app.setName('LifeHub');

  const jarPath = resolveJarPath();
  if (!jarPath) {
    dialog.showErrorBox(
      'LifeHub',
      'Arquivo do servidor (JAR) não encontrado.\n\nGere o pacote com "mvnw package -DskipTests" ou use o instalador.'
    );
    app.quit();
    return;
  }

  const win = createWindow();
  startServer(jarPath);

  const ready = await waitForServer(90000);
  if (!ready) {
    dialog.showErrorBox(
      'LifeHub',
      'Não foi possível iniciar o servidor local.\nVerifique se o Java 17+ está instalado (JAVA_HOME) e tente novamente.'
    );
    stopServer();
    app.quit();
    return;
  }

  if (win && !win.isDestroyed()) {
    win.loadURL(APP_URL);
  }

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow().loadURL(APP_URL);
    }
  });
});

app.on('window-all-closed', () => {
  stopServer();
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('before-quit', stopServer);
process.on('exit', stopServer);
