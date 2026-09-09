/* ==========================================================================
   To-do List — cliente da API REST
   Sem dependências externas: funciona offline, servido pelo próprio Spring Boot.
   ========================================================================== */

(function () {
    'use strict';

    var API = '/api/tarefas';
    var API_PROJETOS = '/api/projetos';
    var API_ETIQUETAS = '/api/etiquetas';
    var API_HABITOS = '/api/habitos';
    var API_PAINEL = '/api/painel';
    var API_DADOS = '/api/dados';
    var API_AUTH = '/api/auth';
    var CHAVE_TOKEN = 'todolist:token';
    var TAMANHO_PAGINA = 50;

    /** Token e conta em uso. Sem token, a aplicação nem chega a ser exibida. */
    var sessao = {
        token: null,
        conta: null
    };

    var state = {
        tarefas: [],          // acumuladas das páginas já carregadas
        projetos: [],
        etiquetas: [],
        habitos: [],
        resumo: null,
        aba: 'tarefas',
        filtro: 'todas',      // todas | pendentes | concluidas
        projetoAtivo: null,   // null = todos, 'nenhum' = caixa de entrada, id = projeto
        etiquetaAtiva: null,  // null = todas
        busca: '',
        paginaCarregada: 0,
        total: 0,
        percentualAnterior: null
    };

    var el = {
        form: document.getElementById('task-form'),
        titulo: document.getElementById('titulo'),
        tituloError: document.getElementById('titulo-error'),
        descricao: document.getElementById('descricao'),
        descricaoCounter: document.getElementById('descricao-counter'),
        projetoSelect: document.getElementById('projeto'),
        prazo: document.getElementById('prazo'),
        prioridade: document.getElementById('prioridade'),
        submitBtn: document.getElementById('submit-btn'),
        list: document.getElementById('task-list'),
        template: document.getElementById('task-template'),
        filtersNav: document.getElementById('filters'),
        filters: document.querySelectorAll('.filters__item'),
        projetosNav: document.getElementById('projetos'),
        busca: document.getElementById('busca'),
        mais: document.getElementById('mais'),
        maisBtn: document.getElementById('mais-btn'),
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
        editProjeto: document.getElementById('edit-projeto'),
        editPrazo: document.getElementById('edit-prazo'),
        editPrioridade: document.getElementById('edit-prioridade'),
        editConcluida: document.getElementById('edit-concluida'),
        settingsBtn: document.getElementById('settings-btn'),
        settingsModal: document.getElementById('settings-modal'),
        settingsForm: document.getElementById('settings-form'),
        prefNome: document.getElementById('pref-nome'),
        swatches: document.getElementById('swatches'),
        themeChoice: document.getElementById('theme-choice'),
        themeBtn: document.getElementById('theme-btn'),
        authView: document.getElementById('auth-view'),
        appView: document.getElementById('app-view'),
        authForm: document.getElementById('auth-form'),
        authModo: document.getElementById('auth-modo'),
        authNome: document.getElementById('auth-nome'),
        campoNome: document.getElementById('campo-nome'),
        authEmail: document.getElementById('auth-email'),
        authSenha: document.getElementById('auth-senha'),
        authDica: document.getElementById('auth-dica'),
        authErro: document.getElementById('auth-error'),
        authSubmit: document.getElementById('auth-submit'),
        authLinha: document.getElementById('auth-linha'),
        logoutBtn: document.getElementById('logout-btn'),
        projetosModal: document.getElementById('projetos-modal'),
        projetoForm: document.getElementById('projeto-form'),
        projetoNome: document.getElementById('projeto-nome'),
        projetoErro: document.getElementById('projeto-error'),
        projetoCores: document.getElementById('projeto-cores'),
        projetoSubmit: document.getElementById('projeto-submit'),
        listaProjetos: document.getElementById('lista-projetos'),
        etiquetasBarra: document.getElementById('etiquetas-barra'),
        campoEtiquetas: document.getElementById('campo-etiquetas'),
        etiquetasEscolha: document.getElementById('etiquetas'),
        editCampoEtiquetas: document.getElementById('edit-campo-etiquetas'),
        editEtiquetasEscolha: document.getElementById('edit-etiquetas'),
        etiquetaForm: document.getElementById('etiqueta-form'),
        etiquetaNome: document.getElementById('etiqueta-nome'),
        etiquetaErro: document.getElementById('etiqueta-error'),
        etiquetaCores: document.getElementById('etiqueta-cores'),
        etiquetaSubmit: document.getElementById('etiqueta-submit'),
        listaEtiquetas: document.getElementById('lista-etiquetas'),
        abas: document.getElementById('abas'),
        abaTarefas: document.getElementById('aba-tarefas'),
        abaHabitos: document.getElementById('aba-habitos'),
        abaPainel: document.getElementById('aba-painel'),
        habitoForm: document.getElementById('habito-form'),
        habitoNome: document.getElementById('habito-nome'),
        habitoErro: document.getElementById('habito-error'),
        habitoDias: document.getElementById('habito-dias'),
        habitoCores: document.getElementById('habito-cores'),
        habitoSubmit: document.getElementById('habito-submit'),
        listaHabitos: document.getElementById('lista-habitos'),
        habitosVazio: document.getElementById('habitos-vazio'),
        habitoTemplate: document.getElementById('habito-template'),
        tileAtrasadas: document.getElementById('tile-atrasadas'),
        tileHoje: document.getElementById('tile-hoje'),
        tileTempo: document.getElementById('tile-tempo'),
        serie: document.getElementById('serie'),
        serieInicio: document.getElementById('serie-inicio'),
        serieFim: document.getElementById('serie-fim'),
        resumoSerie: document.getElementById('resumo-serie'),
        verNumeros: document.getElementById('ver-numeros'),
        tabelaSerie: document.getElementById('tabela-serie'),
        tabelaSerieCorpo: document.getElementById('tabela-serie-corpo'),
        barrasProjeto: document.getElementById('barras-projeto'),
        vazioProjeto: document.getElementById('vazio-projeto'),
        barrasPrioridade: document.getElementById('barras-prioridade'),
        vazioPrioridade: document.getElementById('vazio-prioridade'),
        barrasHabitos: document.getElementById('barras-habitos'),
        vazioHabitos: document.getElementById('vazio-habitos'),
        exportarBtn: document.getElementById('exportar-btn'),
        importarBtn: document.getElementById('importar-btn'),
        importarArquivo: document.getElementById('importar-arquivo'),
        dadosErro: document.getElementById('dados-error')
    };

    /* ---------------------------------------------------------------- HTTP */

    var JSON_HEADERS = { 'Content-Type': 'application/json' };

    function request(url, options) {
        var config = options || {};
        var cabecalhos = {};

        Object.keys(config.headers || {}).forEach(function (nome) {
            cabecalhos[nome] = config.headers[nome];
        });

        if (sessao.token) {
            cabecalhos.Authorization = 'Bearer ' + sessao.token;
        }
        config.headers = cabecalhos;

        return fetch(url, config).then(function (resposta) {
            // 401 fora das rotas de autenticação significa token expirado ou
            // revogado: não adianta repetir, tem que entrar de novo.
            if (resposta.status === 401 && url.indexOf(API_AUTH) !== 0) {
                encerrarSessao('Sua sessão expirou. Entre novamente.');
                throw new Error('Sessão expirada');
            }

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

    function corpoJson(metodo, dados) {
        return { method: metodo, headers: JSON_HEADERS, body: JSON.stringify(dados) };
    }

    var api = {
        listar: function (pagina) {
            return request(API + parametrosDaListagem(pagina));
        },
        resumo: function () {
            return request(API + '/resumo?hoje=' + hojeISO());
        },
        criar: function (tarefa) {
            return request(API, corpoJson('POST', tarefa));
        },
        atualizar: function (id, tarefa) {
            return request(API + '/' + id, corpoJson('PUT', tarefa));
        },
        conclusao: function (id, concluida) {
            return request(API + '/' + id + '/conclusao',
                    corpoJson('PATCH', { concluida: concluida }));
        },
        deletar: function (id) {
            return request(API + '/' + id, { method: 'DELETE' });
        },
        projetos: function () {
            return request(API_PROJETOS);
        },
        criarProjeto: function (projeto) {
            return request(API_PROJETOS, corpoJson('POST', projeto));
        },
        deletarProjeto: function (id) {
            return request(API_PROJETOS + '/' + id, { method: 'DELETE' });
        },
        etiquetas: function () {
            return request(API_ETIQUETAS);
        },
        criarEtiqueta: function (etiqueta) {
            return request(API_ETIQUETAS, corpoJson('POST', etiqueta));
        },
        deletarEtiqueta: function (id) {
            return request(API_ETIQUETAS + '/' + id, { method: 'DELETE' });
        },
        habitos: function () {
            return request(API_HABITOS + '?hoje=' + hojeISO());
        },
        criarHabito: function (habito) {
            return request(API_HABITOS + '?hoje=' + hojeISO(), corpoJson('POST', habito));
        },
        deletarHabito: function (id) {
            return request(API_HABITOS + '/' + id, { method: 'DELETE' });
        },
        marcarHabito: function (id, data) {
            return request(API_HABITOS + '/' + id + '/registros/' + data + '?hoje=' + hojeISO(),
                    { method: 'PUT' });
        },
        desmarcarHabito: function (id, data) {
            return request(API_HABITOS + '/' + id + '/registros/' + data + '?hoje=' + hojeISO(),
                    { method: 'DELETE' });
        },
        painel: function () {
            return request(API_PAINEL + '?hoje=' + hojeISO() + '&dias=30');
        },
        exportar: function () {
            return request(API_DADOS + '/exportar');
        },
        importar: function (dados) {
            return request(API_DADOS + '/importar', corpoJson('POST', dados));
        }
    };

    var auth = {
        registrar: function (dados) {
            return request(API_AUTH + '/registrar', corpoJson('POST', dados));
        },
        login: function (dados) {
            return request(API_AUTH + '/login', corpoJson('POST', dados));
        },
        eu: function () {
            return request(API_AUTH + '/eu');
        }
    };

    /**
     * A filtragem acontece no servidor. Com a listagem paginada, filtrar no
     * cliente esconderia tudo o que está fora da página carregada.
     */
    function parametrosDaListagem(pagina) {
        var partes = ['page=' + pagina, 'size=' + TAMANHO_PAGINA, 'sort=id,asc'];

        if (state.filtro === 'pendentes') {
            partes.push('concluida=false');
        } else if (state.filtro === 'concluidas') {
            partes.push('concluida=true');
        }

        if (state.projetoAtivo === 'nenhum') {
            partes.push('semProjeto=true');
        } else if (state.projetoAtivo) {
            partes.push('projeto=' + state.projetoAtivo);
        }

        if (state.etiquetaAtiva) {
            partes.push('etiqueta=' + state.etiquetaAtiva);
        }

        if (state.busca) {
            partes.push('busca=' + encodeURIComponent(state.busca));
        }

        return '?' + partes.join('&');
    }

    /* ------------------------------------------------------------- Sessão */

    function lerToken() {
        try {
            return window.localStorage.getItem(CHAVE_TOKEN);
        } catch (e) {
            return null;
        }
    }

    function gravarToken(token) {
        try {
            if (token) {
                window.localStorage.setItem(CHAVE_TOKEN, token);
            } else {
                window.localStorage.removeItem(CHAVE_TOKEN);
            }
        } catch (e) {
            // Sem persistência: a sessão vale só até fechar a aba.
        }
    }

    function mostrarEntrada() {
        el.authView.hidden = false;
        el.appView.hidden = true;
        el.authSenha.value = '';
        el.authErro.textContent = '';
    }

    function entrarNaAplicacao(conta) {
        sessao.conta = conta;
        el.authView.hidden = true;
        el.appView.hidden = false;
        el.authForm.reset();
        aplicarIdentidade();
        carregarOrganizacao().then(carregar);
    }

    function encerrarSessao(mensagem) {
        sessao.token = null;
        sessao.conta = null;
        gravarToken(null);

        state.tarefas = [];
        state.projetos = [];
        state.etiquetas = [];
        state.habitos = [];
        state.resumo = null;
        state.filtro = 'todas';
        state.projetoAtivo = null;
        state.etiquetaAtiva = null;
        state.busca = '';
        state.percentualAnterior = null;

        mostrarEntrada();

        if (mensagem) {
            toast(mensagem, 'error');
        }
    }

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
        // O apelido escolhido em Personalizar tem prioridade; sem ele, vale o
        // nome da conta. A saudação usa o primeiro nome, o avatar as iniciais.
        var preferido = window.Prefs.obter().nome;
        var completo = preferido || (sessao.conta ? sessao.conta.nome : '');
        var primeiro = completo ? completo.trim().split(/\s+/)[0] : '';

        el.greeting.textContent = primeiro ? saudacao() + ', ' + primeiro : saudacao() + '!';
        el.avatar.textContent = completo ? iniciais(completo) : '✓';

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

    /* --------------------------------------------------------------- Datas */

    /**
     * Data de hoje no fuso de quem usa. toISOString() daria a data em UTC, que
     * perto da meia-noite é outro dia — e "atrasada" passaria a mentir.
     */
    function hojeISO() {
        var agora = new Date();
        return agora.getFullYear()
                + '-' + String(agora.getMonth() + 1).padStart(2, '0')
                + '-' + String(agora.getDate()).padStart(2, '0');
    }

    function somarDias(iso, dias) {
        var data = new Date(iso + 'T00:00:00');
        data.setDate(data.getDate() + dias);
        return data.getFullYear()
                + '-' + String(data.getMonth() + 1).padStart(2, '0')
                + '-' + String(data.getDate()).padStart(2, '0');
    }

    function formatarDia(iso) {
        // 'T00:00:00' força leitura como data local; sem isso o navegador
        // interpreta a string como UTC e exibe o dia anterior a oeste de Greenwich.
        var data = new Date(iso + 'T00:00:00');
        var opcoes = { day: '2-digit', month: 'short' };

        // O ano só aparece quando não é o corrente: "15 de jan." de 2020 seria
        // indistinguível de janeiro que vem.
        if (data.getFullYear() !== new Date().getFullYear()) {
            opcoes.year = 'numeric';
        }

        return data.toLocaleDateString('pt-BR', opcoes);
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

    /** Comparação lexicográfica: strings AAAA-MM-DD ordenam como datas. */
    function rotuloDePrazo(prazo) {
        var hoje = hojeISO();

        if (prazo < hoje) {
            return { texto: 'Atrasada · ' + formatarDia(prazo), classe: 'e-atrasada' };
        }
        if (prazo === hoje) {
            return { texto: 'Vence hoje', classe: 'e-hoje' };
        }
        if (prazo === somarDias(hoje, 1)) {
            return { texto: 'Amanhã', classe: '' };
        }
        return { texto: formatarDia(prazo), classe: '' };
    }

    var NOME_PRIORIDADE = {
        BAIXA: 'Baixa',
        MEDIA: 'Média',
        ALTA: 'Alta',
        URGENTE: 'Urgente'
    };

    /* ------------------------------------------------------------ Render */

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

        var chipProjeto = no.querySelector('.chip--projeto');
        if (tarefa.projeto) {
            chipProjeto.textContent = tarefa.projeto.nome;
            chipProjeto.style.setProperty('--ponto', corDoProjeto(tarefa.projeto.cor));
            chipProjeto.hidden = false;
        }

        var chipPrazo = no.querySelector('.chip--prazo');
        if (tarefa.prazo && !tarefa.concluida) {
            var rotulo = rotuloDePrazo(tarefa.prazo);
            chipPrazo.textContent = rotulo.texto;
            chipPrazo.className = 'chip chip--prazo ' + rotulo.classe;
            chipPrazo.hidden = false;
        } else if (tarefa.prazo) {
            chipPrazo.textContent = formatarDia(tarefa.prazo);
            chipPrazo.className = 'chip chip--prazo';
            chipPrazo.hidden = false;
        }

        // Só ALTA e URGENTE aparecem: marcar "média" em tudo é ruído visual.
        var chipPrioridade = no.querySelector('.chip--prioridade');
        if (tarefa.prioridade === 'ALTA' || tarefa.prioridade === 'URGENTE') {
            chipPrioridade.textContent = NOME_PRIORIDADE[tarefa.prioridade];
            chipPrioridade.className = 'chip chip--prioridade e-' + tarefa.prioridade.toLowerCase();
            chipPrioridade.hidden = false;
        }

        var caixaEtiquetas = no.querySelector('.task__etiquetas');
        (tarefa.etiquetas || []).forEach(function (etiqueta) {
            var chip = document.createElement('span');
            // Classe própria: visualmente igual ao rótulo de projeto, mas
            // distinguível no DOM — projeto e etiqueta são coisas diferentes.
            chip.className = 'chip chip--etiqueta';
            chip.textContent = etiqueta.nome;
            chip.style.setProperty('--ponto', corDoProjeto(etiqueta.cor));
            caixaEtiquetas.appendChild(chip);
        });

        var check = no.querySelector('.task__check');
        check.setAttribute('aria-pressed', tarefa.concluida ? 'true' : 'false');
        check.setAttribute('aria-label',
            (tarefa.concluida ? 'Reabrir' : 'Concluir') + ' tarefa: ' + tarefa.titulo);

        return no;
    }

    var CORES = {
        indigo: 'hsl(245 72% 56%)',
        violeta: 'hsl(276 66% 56%)',
        azul: 'hsl(208 82% 56%)',
        verde: 'hsl(152 62% 42%)',
        ambar: 'hsl(34 88% 48%)',
        rosa: 'hsl(335 75% 52%)'
    };

    function corDoProjeto(cor) {
        return CORES[cor] || CORES.indigo;
    }

    function renderizar() {
        el.list.textContent = '';

        var fragmento = document.createDocumentFragment();
        state.tarefas.forEach(function (tarefa, i) {
            fragmento.appendChild(montarTarefa(tarefa, i));
        });
        el.list.appendChild(fragmento);

        el.empty.hidden = state.tarefas.length > 0;

        if (state.tarefas.length === 0) {
            if (state.busca) {
                el.emptyTitle.textContent = 'Nada encontrado';
                el.emptyText.textContent = 'Nenhuma tarefa corresponde a “' + state.busca + '”.';
            } else if (state.filtro === 'pendentes') {
                el.emptyTitle.textContent = 'Tudo em dia!';
                el.emptyText.textContent = 'Você não tem nenhuma tarefa pendente aqui.';
            } else if (state.filtro === 'concluidas') {
                el.emptyTitle.textContent = 'Nada concluído ainda';
                el.emptyText.textContent = 'Marque uma tarefa como concluída para vê-la aqui.';
            } else {
                el.emptyTitle.textContent = 'Nenhuma tarefa por aqui';
                el.emptyText.textContent = 'Adicione sua primeira tarefa no formulário acima.';
            }
        }

        var restantes = state.total - state.tarefas.length;
        el.mais.hidden = restantes <= 0;
        el.maisBtn.textContent = 'Carregar mais ' + Math.min(restantes, TAMANHO_PAGINA)
                + ' de ' + restantes;
    }

    function renderizarProjetos() {
        el.projetosNav.textContent = '';

        var opcoes = [{ id: null, nome: 'Todos' }, { id: 'nenhum', nome: 'Caixa de entrada' }]
                .concat(state.projetos.map(function (projeto) {
                    return {
                        id: projeto.id,
                        nome: projeto.nome,
                        cor: projeto.cor,
                        contagem: projeto.tarefasPendentes
                    };
                }));

        opcoes.forEach(function (opcao) {
            var botao = document.createElement('button');
            botao.type = 'button';
            botao.className = 'projeto-chip';
            botao.classList.toggle('is-active', state.projetoAtivo === opcao.id);

            if (opcao.cor) {
                var ponto = document.createElement('span');
                ponto.className = 'projeto-chip__ponto';
                ponto.style.setProperty('--ponto', corDoProjeto(opcao.cor));
                botao.appendChild(ponto);
            }

            botao.appendChild(document.createTextNode(opcao.nome));

            if (opcao.contagem) {
                var contagem = document.createElement('span');
                contagem.className = 'projeto-chip__contagem';
                contagem.textContent = opcao.contagem;
                botao.appendChild(contagem);
            }

            botao.addEventListener('click', function () {
                state.projetoAtivo = opcao.id;
                renderizarProjetos();
                carregar();
            });

            el.projetosNav.appendChild(botao);
        });

        var novo = document.createElement('button');
        novo.type = 'button';
        novo.className = 'projeto-chip projeto-chip--novo';
        novo.textContent = '+ Projeto';
        novo.addEventListener('click', abrirProjetos);
        el.projetosNav.appendChild(novo);
    }

    function renderizarBarraDeEtiquetas() {
        el.etiquetasBarra.textContent = '';

        if (state.etiquetas.length === 0) {
            return;
        }

        [{ id: null, nome: 'Todas as etiquetas' }].concat(state.etiquetas).forEach(function (opcao) {
            var botao = document.createElement('button');
            botao.type = 'button';
            botao.className = 'projeto-chip';
            botao.classList.toggle('is-active', state.etiquetaAtiva === opcao.id);

            if (opcao.cor) {
                var ponto = document.createElement('span');
                ponto.className = 'projeto-chip__ponto';
                ponto.style.setProperty('--ponto', corDoProjeto(opcao.cor));
                botao.appendChild(ponto);
            }

            botao.appendChild(document.createTextNode(opcao.nome));

            if (opcao.tarefasPendentes) {
                var contagem = document.createElement('span');
                contagem.className = 'projeto-chip__contagem';
                contagem.textContent = opcao.tarefasPendentes;
                botao.appendChild(contagem);
            }

            botao.addEventListener('click', function () {
                state.etiquetaAtiva = opcao.id;
                renderizarBarraDeEtiquetas();
                carregar();
            });

            el.etiquetasBarra.appendChild(botao);
        });
    }

    /** Alternar chips em vez de um select múltiplo, que é desconfortável de usar. */
    function montarEscolhaDeEtiquetas(container, campo, selecionadas) {
        container.textContent = '';
        campo.hidden = state.etiquetas.length === 0;

        state.etiquetas.forEach(function (etiqueta) {
            var botao = document.createElement('button');
            botao.type = 'button';
            botao.className = 'etiqueta-toggle';
            botao.dataset.id = etiqueta.id;
            botao.textContent = etiqueta.nome;
            botao.style.setProperty('--ponto', corDoProjeto(etiqueta.cor));
            botao.setAttribute('aria-pressed',
                    selecionadas.indexOf(etiqueta.id) >= 0 ? 'true' : 'false');
            botao.classList.toggle('is-active', selecionadas.indexOf(etiqueta.id) >= 0);

            botao.addEventListener('click', function () {
                var ativo = botao.classList.toggle('is-active');
                botao.setAttribute('aria-pressed', ativo ? 'true' : 'false');
            });

            container.appendChild(botao);
        });
    }

    function limparEscolhaDeEtiquetas() {
        Array.prototype.forEach.call(el.etiquetasEscolha.children, function (botao) {
            botao.classList.remove('is-active');
            botao.setAttribute('aria-pressed', 'false');
        });
    }

    function etiquetasEscolhidas(container) {
        return Array.prototype.filter
                .call(container.children, function (botao) {
                    return botao.classList.contains('is-active');
                })
                .map(function (botao) { return Number(botao.dataset.id); });
    }

    function preencherSelectDeProjetos(select, selecionado) {
        select.textContent = '';

        var vazio = document.createElement('option');
        vazio.value = '';
        vazio.textContent = 'Caixa de entrada';
        select.appendChild(vazio);

        state.projetos.forEach(function (projeto) {
            var opcao = document.createElement('option');
            opcao.value = projeto.id;
            opcao.textContent = projeto.nome;
            select.appendChild(opcao);
        });

        select.value = selecionado == null ? '' : String(selecionado);
    }

    // Circunferência do anel (r = 52), casada com o stroke-dasharray no CSS.
    var CIRCUNFERENCIA = 2 * Math.PI * 52;

    function atualizarResumo() {
        var resumo = state.resumo || {
            total: 0, pendentes: 0, concluidas: 0,
            atrasadas: 0, vencemHoje: 0, percentualConcluido: 0
        };

        el.statTotal.textContent = resumo.total;
        el.statPending.textContent = resumo.pendentes;
        el.statDone.textContent = resumo.concluidas;

        var percentual = resumo.percentualConcluido;
        el.ringValue.style.strokeDashoffset = CIRCUNFERENCIA * (1 - percentual / 100);
        el.ringLabel.firstChild.nodeValue = percentual;
        el.ring.setAttribute('role', 'progressbar');
        el.ring.setAttribute('aria-valuemin', '0');
        el.ring.setAttribute('aria-valuemax', '100');
        el.ring.setAttribute('aria-valuenow', percentual);
        el.ring.setAttribute('aria-label', 'Progresso: ' + percentual + '% concluído');

        el.summary.textContent = frase(resumo);

        // Comemora só na transição para 100%, nunca no carregamento inicial.
        var zerou = percentual === 100 && resumo.total > 0
            && state.percentualAnterior !== null && state.percentualAnterior < 100;

        if (zerou) {
            comemorar();
        }

        el.ring.classList.toggle('is-complete', percentual === 100 && resumo.total > 0);
        state.percentualAnterior = percentual;
    }

    function frase(resumo) {
        if (resumo.total === 0) {
            return 'Sua lista está vazia. Que tal começar agora?';
        }
        if (resumo.atrasadas > 0) {
            return resumo.atrasadas === 1
                    ? '1 tarefa passou do prazo.'
                    : resumo.atrasadas + ' tarefas passaram do prazo.';
        }
        if (resumo.vencemHoje > 0) {
            return resumo.vencemHoje === 1
                    ? '1 tarefa vence hoje.'
                    : resumo.vencemHoje + ' tarefas vencem hoje.';
        }
        if (resumo.pendentes === 0) {
            return 'Tudo concluído. Aproveite o resto do dia!';
        }
        return resumo.pendentes === 1
                ? 'Falta 1 tarefa para zerar o dia.'
                : 'Faltam ' + resumo.pendentes + ' tarefas para zerar o dia.';
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
            el.mais.hidden = true;
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

    function carregarOrganizacao() {
        return Promise.all([api.projetos(), api.etiquetas()])
            .then(function (respostas) {
                state.projetos = Array.isArray(respostas[0]) ? respostas[0] : [];
                state.etiquetas = Array.isArray(respostas[1]) ? respostas[1] : [];

                renderizarProjetos();
                renderizarBarraDeEtiquetas();
                preencherSelectDeProjetos(el.projetoSelect, el.projetoSelect.value || null);
                montarEscolhaDeEtiquetas(el.etiquetasEscolha, el.campoEtiquetas, []);
            })
            .catch(function () {
                state.projetos = [];
                state.etiquetas = [];
            });
    }

    function carregar() {
        mostrarEstado('loading');

        return Promise.all([api.listar(0), api.resumo()])
            .then(function (respostas) {
                var pagina = respostas[0];

                // A resposta é um objeto Page, não um array: content, totalElements...
                state.tarefas = pagina.content || [];
                state.total = pagina.totalElements || 0;
                state.paginaCarregada = 0;
                state.resumo = respostas[1];

                mostrarEstado('pronto');
                renderizar();
                atualizarResumo();
            })
            .catch(function (erro) {
                if (erro.message === 'Sessão expirada') {
                    return;
                }
                el.errorText.textContent = erro.message;
                mostrarEstado('error');
            });
    }

    function carregarMais() {
        el.maisBtn.disabled = true;

        api.listar(state.paginaCarregada + 1)
            .then(function (pagina) {
                state.paginaCarregada += 1;
                state.tarefas = state.tarefas.concat(pagina.content || []);
                state.total = pagina.totalElements || state.total;
                renderizar();
            })
            .catch(function (erro) {
                toast(erro.message, 'error');
            })
            .finally(function () {
                el.maisBtn.disabled = false;
            });
    }

    /** Recarrega a página e o resumo depois de qualquer escrita. */
    function recarregar() {
        return Promise.all([api.listar(0), api.resumo(), api.projetos(), api.etiquetas()])
            .then(function (respostas) {
                state.tarefas = respostas[0].content || [];
                state.total = respostas[0].totalElements || 0;
                state.paginaCarregada = 0;
                state.resumo = respostas[1];
                state.projetos = Array.isArray(respostas[2]) ? respostas[2] : [];
                state.etiquetas = Array.isArray(respostas[3]) ? respostas[3] : [];

                renderizar();
                atualizarResumo();
                renderizarProjetos();
                renderizarBarraDeEtiquetas();
                preencherSelectDeProjetos(el.projetoSelect, el.projetoSelect.value || null);
                montarEscolhaDeEtiquetas(el.etiquetasEscolha, el.campoEtiquetas,
                        etiquetasEscolhidas(el.etiquetasEscolha));
            })
            .catch(function (erro) {
                if (erro.message !== 'Sessão expirada') {
                    toast(erro.message, 'error');
                }
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
            projetoId: el.projetoSelect.value ? Number(el.projetoSelect.value) : null,
            prazo: el.prazo.value || null,
            prioridade: el.prioridade.value,
            etiquetaIds: etiquetasEscolhidas(el.etiquetasEscolha),
            concluida: false
        })
            .then(function () {
                var projetoMantido = el.projetoSelect.value;
                el.form.reset();
                el.projetoSelect.value = projetoMantido;   // continuar no mesmo projeto
                el.prioridade.value = 'MEDIA';
                // As etiquetas, ao contrário do projeto, são escolhidas por
                // tarefa: mantê-las marcadas faria a próxima herdá-las sem
                // ninguém pedir.
                limparEscolhaDeEtiquetas();
                atualizarContador();
                toast('Tarefa criada com sucesso', 'success');
                el.titulo.focus();
                return recarregar();
            })
            .catch(function (erro) {
                if (erro.message !== 'Sessão expirada') {
                    toast(erro.message, 'error');
                }
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

        // PATCH na rota de conclusão: alternar a situação não deveria exigir
        // reenviar título, descrição, projeto e prazo.
        api.conclusao(id, !tarefa.concluida)
            .then(function (atualizada) {
                toast(atualizada.concluida ? 'Tarefa concluída' : 'Tarefa reaberta', 'success');
                return recarregar();
            })
            .catch(function (erro) {
                noDaTarefa.classList.remove('is-busy');
                if (erro.message !== 'Sessão expirada') {
                    toast(erro.message, 'error');
                }
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
                toast('Tarefa excluída', 'success');
                return recarregar();
            })
            .catch(function (erro) {
                noDaTarefa.classList.remove('is-removing');
                if (erro.message !== 'Sessão expirada') {
                    toast(erro.message, 'error');
                }
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
            projetoId: el.editProjeto.value ? Number(el.editProjeto.value) : null,
            prazo: el.editPrazo.value || null,
            prioridade: el.editPrioridade.value,
            etiquetaIds: etiquetasEscolhidas(el.editEtiquetasEscolha),
            concluida: el.editConcluida.checked
        })
            .then(function () {
                fecharModal();
                toast('Tarefa atualizada', 'success');
                return recarregar();
            })
            .catch(function (erro) {
                if (erro.message !== 'Sessão expirada') {
                    toast(erro.message, 'error');
                }
            });
    }

    function buscarNoEstado(id) {
        return state.tarefas.filter(function (t) { return t.id === id; })[0];
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
        el.editPrazo.value = tarefa.prazo || '';
        el.editPrioridade.value = tarefa.prioridade || 'MEDIA';
        el.editConcluida.checked = !!tarefa.concluida;
        preencherSelectDeProjetos(el.editProjeto, tarefa.projeto ? tarefa.projeto.id : null);
        montarEscolhaDeEtiquetas(el.editEtiquetasEscolha, el.editCampoEtiquetas,
                (tarefa.etiquetas || []).map(function (e) { return e.id; }));
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

    /* ------------------------------------------------------------ Hábitos */

    var NOMES_DOS_DIAS = ['seg', 'ter', 'qua', 'qui', 'sex', 'sáb', 'dom'];

    function montarDiasDaSemana() {
        NOMES_DOS_DIAS.forEach(function (nome, indice) {
            var botao = document.createElement('button');
            botao.type = 'button';
            botao.className = 'dia-semana';
            botao.dataset.dia = indice + 1;          // ISO: 1 = segunda
            botao.textContent = nome;
            botao.setAttribute('aria-pressed', 'false');

            botao.addEventListener('click', function () {
                var ativo = botao.classList.toggle('is-active');
                botao.setAttribute('aria-pressed', ativo ? 'true' : 'false');
            });

            el.habitoDias.appendChild(botao);
        });
    }

    function diasEscolhidos() {
        return Array.prototype.filter
                .call(el.habitoDias.children, function (b) {
                    return b.classList.contains('is-active');
                })
                .map(function (b) { return Number(b.dataset.dia); });
    }

    function limparDiasEscolhidos() {
        Array.prototype.forEach.call(el.habitoDias.children, function (b) {
            b.classList.remove('is-active');
            b.setAttribute('aria-pressed', 'false');
        });
    }

    function montarHabito(habito, indice) {
        var no = el.habitoTemplate.content.firstElementChild.cloneNode(true);
        var cor = corDoProjeto(habito.cor);

        no.dataset.id = habito.id;
        no.style.animationDelay = Math.min(indice, 8) * 32 + 'ms';
        no.style.setProperty('--ponto', cor);

        no.querySelector('.habito__nome').textContent = habito.nome;

        var sequencia = no.querySelector('.habito__sequencia');
        sequencia.textContent = habito.sequenciaAtual === 1
                ? '1 dia seguido'
                : habito.sequenciaAtual + ' dias seguidos';
        sequencia.classList.toggle('e-zerada', habito.sequenciaAtual === 0);

        var grade = no.querySelector('.habito__grade');
        var hoje = hojeISO();
        habito.ultimosDias.forEach(function (dia) {
            var celula = document.createElement('span');
            celula.className = 'habito__dia'
                    + (dia.feito ? ' e-feito' : '')
                    + (dia.aplicavel ? '' : ' e-folga')
                    + (dia.data === hoje ? ' e-hoje' : '');
            celula.title = formatarDia(dia.data) + ' — '
                    + (!dia.aplicavel ? 'folga' : dia.feito ? 'feito' : 'não feito');
            grade.appendChild(celula);
        });

        no.querySelector('.habito__recorde').textContent = habito.maiorSequencia === 0
                ? 'Sem recorde ainda'
                : 'Recorde: ' + habito.maiorSequencia
                        + (habito.maiorSequencia === 1 ? ' dia' : ' dias');

        var botaoHoje = no.querySelector('.habito__hoje');
        if (!habito.aplicavelHoje) {
            botaoHoje.textContent = 'Folga hoje';
            botaoHoje.disabled = true;
        } else {
            botaoHoje.textContent = habito.feitoHoje ? '✓ Feito hoje' : 'Marcar hoje';
            botaoHoje.classList.toggle('e-feito', habito.feitoHoje);
        }

        return no;
    }

    function renderizarHabitos() {
        el.listaHabitos.textContent = '';

        var fragmento = document.createDocumentFragment();
        state.habitos.forEach(function (habito, i) {
            fragmento.appendChild(montarHabito(habito, i));
        });
        el.listaHabitos.appendChild(fragmento);

        el.habitosVazio.hidden = state.habitos.length > 0;
    }

    function carregarHabitos() {
        return api.habitos()
            .then(function (habitos) {
                state.habitos = Array.isArray(habitos) ? habitos : [];
                renderizarHabitos();
            })
            .catch(function (erro) {
                if (erro.message !== 'Sessão expirada') {
                    toast(erro.message, 'error');
                }
            });
    }

    el.listaHabitos.addEventListener('click', function (evento) {
        var botao = evento.target.closest('[data-acao]');
        if (!botao) {
            return;
        }

        var no = botao.closest('.habito');
        var id = Number(no.dataset.id);
        var habito = state.habitos.filter(function (h) { return h.id === id; })[0];
        if (!habito) {
            return;
        }

        if (botao.dataset.acao === 'excluir') {
            if (!window.confirm('Excluir o hábito "' + habito.nome
                    + '"? O histórico de dias vai junto.')) {
                return;
            }

            api.deletarHabito(id)
                .then(function () {
                    toast('Hábito excluído', 'success');
                    return carregarHabitos();
                })
                .catch(function (erro) {
                    if (erro.message !== 'Sessão expirada') {
                        toast(erro.message, 'error');
                    }
                });
            return;
        }

        // Marcar ou desmarcar o dia de hoje
        botao.disabled = true;
        var acao = habito.feitoHoje ? api.desmarcarHabito : api.marcarHabito;

        acao(id, hojeISO())
            .then(function () {
                return carregarHabitos();
            })
            .catch(function (erro) {
                botao.disabled = false;
                if (erro.message !== 'Sessão expirada') {
                    toast(erro.message, 'error');
                }
            });
    });

    el.habitoForm.addEventListener('submit', function (evento) {
        evento.preventDefault();

        var nome = el.habitoNome.value.trim();
        if (!nome) {
            el.habitoErro.textContent = 'Informe o nome do hábito.';
            return;
        }

        el.habitoErro.textContent = '';
        el.habitoSubmit.disabled = true;

        api.criarHabito({
            nome: nome,
            cor: corDoNovoHabito,
            diasSemana: diasEscolhidos()
        })
            .then(function () {
                el.habitoNome.value = '';
                limparDiasEscolhidos();
                toast('Hábito criado', 'success');
                return carregarHabitos();
            })
            .catch(function (erro) {
                if (erro.message !== 'Sessão expirada') {
                    el.habitoErro.textContent = erro.message;
                }
            })
            .finally(function () {
                el.habitoSubmit.disabled = false;
            });
    });

    var corDoNovoHabito = 'verde';

    function montarCoresDeHabito() {
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
                corDoNovoHabito = cor.id;
                Array.prototype.forEach.call(el.habitoCores.children, function (outro) {
                    var ativo = outro.dataset.cor === cor.id;
                    outro.classList.toggle('is-active', ativo);
                    outro.setAttribute('aria-checked', ativo ? 'true' : 'false');
                });
            });

            el.habitoCores.appendChild(botao);
        });

        Array.prototype.forEach.call(el.habitoCores.children, function (botao) {
            botao.classList.toggle('is-active', botao.dataset.cor === corDoNovoHabito);
        });
    }

    /* ------------------------------------------------------------- Painel */

    var NOME_DA_PRIORIDADE = {
        BAIXA: 'Baixa', MEDIA: 'Média', ALTA: 'Alta', URGENTE: 'Urgente'
    };

    /**
     * Barra horizontal com nome e valor sempre visíveis.
     *
     * A escolha da forma veio da validação da paleta: seis tons de acento
     * escolhidos pelo usuário não passam numa checagem de todos os pares —
     * sempre há um par que alguma forma de daltonismo colapsa. Aqui a
     * identidade vem da posição e do texto, e a cor é só reforço. Por isso
     * também não há legenda: não existe cor para casar com nome.
     */
    function montarBarra(nome, valor, base, cor, sufixo) {
        var linha = document.createElement('div');
        linha.className = 'barra';

        var rotulo = document.createElement('span');
        rotulo.className = 'barra__nome';
        rotulo.textContent = nome;

        var numero = document.createElement('span');
        numero.className = 'barra__valor';
        numero.textContent = valor + (sufixo || '');

        var trilha = document.createElement('div');
        trilha.className = 'barra__trilha';

        var preenchimento = document.createElement('div');
        preenchimento.className = 'barra__preenchimento';
        preenchimento.style.width = (base > 0 ? (valor / base) * 100 : 0) + '%';
        if (cor) {
            preenchimento.style.setProperty('--ponto', corDoProjeto(cor));
        }

        trilha.appendChild(preenchimento);
        linha.appendChild(rotulo);
        linha.appendChild(numero);
        linha.appendChild(trilha);
        return linha;
    }

    /**
     * O denominador da barra depende da pergunta que o gráfico responde.
     *
     * 'total' para distribuições — "onde está o meu trabalho?" — em que a
     * barra é a fatia do conjunto. Normalizar pelo máximo aqui encheria as
     * três barras quando os valores fossem iguais, e à primeira vista pareceria
     * que tudo está no limite.
     *
     * 'maximo' para comparação de grandeza, como as sequências de hábitos, em
     * que o que interessa é qual é a maior.
     */
    function preencherBarras(container, vazio, itens, modo, sufixo) {
        container.textContent = '';
        vazio.hidden = itens.length > 0;

        var base = modo === 'total'
                ? itens.reduce(function (soma, item) { return soma + item.valor; }, 0)
                : itens.reduce(function (maior, item) {
                    return Math.max(maior, item.valor);
                }, 0);

        itens.forEach(function (item) {
            container.appendChild(montarBarra(item.nome, item.valor, base, item.cor, sufixo));
        });
    }

    function renderizarSerie(pontos) {
        el.serie.textContent = '';
        el.tabelaSerieCorpo.textContent = '';

        var maximo = pontos.reduce(function (maior, ponto) {
            return Math.max(maior, ponto.quantidade);
        }, 0);
        var total = pontos.reduce(function (soma, ponto) {
            return soma + ponto.quantidade;
        }, 0);

        pontos.forEach(function (ponto) {
            var barra = document.createElement('div');
            barra.className = 'serie__barra' + (ponto.quantidade === 0 ? ' e-zero' : '');
            // Altura mínima de 2px também no zero: um dia sem conclusão é
            // informação, e sumir do gráfico esconderia o intervalo.
            barra.style.height = maximo > 0
                    ? Math.max((ponto.quantidade / maximo) * 100, 1.5) + '%'
                    : '2px';
            barra.tabIndex = 0;
            barra.dataset.dica = formatarDia(ponto.data) + ': '
                    + ponto.quantidade + (ponto.quantidade === 1 ? ' tarefa' : ' tarefas');
            barra.setAttribute('aria-label', barra.dataset.dica);
            el.serie.appendChild(barra);

            var linha = document.createElement('tr');
            var dia = document.createElement('td');
            dia.textContent = formatarDia(ponto.data);
            var quantidade = document.createElement('td');
            quantidade.textContent = ponto.quantidade;
            linha.appendChild(dia);
            linha.appendChild(quantidade);
            el.tabelaSerieCorpo.appendChild(linha);
        });

        if (pontos.length > 0) {
            el.serieInicio.textContent = formatarDia(pontos[0].data);
            el.serieFim.textContent = formatarDia(pontos[pontos.length - 1].data);
        }

        el.resumoSerie.textContent = total + ' tarefas concluídas em '
                + pontos.length + ' dias, com pico de ' + maximo + ' num único dia.';
    }

    function carregarPainel() {
        return api.painel()
            .then(function (painel) {
                var resumo = painel.resumo;

                el.tileAtrasadas.textContent = resumo.atrasadas;
                el.tileAtrasadas.classList.toggle('e-alerta', resumo.atrasadas > 0);
                el.tileHoje.textContent = resumo.vencemHoje;
                el.tileHoje.classList.toggle('e-atencao', resumo.vencemHoje > 0);

                el.tileTempo.textContent = painel.horasMediasParaConcluir === null
                        || painel.horasMediasParaConcluir === undefined
                        ? '—'
                        : formatarDuracao(painel.horasMediasParaConcluir);

                renderizarSerie(painel.concluidasPorDia || []);

                preencherBarras(el.barrasProjeto, el.vazioProjeto,
                        (painel.pendentesPorProjeto || []).map(function (c) {
                            return { nome: c.rotulo, valor: c.quantidade, cor: c.cor };
                        }), 'total');

                preencherBarras(el.barrasPrioridade, el.vazioPrioridade,
                        (painel.pendentesPorPrioridade || []).map(function (c) {
                            return {
                                nome: NOME_DA_PRIORIDADE[c.rotulo] || c.rotulo,
                                valor: c.quantidade,
                                cor: c.cor
                            };
                        }), 'total');

                preencherBarras(el.barrasHabitos, el.vazioHabitos,
                        (painel.habitos || []).map(function (h) {
                            return { nome: h.nome, valor: h.atual, cor: h.cor };
                        }), 'maximo', ' d');
            })
            .catch(function (erro) {
                if (erro.message !== 'Sessão expirada') {
                    toast(erro.message, 'error');
                }
            });
    }

    /** Horas viram dias quando passam de 48: "72 h" diz menos que "3 dias". */
    function formatarDuracao(horas) {
        if (horas < 1) {
            return Math.round(horas * 60) + ' min';
        }
        if (horas < 48) {
            return (Math.round(horas * 10) / 10) + ' h';
        }
        return (Math.round(horas / 24 * 10) / 10) + ' dias';
    }

    el.verNumeros.addEventListener('click', function () {
        var mostrando = el.tabelaSerie.hidden;
        el.tabelaSerie.hidden = !mostrando;
        el.verNumeros.setAttribute('aria-expanded', mostrando ? 'true' : 'false');
        el.verNumeros.textContent = mostrando ? 'Ocultar números' : 'Ver números';
    });

    /* --------------------------------------------------- Exportar/importar */

    el.exportarBtn.addEventListener('click', function () {
        el.dadosErro.textContent = '';
        el.exportarBtn.disabled = true;

        api.exportar()
            .then(function (dados) {
                var texto = JSON.stringify(dados, null, 2);
                var url = URL.createObjectURL(
                        new Blob([texto], { type: 'application/json' }));

                var link = document.createElement('a');
                link.href = url;
                link.download = 'todolist-' + hojeISO() + '.json';
                document.body.appendChild(link);
                link.click();
                link.remove();
                // Liberar depois do clique: revogar antes cancelaria o download.
                setTimeout(function () { URL.revokeObjectURL(url); }, 1000);

                toast('Arquivo gerado', 'success');
            })
            .catch(function (erro) {
                if (erro.message !== 'Sessão expirada') {
                    el.dadosErro.textContent = erro.message;
                }
            })
            .finally(function () {
                el.exportarBtn.disabled = false;
            });
    });

    el.importarBtn.addEventListener('click', function () {
        el.dadosErro.textContent = '';
        el.importarArquivo.click();
    });

    el.importarArquivo.addEventListener('change', function () {
        var arquivo = el.importarArquivo.files && el.importarArquivo.files[0];
        if (!arquivo) {
            return;
        }

        var leitor = new FileReader();

        leitor.onload = function () {
            var dados;
            try {
                dados = JSON.parse(leitor.result);
            } catch (e) {
                el.dadosErro.textContent = 'O arquivo não é um JSON válido.';
                el.importarArquivo.value = '';
                return;
            }

            el.importarBtn.disabled = true;

            api.importar(dados)
                .then(function (resultado) {
                    var partes = [];
                    if (resultado.tarefas) { partes.push(resultado.tarefas + ' tarefas'); }
                    if (resultado.projetos) { partes.push(resultado.projetos + ' projetos'); }
                    if (resultado.etiquetas) { partes.push(resultado.etiquetas + ' etiquetas'); }
                    if (resultado.habitos) { partes.push(resultado.habitos + ' hábitos'); }

                    toast(partes.length ? 'Importado: ' + partes.join(', ')
                            : 'Nada novo para importar', 'success');

                    if (resultado.reaproveitados && resultado.reaproveitados.length) {
                        el.dadosErro.textContent = 'Já existiam e foram reaproveitados: '
                                + resultado.reaproveitados.join(', ');
                    }

                    return recarregar();
                })
                .catch(function (erro) {
                    if (erro.message !== 'Sessão expirada') {
                        el.dadosErro.textContent = erro.message;
                    }
                })
                .finally(function () {
                    el.importarBtn.disabled = false;
                    // Zera o campo para o mesmo arquivo poder ser escolhido de novo.
                    el.importarArquivo.value = '';
                });
        };

        leitor.readAsText(arquivo);
    });

    /* ---------------------------------------------------- Atalhos de teclado */

    function digitando(alvo) {
        if (!alvo) {
            return false;
        }
        var etiqueta = alvo.tagName;
        return etiqueta === 'INPUT' || etiqueta === 'TEXTAREA' || etiqueta === 'SELECT'
                || alvo.isContentEditable;
    }

    function algumModalAberto() {
        return !el.modal.hidden || !el.settingsModal.hidden || !el.projetosModal.hidden;
    }

    document.addEventListener('keydown', function (evento) {
        // Nunca sequestra teclas de quem está escrevendo, nem com modal aberto,
        // nem com combinação de modificadores (que pertencem ao navegador).
        if (digitando(evento.target) || algumModalAberto()
                || evento.ctrlKey || evento.metaKey || evento.altKey) {
            return;
        }
        if (el.appView.hidden) {
            return;
        }

        var atalhos = {
            n: function () { trocarAba('tarefas'); el.titulo.focus(); },
            '/': function () { trocarAba('tarefas'); el.busca.focus(); },
            1: function () { trocarAba('tarefas'); },
            2: function () { trocarAba('habitos'); },
            3: function () { trocarAba('painel'); }
        };

        var acao = atalhos[evento.key];
        if (acao) {
            evento.preventDefault();
            acao();
        }
    });

    /* --------------------------------------------------------------- Abas */

    function trocarAba(nome) {
        state.aba = nome;

        Array.prototype.forEach.call(el.abas.children, function (botao) {
            var ativo = botao.dataset.aba === nome;
            botao.classList.toggle('is-active', ativo);
            botao.setAttribute('aria-selected', ativo ? 'true' : 'false');
        });

        el.abaTarefas.hidden = nome !== 'tarefas';
        el.abaHabitos.hidden = nome !== 'habitos';
        el.abaPainel.hidden = nome !== 'painel';

        // Carrega sob demanda: quem só usa tarefas nunca paga pela consulta
        // de hábitos.
        if (nome === 'habitos') {
            carregarHabitos();
        } else if (nome === 'painel') {
            carregarPainel();
        }
    }

    Array.prototype.forEach.call(el.abas.children, function (botao) {
        botao.addEventListener('click', function () {
            trocarAba(botao.dataset.aba);
        });
    });

    /* -------------------------------------------------- Painel de projetos */

    var corDoNovoProjeto = 'indigo';

    function montarCoresDeProjeto() {
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
                corDoNovoProjeto = cor.id;
                marcarCorDeProjeto(cor.id);
            });

            el.projetoCores.appendChild(botao);
        });
        marcarCorDeProjeto(corDoNovoProjeto);
    }

    function marcarCorDeProjeto(id) {
        Array.prototype.forEach.call(el.projetoCores.children, function (botao) {
            var ativo = botao.dataset.cor === id;
            botao.classList.toggle('is-active', ativo);
            botao.setAttribute('aria-checked', ativo ? 'true' : 'false');
        });
    }

    function renderizarListaDeProjetos() {
        el.listaProjetos.textContent = '';

        state.projetos.forEach(function (projeto) {
            var item = document.createElement('li');
            item.className = 'item-projeto';

            var ponto = document.createElement('span');
            ponto.className = 'item-projeto__ponto';
            ponto.style.setProperty('--ponto', corDoProjeto(projeto.cor));

            var nome = document.createElement('span');
            nome.className = 'item-projeto__nome';
            nome.textContent = projeto.nome;

            var contagem = document.createElement('span');
            contagem.className = 'item-projeto__contagem';
            contagem.textContent = projeto.tarefasPendentes === 1
                    ? '1 pendente'
                    : projeto.tarefasPendentes + ' pendentes';

            var excluir = document.createElement('button');
            excluir.type = 'button';
            excluir.className = 'icon-btn icon-btn--danger';
            excluir.title = 'Excluir projeto';
            excluir.setAttribute('aria-label', 'Excluir o projeto ' + projeto.nome);
            excluir.innerHTML = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" '
                    + 'stroke-width="2" stroke-linecap="round" stroke-linejoin="round">'
                    + '<path d="M4 7h16M10 11v6M14 11v6"></path>'
                    + '<path d="M6 7l1 12a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2l1-12"></path>'
                    + '<path d="M9 7V5a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"></path></svg>';

            excluir.addEventListener('click', function () {
                if (!window.confirm('Excluir o projeto "' + projeto.nome
                        + '"? As tarefas dele voltam para a caixa de entrada.')) {
                    return;
                }

                api.deletarProjeto(projeto.id)
                    .then(function () {
                        if (state.projetoAtivo === projeto.id) {
                            state.projetoAtivo = null;
                        }
                        toast('Projeto excluído', 'success');
                        return recarregar();
                    })
                    .then(renderizarListaDeProjetos)
                    .catch(function (erro) {
                        if (erro.message !== 'Sessão expirada') {
                            toast(erro.message, 'error');
                        }
                    });
            });

            item.appendChild(ponto);
            item.appendChild(nome);
            item.appendChild(contagem);
            item.appendChild(excluir);
            el.listaProjetos.appendChild(item);
        });
    }

    var corDaNovaEtiqueta = 'rosa';

    function montarCoresDeEtiqueta() {
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
                corDaNovaEtiqueta = cor.id;
                marcarCorDeEtiqueta(cor.id);
            });

            el.etiquetaCores.appendChild(botao);
        });
        marcarCorDeEtiqueta(corDaNovaEtiqueta);
    }

    function marcarCorDeEtiqueta(id) {
        Array.prototype.forEach.call(el.etiquetaCores.children, function (botao) {
            var ativo = botao.dataset.cor === id;
            botao.classList.toggle('is-active', ativo);
            botao.setAttribute('aria-checked', ativo ? 'true' : 'false');
        });
    }

    function renderizarListaDeEtiquetas() {
        el.listaEtiquetas.textContent = '';

        state.etiquetas.forEach(function (etiqueta) {
            var item = document.createElement('li');
            item.className = 'item-projeto';

            var ponto = document.createElement('span');
            ponto.className = 'item-projeto__ponto';
            ponto.style.setProperty('--ponto', corDoProjeto(etiqueta.cor));

            var nome = document.createElement('span');
            nome.className = 'item-projeto__nome';
            nome.textContent = etiqueta.nome;

            var contagem = document.createElement('span');
            contagem.className = 'item-projeto__contagem';
            contagem.textContent = etiqueta.tarefasPendentes === 1
                    ? '1 pendente'
                    : etiqueta.tarefasPendentes + ' pendentes';

            var excluir = document.createElement('button');
            excluir.type = 'button';
            excluir.className = 'icon-btn icon-btn--danger';
            excluir.title = 'Excluir etiqueta';
            excluir.setAttribute('aria-label', 'Excluir a etiqueta ' + etiqueta.nome);
            excluir.innerHTML = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" '
                    + 'stroke-width="2" stroke-linecap="round" stroke-linejoin="round">'
                    + '<path d="M4 7h16M10 11v6M14 11v6"></path>'
                    + '<path d="M6 7l1 12a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2l1-12"></path>'
                    + '<path d="M9 7V5a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"></path></svg>';

            excluir.addEventListener('click', function () {
                if (!window.confirm('Excluir a etiqueta "' + etiqueta.nome
                        + '"? As tarefas continuam, apenas sem essa marcação.')) {
                    return;
                }

                api.deletarEtiqueta(etiqueta.id)
                    .then(function () {
                        if (state.etiquetaAtiva === etiqueta.id) {
                            state.etiquetaAtiva = null;
                        }
                        toast('Etiqueta excluída', 'success');
                        return recarregar();
                    })
                    .then(renderizarListaDeEtiquetas)
                    .catch(function (erro) {
                        if (erro.message !== 'Sessão expirada') {
                            toast(erro.message, 'error');
                        }
                    });
            });

            item.appendChild(ponto);
            item.appendChild(nome);
            item.appendChild(contagem);
            item.appendChild(excluir);
            el.listaEtiquetas.appendChild(item);
        });
    }

    function abrirProjetos() {
        focoAnterior = document.activeElement;
        el.projetoNome.value = '';
        el.projetoErro.textContent = '';
        el.etiquetaNome.value = '';
        el.etiquetaErro.textContent = '';
        renderizarListaDeProjetos();
        renderizarListaDeEtiquetas();
        el.projetosModal.hidden = false;
        el.projetoNome.focus();
    }

    function fecharProjetos() {
        el.projetosModal.hidden = true;
        devolverFoco();
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
    el.maisBtn.addEventListener('click', carregarMais);
    el.titulo.addEventListener('input', limparErroTitulo);
    el.descricao.addEventListener('input', atualizarContador);

    el.editTitulo.addEventListener('input', function () {
        el.editTitulo.classList.remove('is-invalid');
        el.editTituloError.textContent = '';
    });

    // Busca com espera: a consulta vai ao servidor, então disparar a cada tecla
    // seria uma requisição por letra digitada.
    var esperaDaBusca = null;
    el.busca.addEventListener('input', function () {
        clearTimeout(esperaDaBusca);
        esperaDaBusca = setTimeout(function () {
            state.busca = el.busca.value.trim();
            carregar();
        }, 320);
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
            carregar();
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

    Array.prototype.forEach.call(
        el.projetosModal.querySelectorAll('[data-close-projetos]'), function (botao) {
            botao.addEventListener('click', fecharProjetos);
        });

    el.etiquetaForm.addEventListener('submit', function (evento) {
        evento.preventDefault();

        var nome = el.etiquetaNome.value.trim();
        if (!nome) {
            el.etiquetaErro.textContent = 'Informe o nome da etiqueta.';
            return;
        }

        el.etiquetaErro.textContent = '';
        el.etiquetaSubmit.disabled = true;

        api.criarEtiqueta({ nome: nome, cor: corDaNovaEtiqueta })
            .then(function () {
                el.etiquetaNome.value = '';
                toast('Etiqueta criada', 'success');
                return recarregar();
            })
            .then(renderizarListaDeEtiquetas)
            .catch(function (erro) {
                if (erro.message !== 'Sessão expirada') {
                    el.etiquetaErro.textContent = erro.message;
                }
            })
            .finally(function () {
                el.etiquetaSubmit.disabled = false;
            });
    });

    el.projetoForm.addEventListener('submit', function (evento) {
        evento.preventDefault();

        var nome = el.projetoNome.value.trim();
        if (!nome) {
            el.projetoErro.textContent = 'Informe o nome do projeto.';
            return;
        }

        el.projetoErro.textContent = '';
        el.projetoSubmit.disabled = true;

        api.criarProjeto({ nome: nome, cor: corDoNovoProjeto })
            .then(function () {
                el.projetoNome.value = '';
                toast('Projeto criado', 'success');
                return recarregar();
            })
            .then(renderizarListaDeProjetos)
            .catch(function (erro) {
                if (erro.message !== 'Sessão expirada') {
                    el.projetoErro.textContent = erro.message;
                }
            })
            .finally(function () {
                el.projetoSubmit.disabled = false;
            });
    });

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
        } else if (!el.projetosModal.hidden) {
            fecharProjetos();
        }
    });

    /* --------------------------------------------------- Tela de entrada */

    var modoAuth = 'login';

    function definirModo(modo) {
        modoAuth = modo;

        Array.prototype.forEach.call(el.authModo.children, function (botao) {
            var ativo = botao.dataset.modo === modo;
            botao.classList.toggle('is-active', ativo);
            botao.setAttribute('aria-selected', ativo ? 'true' : 'false');
        });

        var cadastro = modo === 'registrar';
        el.campoNome.hidden = !cadastro;
        el.authDica.hidden = !cadastro;
        el.authSubmit.textContent = cadastro ? 'Criar conta' : 'Entrar';
        el.authLinha.textContent = cadastro
                ? 'Crie sua conta para começar.'
                : 'Entre para ver as suas tarefas.';
        el.authSenha.setAttribute('autocomplete', cadastro ? 'new-password' : 'current-password');
        el.authErro.textContent = '';
    }

    Array.prototype.forEach.call(el.authModo.children, function (botao) {
        botao.addEventListener('click', function () {
            definirModo(botao.dataset.modo);
        });
    });

    el.authForm.addEventListener('submit', function (evento) {
        evento.preventDefault();

        var email = el.authEmail.value.trim();
        var senha = el.authSenha.value;
        var nome = el.authNome.value.trim();

        if (!email || !senha) {
            el.authErro.textContent = 'Preencha e-mail e senha.';
            return;
        }
        if (modoAuth === 'registrar' && !nome) {
            el.authErro.textContent = 'Informe o seu nome.';
            return;
        }

        el.authErro.textContent = '';
        el.authSubmit.disabled = true;

        var cadastro = modoAuth === 'registrar';
        var promessa = cadastro
                ? auth.registrar({ nome: nome, email: email, senha: senha })
                : auth.login({ email: email, senha: senha });

        promessa
            .then(function (resposta) {
                sessao.token = resposta.token;
                gravarToken(resposta.token);
                entrarNaAplicacao(resposta.usuario);
                toast(cadastro ? 'Conta criada. Boas-vindas!' : 'Bem-vindo de volta', 'success');
            })
            .catch(function (erro) {
                el.authErro.textContent = erro.message;
            })
            .finally(function () {
                el.authSubmit.disabled = false;
            });
    });

    el.logoutBtn.addEventListener('click', function () {
        encerrarSessao('Você saiu da conta.');
    });

    /* ------------------------------------------------------------- Início */

    // Instalável e abrível sem rede. Falhar aqui não pode derrubar a aplicação:
    // service worker é melhoria, não requisito.
    if ('serviceWorker' in navigator) {
        window.addEventListener('load', function () {
            navigator.serviceWorker.register('/sw.js').catch(function () {
                // Sem service worker o aplicativo funciona igual, só não offline.
            });
        });
    }

    montarAmostras();
    montarCoresDeProjeto();
    montarCoresDeEtiqueta();
    montarCoresDeHabito();
    montarDiasDaSemana();
    atualizarContador();
    definirModo('login');

    sessao.token = lerToken();

    if (!sessao.token) {
        mostrarEntrada();
    } else {
        // Um token guardado pode ter expirado enquanto a aba estava fechada:
        // confirma com a API antes de exibir a aplicação.
        auth.eu()
            .then(entrarNaAplicacao)
            .catch(function () {
                encerrarSessao();
            });
    }
})();
