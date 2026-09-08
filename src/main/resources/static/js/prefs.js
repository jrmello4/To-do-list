/* ==========================================================================
   Preferências pessoais (nome, cor de destaque e tema).

   Carregado de forma síncrona no <head>: o tema e a cor precisam ser
   aplicados antes da primeira pintura, senão a página pisca no tema errado.
   Guardadas em localStorage — ficam só neste navegador, nada vai para a API.
   ========================================================================== */

window.Prefs = (function () {
    'use strict';

    var CHAVE = 'todolist:prefs';

    var PALETA = [
        { id: 'indigo',  nome: 'Índigo',   h: 245, s: 72 },
        { id: 'violeta', nome: 'Violeta',  h: 276, s: 66 },
        { id: 'azul',    nome: 'Azul',     h: 208, s: 82 },
        { id: 'verde',   nome: 'Verde',    h: 152, s: 62 },
        { id: 'ambar',   nome: 'Âmbar',    h: 34,  s: 88 },
        { id: 'rosa',    nome: 'Rosa',     h: 335, s: 75 }
    ];

    var PADRAO = { nome: '', cor: 'indigo', tema: 'system' };

    // localStorage lança exceção em modo privado e com cookies bloqueados.
    function ler() {
        try {
            var bruto = window.localStorage.getItem(CHAVE);
            if (!bruto) {
                return copiar(PADRAO);
            }

            var salvo = JSON.parse(bruto);
            return {
                nome: typeof salvo.nome === 'string' ? salvo.nome.slice(0, 40) : PADRAO.nome,
                cor: corValida(salvo.cor) ? salvo.cor : PADRAO.cor,
                tema: temaValido(salvo.tema) ? salvo.tema : PADRAO.tema
            };
        } catch (e) {
            return copiar(PADRAO);
        }
    }

    function gravar(prefs) {
        try {
            window.localStorage.setItem(CHAVE, JSON.stringify(prefs));
        } catch (e) {
            // Sem persistência disponível: as preferências valem só nesta sessão.
        }
    }

    function copiar(o) {
        return { nome: o.nome, cor: o.cor, tema: o.tema };
    }

    function corValida(id) {
        return PALETA.some(function (c) { return c.id === id; });
    }

    function temaValido(t) {
        return t === 'light' || t === 'dark' || t === 'system';
    }

    function buscarCor(id) {
        return PALETA.filter(function (c) { return c.id === id; })[0] || PALETA[0];
    }

    var estado = ler();
    var consultaEscuro = window.matchMedia ? window.matchMedia('(prefers-color-scheme: dark)') : null;

    /** Resolve "system" para o tema real em uso neste momento. */
    function temaEfetivo() {
        if (estado.tema === 'system') {
            return consultaEscuro && consultaEscuro.matches ? 'dark' : 'light';
        }
        return estado.tema;
    }

    function aplicar() {
        var raiz = document.documentElement;
        var cor = buscarCor(estado.cor);

        raiz.setAttribute('data-theme', temaEfetivo());
        raiz.setAttribute('data-theme-pref', estado.tema);
        raiz.style.setProperty('--accent-h', cor.h);
        raiz.style.setProperty('--accent-s', cor.s + '%');
    }

    aplicar();

    // Com o tema em "system", acompanha a mudança na preferência do SO.
    if (consultaEscuro && consultaEscuro.addEventListener) {
        consultaEscuro.addEventListener('change', function () {
            if (estado.tema === 'system') {
                aplicar();
            }
        });
    }

    return {
        PALETA: PALETA,

        obter: function () {
            return copiar(estado);
        },

        temaEfetivo: temaEfetivo,

        definir: function (parcial) {
            if (typeof parcial.nome === 'string') {
                estado.nome = parcial.nome.trim().slice(0, 40);
            }
            if (corValida(parcial.cor)) {
                estado.cor = parcial.cor;
            }
            if (temaValido(parcial.tema)) {
                estado.tema = parcial.tema;
            }

            aplicar();
            gravar(estado);
            return copiar(estado);
        }
    };
})();
