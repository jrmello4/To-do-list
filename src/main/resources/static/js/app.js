/* ==========================================================================
   To-do List — cliente da API REST (/api/tarefas)
   Sem dependências externas: funciona offline, servido pelo próprio Spring Boot.
   ========================================================================== */

(function () {
    'use strict';

    var API = '/api/tarefas';

    /** Estado da aplicação em memória. */
    var state = {
        tarefas: [],
        filtro: 'todas',
        percentualAnterior: null
    };

    var el = {
        form: document.getElementById('task-form'),
        titulo: document.getElementById('titulo'),
        tituloError: document.getElementById('titulo-error'),
        descricao: document.getElementById('descricao'),
        descricaoCounter: document.getElementById('descricao-counter'),
        submitBtn: document.getElementById('submit-btn'),
        list: document.getElementById('task-list'),
        template: document.getElementById('task-template'),
        filtersNav: document.getElementById('filters'),
        filters: document.querySelectorAll('.filters__item'),
        loading: document.getElementById('state-loading'),
        empty: document.getElementById('state-empty'),
        emptyTitle: document.getElementById('empty-title'),
        emptyText: document.getElementById('empty-text'),
        error: document.getElementById('state-error'),
        errorText: document.getElementById('error-text'),
        retryBtn: document.getElementById('retry-btn'),
        greeting: document.getElementById('greeting'),
        today: document.getElementById('today'),
        avatar: document.getElementById('avatar'),
        summary: document.getElementById('summary'),
        ring: document.getElementById('ring'),
        ringValue: document.getElementById('ring-value'),
        ringLabel: document.getElementById('ring-label'),
        statTotal: document.getElementById('stat-total'),
        statPending: document.getElementById('stat-pending'),
        statDone: document.getElementById('stat-done'),
        toasts: document.getElementById('toasts'),
        confetti: document.getElementById('confetti'),
        modal: document.getElementById('edit-modal'),
        editForm: document.getElementById('edit-form'),
        editId: document.getElementById('edit-id'),
        editTitulo: document.getElementById('edit-titulo'),
        editTituloError: document.getElementById('edit-titulo-error'),
        editDescricao: document.getElementById('edit-descricao'),
        editConcluida: document.getElementById('edit-concluida'),
        settingsBtn: document.getElementById('settings-btn'),
        settingsModal: document.getElementById('settings-modal'),
        settingsForm: document.getElementById('settings-form'),
        prefNome: document.getElementById('pref-nome'),
        swatches: document.getElementById('swatches'),
        themeChoice: document.getElementById('theme-choice'),
        themeBtn: document.getElementById('theme-btn')
    };

    /* ---------------------------------------------------------------- HTTP */

    /**
     * Envolve o fetch tratando o corpo de erro padronizado pela API
     * (ErrorResponse: status, mensagem, timestamp, erros).
     */
    function request(url, options) {
        return fetch(url, options).then(function (resposta) {
            if (resposta.status === 204) {
                return null;
            }

            return resposta.text().then(function (texto) {
                var corpo = null;

                if (texto) {
                    try {
                        corpo = JSON.parse(texto);
                    } catch (e) {
                        corpo = null;
                    }
                }

                if (!resposta.ok) {
                    throw new Error(mensagemDeErro(corpo, resposta.status));
                }

                return corpo;
            });
        });
    }

    function mensagemDeErro(corpo, status) {
        if (corpo && Array.isArray(corpo.erros) && corpo.erros.length > 0) {
            return corpo.erros.join(' · ');
        }
        if (corpo && corpo.mensagem) {
            return corpo.mensagem;
        }
        return 'Erro inesperado (HTTP ' + status + ')';
    }

    var JSON_HEADERS = { 'Content-Type': 'application/json' };

    var api = {
        listar: function () {
            return request(API);
        },
        criar: function (tarefa) {
            return request(API, {
                method: 'POST',
                headers: JSON_HEADERS,
                body: JSON.stringify(tarefa)
            });
        },
        atualizar: function (id, tarefa) {
            return request(API + '/' + id, {
                method: 'PUT',
                headers: JSON_HEADERS,
                body: JSON.stringify(tarefa)
            });
        },
        deletar: function (id) {
            return request(API + '/' + id, { method: 'DELETE' });
        }
    };

    /* ------------------------------------------------------ Personalização */

    function saudacao() {
        var hora = new Date().getHours();
        if (hora < 12) {
            return 'Bom dia';
        }
        if (hora < 18) {
            return 'Boa tarde';
        }
        return 'Boa noite';
    }

    function iniciais(nome) {
        var partes = nome.trim().split(/\s+/).filter(Boolean);
        if (partes.length === 0) {
            return '';
        }
        if (partes.length === 1) {
            return partes[0].charAt(0).toUpperCase();
        }
        return (partes[0].charAt(0) + partes[partes.length - 1].charAt(0)).toUpperCase();
    }

    function aplicarIdentidade() {
        var nome = window.Prefs.obter().nome;

        el.greeting.textContent = nome ? saudacao() + ', ' + nome : saudacao() + '!';
        el.avatar.textContent = nome ? iniciais(nome) : '✓';

        el.today.textContent = new Date().toLocaleDateString('pt-BR', {
            weekday: 'long',
            day: 'numeric',
            month: 'long'
        });

        var meta = document.querySelector('meta[name="theme-color"]');
        if (meta) {
            meta.setAttribute('content',
                getComputedStyle(document.documentElement).getPropertyValue('--accent').trim());
        }
    }

    /* ------------------------------------------------------------ Render */

    function tarefasFiltradas() {
        if (state.filtro === 'pendentes') {
            return state.tarefas.filter(function (t) { return !t.concluida; });
        }
        if (state.filtro === 'concluidas') {
            return state.tarefas.filter(function (t) { return t.concluida; });
        }
        return state.tarefas;
    }

    function formatarData(iso) {
        if (!iso) {
            return '';
        }

        var data = new Date(iso);
        if (isNaN(data.getTime())) {
            return '';
        }

        return data.toLocaleDateString('pt-BR', {
            day: '2-digit',
            month: 'short',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    function montarTarefa(tarefa, indice) {
        var no = el.template.content.firstElementChild.cloneNode(true);

        no.dataset.id = tarefa.id;
        no.classList.toggle('is-done', !!tarefa.concluida);

        // Entrada escalonada: as tarefas surgem em cascata, não todas de uma vez.
        no.style.animationDelay = Math.min(indice, 8) * 32 + 'ms';

        // textContent evita qualquer injeção de HTML vinda do banco.
        no.querySelector('.task__title').textContent = tarefa.titulo;
        no.querySelector('.task__desc').textContent = tarefa.descricao || '';
        no.querySelector('.badge').textContent = tarefa.concluida ? 'Concluída' : 'Pendente';
        no.querySelector('.task__date').textContent = formatarData(tarefa.dataCriacao);

        var check = no.querySelector('.task__check');
        check.setAttribute('aria-pressed', tarefa.concluida ? 'true' : 'false');
        check.setAttribute('aria-label',
            (tarefa.concluida ? 'Reabrir' : 'Concluir') + ' tarefa: ' + tarefa.titulo);

        return no;
    }

    function renderizar() {
        var visiveis = tarefasFiltradas();

        el.list.textContent = '';

        var fragmento = document.createDocumentFragment();
        visiveis.forEach(function (tarefa, i) {
            fragmento.appendChild(montarTarefa(tarefa, i));
        });
        el.list.appendChild(fragmento);

        el.empty.hidden = visiveis.length > 0;

        if (visiveis.length === 0) {
            if (state.tarefas.length === 0) {
                el.emptyTitle.textContent = 'Nenhuma tarefa por aqui';
                el.emptyText.textContent = 'Adicione sua primeira tarefa no formulário acima.';
            } else if (state.filtro === 'pendentes') {
                el.emptyTitle.textContent = 'Tudo em dia!';
                el.emptyText.textContent = 'Você não tem nenhuma tarefa pendente.';
            } else {
                el.emptyTitle.textContent = 'Nada concluído ainda';
                el.emptyText.textContent = 'Marque uma tarefa como concluída para vê-la aqui.';
            }
        }

        atualizarResumo();
    }

    // Circunferência do anel (r = 52), casada com o stroke-dasharray no CSS.
    var CIRCUNFERENCIA = 2 * Math.PI * 52;

    function atualizarResumo() {
        var total = state.tarefas.length;
        var concluidas = state.tarefas.filter(function (t) { return t.concluida; }).length;
        var pendentes = total - concluidas;
        var percentual = total === 0 ? 0 : Math.round((concluidas / total) * 100);

        el.statTotal.textContent = total;
        el.statPending.textContent = pendentes;
        el.statDone.textContent = concluidas;

        el.ringValue.style.strokeDashoffset = CIRCUNFERENCIA * (1 - percentual / 100);
        el.ringLabel.firstChild.nodeValue = percentual;
        el.ring.setAttribute('role', 'progressbar');
        el.ring.setAttribute('aria-valuemin', '0');
        el.ring.setAttribute('aria-valuemax', '100');
        el.ring.setAttribute('aria-valuenow', percentual);
        el.ring.setAttribute('aria-label', 'Progresso: ' + percentual + '% concluído');

        if (total === 0) {
            el.summary.textContent = 'Sua lista está vazia. Que tal começar agora?';
        } else if (pendentes === 0) {
            el.summary.textContent = 'Tudo concluído. Aproveite o resto do dia!';
        } else if (pendentes === 1) {
            el.summary.textContent = 'Falta 1 tarefa para zerar o dia.';
        } else {
            el.summary.textContent = 'Faltam ' + pendentes + ' tarefas para zerar o dia.';
        }

        // Comemora só na transição para 100%, nunca no carregamento inicial.
        var zerou = percentual === 100 && total > 0
            && state.percentualAnterior !== null && state.percentualAnterior < 100;

        if (zerou) {
            comemorar();
        }

        el.ring.classList.toggle('is-complete', percentual === 100 && total > 0);
        state.percentualAnterior = percentual;
    }

    /* --------------------------------------------------------- Comemoração */

    function comemorar() {
        if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
            return;
        }

        var raiz = getComputedStyle(document.documentElement);
        var matiz = parseFloat(raiz.getPropertyValue('--accent-h')) || 245;

        for (var i = 0; i < 26; i++) {
            var bit = document.createElement('span');
            bit.className = 'confetti__bit';
            bit.style.left = Math.random() * 100 + '%';
            bit.style.background = 'hsl(' + (matiz + Math.random() * 90 - 45) + ' 80% 60%)';
            bit.style.animationDelay = Math.random() * 350 + 'ms';
            bit.style.animationDuration = 1500 + Math.random() * 900 + 'ms';
            el.confetti.appendChild(bit);
        }

        setTimeout(function () { el.confetti.textContent = ''; }, 2900);
    }

    function mostrarEstado(nome) {
        el.loading.hidden = nome !== 'loading';
        el.error.hidden = nome !== 'error';
        el.list.hidden = nome !== 'pronto';

        if (nome !== 'pronto') {
            el.empty.hidden = true;
        }
    }

    /* ------------------------------------------------------------- Toasts */

    var MAX_TOASTS = 3;

    function toast(mensagem, tipo) {
        // Mantém a pilha curta: ações em sequência não devem cobrir a tela.
        while (el.toasts.children.length >= MAX_TOASTS) {
            el.toasts.removeChild(el.toasts.firstElementChild);
        }

        var no = document.createElement('div');
        no.className = 'toast toast--' + (tipo || 'info');
        no.textContent = mensagem;
        el.toasts.appendChild(no);

        setTimeout(function () {
            no.classList.add('is-leaving');
            setTimeout(function () { no.remove(); }, 200);
        }, 3200);
    }

    /* ------------------------------------------------------------- Ações */

    function carregar() {
        mostrarEstado('loading');

        return api.listar()
            .then(function (tarefas) {
                state.tarefas = Array.isArray(tarefas) ? tarefas : [];
                mostrarEstado('pronto');
                renderizar();
            })
            .catch(function (erro) {
                el.errorText.textContent = erro.message;
                mostrarEstado('error');
            });
    }

    function criar(evento) {
        evento.preventDefault();

        var titulo = el.titulo.value.trim();

        if (!titulo) {
            el.tituloError.textContent = 'O título é obrigatório';
            el.titulo.classList.add('is-invalid');
            el.titulo.focus();
            return;
        }

        limparErroTitulo();
        el.submitBtn.disabled = true;

        api.criar({
            titulo: titulo,
            descricao: el.descricao.value.trim() || null,
            concluida: false
        })
            .then(function (criada) {
                state.tarefas.push(criada);
                el.form.reset();
                atualizarContador();
                renderizar();
                toast('Tarefa criada com sucesso', 'success');
                el.titulo.focus();
            })
            .catch(function (erro) {
                toast(erro.message, 'error');
            })
            .finally(function () {
                el.submitBtn.disabled = false;
            });
    }

    function alternarConclusao(id, noDaTarefa) {
        var tarefa = buscarNoEstado(id);
        if (!tarefa) {
            return;
        }

        noDaTarefa.classList.add('is-busy');

        api.atualizar(id, {
            titulo: tarefa.titulo,
            descricao: tarefa.descricao,
            concluida: !tarefa.concluida
        })
            .then(function (atualizada) {
                substituirNoEstado(atualizada);
                renderizar();
                toast(atualizada.concluida ? 'Tarefa concluída' : 'Tarefa reaberta', 'success');
            })
            .catch(function (erro) {
                noDaTarefa.classList.remove('is-busy');
                toast(erro.message, 'error');
            });
    }

    function deletar(id, noDaTarefa) {
        var tarefa = buscarNoEstado(id);
        if (!tarefa) {
            return;
        }

        if (!window.confirm('Excluir a tarefa "' + tarefa.titulo + '"?')) {
            return;
        }

        noDaTarefa.classList.add('is-removing');

        api.deletar(id)
            .then(function () {
                state.tarefas = state.tarefas.filter(function (t) { return t.id !== id; });
                renderizar();
                toast('Tarefa excluída', 'success');
            })
            .catch(function (erro) {
                noDaTarefa.classList.remove('is-removing');
                toast(erro.message, 'error');
            });
    }

    function salvarEdicao(evento) {
        evento.preventDefault();

        var id = Number(el.editId.value);
        var titulo = el.editTitulo.value.trim();

        if (!titulo) {
            el.editTituloError.textContent = 'O título é obrigatório';
            el.editTitulo.classList.add('is-invalid');
            el.editTitulo.focus();
            return;
        }

        api.atualizar(id, {
            titulo: titulo,
            descricao: el.editDescricao.value.trim() || null,
            concluida: el.editConcluida.checked
        })
            .then(function (atualizada) {
                substituirNoEstado(atualizada);
                renderizar();
                fecharModal();
                toast('Tarefa atualizada', 'success');
            })
            .catch(function (erro) {
                toast(erro.message, 'error');
            });
    }

    function buscarNoEstado(id) {
        return state.tarefas.filter(function (t) { return t.id === id; })[0];
    }

    function substituirNoEstado(tarefa) {
        state.tarefas = state.tarefas.map(function (t) {
            return t.id === tarefa.id ? tarefa : t;
        });
    }

    /* -------------------------------------------------------------- Modais */

    var focoAnterior = null;

    function abrirModal(id) {
        var tarefa = buscarNoEstado(id);
        if (!tarefa) {
            return;
        }

        focoAnterior = document.activeElement;

        el.editId.value = tarefa.id;
        el.editTitulo.value = tarefa.titulo;
        el.editDescricao.value = tarefa.descricao || '';
        el.editConcluida.checked = !!tarefa.concluida;
        el.editTitulo.classList.remove('is-invalid');
        el.editTituloError.textContent = '';

        el.modal.hidden = false;
        el.editTitulo.focus();
        el.editTitulo.select();
    }

    function fecharModal() {
        el.modal.hidden = true;
        devolverFoco();
    }

    function abrirAjustes() {
        focoAnterior = document.activeElement;

        var prefs = window.Prefs.obter();
        el.prefNome.value = prefs.nome;
        marcarCor(prefs.cor);
        marcarTema(prefs.tema);

        el.settingsModal.hidden = false;
        el.prefNome.focus();
    }

    function fecharAjustes() {
        el.settingsModal.hidden = true;
        devolverFoco();
    }

    function devolverFoco() {
        if (focoAnterior && typeof focoAnterior.focus === 'function') {
            focoAnterior.focus();
            focoAnterior = null;
        }
    }

    /* ------------------------------------------------- Painel de ajustes */

    function montarAmostras() {
        window.Prefs.PALETA.forEach(function (cor) {
            var botao = document.createElement('button');
            botao.type = 'button';
            botao.className = 'swatch';
            botao.dataset.cor = cor.id;
            botao.title = cor.nome;
            botao.setAttribute('role', 'radio');
            botao.setAttribute('aria-label', cor.nome);
            botao.style.setProperty('--amostra',
                'linear-gradient(140deg, hsl(' + cor.h + ' ' + cor.s + '% 56%), hsl('
                    + (cor.h + 34) + ' ' + cor.s + '% 46%))');

            botao.addEventListener('click', function () {
                // Pré-visualiza na hora: a cor muda enquanto o painel está aberto.
                window.Prefs.definir({ cor: cor.id });
                marcarCor(cor.id);
                aplicarIdentidade();
            });

            el.swatches.appendChild(botao);
        });
    }

    function marcarCor(id) {
        Array.prototype.forEach.call(el.swatches.children, function (botao) {
            var ativo = botao.dataset.cor === id;
            botao.classList.toggle('is-active', ativo);
            botao.setAttribute('aria-checked', ativo ? 'true' : 'false');
        });
    }

    function marcarTema(valor) {
        Array.prototype.forEach.call(el.themeChoice.children, function (botao) {
            var ativo = botao.dataset.themeValue === valor;
            botao.classList.toggle('is-active', ativo);
            botao.setAttribute('aria-checked', ativo ? 'true' : 'false');
        });
    }

    /* ---------------------------------------------------------- Auxiliares */

    function limparErroTitulo() {
        el.tituloError.textContent = '';
        el.titulo.classList.remove('is-invalid');
    }

    function atualizarContador() {
        el.descricaoCounter.textContent = el.descricao.value.length + '/1000';
    }

    /* ------------------------------------------------------------ Eventos */

    el.form.addEventListener('submit', criar);
    el.editForm.addEventListener('submit', salvarEdicao);
    el.retryBtn.addEventListener('click', carregar);
    el.titulo.addEventListener('input', limparErroTitulo);
    el.descricao.addEventListener('input', atualizarContador);

    el.editTitulo.addEventListener('input', function () {
        el.editTitulo.classList.remove('is-invalid');
        el.editTituloError.textContent = '';
    });

    // Delegação de eventos: a lista é recriada a cada render.
    el.list.addEventListener('click', function (evento) {
        var botao = evento.target.closest('[data-action]');
        if (!botao) {
            return;
        }

        var noDaTarefa = botao.closest('.task');
        var id = Number(noDaTarefa.dataset.id);
        var acao = botao.dataset.action;

        if (acao === 'toggle') {
            alternarConclusao(id, noDaTarefa);
        } else if (acao === 'edit') {
            abrirModal(id);
        } else if (acao === 'delete') {
            deletar(id, noDaTarefa);
        }
    });

    Array.prototype.forEach.call(el.filters, function (botao, indice) {
        botao.addEventListener('click', function () {
            state.filtro = botao.dataset.filter;

            Array.prototype.forEach.call(el.filters, function (outro) {
                outro.classList.toggle('is-active', outro === botao);
            });

            // Move o indicador deslizante para a opção escolhida.
            el.filtersNav.style.setProperty('--indice', indice);
            renderizar();
        });
    });

    Array.prototype.forEach.call(el.modal.querySelectorAll('[data-close-modal]'), function (botao) {
        botao.addEventListener('click', fecharModal);
    });

    Array.prototype.forEach.call(
        el.settingsModal.querySelectorAll('[data-close-settings]'), function (botao) {
            botao.addEventListener('click', fecharAjustes);
        });

    el.settingsBtn.addEventListener('click', abrirAjustes);

    el.settingsForm.addEventListener('submit', function (evento) {
        evento.preventDefault();
        window.Prefs.definir({ nome: el.prefNome.value });
        aplicarIdentidade();
        fecharAjustes();
        toast('Preferências salvas', 'success');
    });

    Array.prototype.forEach.call(el.themeChoice.children, function (botao) {
        botao.addEventListener('click', function () {
            window.Prefs.definir({ tema: botao.dataset.themeValue });
            marcarTema(botao.dataset.themeValue);
            aplicarIdentidade();
        });
    });

    // Botão da barra superior: percorre sistema -> claro -> escuro -> sistema.
    var CICLO = { system: 'light', light: 'dark', dark: 'system' };
    var NOME_TEMA = { system: 'do sistema', light: 'claro', dark: 'escuro' };

    el.themeBtn.addEventListener('click', function () {
        var proximo = CICLO[window.Prefs.obter().tema];
        window.Prefs.definir({ tema: proximo });
        marcarTema(proximo);
        aplicarIdentidade();
        toast('Tema ' + NOME_TEMA[proximo], 'info');
    });

    document.addEventListener('keydown', function (evento) {
        if (evento.key !== 'Escape') {
            return;
        }
        if (!el.modal.hidden) {
            fecharModal();
        } else if (!el.settingsModal.hidden) {
            fecharAjustes();
        }
    });

    /* ------------------------------------------------------------- Início */

    montarAmostras();
    aplicarIdentidade();
    atualizarContador();
    carregar();
})();
