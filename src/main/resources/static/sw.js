// Service Worker do LifeHub.
// Estratégia: network-first para conteúdo estático (atualizações aparecem na hora),
// com fallback para o cache quando o servidor estiver offline.
const CACHE_NAME = 'lifehub-cache-v2';
const STATIC_ASSETS = [
  '/',
  '/index.html',
  '/css/style.css',
  '/js/app.js',
  '/js/chart.umd.min.js',
  '/favicon.svg',
  '/manifest.json'
];

const SKIP_PATTERNS = ['/api/', '/swagger-ui', '/v3/api-docs', '/api-docs', '/h2-console', '/actuator'];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(STATIC_ASSETS))
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key))
      );
    })
  );
  self.clients.claim();
});

const shouldSkip = (url) => SKIP_PATTERNS.some((p) => url.includes(p));

self.addEventListener('fetch', (event) => {
  const request = event.request;
  if (shouldSkip(request.url) || request.method !== 'GET') {
    return;
  }

  // Navegações (abrir/renovar a UI) preferem sempre a versão nova do servidor.
  if (request.mode === 'navigate') {
    event.respondWith(
      fetch(request)
        .then((response) => {
          const copy = response.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put('/index.html', copy));
          return response;
        })
        .catch(() =>
          caches.match('/index.html').then((cached) => cached || caches.match('/'))
        )
    );
    return;
  }

  event.respondWith(
    fetch(request)
      .then((response) => {
        if (response && response.status === 200 && response.type === 'basic') {
          const copy = response.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put(request, copy));
        }
        return response;
      })
      .catch(() => caches.match(request))
  );
});
