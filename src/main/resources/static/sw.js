/* ==========================================================================
   Service worker: deixa o aplicativo instalável e abrível sem rede.

   Estratégia: rede primeiro, cache como reserva.

   Cache primeiro seria mais rápido, mas serviria a versão antiga da interface
   depois de cada deploy até alguém limpar o cache — e uma interface velha
   conversando com uma API nova quebra de formas difíceis de diagnosticar.
   ========================================================================== */

var CACHE = 'todolist-v1';

var ESSENCIAIS = [
    '/',
    '/index.html',
    '/css/style.css',
    '/js/prefs.js',
    '/js/app.js',
    '/favicon.svg',
    '/manifest.webmanifest'
];

self.addEventListener('install', function (evento) {
    evento.waitUntil(
        caches.open(CACHE).then(function (cache) {
            return cache.addAll(ESSENCIAIS);
        }).then(function () {
            return self.skipWaiting();
        })
    );
});

self.addEventListener('activate', function (evento) {
    evento.waitUntil(
        caches.keys().then(function (nomes) {
            return Promise.all(nomes
                    .filter(function (nome) { return nome !== CACHE; })
                    .map(function (nome) { return caches.delete(nome); }));
        }).then(function () {
            return self.clients.claim();
        })
    );
});

self.addEventListener('fetch', function (evento) {
    var requisicao = evento.request;

    // Só GET, e só o que é desta origem.
    if (requisicao.method !== 'GET' || new URL(requisicao.url).origin !== self.location.origin) {
        return;
    }

    // Chamadas de API nunca vêm do cache: uma lista de tarefas velha
    // apresentada como atual é pior do que um erro de rede honesto.
    if (new URL(requisicao.url).pathname.indexOf('/api/') === 0) {
        return;
    }

    evento.respondWith(
        fetch(requisicao)
            .then(function (resposta) {
                if (resposta && resposta.ok) {
                    var copia = resposta.clone();
                    caches.open(CACHE).then(function (cache) {
                        cache.put(requisicao, copia);
                    });
                }
                return resposta;
            })
            .catch(function () {
                return caches.match(requisicao).then(function (guardada) {
                    return guardada || caches.match('/index.html');
                });
            })
    );
});
