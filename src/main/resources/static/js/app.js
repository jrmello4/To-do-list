/**
 * To-do List - Frontend Application Logic
 * Nível 1 (Prioridades, Vencimento, Lixeira)
 * Nível 2 (Kanban Drag & Drop, Subtarefas, Exportação CSV)
 * Nível 4 (Autenticação JWT, Multi-usuário, Perfil)
 * Novidades: Modo Foco Pomodoro, Dashboard com Chart.js, Tarefas Recorrentes
 */

document.addEventListener('DOMContentLoaded', () => {
  // --- DOM Elements ---
  const themeToggle = document.getElementById('themeToggle');
  const sunIcon = document.getElementById('sunIcon');
  const moonIcon = document.getElementById('moonIcon');

  // Navigation
  const viewTabButtons = document.querySelectorAll('.view-tab-btn');
  const listView = document.getElementById('listView');
  const kanbanView = document.getElementById('kanbanView');
  const trashView = document.getElementById('trashView');
  const statsView = document.getElementById('statsView');
  const createTaskSection = document.getElementById('createTaskSection');
  const mainToolbar = document.getElementById('mainToolbar');
  const trashCountBadge = document.getElementById('trashCountBadge');

  // Metrics (Dashboard cards no topo)
  const metricTotal = document.getElementById('metricTotal');
  const metricPending = document.getElementById('metricPending');
  const metricOverdue = document.getElementById('metricOverdue');
  const metricCompleted = document.getElementById('metricCompleted');
  const progressPercentage = document.getElementById('progressPercentage');
  const progressBarFill = document.getElementById('progressBarFill');

  // Pomodoro Elements
  const pomodoroSection = document.getElementById('pomodoroSection');
  const pomodoroTaskLabel = document.getElementById('pomodoroTaskLabel');
  const pomodoroDisplay = document.getElementById('pomodoroDisplay');
  const btnPomoStart = document.getElementById('btnPomoStart');
  const btnPomoPause = document.getElementById('btnPomoPause');
  const btnPomoReset = document.getElementById('btnPomoReset');
  const pomoModeButtons = document.querySelectorAll('.pomo-mode-btn');

  // Stats Elements (Chart.js View)
  const btnRefreshStats = document.getElementById('btnRefreshStats');
  const kpiOnTimeRate = document.getElementById('kpiOnTimeRate');
  const kpiPomodorosDone = document.getElementById('kpiPomodorosDone');
  const kpiRecurringTasks = document.getElementById('kpiRecurringTasks');
  const categoryChartCanvas = document.getElementById('categoryChart');
  const priorityChartCanvas = document.getElementById('priorityChart');
  const weeklyTrendChartCanvas = document.getElementById('weeklyTrendChart');

  // Forms & Filters
  const createTaskForm = document.getElementById('createTaskForm');
  const taskRecurrence = document.getElementById('taskRecurrence');
  const taskEstimatedPomodoros = document.getElementById('taskEstimatedPomodoros');
  const searchInput = document.getElementById('searchInput');
  const clearSearchBtn = document.getElementById('clearSearchBtn');
  const categoryFilter = document.getElementById('categoryFilter');
  const priorityFilter = document.getElementById('priorityFilter');
  const statusFilterButtons = document.querySelectorAll('#statusTabs .filter-btn');

  // List & Trash Containers
  const taskList = document.getElementById('taskList');
  const emptyState = document.getElementById('emptyState');
  const emptyMessage = document.getElementById('emptyMessage');
  const trashList = document.getElementById('trashList');
  const emptyTrashState = document.getElementById('emptyTrashState');
  const btnEmptyTrash = document.getElementById('btnEmptyTrash');

  // Kanban Columns
  const cardsAFazer = document.getElementById('cardsAFazer');
  const cardsEmAndamento = document.getElementById('cardsEmAndamento');
  const cardsConcluida = document.getElementById('cardsConcluida');
  const countAFazer = document.getElementById('countAFazer');
  const countEmAndamento = document.getElementById('countEmAndamento');
  const countConcluida = document.getElementById('countConcluida');
  const kanbanColumns = document.querySelectorAll('.kanban-column');

  // Edit Task Modal
  const editModal = document.getElementById('editModal');
  const editTaskForm = document.getElementById('editTaskForm');
  const editTaskId = document.getElementById('editTaskId');
  const editTaskTitle = document.getElementById('editTaskTitle');
  const editTaskDescription = document.getElementById('editTaskDescription');
  const editTaskDueDate = document.getElementById('editTaskDueDate');
  const editTaskPriority = document.getElementById('editTaskPriority');
  const editTaskCategory = document.getElementById('editTaskCategory');
  const editTaskStatus = document.getElementById('editTaskStatus');
  const editTaskRecurrence = document.getElementById('editTaskRecurrence');
  const editTaskEstimatedPomodoros = document.getElementById('editTaskEstimatedPomodoros');
  const btnCancelEdit = document.getElementById('btnCancelEdit');
  const btnModalCancel = document.getElementById('btnModalCancel');
  const modalBackdrop = document.getElementById('modalBackdrop');
  const editTaskTagsList = document.getElementById('editTaskTagsList');
  const editTaskAttachmentsList = document.getElementById('editTaskAttachmentsList');
  const editAttachmentInput = document.getElementById('editAttachmentInput');
  const editAttachmentFileName = document.getElementById('editAttachmentFileName');
  const btnUploadAttachment = document.getElementById('btnUploadAttachment');

  // Header Actions & Auth Elements
  const btnExportCsv = document.getElementById('btnExportCsv');
  const btnExportPdf = document.getElementById('btnExportPdf');
  const btnNotification = document.getElementById('btnNotification');
  const notificationContainer = document.getElementById('notificationContainer');
  const notificationBadge = document.getElementById('notificationBadge');
  const notificationDropdown = document.getElementById('notificationDropdown');
  const notificationList = document.getElementById('notificationList');
  const btnRequestNotifyPerm = document.getElementById('btnRequestNotifyPerm');

  // Tag Elements
  const tagFilter = document.getElementById('tagFilter');
  const createTaskTagsList = document.getElementById('createTaskTagsList');
  const btnOpenCreateTag = document.getElementById('btnOpenCreateTag');
  const tagModal = document.getElementById('tagModal');
  const tagModalBackdrop = document.getElementById('tagModalBackdrop');
  const btnCancelTag = document.getElementById('btnCancelTag');
  const btnTagModalCancel = document.getElementById('btnTagModalCancel');
  const createTagForm = document.getElementById('createTagForm');
  const newTagName = document.getElementById('newTagName');
  const newTagColor = document.getElementById('newTagColor');
  const tagColorPresets = document.getElementById('tagColorPresets');

  const btnLoginOpen = document.getElementById('btnLoginOpen');
  const btnHeaderGuest = document.getElementById('btnHeaderGuest');
  const userProfile = document.getElementById('userProfile');
  const userAvatar = document.getElementById('userAvatar');
  const userName = document.getElementById('userName');
  const userEmail = document.getElementById('userEmail');
  const btnLogout = document.getElementById('btnLogout');

  // Auth Modal Elements
  const authModal = document.getElementById('authModal');
  const authModalBackdrop = document.getElementById('authModalBackdrop');
  const btnCancelAuth = document.getElementById('btnCancelAuth');
  const btnGuestLogin = document.getElementById('btnGuestLogin');
  const tabLogin = document.getElementById('tabLogin');
  const tabRegister = document.getElementById('tabRegister');
  const authAlert = document.getElementById('authAlert');
  const loginForm = document.getElementById('loginForm');
  const registerForm = document.getElementById('registerForm');
  const loginEmail = document.getElementById('loginEmail');
  const loginPassword = document.getElementById('loginPassword');
  const registerName = document.getElementById('registerName');
  const registerEmail = document.getElementById('registerEmail');
  const registerPassword = document.getElementById('registerPassword');

  const toastContainer = document.getElementById('toastContainer');

  // --- State ---
  let currentView = 'list'; // 'list' | 'kanban' | 'trash' | 'stats'
  let currentStatusFilter = 'all'; // 'all' | 'pending' | 'completed'
  let currentCategoryFilter = '';
  let currentPriorityFilter = '';
  let currentTagFilter = '';
  let currentSearch = '';
  let searchTimeout = null;
  let cachedTasks = [];
  let cachedTags = [];
  let currentEditingTaskId = null;
  let notificationPollInterval = null;
  let lastNotifiedTaskIds = new Set();

  // Pomodoro State
  let pomoDuration = 1500; // 25 min default
  let pomoTimeLeft = 1500;
  let pomoInterval = null;
  let pomoMode = 'pomodoro'; // 'pomodoro' | 'short' | 'long'
  let activeFocusTaskId = null;
  let activeFocusTaskTitle = null;

  // Chart Instances
  let categoryChartInstance = null;
  let priorityChartInstance = null;
  let weeklyTrendChartInstance = null;

  // --- Theme Management ---
  const initTheme = () => {
    const savedTheme = localStorage.getItem('theme') || 
      (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
    setTheme(savedTheme);
  };

  const setTheme = (theme) => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
    if (theme === 'dark') {
      sunIcon.classList.remove('hidden');
      moonIcon.classList.add('hidden');
    } else {
      sunIcon.classList.add('hidden');
      moonIcon.classList.remove('hidden');
    }
    if (currentView === 'stats') {
      fetchStats();
    }
  };

  themeToggle.addEventListener('click', () => {
    const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
    setTheme(currentTheme === 'dark' ? 'light' : 'dark');
  });

  // --- Toast Notifications ---
  const showToast = (message, type = 'success') => {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<span>${escapeHtml(message)}</span>`;
    toastContainer.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(100%)';
      toast.style.transition = 'all 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, 3200);
  };

  // --- Web Audio Chime Synthesizer (Nativo e Offline) ---
  const playPomodoroChime = () => {
    try {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (!AudioCtx) return;
      const ctx = new AudioCtx();

      const osc = ctx.createOscillator();
      const gain = ctx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(587.33, ctx.currentTime); // D5
      osc.frequency.setValueAtTime(880.00, ctx.currentTime + 0.15); // A5

      gain.gain.setValueAtTime(0.25, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 1.2);

      osc.connect(gain);
      gain.connect(ctx.destination);

      osc.start();
      osc.stop(ctx.currentTime + 1.2);
    } catch (e) {
      console.warn('Web Audio sintetizador indisponível:', e);
    }
  };

  // --- Authentication Helpers ---
  const getStoredToken = () => localStorage.getItem('auth_token');
  const getStoredUser = () => {
    try {
      const u = localStorage.getItem('auth_user');
      return u ? JSON.parse(u) : null;
    } catch {
      return null;
    }
  };

  const setAuthData = (token, user) => {
    localStorage.setItem('auth_token', token);
    localStorage.setItem('auth_user', JSON.stringify(user));
    updateAuthUI();
  };

  const clearAuthData = () => {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
    updateAuthUI();
  };

  const updateAuthUI = () => {
    const user = getStoredUser();
    const token = getStoredToken();
    if (token && user) {
      if (btnLoginOpen) btnLoginOpen.classList.add('hidden');
      if (btnHeaderGuest) btnHeaderGuest.classList.add('hidden');
      if (userProfile) {
        userProfile.classList.remove('hidden');
        userName.textContent = user.nome || 'Usuário';
        userEmail.textContent = user.email || '';
        userAvatar.textContent = (user.nome || user.email || 'U').charAt(0).toUpperCase();
      }
    } else {
      if (btnLoginOpen) btnLoginOpen.classList.remove('hidden');
      if (btnHeaderGuest) btnHeaderGuest.classList.remove('hidden');
      if (userProfile) userProfile.classList.add('hidden');
    }
  };

  const openAuthModal = (tab = 'login') => {
    authAlert.classList.add('hidden');
    authAlert.textContent = '';
    authAlert.className = 'auth-alert hidden';
    switchAuthTab(tab);
    authModal.classList.remove('hidden');
  };

  const closeAuthModal = () => {
    authModal.classList.add('hidden');
  };

  const switchAuthTab = (tab) => {
    authAlert.classList.add('hidden');
    authAlert.textContent = '';
    if (tab === 'login') {
      tabLogin.classList.add('active');
      tabRegister.classList.remove('active');
      loginForm.classList.remove('hidden');
      registerForm.classList.add('hidden');
      setTimeout(() => loginEmail.focus(), 100);
    } else {
      tabRegister.classList.add('active');
      tabLogin.classList.remove('active');
      registerForm.classList.remove('hidden');
      loginForm.classList.add('hidden');
      setTimeout(() => registerName.focus(), 100);
    }
  };

  const showAuthAlert = (message, type = 'error') => {
    authAlert.textContent = message;
    authAlert.className = `auth-alert ${type}`;
    authAlert.classList.remove('hidden');
  };

  // --- Unified Authenticated Fetch Wrapper ---
  const apiFetch = async (url, options = {}) => {
    const token = getStoredToken();
    const headers = { ...(options.headers || {}) };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, { ...options, headers });

    if (response.status === 401) {
      clearAuthData();
      cachedTasks = [];
      resetViewData();
      showAuthAlert('Sua sessão expirou ou o login é necessário. Por favor, autentique-se.', 'error');
      openAuthModal('login');
      throw new Error('Não autenticado (401)');
    }

    return response;
  };

  const resetViewData = () => {
    renderListView([]);
    renderKanbanView([]);
    trashList.innerHTML = '';
    emptyTrashState.classList.remove('hidden');
    metricTotal.textContent = '0';
    metricPending.textContent = '0';
    metricOverdue.textContent = '0';
    metricCompleted.textContent = '0';
    trashCountBadge.textContent = '0';
    progressPercentage.textContent = '0%';
    progressBarFill.style.width = '0%';
    cachedTags = [];
    if (tagFilter) tagFilter.innerHTML = '<option value="">Todas as Tags</option>';
    if (createTaskTagsList) createTaskTagsList.innerHTML = '<span class="text-muted" style="font-size:0.82rem;">Faça login para gerenciar tags.</span>';
    if (notificationBadge) notificationBadge.classList.add('hidden');
    if (notificationList) notificationList.innerHTML = '<div class="notification-empty">Nenhum alerta pendente 🎉</div>';
    if (notificationPollInterval) clearInterval(notificationPollInterval);
  };

  // --- View Switcher ---
  viewTabButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      viewTabButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      currentView = btn.dataset.view;

      listView.classList.add('hidden');
      kanbanView.classList.add('hidden');
      trashView.classList.add('hidden');
      statsView.classList.add('hidden');

      if (currentView === 'list') {
        listView.classList.remove('hidden');
        createTaskSection.classList.remove('hidden');
        mainToolbar.classList.remove('hidden');
        pomodoroSection.classList.remove('hidden');
        fetchTasks();
      } else if (currentView === 'kanban') {
        kanbanView.classList.remove('hidden');
        createTaskSection.classList.remove('hidden');
        mainToolbar.classList.remove('hidden');
        pomodoroSection.classList.remove('hidden');
        fetchTasks();
      } else if (currentView === 'trash') {
        trashView.classList.remove('hidden');
        createTaskSection.classList.add('hidden');
        mainToolbar.classList.add('hidden');
        pomodoroSection.classList.add('hidden');
        fetchTrash();
      } else if (currentView === 'stats') {
        statsView.classList.remove('hidden');
        createTaskSection.classList.add('hidden');
        mainToolbar.classList.add('hidden');
        pomodoroSection.classList.remove('hidden');
        fetchStats();
      }
    });
  });

  // --- Pomodoro Controller ---
  const formatTime = (seconds) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  const updatePomodoroDisplay = () => {
    pomodoroDisplay.textContent = formatTime(pomoTimeLeft);
    document.title = `${formatTime(pomoTimeLeft)} - To-do List Focus`;
  };

  const startPomodoro = () => {
    if (pomoInterval) return;
    btnPomoStart.disabled = true;
    btnPomoPause.disabled = false;

    pomoInterval = setInterval(() => {
      if (pomoTimeLeft > 0) {
        pomoTimeLeft--;
        updatePomodoroDisplay();
      } else {
        finishPomodoroCycle();
      }
    }, 1000);
  };

  const pausePomodoro = () => {
    clearInterval(pomoInterval);
    pomoInterval = null;
    btnPomoStart.disabled = false;
    btnPomoPause.disabled = true;
  };

  const resetPomodoro = () => {
    pausePomodoro();
    pomoTimeLeft = pomoDuration;
    updatePomodoroDisplay();
    document.title = 'To-do List - Produtividade & Kanban';
  };

  const finishPomodoroCycle = async () => {
    pausePomodoro();
    playPomodoroChime();

    if (pomoMode === 'pomodoro') {
      if (activeFocusTaskId) {
        try {
          await apiFetch(`/api/tarefas/${activeFocusTaskId}/pomodoro/increment`, { method: 'PATCH' });
          showToast(`🍅 Pomodoro concluído para "${activeFocusTaskTitle}"!`);
          await refreshData();
          if (currentView === 'stats') fetchStats();
        } catch (err) {
          console.error(err);
        }
      } else {
        showToast('🍅 Ciclo de Pomodoro de 25 minutos concluído! Hora da pausa.');
      }
      // Alterna automaticamente para pausa curta
      switchPomodoroMode('short', 300);
    } else {
      showToast('☕ Pausa concluída! Pronto para o próximo foco?');
      switchPomodoroMode('pomodoro', 1500);
    }
  };

  const switchPomodoroMode = (mode, duration) => {
    pomoMode = mode;
    pomoDuration = duration;
    pomoTimeLeft = duration;

    pomoModeButtons.forEach(btn => {
      btn.classList.toggle('active', btn.dataset.mode === mode);
    });

    resetPomodoro();
  };

  pomoModeButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const mode = btn.dataset.mode;
      const time = parseInt(btn.dataset.time, 10);
      switchPomodoroMode(mode, time);
    });
  });

  btnPomoStart.addEventListener('click', startPomodoro);
  btnPomoPause.addEventListener('click', pausePomodoro);
  btnPomoReset.addEventListener('click', resetPomodoro);

  const focusOnTask = (taskId, taskTitle) => {
    activeFocusTaskId = taskId;
    activeFocusTaskTitle = taskTitle;
    pomodoroTaskLabel.textContent = `Focando em: "${taskTitle}"`;
    switchPomodoroMode('pomodoro', 1500);
    pomodoroSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
    startPomodoro();
    showToast(`🍅 Foco iniciado para "${taskTitle}"!`);
  };

  // --- Helper Functions: Tags & Anexos ---
  const isColorLight = (hex) => {
    if (!hex || hex.length < 7) return false;
    const r = parseInt(hex.slice(1, 3), 16) || 0;
    const g = parseInt(hex.slice(3, 5), 16) || 0;
    const b = parseInt(hex.slice(5, 7), 16) || 0;
    const yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000;
    return yiq >= 128;
  };

  const renderTagsHtml = (tags) => {
    if (!tags || tags.length === 0) return '';
    return '<div class="task-tags-group">' + tags.map(t => {
      const isLight = isColorLight(t.cor);
      const textColor = isLight ? '#0f172a' : '#ffffff';
      return `<span class="badge badge-tag" style="background-color: ${t.cor}; color: ${textColor};" data-tag-id="${t.id}" title="Filtrar por #${escapeHtml(t.nome)}">#${escapeHtml(t.nome)}</span>`;
    }).join('') + '</div>';
  };

  const formatFileSize = (bytes) => {
    if (!bytes || bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  };

  const renderAttachmentsHtml = (anexos) => {
    if (!anexos || anexos.length === 0) return '';
    return '<div class="task-attachments-section">' + anexos.map(a => {
      if (a.isImagem) {
        return `<a href="${a.urlDownload}" target="_blank" class="attachment-thumbnail-card" title="${escapeHtml(a.nomeOriginal)} (${formatFileSize(a.tamanho)})">
          <img src="${a.urlDownload}" alt="${escapeHtml(a.nomeOriginal)}" loading="lazy">
        </a>`;
      } else {
        return `<a href="${a.urlDownload}" target="_blank" class="attachment-file-pill" title="Baixar ${escapeHtml(a.nomeOriginal)}">
          📎 ${escapeHtml(a.nomeOriginal)} (${formatFileSize(a.tamanho)})
        </a>`;
      }
    }).join('') + '</div>';
  };

  // --- API Calls ---
  const fetchTasks = async () => {
    try {
      const params = new URLSearchParams();
      if (currentStatusFilter === 'pending') params.append('concluida', 'false');
      if (currentStatusFilter === 'completed') params.append('concluida', 'true');
      if (currentCategoryFilter) params.append('categoria', currentCategoryFilter);
      if (currentPriorityFilter) params.append('prioridade', currentPriorityFilter);
      if (currentTagFilter) params.append('tagId', currentTagFilter);
      if (currentSearch.trim()) params.append('busca', currentSearch.trim());

      const url = `/api/tarefas${params.toString() ? '?' + params.toString() : ''}`;
      const response = await apiFetch(url);
      if (!response.ok) throw new Error('Falha ao carregar tarefas');

      cachedTasks = await response.json();
      if (currentView === 'list') {
        renderListView(cachedTasks);
      } else if (currentView === 'kanban') {
        renderKanbanView(cachedTasks);
      }
    } catch (error) {
      if (error.message !== 'Não autenticado (401)') {
        console.error(error);
        showToast('Erro ao carregar tarefas da API.', 'error');
      }
    }
  };

  const fetchSummary = async () => {
    try {
      const response = await apiFetch('/api/tarefas/resumo');
      if (!response.ok) throw new Error('Falha ao carregar resumo');
      const summary = await response.json();

      metricTotal.textContent = summary.total;
      metricPending.textContent = summary.pendentes;
      metricOverdue.textContent = summary.atrasadas;
      metricCompleted.textContent = summary.concluidas;
      trashCountBadge.textContent = summary.naLixeira;

      const percent = summary.total > 0 ? Math.round((summary.concluidas / summary.total) * 100) : 0;
      progressPercentage.textContent = `${percent}%`;
      progressBarFill.style.width = `${percent}%`;
    } catch (error) {
      if (error.message !== 'Não autenticado (401)') {
        console.error('Erro ao buscar resumo:', error);
      }
    }
  };

  const fetchTrash = async () => {
    try {
      const response = await apiFetch('/api/tarefas/lixeira');
      if (!response.ok) throw new Error('Falha ao carregar lixeira');
      const tasks = await response.json();
      renderTrashView(tasks);
    } catch (error) {
      if (error.message !== 'Não autenticado (401)') {
        console.error(error);
        showToast('Erro ao carregar lixeira.', 'error');
      }
    }
  };

  // --- Tags Management ---
  const fetchTags = async () => {
    try {
      const response = await apiFetch('/api/tags');
      if (!response.ok) return;
      cachedTags = await response.json();
      populateTagsUI();
    } catch {
      // Silencioso se não autenticado
    }
  };

  const populateTagsUI = () => {
    if (tagFilter) {
      const selectedVal = tagFilter.value;
      tagFilter.innerHTML = '<option value="">Todas as Tags</option>';
      cachedTags.forEach(tag => {
        const opt = document.createElement('option');
        opt.value = tag.id;
        opt.textContent = '#' + tag.nome;
        if (selectedVal == tag.id) opt.selected = true;
        tagFilter.appendChild(opt);
      });
    }

    if (createTaskTagsList) {
      renderTagsCheckboxes(createTaskTagsList, []);
    }
  };

  const renderTagsCheckboxes = (container, selectedIds = []) => {
    if (!container) return;
    if (!cachedTags || cachedTags.length === 0) {
      container.innerHTML = '<span class="text-muted" style="font-size:0.82rem;">Nenhuma tag criada ainda.</span>';
      return;
    }

    const idsSet = new Set(selectedIds.map(item => typeof item === 'object' ? item.id : item));

    container.innerHTML = cachedTags.map(tag => {
      const isSelected = idsSet.has(tag.id);
      const isLight = isColorLight(tag.cor);
      const textColor = isLight ? '#0f172a' : '#ffffff';
      return `
        <label class="tag-checkbox-pill ${isSelected ? 'selected' : ''}" style="background-color: ${tag.cor}; color: ${textColor};">
          <input type="checkbox" value="${tag.id}" ${isSelected ? 'checked' : ''}>
          #${escapeHtml(tag.nome)}
        </label>
      `;
    }).join('');

    container.querySelectorAll('input[type="checkbox"]').forEach(cb => {
      cb.addEventListener('change', () => {
        cb.closest('.tag-checkbox-pill').classList.toggle('selected', cb.checked);
      });
    });
  };

  // --- Notifications Center ---
  const fetchNotifications = async () => {
    try {
      const response = await apiFetch('/api/tarefas/notificacoes');
      if (!response.ok) return;
      const notifications = await response.json();
      renderNotifications(notifications);
    } catch {
      // Silencioso se não autenticado
    }
  };

  const renderNotifications = (notifications) => {
    if (!notificationBadge || !notificationList) return;

    const count = notifications ? notifications.length : 0;
    if (count > 0) {
      notificationBadge.textContent = count > 99 ? '99+' : count;
      notificationBadge.classList.remove('hidden');
    } else {
      notificationBadge.classList.add('hidden');
    }

    if (count === 0) {
      notificationList.innerHTML = '<div class="notification-empty">Nenhum alerta pendente 🎉</div>';
      return;
    }

    notificationList.innerHTML = notifications.map(n => {
      let tagClass = 'notif-tag-urgente';
      let tagLabel = 'URGENTE';
      if (n.tipo === 'ATRASADA') {
        tagClass = 'notif-tag-atrasada';
        tagLabel = 'ATRASADA';
      } else if (n.tipo === 'VENCE_HOJE') {
        tagClass = 'notif-tag-vence-hoje';
        tagLabel = 'HOJE';
      } else if (n.tipo === 'VENCE_BREVE') {
        tagClass = 'notif-tag-vence-breve';
        tagLabel = 'AMANHÃ';
      }

      return `
        <div class="notification-item" data-task-id="${n.taskId}">
          <div class="notification-item-top">
            <span class="notification-item-title">${escapeHtml(n.titulo)}</span>
            <span class="notification-item-tag ${tagClass}">${tagLabel}</span>
          </div>
          <div class="notification-item-desc">${escapeHtml(n.mensagem)}</div>
        </div>
      `;
    }).join('');

    // Dispara notificação nativa do Windows/Navegador para tarefas novas
    if ('Notification' in window && Notification.permission === 'granted') {
      notifications.forEach(n => {
        if (!lastNotifiedTaskIds.has(n.taskId)) {
          lastNotifiedTaskIds.add(n.taskId);
          new Notification(n.titulo, {
            body: n.mensagem,
            icon: '/favicon.ico'
          });
        }
      });
    }

    // Clique na notificação navega até a tarefa
    notificationList.querySelectorAll('.notification-item').forEach(item => {
      item.addEventListener('click', () => {
        const taskId = item.dataset.taskId;
        notificationDropdown.classList.add('hidden');
        if (currentView !== 'list') switchView('list');
        const taskEl = document.querySelector(`.task-item[data-id="${taskId}"]`);
        if (taskEl) {
          taskEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
          taskEl.style.outline = '3px solid var(--primary)';
          setTimeout(() => { if (taskEl) taskEl.style.outline = ''; }, 2500);
        }
      });
    });
  };

  // --- Fetch & Render Chart.js Dashboard ---
  const fetchStats = async () => {
    try {
      const response = await apiFetch('/api/tarefas/estatisticas');
      if (!response.ok) throw new Error('Falha ao buscar estatísticas');
      const stats = await response.json();

      kpiOnTimeRate.textContent = `${stats.taxaConclusaoNoPrazo}%`;
      kpiPomodorosDone.textContent = stats.totalPomodorosRealizados || 0;
      kpiRecurringTasks.textContent = stats.totalRecorrentes || 0;

      renderCharts(stats);
    } catch (error) {
      if (error.message !== 'Não autenticado (401)') {
        console.error(error);
        showToast('Erro ao carregar estatísticas.', 'error');
      }
    }
  };

  if (btnRefreshStats) {
    btnRefreshStats.addEventListener('click', fetchStats);
  }

  const renderCharts = (stats) => {
    if (typeof Chart === 'undefined') return;

    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    const textColor = isDark ? '#94a3b8' : '#475569';
    const gridColor = isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)';

    // Chart 1: Donut Categorias
    const catKeys = Object.keys(stats.porCategoria || {});
    const catValues = Object.values(stats.porCategoria || {});
    const catLabels = catKeys.map(k => getCategoryLabel(k));

    if (categoryChartInstance) categoryChartInstance.destroy();
    categoryChartInstance = new Chart(categoryChartCanvas, {
      type: 'doughnut',
      data: {
        labels: catLabels,
        datasets: [{
          data: catValues,
          backgroundColor: [
            '#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899', '#64748b'
          ],
          borderWidth: 2,
          borderColor: isDark ? '#131b2e' : '#ffffff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: { color: textColor, boxWidth: 12, padding: 12 }
          }
        }
      }
    });

    // Chart 2: Barras Prioridades
    const prioOrder = ['BAIXA', 'MEDIA', 'ALTA', 'URGENTE'];
    const prioLabels = ['🟢 Baixa', '🟡 Média', '🟠 Alta', '🔴 Urgente'];
    const prioValues = prioOrder.map(p => stats.porPrioridade[p] || 0);

    if (priorityChartInstance) priorityChartInstance.destroy();
    priorityChartInstance = new Chart(priorityChartCanvas, {
      type: 'bar',
      data: {
        labels: prioLabels,
        datasets: [{
          label: 'Quantidade',
          data: prioValues,
          backgroundColor: ['#10b981', '#f59e0b', '#f97316', '#ef4444'],
          borderRadius: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false }
        },
        scales: {
          x: { ticks: { color: textColor }, grid: { display: false } },
          y: { ticks: { color: textColor, stepSize: 1 }, grid: { color: gridColor } }
        }
      }
    });

    // Chart 3: Linhas / Barras Tendência 7 Dias
    if (weeklyTrendChartInstance) weeklyTrendChartInstance.destroy();
    weeklyTrendChartInstance = new Chart(weeklyTrendChartCanvas, {
      type: 'bar',
      data: {
        labels: stats.ultimos7DiasRotulos || [],
        datasets: [
          {
            label: 'Criadas',
            data: stats.ultimos7DiasCriadas || [],
            backgroundColor: '#3b82f6',
            borderRadius: 4
          },
          {
            label: 'Concluídas',
            data: stats.ultimos7DiasConcluidas || [],
            backgroundColor: '#10b981',
            borderRadius: 4
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'top',
            labels: { color: textColor }
          }
        },
        scales: {
          x: { ticks: { color: textColor }, grid: { display: false } },
          y: { ticks: { color: textColor, stepSize: 1 }, grid: { color: gridColor } }
        }
      }
    });
  };

  // --- Render: List View ---
  const renderListView = (tasks) => {
    taskList.innerHTML = '';

    if (!tasks || tasks.length === 0) {
      emptyState.classList.remove('hidden');
      if (currentSearch) {
        emptyMessage.textContent = `Nenhuma tarefa encontrada para "${currentSearch}".`;
      } else {
        emptyMessage.textContent = 'Nenhuma tarefa corresponde aos filtros selecionados.';
      }
      return;
    }

    emptyState.classList.add('hidden');

    tasks.forEach(task => {
      const taskEl = document.createElement('div');
      taskEl.className = `task-item ${task.concluida ? 'is-completed' : ''}`;
      taskEl.dataset.id = task.id;

      const dueBadge = getDueDateBadge(task);
      const recurrenceBadge = getRecurrenceBadge(task);
      const pomodoroBadge = getPomodoroBadge(task);

      taskEl.innerHTML = `
        <div class="task-checkbox-wrapper">
          <input type="checkbox" class="custom-checkbox" ${task.concluida ? 'checked' : ''} aria-label="Marcar tarefa">
        </div>
        <div class="task-content">
          <div class="task-title-row">
            <h4 class="task-title">${escapeHtml(task.titulo)}</h4>
            <span class="badge badge-p-${task.prioridade}">${getPriorityLabel(task.prioridade)}</span>
            <span class="badge badge-category">${getCategoryLabel(task.categoria)}</span>
            ${renderTagsHtml(task.tags)}
            ${recurrenceBadge}
            ${pomodoroBadge}
            ${dueBadge}
            <span class="badge ${task.concluida ? 'badge-completed' : 'badge-pending'}">
              ${getStatusLabel(task.status)}
            </span>
          </div>

          ${task.descricao ? `<p class="task-description">${escapeHtml(task.descricao)}</p>` : ''}

          <!-- Anexos Section -->
          ${renderAttachmentsHtml(task.anexos)}

          <!-- Subtarefas Section -->
          <div class="subtasks-wrapper">
            <div class="subtasks-header">
              <span>Subtarefas (${task.subtarefasConcluidas || 0}/${task.totalSubtarefas || 0})</span>
            </div>
            <div class="subtasks-list" id="subtaskList-${task.id}">
              ${renderSubtaskListHtml(task)}
            </div>
            <form class="subtask-add-box" data-task-id="${task.id}">
              <input type="text" class="subtask-input" placeholder="Nova etapa / subtarefa..." required maxlength="200">
              <button type="submit" class="btn btn-secondary btn-add-sub">+ Adicionar</button>
            </form>
          </div>

          <div class="task-meta">
            <span>Criada em ${formatDate(task.dataCriacao)}</span>
          </div>
        </div>

        <div class="task-actions">
          <button class="action-btn btn-focus" title="Focar nesta tarefa no Pomodoro" aria-label="Focar">
            🍅
          </button>
          <button class="action-btn btn-edit" title="Editar tarefa" aria-label="Editar">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
            </svg>
          </button>
          <button class="action-btn btn-delete" title="Mover para a lixeira" aria-label="Excluir">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6"></polyline>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            </svg>
          </button>
        </div>
      `;

      // Event: Click Tag Badge
      taskEl.querySelectorAll('.badge-tag').forEach(tagEl => {
        tagEl.addEventListener('click', (e) => {
          e.stopPropagation();
          const tagId = tagEl.dataset.tagId;
          currentTagFilter = tagId;
          if (tagFilter) tagFilter.value = tagId;
          fetchTasks();
        });
      });

      // Event: Focus Pomodoro
      taskEl.querySelector('.btn-focus').addEventListener('click', () => focusOnTask(task.id, task.titulo));

      // Event: Toggle status
      const checkbox = taskEl.querySelector('.custom-checkbox');
      checkbox.addEventListener('change', () => handleToggleTask(task.id));

      // Event: Edit task
      const editBtn = taskEl.querySelector('.btn-edit');
      editBtn.addEventListener('click', () => openEditModal(task));

      // Event: Move to trash
      const deleteBtn = taskEl.querySelector('.btn-delete');
      deleteBtn.addEventListener('click', () => handleMoveToTrash(task.id));

      // Subtasks inline add
      const subtaskForm = taskEl.querySelector('.subtask-add-box');
      subtaskForm.addEventListener('submit', (e) => handleAddSubtask(e, task.id));

      // Subtasks item clicks
      bindSubtaskEvents(taskEl, task.id);

      taskList.appendChild(taskEl);
    });
  };

  // --- Render: Kanban View ---
  const renderKanbanView = (tasks) => {
    cardsAFazer.innerHTML = '';
    cardsEmAndamento.innerHTML = '';
    cardsConcluida.innerHTML = '';

    let todoCount = 0;
    let progressCount = 0;
    let doneCount = 0;

    tasks.forEach(task => {
      const card = createKanbanCard(task);

      if (task.status === 'EM_ANDAMENTO') {
        cardsEmAndamento.appendChild(card);
        progressCount++;
      } else if (task.status === 'CONCLUIDA') {
        cardsConcluida.appendChild(card);
        doneCount++;
      } else {
        cardsAFazer.appendChild(card);
        todoCount++;
      }
    });

    countAFazer.textContent = todoCount;
    countEmAndamento.textContent = progressCount;
    countConcluida.textContent = doneCount;
  };

  const createKanbanCard = (task) => {
    const card = document.createElement('div');
    card.className = 'kanban-card';
    card.draggable = true;
    card.dataset.id = task.id;

    const dueBadge = getDueDateBadge(task);
    const recurrenceBadge = getRecurrenceBadge(task);
    const pomodoroBadge = getPomodoroBadge(task);

    card.innerHTML = `
      <div class="kanban-card-title">${escapeHtml(task.titulo)}</div>
      <div class="kanban-card-badges">
        <span class="badge badge-p-${task.prioridade}">${getPriorityLabel(task.prioridade)}</span>
        <span class="badge badge-category">${getCategoryLabel(task.categoria)}</span>
        ${renderTagsHtml(task.tags)}
        ${recurrenceBadge}
        ${pomodoroBadge}
        ${dueBadge}
      </div>
      ${task.descricao ? `<p class="task-description" style="font-size:0.82rem;">${escapeHtml(task.descricao)}</p>` : ''}
      ${renderAttachmentsHtml(task.anexos)}
      <div class="kanban-card-footer">
        <span>${task.totalSubtarefas > 0 ? `Subtarefas: ${task.subtarefasConcluidas}/${task.totalSubtarefas}` : ''}</span>
        <div class="task-actions">
          <button class="action-btn btn-focus" title="Focar Pomodoro" aria-label="Focar">🍅</button>
          <button class="action-btn btn-edit" title="Editar"><svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg></button>
          <button class="action-btn btn-delete" title="Lixeira"><svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg></button>
        </div>
      </div>
    `;

    // Click Tag Badge in Kanban
    card.querySelectorAll('.badge-tag').forEach(tagEl => {
      tagEl.addEventListener('click', (e) => {
        e.stopPropagation();
        const tagId = tagEl.dataset.tagId;
        currentTagFilter = tagId;
        if (tagFilter) tagFilter.value = tagId;
        fetchTasks();
      });
    });

    // Drag Events
    card.addEventListener('dragstart', (e) => {
      card.classList.add('is-dragging');
      e.dataTransfer.setData('text/plain', task.id);
      e.dataTransfer.effectAllowed = 'move';
    });

    card.addEventListener('dragend', () => {
      card.classList.remove('is-dragging');
    });

    // Actions
    card.querySelector('.btn-focus').addEventListener('click', () => focusOnTask(task.id, task.titulo));
    card.querySelector('.btn-edit').addEventListener('click', () => openEditModal(task));
    card.querySelector('.btn-delete').addEventListener('click', () => handleMoveToTrash(task.id));

    return card;
  };

  // Kanban Columns Drag & Drop Listeners
  kanbanColumns.forEach(column => {
    column.addEventListener('dragover', (e) => {
      e.preventDefault();
      e.dataTransfer.dropEffect = 'move';
      column.classList.add('drag-over');
    });

    column.addEventListener('dragleave', () => {
      column.classList.remove('drag-over');
    });

    column.addEventListener('drop', async (e) => {
      e.preventDefault();
      column.classList.remove('drag-over');
      const taskId = e.dataTransfer.getData('text/plain');
      const targetStatus = column.dataset.status;

      if (!taskId || !targetStatus) return;

      try {
        const response = await apiFetch(`/api/tarefas/${taskId}/status-kanban?status=${targetStatus}`, {
          method: 'PATCH'
        });
        if (!response.ok) throw new Error('Falha ao mover tarefa');

        showToast(`Tarefa movida para ${getStatusLabel(targetStatus)}!`);
        await refreshData();
      } catch (err) {
        if (err.message !== 'Não autenticado (401)') {
          console.error(err);
          showToast('Erro ao mover tarefa.', 'error');
        }
      }
    });
  });

  // --- Render: Trash View ---
  const renderTrashView = (tasks) => {
    trashList.innerHTML = '';

    if (!tasks || tasks.length === 0) {
      emptyTrashState.classList.remove('hidden');
      return;
    }

    emptyTrashState.classList.add('hidden');

    tasks.forEach(task => {
      const taskEl = document.createElement('div');
      taskEl.className = 'task-item';
      taskEl.style.opacity = '0.75';

      taskEl.innerHTML = `
        <div class="task-content">
          <div class="task-title-row">
            <h4 class="task-title">${escapeHtml(task.titulo)}</h4>
            <span class="badge badge-p-${task.prioridade}">${getPriorityLabel(task.prioridade)}</span>
            <span class="badge badge-category">${getCategoryLabel(task.categoria)}</span>
          </div>
          ${task.descricao ? `<p class="task-description">${escapeHtml(task.descricao)}</p>` : ''}
          <div class="task-meta">
            <span>Excluída em ${formatDate(task.dataDelecao)}</span>
          </div>
        </div>
        <div class="task-actions">
          <button class="action-btn btn-restore" title="Restaurar tarefa" aria-label="Restaurar">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"></path>
              <polyline points="3 3 3 8 8 8"></polyline>
            </svg>
          </button>
          <button class="action-btn btn-delete" title="Excluir definitivamente" aria-label="Excluir definitivo">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3 6 5 6 21 6"></polyline>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
              <line x1="10" y1="11" x2="10" y2="17"></line>
              <line x1="14" y1="11" x2="14" y2="17"></line>
            </svg>
          </button>
        </div>
      `;

      taskEl.querySelector('.btn-restore').addEventListener('click', () => handleRestoreTask(task.id));
      taskEl.querySelector('.btn-delete').addEventListener('click', () => handlePermanentDelete(task.id));

      trashList.appendChild(taskEl);
    });
  };

  // --- Subtasks Helper & Handlers ---
  const renderSubtaskListHtml = (task) => {
    if (!task.subtarefas || task.subtarefas.length === 0) return '';
    return task.subtarefas.map(s => `
      <div class="subtask-row ${s.concluida ? 'is-done' : ''}" data-subtask-id="${s.id}">
        <input type="checkbox" class="subtask-checkbox" ${s.concluida ? 'checked' : ''}>
        <span class="subtask-title">${escapeHtml(s.titulo)}</span>
        <button type="button" class="btn-subtask-del" title="Excluir subtarefa">&times;</button>
      </div>
    `).join('');
  };

  const bindSubtaskEvents = (container, taskId) => {
    const checkboxes = container.querySelectorAll('.subtask-checkbox');
    checkboxes.forEach(chk => {
      chk.addEventListener('change', async (e) => {
        const row = e.target.closest('.subtask-row');
        const subtaskId = row.dataset.subtaskId;
        try {
          const res = await apiFetch(`/api/tarefas/${taskId}/subtarefas/${subtaskId}/toggle`, { method: 'PATCH' });
          if (!res.ok) throw new Error();
          await refreshData();
        } catch (err) {
          if (err.message !== 'Não autenticado (401)') {
            showToast('Erro ao alternar subtarefa.', 'error');
          }
        }
      });
    });

    const delButtons = container.querySelectorAll('.btn-subtask-del');
    delButtons.forEach(btn => {
      btn.addEventListener('click', async (e) => {
        const row = e.target.closest('.subtask-row');
        const subtaskId = row.dataset.subtaskId;
        try {
          const res = await apiFetch(`/api/tarefas/${taskId}/subtarefas/${subtaskId}`, { method: 'DELETE' });
          if (!res.ok) throw new Error();
          showToast('Subtarefa removida.');
          await refreshData();
        } catch (err) {
          if (err.message !== 'Não autenticado (401)') {
            showToast('Erro ao deletar subtarefa.', 'error');
          }
        }
      });
    });
  };

  const handleAddSubtask = async (e, taskId) => {
    e.preventDefault();
    const input = e.target.querySelector('.subtask-input');
    const titulo = input.value.trim();
    if (!titulo) return;

    try {
      const res = await apiFetch(`/api/tarefas/${taskId}/subtarefas`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ titulo, concluida: false })
      });
      if (!res.ok) throw new Error();
      input.value = '';
      showToast('Subtarefa adicionada!');
      await refreshData();
    } catch (err) {
      if (err.message !== 'Não autenticado (401)') {
        showToast('Erro ao adicionar subtarefa.', 'error');
      }
    }
  };

  // --- Task Action Handlers ---
  const handleToggleTask = async (id) => {
    try {
      const response = await apiFetch(`/api/tarefas/${id}/toggle`, { method: 'PATCH' });
      if (!response.ok) throw new Error('Falha ao alternar status');
      const updated = await response.json();
      showToast(updated.concluida ? 'Tarefa marcada como concluída!' : 'Tarefa reaberta!');
      await refreshData();
      if (currentView === 'stats') fetchStats();
    } catch (error) {
      if (error.message !== 'Não autenticado (401)') {
        console.error(error);
        showToast('Erro ao atualizar status da tarefa.', 'error');
      }
    }
  };

  const handleMoveToTrash = async (id) => {
    try {
      const response = await apiFetch(`/api/tarefas/${id}`, { method: 'DELETE' });
      if (!response.ok) throw new Error('Falha ao mover para lixeira');
      showToast('Tarefa enviada para a lixeira.');
      await refreshData();
      if (currentView === 'stats') fetchStats();
    } catch (error) {
      if (error.message !== 'Não autenticado (401)') {
        console.error(error);
        showToast('Erro ao mover tarefa para lixeira.', 'error');
      }
    }
  };

  const handleRestoreTask = async (id) => {
    try {
      const response = await apiFetch(`/api/tarefas/${id}/restaurar`, { method: 'PATCH' });
      if (!response.ok) throw new Error('Falha ao restaurar');
      showToast('Tarefa restaurada com sucesso!');
      await Promise.all([fetchTrash(), fetchSummary()]);
      if (currentView === 'stats') fetchStats();
    } catch (error) {
      if (error.message !== 'Não autenticado (401)') {
        console.error(error);
        showToast('Erro ao restaurar tarefa.', 'error');
      }
    }
  };

  const handlePermanentDelete = async (id) => {
    if (!confirm('Deseja excluir definitivamente esta tarefa? Esta ação não pode ser desfeita.')) return;
    try {
      const response = await apiFetch(`/api/tarefas/${id}/definitivo`, { method: 'DELETE' });
      if (!response.ok) throw new Error('Falha ao excluir definitivamente');
      showToast('Tarefa excluída permanentemente.');
      await Promise.all([fetchTrash(), fetchSummary()]);
      if (currentView === 'stats') fetchStats();
    } catch (error) {
      if (error.message !== 'Não autenticado (401)') {
        console.error(error);
        showToast('Erro ao excluir tarefa.', 'error');
      }
    }
  };

  btnEmptyTrash.addEventListener('click', async () => {
    if (!confirm('Tem certeza que deseja esvaziar toda a lixeira?')) return;
    try {
      const response = await apiFetch('/api/tarefas/lixeira/esvaziar', { method: 'DELETE' });
      if (!response.ok) throw new Error('Falha ao esvaziar lixeira');
      showToast('Lixeira esvaziada!');
      await Promise.all([fetchTrash(), fetchSummary()]);
      if (currentView === 'stats') fetchStats();
    } catch (error) {
      if (error.message !== 'Não autenticado (401)') {
        console.error(error);
        showToast('Erro ao esvaziar lixeira.', 'error');
      }
    }
  });

  // Create Task Form Submit
  createTaskForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(createTaskForm);

    const titulo = formData.get('titulo').trim();
    const descricao = formData.get('descricao').trim();
    const dataVencimento = formData.get('dataVencimento') || null;
    const prioridade = formData.get('prioridade');
    const categoria = formData.get('categoria');
    const status = formData.get('status');
    const recorrencia = formData.get('recorrencia') || 'NENHUMA';
    const pomodorosEstimados = parseInt(formData.get('pomodorosEstimados') || '1', 10);

    const tagIds = Array.from(createTaskTagsList ? createTaskTagsList.querySelectorAll('input[type="checkbox"]:checked') : [])
      .map(cb => parseInt(cb.value, 10));

    if (!titulo) return;

    try {
      const response = await apiFetch('/api/tarefas', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          titulo,
          descricao,
          dataVencimento,
          prioridade,
          categoria,
          status,
          recorrencia,
          pomodorosEstimados,
          tagIds,
          concluida: status === 'CONCLUIDA'
        })
      });

      if (!response.ok) {
        const errData = await response.json().catch(() => ({}));
        throw new Error(errData.mensagem || 'Erro ao criar tarefa');
      }

      createTaskForm.reset();
      if (taskEstimatedPomodoros) taskEstimatedPomodoros.value = 1;
      if (createTaskTagsList) renderTagsCheckboxes(createTaskTagsList, []);
      showToast('Tarefa criada com sucesso!');
      await refreshData();
      if (currentView === 'stats') fetchStats();
    } catch (error) {
      if (error.message !== 'Não autenticado (401)') {
        console.error(error);
        showToast(error.message || 'Erro ao criar tarefa.', 'error');
      }
    }
  });

  // Modal Open & Attachments Rendering
  const renderModalAttachments = (anexos) => {
    if (!editTaskAttachmentsList) return;
    if (!anexos || anexos.length === 0) {
      editTaskAttachmentsList.innerHTML = '<span class="text-muted" style="font-size:0.82rem;">Nenhum anexo nesta tarefa.</span>';
      return;
    }

    editTaskAttachmentsList.innerHTML = anexos.map(a => `
      <div class="attachment-modal-item">
        <div class="attachment-modal-item-left">
          ${a.isImagem ? `<img src="${a.urlDownload}" class="attachment-thumb-small">` : '📎'}
          <a href="${a.urlDownload}" target="_blank" title="Baixar / Visualizar">${escapeHtml(a.nomeOriginal)}</a>
          <span class="text-muted" style="font-size:0.75rem;">(${formatFileSize(a.tamanho)})</span>
        </div>
        <button type="button" class="btn-del-attachment" data-anexo-id="${a.id}" title="Excluir este anexo">&times;</button>
      </div>
    `).join('');

    editTaskAttachmentsList.querySelectorAll('.btn-del-attachment').forEach(btn => {
      btn.addEventListener('click', async () => {
        const anexoId = btn.dataset.anexoId;
        if (!confirm('Deseja excluir este anexo?')) return;
        try {
          const res = await apiFetch(`/api/tarefas/${currentEditingTaskId}/anexos/${anexoId}`, { method: 'DELETE' });
          if (!res.ok) throw new Error();
          showToast('Anexo removido com sucesso!');
          await refreshData();
          const updatedTask = cachedTasks.find(t => t.id === currentEditingTaskId);
          if (updatedTask) {
            renderModalAttachments(updatedTask.anexos || []);
          }
        } catch {
          showToast('Erro ao excluir anexo.', 'error');
        }
      });
    });
  };

  const openEditModal = (task) => {
    currentEditingTaskId = task.id;
    editTaskId.value = task.id;
    editTaskTitle.value = task.titulo;
    editTaskDescription.value = task.descricao || '';
    editTaskDueDate.value = task.dataVencimento || '';
    editTaskPriority.value = task.prioridade || 'MEDIA';
    editTaskCategory.value = task.categoria || 'GERAL';
    editTaskStatus.value = task.status || 'A_FAZER';
    editTaskRecurrence.value = task.recorrencia || 'NENHUMA';
    editTaskEstimatedPomodoros.value = task.pomodorosEstimados || 1;

    // Render tags no modal de edição
    if (editTaskTagsList) {
      renderTagsCheckboxes(editTaskTagsList, task.tags || []);
    }

    // Render anexos no modal de edição
    renderModalAttachments(task.anexos || []);

    // Reset upload box
    if (editAttachmentInput) editAttachmentInput.value = '';
    if (editAttachmentFileName) editAttachmentFileName.textContent = 'Nenhum arquivo selecionado';
    if (btnUploadAttachment) btnUploadAttachment.classList.add('hidden');

    editModal.classList.remove('hidden');
    editTaskTitle.focus();
  };

  const closeEditModal = () => {
    editModal.classList.add('hidden');
  };

  btnCancelEdit.addEventListener('click', closeEditModal);
  btnModalCancel.addEventListener('click', closeEditModal);
  modalBackdrop.addEventListener('click', closeEditModal);
  window.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      if (!editModal.classList.contains('hidden')) closeEditModal();
      if (!authModal.classList.contains('hidden')) closeAuthModal();
      if (tagModal && !tagModal.classList.contains('hidden')) closeTagModal();
    }
  });

  // Upload anexo no modal de edição
  if (editAttachmentInput) {
    editAttachmentInput.addEventListener('change', () => {
      const file = editAttachmentInput.files[0];
      if (file) {
        editAttachmentFileName.textContent = file.name;
        btnUploadAttachment.classList.remove('hidden');
      } else {
        editAttachmentFileName.textContent = 'Nenhum arquivo selecionado';
        btnUploadAttachment.classList.add('hidden');
      }
    });
  }

  if (btnUploadAttachment) {
    btnUploadAttachment.addEventListener('click', async () => {
      const file = editAttachmentInput.files[0];
      if (!file || !currentEditingTaskId) return;

      const formData = new FormData();
      formData.append('arquivo', file);

      try {
        btnUploadAttachment.disabled = true;
        btnUploadAttachment.textContent = 'Enviando...';
        const res = await apiFetch(`/api/tarefas/${currentEditingTaskId}/anexos`, {
          method: 'POST',
          body: formData
        });

        if (!res.ok) throw new Error('Falha no upload do anexo');

        showToast('Arquivo anexado com sucesso!');
        editAttachmentInput.value = '';
        editAttachmentFileName.textContent = 'Nenhum arquivo selecionado';
        btnUploadAttachment.classList.add('hidden');

        await refreshData();
        const updatedTask = cachedTasks.find(t => t.id === currentEditingTaskId);
        if (updatedTask) {
          renderModalAttachments(updatedTask.anexos || []);
        }
      } catch (err) {
        showToast('Erro ao enviar anexo: ' + err.message, 'error');
      } finally {
        btnUploadAttachment.disabled = false;
        btnUploadAttachment.textContent = 'Enviar Anexo';
      }
    });
  }

  // Edit Task Submit
  editTaskForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = editTaskId.value;
    const titulo = editTaskTitle.value.trim();
    const descricao = editTaskDescription.value.trim();
    const dataVencimento = editTaskDueDate.value || null;
    const prioridade = editTaskPriority.value;
    const categoria = editTaskCategory.value;
    const status = editTaskStatus.value;
    const recorrencia = editTaskRecurrence.value;
    const pomodorosEstimados = parseInt(editTaskEstimatedPomodoros.value || '1', 10);

    const tagIds = Array.from(editTaskTagsList ? editTaskTagsList.querySelectorAll('input[type="checkbox"]:checked') : [])
      .map(cb => parseInt(cb.value, 10));

    if (!titulo) return;

    try {
      const response = await apiFetch(`/api/tarefas/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          titulo,
          descricao,
          dataVencimento,
          prioridade,
          categoria,
          status,
          recorrencia,
          pomodorosEstimados,
          tagIds,
          concluida: status === 'CONCLUIDA'
        })
      });

      if (!response.ok) throw new Error('Falha ao atualizar tarefa');

      closeEditModal();
      showToast('Tarefa atualizada com sucesso!');
      await refreshData();
      if (currentView === 'stats') fetchStats();
    } catch (error) {
      if (error.message !== 'Não autenticado (401)') {
        console.error(error);
        showToast('Erro ao atualizar tarefa.', 'error');
      }
    }
  });

  // --- Tag Modal Handlers ---
  const openTagModal = () => {
    createTagForm.reset();
    newTagColor.value = '#6366f1';
    tagModal.classList.remove('hidden');
    setTimeout(() => newTagName.focus(), 100);
  };

  const closeTagModal = () => {
    tagModal.classList.add('hidden');
  };

  if (btnOpenCreateTag) btnOpenCreateTag.addEventListener('click', openTagModal);
  if (btnCancelTag) btnCancelTag.addEventListener('click', closeTagModal);
  if (btnTagModalCancel) btnTagModalCancel.addEventListener('click', closeTagModal);
  if (tagModalBackdrop) tagModalBackdrop.addEventListener('click', closeTagModal);

  if (tagColorPresets) {
    tagColorPresets.querySelectorAll('.color-preset').forEach(preset => {
      preset.addEventListener('click', () => {
        newTagColor.value = preset.dataset.color;
      });
    });
  }

  if (createTagForm) {
    createTagForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const nome = newTagName.value.trim();
      const cor = newTagColor.value || '#6366f1';
      if (!nome) return;

      try {
        const res = await apiFetch('/api/tags', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ nome, cor })
        });

        if (!res.ok) {
          const err = await res.json().catch(() => ({}));
          throw new Error(err.mensagem || 'Erro ao criar tag');
        }

        const novaTag = await res.json();
        showToast(`Tag #${novaTag.nome} criada com sucesso!`);
        closeTagModal();
        await fetchTags();

        // Se o modal de edição estiver aberto, seleciona a nova tag automaticamente
        if (!editModal.classList.contains('hidden') && editTaskTagsList) {
          const currentSelected = Array.from(editTaskTagsList.querySelectorAll('input:checked')).map(cb => parseInt(cb.value, 10));
          currentSelected.push(novaTag.id);
          renderTagsCheckboxes(editTaskTagsList, currentSelected);
        }
      } catch (err) {
        showToast(err.message, 'error');
      }
    });
  }

  // --- Filter Tag Selector ---
  if (tagFilter) {
    tagFilter.addEventListener('change', (e) => {
      currentTagFilter = e.target.value;
      fetchTasks();
    });
  }

  // --- Central de Notificações Handlers ---
  if (btnNotification) {
    btnNotification.addEventListener('click', (e) => {
      e.stopPropagation();
      notificationDropdown.classList.toggle('hidden');
    });
  }

  document.addEventListener('click', (e) => {
    if (notificationContainer && !notificationContainer.contains(e.target)) {
      notificationDropdown.classList.add('hidden');
    }
  });

  if (btnRequestNotifyPerm) {
    btnRequestNotifyPerm.addEventListener('click', async () => {
      if (!('Notification' in window)) {
        showToast('Seu navegador não suporta notificações de área de trabalho.', 'warning');
        return;
      }
      const perm = await Notification.requestPermission();
      if (perm === 'granted') {
        showToast('Notificações ativadas com sucesso!');
        new Notification('To-do List Pro', {
          body: 'Notificações na área de trabalho ativadas com sucesso!',
          icon: '/favicon.ico'
        });
        fetchNotifications();
      } else {
        showToast('Permissão de notificação não concedida.', 'warning');
      }
    });
  }

  // --- Export PDF Handler ---
  if (btnExportPdf) {
    btnExportPdf.addEventListener('click', async (e) => {
      e.preventDefault();
      try {
        showToast('Gerando Relatório Executivo em PDF...');
        const response = await apiFetch('/api/tarefas/export/pdf');
        if (!response.ok) throw new Error('Falha ao exportar relatório PDF');

        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = 'relatorio-executivo-tarefas.pdf';
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(downloadUrl);
        showToast('Relatório PDF exportado com sucesso!');
      } catch (err) {
        if (err.message !== 'Não autenticado (401)') {
          console.error(err);
          showToast('Erro ao exportar relatório PDF.', 'error');
        }
      }
    });
  }

  // --- Export CSV Handler ---
  if (btnExportCsv) {
    btnExportCsv.addEventListener('click', async (e) => {
      e.preventDefault();
      try {
        const response = await apiFetch('/api/tarefas/export/csv');
        if (!response.ok) throw new Error('Falha ao exportar CSV');

        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = 'tarefas.csv';
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(downloadUrl);
        showToast('Exportação CSV concluída com sucesso!');
      } catch (err) {
        if (err.message !== 'Não autenticado (401)') {
          console.error(err);
          showToast('Erro ao exportar tarefas para CSV.', 'error');
        }
      }
    });
  }

  // --- Auth Event Listeners ---
  if (btnLoginOpen) {
    btnLoginOpen.addEventListener('click', () => openAuthModal('login'));
  }

  const loginAsGuest = async () => {
    try {
      if (btnGuestLogin) {
        btnGuestLogin.disabled = true;
        btnGuestLogin.textContent = 'Entrando como Convidado...';
      }
      if (btnHeaderGuest) {
        btnHeaderGuest.disabled = true;
        btnHeaderGuest.textContent = 'Entrando...';
      }

      const response = await fetch('/api/auth/convidado', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      });

      if (!response.ok) {
        const errData = await response.json().catch(() => ({}));
        throw new Error(errData.mensagem || 'Falha ao acessar como convidado.');
      }

      const data = await response.json();
      const userObj = data.usuario || { id: data.id, nome: data.nome, email: data.email, role: data.role };
      setAuthData(data.token, userObj);
      closeAuthModal();
      showToast(`Bem-vindo, ${userObj.nome}! Modo de testes ativado.`);
      await refreshData();
      startNotificationPolling();
    } catch (err) {
      showToast(err.message, 'error');
      showAuthAlert(err.message, 'error');
    } finally {
      if (btnGuestLogin) {
        btnGuestLogin.disabled = false;
        btnGuestLogin.innerHTML = `
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
            <circle cx="8.5" cy="7" r="4"></circle>
            <polyline points="17 11 19 13 23 9"></polyline>
          </svg>
          ⚡ Entrar como Convidado (Acesso Completo)
        `;
      }
      if (btnHeaderGuest) {
        btnHeaderGuest.disabled = false;
        btnHeaderGuest.innerHTML = `
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
            <circle cx="8.5" cy="7" r="4"></circle>
            <polyline points="17 11 19 13 23 9"></polyline>
          </svg>
          Convidado
        `;
      }
    }
  };

  if (btnGuestLogin) btnGuestLogin.addEventListener('click', loginAsGuest);
  if (btnHeaderGuest) btnHeaderGuest.addEventListener('click', loginAsGuest);

  if (btnLogout) {
    btnLogout.addEventListener('click', () => {
      clearAuthData();
      resetViewData();
      activeFocusTaskId = null;
      activeFocusTaskTitle = null;
      pomodoroTaskLabel.textContent = 'Nenhuma tarefa em foco (clique em "🍅 Focar" em um card)';
      resetPomodoro();
      showToast('Você saiu da sua conta.');
      openAuthModal('login');
    });
  }

  if (btnCancelAuth) btnCancelAuth.addEventListener('click', closeAuthModal);
  if (authModalBackdrop) authModalBackdrop.addEventListener('click', closeAuthModal);
  if (tabLogin) tabLogin.addEventListener('click', () => switchAuthTab('login'));
  if (tabRegister) tabRegister.addEventListener('click', () => switchAuthTab('register'));

  // Submit Login
  if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const email = loginEmail.value.trim();
      const senha = loginPassword.value;

      if (!email || !senha) return;

      try {
        const response = await fetch('/api/auth/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email, senha })
        });

        if (!response.ok) {
          const errData = await response.json().catch(() => ({}));
          throw new Error(errData.mensagem || 'Credenciais inválidas. Verifique seu e-mail e senha.');
        }

        const data = await response.json();
        const userObj = data.usuario || { id: data.id, nome: data.nome, email: data.email, role: data.role };
        setAuthData(data.token, userObj);
        loginForm.reset();
        closeAuthModal();
        showToast(`Bem-vindo de volta, ${userObj.nome}!`);
        await refreshData();
      } catch (err) {
        showAuthAlert(err.message, 'error');
      }
    });
  }

  // Submit Register
  if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const nome = registerName.value.trim();
      const email = registerEmail.value.trim();
      const senha = registerPassword.value;

      if (!nome || !email || !senha) return;

      try {
        const response = await fetch('/api/auth/cadastro', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ nome, email, senha })
        });

        if (!response.ok) {
          const errData = await response.json().catch(() => ({}));
          throw new Error(errData.mensagem || 'Erro ao criar conta. Este e-mail pode já estar cadastrado.');
        }

        const data = await response.json();
        const userObj = data.usuario || { id: data.id, nome: data.nome, email: data.email, role: data.role };
        setAuthData(data.token, userObj);
        registerForm.reset();
        closeAuthModal();
        showToast(`Conta criada com sucesso! Bem-vindo, ${userObj.nome}!`);
        await refreshData();
      } catch (err) {
        showAuthAlert(err.message, 'error');
      }
    });
  }

  // Filter Event Listeners
  statusFilterButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      statusFilterButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      currentStatusFilter = btn.dataset.filter;
      fetchTasks();
    });
  });

  categoryFilter.addEventListener('change', (e) => {
    currentCategoryFilter = e.target.value;
    fetchTasks();
  });

  priorityFilter.addEventListener('change', (e) => {
    currentPriorityFilter = e.target.value;
    fetchTasks();
  });

  // Search Input with Debounce
  searchInput.addEventListener('input', (e) => {
    currentSearch = e.target.value;
    if (currentSearch) {
      clearSearchBtn.classList.remove('hidden');
    } else {
      clearSearchBtn.classList.add('hidden');
    }

    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
      fetchTasks();
    }, 300);
  });

  clearSearchBtn.addEventListener('click', () => {
    searchInput.value = '';
    currentSearch = '';
    clearSearchBtn.classList.add('hidden');
    fetchTasks();
  });

  // Refresh Helper
  const refreshData = async () => {
    fetchTags();
    fetchNotifications();
    if (currentView === 'trash') {
      await Promise.all([fetchTrash(), fetchSummary()]);
    } else if (currentView === 'stats') {
      await Promise.all([fetchStats(), fetchSummary()]);
    } else {
      await Promise.all([fetchTasks(), fetchSummary()]);
    }
  };

  // --- Badges & Format Helpers ---
  const getDueDateBadge = (task) => {
    if (!task.dataVencimento) return '';
    const today = new Date().toISOString().split('T')[0];

    if (task.estaAtrasada) {
      return `<span class="badge badge-overdue">⚠️ Atrasada (${formatDateOnly(task.dataVencimento)})</span>`;
    } else if (task.dataVencimento === today) {
      return `<span class="badge badge-due" style="border: 1px solid var(--warning);">📅 Vence Hoje</span>`;
    } else {
      return `<span class="badge badge-due">📅 ${formatDateOnly(task.dataVencimento)}</span>`;
    }
  };

  const getRecurrenceBadge = (task) => {
    if (!task.recorrencia || task.recorrencia === 'NENHUMA') return '';
    const map = {
      DIARIA: '🔁 Diária',
      SEMANAL: '🔁 Semanal',
      MENSAL: '🔁 Mensal'
    };
    return `<span class="badge badge-recurrence">${map[task.recorrencia] || task.recorrencia}</span>`;
  };

  const getPomodoroBadge = (task) => {
    const done = task.pomodorosRealizados || 0;
    const est = task.pomodorosEstimados || 1;
    return `<span class="badge badge-pomodoro" title="${done} de ${est} pomodoros concluídos">🍅 ${done}/${est}</span>`;
  };

  const getPriorityLabel = (p) => {
    switch (p) {
      case 'URGENTE': return '🔴 Urgente';
      case 'ALTA': return '🟠 Alta';
      case 'MEDIA': return '🟡 Média';
      case 'BAIXA': return '🟢 Baixa';
      default: return p || '';
    }
  };

  const getCategoryLabel = (c) => {
    switch (c) {
      case 'TRABALHO': return '💼 Trabalho';
      case 'ESTUDOS': return '🎓 Estudos';
      case 'PESSOAL': return '🏠 Pessoal';
      case 'FINANCAS': return '💰 Finanças';
      case 'SAUDE': return '🏃 Saúde';
      case 'GERAL': default: return '📁 Geral';
    }
  };

  const getStatusLabel = (s) => {
    switch (s) {
      case 'EM_ANDAMENTO': return 'Em Andamento';
      case 'CONCLUIDA': return 'Concluída';
      case 'A_FAZER': default: return 'A Fazer';
    }
  };

  const escapeHtml = (str) => {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
  };

  const formatDate = (isoString) => {
    if (!isoString) return '';
    try {
      const date = new Date(isoString);
      return new Intl.DateTimeFormat('pt-BR', {
        day: '2-digit', month: '2-digit', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
      }).format(date);
    } catch {
      return isoString;
    }
  };

  const formatDateOnly = (dateString) => {
    if (!dateString) return '';
    const [y, m, d] = dateString.split('-');
    return `${d}/${m}/${y}`;
  };

  const startNotificationPolling = () => {
    if (notificationPollInterval) clearInterval(notificationPollInterval);
    notificationPollInterval = setInterval(() => {
      if (getStoredToken()) {
        fetchNotifications();
      }
    }, 30000);
  };

  // --- Initial Boot & Auth Validation ---
  const checkInitialAuth = async () => {
    initTheme();
    updateAuthUI();
    updatePomodoroDisplay();

    const token = getStoredToken();
    if (!token) {
      openAuthModal('login');
      return;
    }

    try {
      const response = await apiFetch('/api/auth/me');
      if (response.ok) {
        const user = await response.json();
        localStorage.setItem('auth_user', JSON.stringify(user));
        updateAuthUI();
        await refreshData();
        startNotificationPolling();
      }
    } catch {
      // 401 is handled automatically in apiFetch
    }
  };

  checkInitialAuth();
});
