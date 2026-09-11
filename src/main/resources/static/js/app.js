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
  const trashCountBadge = document.getElementById('sidebarTrashBadge');

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
  // Enter/exit same direction for spatial consistency (Emil Kowalski / Sonner)
  const showToast = (message, type = 'success') => {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<span>${escapeHtml(message)}</span>`;
    toastContainer.appendChild(toast);

    setTimeout(() => {
      toast.classList.add('is-leaving');
      setTimeout(() => toast.remove(), 180);
    }, 3200);
  };

  // --- Web Audio Chime Synthesizer (Nativo e Offline) ---
  let pomoSoundEnabled = localStorage.getItem('pomo_sound') !== 'false';

  const playPomodoroChime = () => {
    if (!pomoSoundEnabled) return;
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
  let desktopMode = false;
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
    if (btnLogout) btnLogout.classList.toggle('hidden', desktopMode);
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
    if (type === 'error' || type === 'danger') {
      authAlert.classList.remove('shake');
      void authAlert.offsetWidth;
      authAlert.classList.add('shake');
    }
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
      if (desktopMode && !options.__retried) {
        const ok = await tryDesktopLogin();
        if (ok) {
          return apiFetch(url, { ...options, __retried: true });
        }
      }
      clearAuthData();
      cachedTasks = [];
      resetViewData();
      showAuthAlert('Sua sessão expirou ou o login é necessário. Por favor, autentique-se.', 'error');
      openAuthModal('login');
      throw new Error('Não autenticado (401)');
    }

    return response;
  };

  // No app desktop o servidor é local e de usuário único: entra automaticamente.
  const tryDesktopLogin = async () => {
    try {
      const res = await fetch('/api/auth/desktop', { method: 'POST' });
      if (!res.ok) return false;
      const data = await res.json();
      desktopMode = true;
      setAuthData(data.token, { id: data.id, nome: data.nome, email: data.email, role: data.role });
      return true;
    } catch {
      return false;
    }
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
    if (notificationList) notificationList.innerHTML = '<div class="notification-empty"><strong>Tudo em dia</strong>Nenhum alerta pendente no radar.</div>';
    if (notificationPollInterval) clearInterval(notificationPollInterval);
  };

  // --- View Switcher ---
  const switchView = (viewName) => {
    if (!viewName) return;
    currentView = viewName;
    viewTabButtons.forEach(b => b.classList.toggle('active', b.dataset.view === viewName));

    listView.classList.add('hidden');
    kanbanView.classList.add('hidden');
    trashView.classList.add('hidden');
    statsView.classList.add('hidden');

    if (viewName === 'list') {
      listView.classList.remove('hidden');
      createTaskSection.classList.remove('hidden');
      mainToolbar.classList.remove('hidden');
      pomodoroSection.classList.remove('hidden');
      fetchTasks();
    } else if (viewName === 'kanban') {
      kanbanView.classList.remove('hidden');
      createTaskSection.classList.remove('hidden');
      mainToolbar.classList.remove('hidden');
      pomodoroSection.classList.remove('hidden');
      fetchTasks();
    } else if (viewName === 'trash') {
      trashView.classList.remove('hidden');
      createTaskSection.classList.add('hidden');
      mainToolbar.classList.add('hidden');
      pomodoroSection.classList.add('hidden');
      fetchTrash();
    } else if (viewName === 'stats') {
      statsView.classList.remove('hidden');
      createTaskSection.classList.add('hidden');
      mainToolbar.classList.add('hidden');
      pomodoroSection.classList.remove('hidden');
      fetchStats();
    }
  };

  viewTabButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      switchView(btn.dataset.view);
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
    document.title = `${formatTime(pomoTimeLeft)} - LifeHub Foco`;
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
    document.title = 'LifeHub — Produtividade & Vida Pessoal';
  };

  const finishPomodoroCycle = async () => {
    pausePomodoro();
    playPomodoroChime();

    if (pomoMode === 'pomodoro') {
      if (activeFocusTaskId) {
        try {
          await apiFetch(`/api/tarefas/${activeFocusTaskId}/pomodoro/increment`, { method: 'PATCH' });
          showToast(`Pomodoro concluído para "${activeFocusTaskTitle}"!`);
          await refreshData();
          if (currentView === 'stats') fetchStats();
        } catch (err) {
          console.error(err);
        }
      } else {
        showToast('Ciclo de Pomodoro de 25 minutos concluído! Hora da pausa.');
      }
      // Alterna automaticamente para pausa curta
      switchPomodoroMode('short', 300);
    } else {
      showToast('Pausa concluída! Pronto para o próximo foco?');
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
    showToast(`Foco iniciado para "${taskTitle}"!`);
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
      return `<span class="badge badge-tag" style="background-color: ${safeColor(t.cor, '#64748b')}; color: ${textColor};" data-tag-id="${t.id}" title="Filtrar por #${escapeHtml(t.nome)}">#${escapeHtml(t.nome)}</span>`;
    }).join('') + '</div>';
  };

  const formatFileSize = (bytes) => {
    if (!bytes || bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  };

  const baixarAnexo = async (url, nome) => {
    try {
      const res = await apiFetch(url);
      if (!res.ok) throw new Error('Falha ao baixar anexo');
      const blob = await res.blob();
      const objUrl = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = objUrl;
      a.download = nome || 'anexo';
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(objUrl);
    } catch (err) {
      if (err.message !== 'Não autenticado (401)') showToast('Erro ao baixar anexo.', 'error');
    }
  };

  const abrirAnexo = async (url, nome) => {
    try {
      const res = await apiFetch(url);
      if (!res.ok) throw new Error('Falha ao abrir anexo');
      const blob = await res.blob();
      const objUrl = window.URL.createObjectURL(blob);
      const win = window.open(objUrl, '_blank', 'noopener');
      if (!win) {
        await baixarAnexo(url, nome);
      } else {
        setTimeout(() => window.URL.revokeObjectURL(objUrl), 60000);
      }
    } catch (err) {
      if (err.message !== 'Não autenticado (401)') showToast('Erro ao abrir anexo.', 'error');
    }
  };

  // Hidrata thumbnails de imagem que exigem Authorization (sem token na URL).
  const hidratarAnexosImagens = (root) => {
    if (!root) return;
    root.querySelectorAll('img[data-anexo-url]').forEach(async (img) => {
      if (img.dataset.hidratado === '1') return;
      img.dataset.hidratado = '1';
      const url = img.dataset.anexoUrl;
      try {
        const res = await apiFetch(url);
        if (!res.ok) return;
        const blob = await res.blob();
        img.src = window.URL.createObjectURL(blob);
      } catch {
        // 401 já tratado em apiFetch
      }
    });
  };

  const renderAttachmentsHtml = (anexos) => {
    if (!anexos || anexos.length === 0) return '';
    return '<div class="task-attachments-section">' + anexos.map(a => {
      const url = escapeHtml(a.urlDownload);
      const nome = escapeHtml(a.nomeOriginal);
      if (a.isImagem) {
        return `<a href="#" data-anexo-open data-anexo-url="${url}" data-anexo-nome="${nome}" class="attachment-thumbnail-card" title="${nome} (${formatFileSize(a.tamanho)})">
          <img data-anexo-url="${url}" alt="${nome}" loading="lazy">
        </a>`;
      }
      return `<a href="#" data-anexo-download data-anexo-url="${url}" data-anexo-nome="${nome}" class="attachment-file-pill" title="Baixar ${nome}">
        ${nome} (${formatFileSize(a.tamanho)})
      </a>`;
    }).join('') + '</div>';
  };

  // --- Period Filter Logic ---
  let currentPeriodFilter = 'all'; // 'all' | 'overdue' | 'today' | 'week' | 'nodate'

  // Data local no formato YYYY-MM-DD (evita o deslocamento de UTC do toISOString).
  const localISODate = (date = new Date()) => {
    const d = (date instanceof Date) ? date : new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  const filterTasksByPeriod = (tasks) => {
    if (!tasks || currentPeriodFilter === 'all') return tasks;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const todayStr = localISODate(today);

    const nextWeek = new Date(today);
    nextWeek.setDate(nextWeek.getDate() + 7);
    const nextWeekStr = localISODate(nextWeek);

    return tasks.filter(t => {
      if (currentPeriodFilter === 'nodate') {
        return !t.dataVencimento;
      }
      if (!t.dataVencimento) return false;
      if (currentPeriodFilter === 'overdue') {
        return t.dataVencimento < todayStr && !t.concluida;
      }
      if (currentPeriodFilter === 'today') {
        return t.dataVencimento === todayStr;
      }
      if (currentPeriodFilter === 'week') {
        return t.dataVencimento >= todayStr && t.dataVencimento <= nextWeekStr;
      }
      return true;
    });
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

      const rawTasks = await response.json();
      cachedTasks = filterTasksByPeriod(rawTasks);
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
        <label class="tag-checkbox-pill ${isSelected ? 'selected' : ''}" style="background-color: ${safeColor(tag.cor, '#64748b')}; color: ${textColor};">
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

      // Alertas de jogos (próxima hora) — mesclados antes de renderizar uma única vez
      let merged = notifications || [];
      try {
        const alertRes = await apiFetch('/api/esportes/alertas');
        if (alertRes.ok) {
          const alertas = await alertRes.json();
          const extras = (alertas || []).map(a => ({
            tipo: 'VENCE_BREVE',
            taskId: a.eventoId,
            titulo: a.titulo,
            mensagem: a.mensagem
          }));
          merged = [...merged, ...extras];
        }
      } catch { /* silencioso */ }

      renderNotifications(merged);
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
      notificationList.innerHTML = '<div class="notification-empty">Nenhum alerta pendente </div>';
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
            icon: '/favicon.svg'
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
    const prioLabels = ['Baixa', 'Média', 'Alta', 'Urgente'];
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
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="13" r="8"></circle>
              <path d="M12 9v4l2 2"></path>
              <path d="M9 2h6"></path>
            </svg>
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

      hidratarAnexosImagens(taskEl);

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
          <button class="action-btn btn-focus" title="Focar Pomodoro" aria-label="Focar">
            <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="13" r="8"></circle>
              <path d="M12 9v4l2 2"></path>
              <path d="M9 2h6"></path>
            </svg>
          </button>
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

    hidratarAnexosImagens(card);

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
        <button type="button" class="btn-edit-sub" data-task-id="${task.id}" data-sub-id="${s.id}" data-title="${escapeHtml(s.titulo)}" title="Editar subtarefa">Editar</button>
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

    const editButtons = container.querySelectorAll('.btn-edit-sub');
    editButtons.forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        openSubtaskEditModal(btn.dataset.taskId, btn.dataset.subId, btn.dataset.title);
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

    editTaskAttachmentsList.innerHTML = anexos.map(a => {
      const url = escapeHtml(a.urlDownload);
      const nome = escapeHtml(a.nomeOriginal);
      const img = a.isImagem ? `<img data-anexo-url="${url}" class="attachment-thumb-small" alt="${nome}">` : '';
      return `
      <div class="attachment-modal-item">
        <div class="attachment-modal-item-left">
          ${img}
          <a href="#" data-anexo-download data-anexo-url="${url}" data-anexo-nome="${nome}" title="Baixar / Visualizar">${nome}</a>
          <span class="text-muted" style="font-size:0.75rem;">(${formatFileSize(a.tamanho)})</span>
        </div>
        <button type="button" class="btn-del-attachment" data-anexo-id="${a.id}" title="Excluir este anexo">&times;</button>
      </div>
    `;
    }).join('');

    hidratarAnexosImagens(editTaskAttachmentsList);

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

  // Resolve a tarefa por id (cache local ou API) antes de abrir o modal de edição.
  const editarTarefaPorId = async (taskId) => {
    let task = (cachedTasks || []).find(t => String(t.id) === String(taskId));
    if (!task) {
      try {
        const res = await apiFetch(`/api/tarefas/${taskId}`);
        if (res.ok) task = await res.json();
      } catch {
        // 401 tratado em apiFetch
      }
    }
    if (task) {
      openEditModal(task);
    } else {
      showToast('Tarefa não encontrada.', 'error');
    }
  };

  btnCancelEdit.addEventListener('click', closeEditModal);
  btnModalCancel.addEventListener('click', closeEditModal);
  modalBackdrop.addEventListener('click', closeEditModal);
  // (Escape/atalhos globais são tratados em um único listener adiante)

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
        new Notification('LifeHub', {
          body: 'Notificações na área de trabalho ativadas com sucesso!',
          icon: '/favicon.svg'
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

  // Anexos: download/abertura autenticados via Blob (sem token na URL)
  document.addEventListener('click', (e) => {
    const downloadEl = e.target.closest('[data-anexo-download]');
    if (downloadEl) {
      e.preventDefault();
      baixarAnexo(downloadEl.dataset.anexoUrl, downloadEl.dataset.anexoNome);
      return;
    }
    const openEl = e.target.closest('[data-anexo-open]');
    if (openEl) {
      e.preventDefault();
      abrirAnexo(openEl.dataset.anexoUrl, openEl.dataset.anexoNome);
    }
  });

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
          Entrar como convidado
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
      if (desktopMode) {
        showToast('No modo desktop a sessão é renovada automaticamente.', 'info');
        return;
      }
      clearAuthData();
      resetViewData();
      lastNotifiedTaskIds.clear();
      activeFocusTaskId = null;
      activeFocusTaskTitle = null;
      pomodoroTaskLabel.textContent = 'Nenhuma tarefa em foco (clique em Focar em um card)';
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
    updateHeaderGreeting();
    if (typeof fetchDashboardData === 'function' && currentAppView === 'dashboard') {
      await Promise.all([fetchDashboardData(), fetchSummary()]);
    } else if (typeof fetchFinancesData === 'function' && currentAppView === 'finances') {
      await Promise.all([fetchFinancesData(), fetchSummary()]);
    } else if (typeof fetchCalendarData === 'function' && currentAppView === 'calendar') {
      await Promise.all([fetchCalendarData(), fetchSummary()]);
    } else if (typeof fetchGoalsData === 'function' && currentAppView === 'goals') {
      await Promise.all([fetchGoalsData(), fetchSummary()]);
    } else if (currentAppView === 'trash') {
      await Promise.all([fetchTrash(), fetchSummary()]);
    } else if (currentAppView === 'stats') {
      await Promise.all([fetchStats(), fetchSummary()]);
    } else {
      await Promise.all([fetchTasks(), fetchSummary()]);
    }
  };

  // --- Badges & Format Helpers ---
  const getDueDateBadge = (task) => {
    if (!task.dataVencimento) return '';
    const today = localISODate();

    if (task.estaAtrasada) {
      return `<span class="badge badge-overdue">Atrasada (${formatDateOnly(task.dataVencimento)})</span>`;
    } else if (task.dataVencimento === today) {
      return `<span class="badge badge-due" style="border: 1px solid var(--warning);">Vence Hoje</span>`;
    } else {
      return `<span class="badge badge-due">${formatDateOnly(task.dataVencimento)}</span>`;
    }
  };

  const getRecurrenceBadge = (task) => {
    if (!task.recorrencia || task.recorrencia === 'NENHUMA') return '';
    const map = {
      DIARIA: 'Diária',
      SEMANAL: 'Semanal',
      MENSAL: 'Mensal'
    };
    return `<span class="badge badge-recurrence">${map[task.recorrencia] || task.recorrencia}</span>`;
  };

  const getPomodoroBadge = (task) => {
    const done = task.pomodorosRealizados || 0;
    const est = task.pomodorosEstimados || 1;
    return `<span class="badge badge-pomodoro" title="${done} de ${est} pomodoros concluídos">${done}/${est}</span>`;
  };

  const getPriorityLabel = (p) => {
    switch (p) {
      case 'URGENTE': return 'Urgente';
      case 'ALTA': return 'Alta';
      case 'MEDIA': return 'Média';
      case 'BAIXA': return 'Baixa';
      default: return p || '';
    }
  };

  const getCategoryLabel = (c) => {
    switch (c) {
      case 'TRABALHO': return 'Trabalho';
      case 'ESTUDOS': return 'Estudos';
      case 'PESSOAL': return 'Pessoal';
      case 'FINANCAS': return 'Finanças';
      case 'SAUDE': return 'Saúde';
      case 'GERAL': default: return 'Geral';
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
    if (str === null || str === undefined) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  };

  // Evita injeção de CSS via valores de cor vindos da API.
  const safeColor = (color, fallback = '') => {
    if (typeof color !== 'string') return fallback;
    const c = color.trim();
    if (/^var\(--[a-zA-Z0-9-]+\)$/.test(c)) return c;
    if (/^#[0-9a-fA-F]{3,8}$/.test(c)) return c;
    if (/^rgba?\([\d.,\s%]+\)$/i.test(c)) return c;
    if (/^hsla?\([\d.,\s%]+\)$/i.test(c)) return c;
    if (/^[a-zA-Z]{3,20}$/.test(c)) return c;
    return fallback;
  };

  const hexAlpha = (color, alpha, fallback) => {
    const c = safeColor(color, '');
    return /^#[0-9a-fA-F]{6}$/.test(c) ? c + alpha : fallback;
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
      const desktop = await tryDesktopLogin();
      if (desktop) {
        await refreshData();
        startNotificationPolling();
        return;
      }
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

  // --- Subtask Edit Modal ---
  const subtaskEditModal = document.getElementById('subtaskEditModal');
  const subtaskEditModalBackdrop = document.getElementById('subtaskEditModalBackdrop');
  const btnCloseSubtaskEdit = document.getElementById('btnCloseSubtaskEdit');
  const btnCancelSubtaskEdit = document.getElementById('btnCancelSubtaskEdit');
  const subtaskEditForm = document.getElementById('subtaskEditForm');
  const editSubtaskTaskId = document.getElementById('editSubtaskTaskId');
  const editSubtaskSubId = document.getElementById('editSubtaskSubId');
  const editSubtaskTitle = document.getElementById('editSubtaskTitle');

  const openSubtaskEditModal = (taskId, subId, currentTitle) => {
    if (!subtaskEditModal) return;
    editSubtaskTaskId.value = taskId;
    editSubtaskSubId.value = subId;
    editSubtaskTitle.value = currentTitle || '';
    subtaskEditModal.classList.remove('hidden');
    setTimeout(() => editSubtaskTitle.focus(), 100);
  };

  const closeSubtaskEditModal = () => {
    if (subtaskEditModal) subtaskEditModal.classList.add('hidden');
  };

  if (btnCloseSubtaskEdit) btnCloseSubtaskEdit.addEventListener('click', closeSubtaskEditModal);
  if (btnCancelSubtaskEdit) btnCancelSubtaskEdit.addEventListener('click', closeSubtaskEditModal);
  if (subtaskEditModalBackdrop) subtaskEditModalBackdrop.addEventListener('click', closeSubtaskEditModal);

  if (subtaskEditForm) {
    subtaskEditForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const taskId = editSubtaskTaskId.value;
      const subId = editSubtaskSubId.value;
      const novoTitulo = editSubtaskTitle.value.trim();
      if (!novoTitulo) return;

      try {
        const res = await apiFetch(`/api/tarefas/${taskId}/subtarefas/${subId}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ titulo: novoTitulo })
        });
        if (!res.ok) throw new Error('Falha ao atualizar subtarefa');
        showToast('Subtarefa atualizada com sucesso!');
        closeSubtaskEditModal();
        await refreshData();
      } catch (err) {
        showToast(err.message, 'error');
      }
    });
  }

  // --- Keyboard Shortcuts Modal & Handlers ---
  const shortcutsModal = document.getElementById('shortcutsModal');
  const shortcutsModalBackdrop = document.getElementById('shortcutsModalBackdrop');
  const btnCloseShortcuts = document.getElementById('btnCloseShortcuts');
  const btnOkShortcuts = document.getElementById('btnOkShortcuts');
  const btnShortcuts = document.getElementById('btnShortcuts');

  const openShortcutsModal = () => {
    if (shortcutsModal) shortcutsModal.classList.remove('hidden');
  };
  const closeShortcutsModal = () => {
    if (shortcutsModal) shortcutsModal.classList.add('hidden');
  };

  if (btnShortcuts) btnShortcuts.addEventListener('click', openShortcutsModal);
  if (btnCloseShortcuts) btnCloseShortcuts.addEventListener('click', closeShortcutsModal);
  if (btnOkShortcuts) btnOkShortcuts.addEventListener('click', closeShortcutsModal);
  if (shortcutsModalBackdrop) shortcutsModalBackdrop.addEventListener('click', closeShortcutsModal);

  window.addEventListener('keydown', (e) => {
    const tag = document.activeElement ? document.activeElement.tagName.toLowerCase() : '';
    const isInput = tag === 'input' || tag === 'textarea' || tag === 'select';

    if (e.key === 'Escape') {
      if (editModal && !editModal.classList.contains('hidden')) closeEditModal();
      if (authModal && !authModal.classList.contains('hidden')) closeAuthModal();
      if (tagModal && !tagModal.classList.contains('hidden')) closeTagModal();
      if (shortcutsModal && !shortcutsModal.classList.contains('hidden')) closeShortcutsModal();
      if (subtaskEditModal && !subtaskEditModal.classList.contains('hidden')) closeSubtaskEditModal();
      if (notificationDropdown) notificationDropdown.classList.add('hidden');
      return;
    }

    if (isInput) return;

    if (e.key === 'n' || e.key === 'N') {
      e.preventDefault();
      const taskTitleInput = document.getElementById('taskTitle');
      if (taskTitleInput) {
        taskTitleInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
        setTimeout(() => taskTitleInput.focus(), 150);
      }
    } else if (e.key === '/') {
      e.preventDefault();
      if (searchInput) {
        searchInput.focus();
        searchInput.select();
      }
    } else if (e.key === 'p' || e.key === 'P') {
      e.preventDefault();
      if (pomoInterval) {
        pausePomodoro();
        showToast('Pomodoro pausado');
      } else {
        startPomodoro();
        showToast('Pomodoro iniciado!');
      }
    } else if (e.key === '1') {
      e.preventDefault();
      switchAppView('dashboard');
    } else if (e.key === '2') {
      e.preventDefault();
      switchAppView('tasks');
    } else if (e.key === '3') {
      e.preventDefault();
      switchAppView('finances');
    } else if (e.key === 'f' || e.key === 'F') {
      e.preventDefault();
      openTransactionModal('DESPESA');
    } else if (e.key === 'k' || e.key === 'K') {
      e.preventDefault();
      if (currentAppView !== 'tasks') switchAppView('tasks');
      switchView('kanban');
    } else if (e.key === 'l' || e.key === 'L') {
      e.preventDefault();
      if (currentAppView !== 'tasks') switchAppView('tasks');
      switchView('list');
    } else if (e.key === '?') {
      e.preventDefault();
      openShortcutsModal();
    }
  });

  // --- Period Filter Pills Handler ---
  const periodFilterGroup = document.getElementById('periodFilterGroup');
  if (periodFilterGroup) {
    periodFilterGroup.querySelectorAll('.period-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        periodFilterGroup.querySelectorAll('.period-pill').forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
        currentPeriodFilter = pill.dataset.period;
        fetchTasks();
      });
    });
  }

  // --- Pomodoro Sound Toggle ---
  const btnPomoSound = document.getElementById('btnPomoSound');
  const updatePomoSoundButtonUI = () => {
    if (btnPomoSound) {
      btnPomoSound.textContent = pomoSoundEnabled ? 'Som ligado' : 'Som mudo';
      btnPomoSound.style.opacity = pomoSoundEnabled ? '1' : '0.65';
    }
  };
  if (btnPomoSound) {
    updatePomoSoundButtonUI();
    btnPomoSound.addEventListener('click', () => {
      pomoSoundEnabled = !pomoSoundEnabled;
      localStorage.setItem('pomo_sound', pomoSoundEnabled ? 'true' : 'false');
      updatePomoSoundButtonUI();
      showToast(pomoSoundEnabled ? 'Alarme sonoro do Pomodoro ativado.' : 'Alarme sonoro do Pomodoro mutado.', 'info');
    });
  }

  // --- Service Worker (PWA Offline & Shell Cache) ---
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('/sw.js').catch(err => {
        console.warn('Registro do Service Worker falhou:', err);
      });
    });
  }

  

  // =========================================================================
  // LIFEHUB COCKPIT & FINANCIAL DASHBOARD CONTROLLER
  // =========================================================================

  // --- Currency & Date Formatting Helpers ---
  const formatCurrency = (val) => {
    const num = Number(val) || 0;
    return num.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  };

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour >= 5 && hour < 12) return 'Bom dia';
    if (hour >= 12 && hour < 18) return 'Boa tarde';
    return 'Boa noite';
  };

  const updateHeaderGreeting = () => {
    const headerGreeting = document.getElementById('headerGreeting');
    const headerDateFormatted = document.getElementById('headerDateFormatted');
    const user = getStoredUser();
    const name = user ? (user.nome ? user.nome.split(' ')[0] : 'Usuário') : 'Visitante';

    if (headerGreeting) {
      headerGreeting.textContent = `${getGreeting()}, ${name}!`;
    }

    if (headerDateFormatted) {
      const now = new Date();
      const options = { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' };
      const formatted = now.toLocaleDateString('pt-BR', options);
      headerDateFormatted.textContent = formatted.charAt(0).toUpperCase() + formatted.slice(1);
    }
  };

  // --- App Views Navigation (Sidebar) ---
  let currentAppView = 'dashboard';
  const appSidebar = document.getElementById('appSidebar');
  const btnToggleSidebar = document.getElementById('btnToggleSidebar');
  const btnMobileMenu = document.getElementById('btnMobileMenu');
  const sidebarNavItems = document.querySelectorAll('.sidebar-nav-item');
  const sidebarSaldoTotal = document.getElementById('sidebarSaldoTotal');
  const sidebarTrashBadge = document.getElementById('sidebarTrashBadge');

  const dashboardView = document.getElementById('dashboardView');
  const tasksView = document.getElementById('tasksView');
  const financesView = document.getElementById('financesView');
  const calendarView = document.getElementById('calendarView');
  const goalsView = document.getElementById('goalsView');

  // Load saved sidebar state
  if (localStorage.getItem('sidebar_collapsed') === 'true' && appSidebar) {
    appSidebar.classList.add('collapsed');
  }

  if (btnToggleSidebar && appSidebar) {
    btnToggleSidebar.addEventListener('click', () => {
      appSidebar.classList.toggle('collapsed');
      localStorage.setItem('sidebar_collapsed', appSidebar.classList.contains('collapsed') ? 'true' : 'false');
    });
  }

  const sidebarScrim = document.getElementById('sidebarScrim');
  const appLayoutEl = document.getElementById('appLayout');

  const setMobileSidebar = (open) => {
    if (!appSidebar) return;
    appSidebar.classList.toggle('mobile-open', open);
    if (appLayoutEl) appLayoutEl.classList.toggle('sidebar-mobile-open', open);
    if (sidebarScrim) {
      sidebarScrim.classList.toggle('is-visible', open);
      sidebarScrim.hidden = !open;
    }
    document.body.style.overflow = open ? 'hidden' : '';
  };

  if (btnMobileMenu && appSidebar) {
    btnMobileMenu.addEventListener('click', () => {
      setMobileSidebar(!appSidebar.classList.contains('mobile-open'));
    });
  }
  if (sidebarScrim) {
    sidebarScrim.addEventListener('click', () => setMobileSidebar(false));
  }

  const switchAppView = (viewName) => {
    currentAppView = viewName;

    sidebarNavItems.forEach(item => {
      item.classList.toggle('active', item.dataset.appView === viewName);
    });

    if (dashboardView) dashboardView.classList.add('hidden');
    if (tasksView) tasksView.classList.add('hidden');
    if (financesView) financesView.classList.add('hidden');
    if (statsView) statsView.classList.add('hidden');
    if (trashView) trashView.classList.add('hidden');
    if (calendarView) calendarView.classList.add('hidden');
    if (goalsView) goalsView.classList.add('hidden');

    if (viewName === 'dashboard') {
      if (dashboardView) dashboardView.classList.remove('hidden');
      fetchDashboardData();
    } else if (viewName === 'tasks') {
      if (tasksView) tasksView.classList.remove('hidden');
      fetchTasks();
    } else if (viewName === 'finances') {
      if (financesView) financesView.classList.remove('hidden');
      fetchFinancesData();
    } else if (viewName === 'calendar') {
      if (calendarView) calendarView.classList.remove('hidden');
      fetchCalendarData();
    } else if (viewName === 'goals') {
      if (goalsView) goalsView.classList.remove('hidden');
      fetchGoalsData();
    } else if (viewName === 'stats') {
      if (statsView) statsView.classList.remove('hidden');
      fetchStats();
    } else if (viewName === 'trash') {
      if (trashView) trashView.classList.remove('hidden');
      fetchTrash();
    }

    if (appSidebar && appSidebar.classList.contains('mobile-open')) {
      setMobileSidebar(false);
    }
  };

  sidebarNavItems.forEach(btn => {
    btn.addEventListener('click', () => {
      switchAppView(btn.dataset.appView);
    });
  });

  // Quick Action Buttons in Header
  const btnQuickNewTask = document.getElementById('btnQuickNewTask');
  const btnQuickNewTrans = document.getElementById('btnQuickNewTrans');

  if (btnQuickNewTask) {
    btnQuickNewTask.addEventListener('click', () => {
      switchAppView('tasks');
      const titleInput = document.getElementById('taskTitle');
      if (titleInput) {
        titleInput.scrollIntoView({ behavior: 'smooth' });
        titleInput.focus();
      }
    });
  }

  if (btnQuickNewTrans) {
    btnQuickNewTrans.addEventListener('click', () => {
      openTransactionModal('DESPESA');
    });
  }

  // ==========================================================================
  // LIFEHUB EXTENSIONS: CLIMA, HÁBITOS, NOTAS RÁPIDAS & RADAR ESPORTIVO
  // ==========================================================================

  // --- Clima Local (Open-Meteo API) ---
  const weatherIconEl = document.getElementById('weatherIcon');
  const weatherTextEl = document.getElementById('weatherText');
  const weatherChip = document.getElementById('weatherChip');

  const getWeatherInterpretation = (code) => {
    if (code === 0) return { icon: '', text: 'Céu limpo' };
    if ([1, 2, 3].includes(code)) return { icon: '', text: 'Parcialmente nublado' };
    if ([45, 48].includes(code)) return { icon: '', text: 'Nevoeiro' };
    if ([51, 53, 55, 61, 63, 65, 80, 81, 82].includes(code)) return { icon: '', text: 'Chuva' };
    if ([71, 73, 75, 77, 85, 86].includes(code)) return { icon: '', text: 'Neve' };
    if ([95, 96, 99].includes(code)) return { icon: '', text: 'Tempestade' };
    return { icon: '', text: 'Tempo firme' };
  };

  const fetchWeatherData = async () => {
    if (!weatherTextEl) return;
    try {
      let lat = -23.5505;
      let lon = -46.6333;

      const getCoords = () => new Promise((resolve) => {
        if (!navigator.geolocation) return resolve({ lat, lon });
        navigator.geolocation.getCurrentPosition(
          (pos) => resolve({ lat: pos.coords.latitude, lon: pos.coords.longitude }),
          () => resolve({ lat, lon }),
          { timeout: 3000 }
        );
      });

      const coords = await getCoords();
      const url = `https://api.open-meteo.com/v1/forecast?latitude=${coords.lat}&longitude=${coords.lon}&current_weather=true&daily=temperature_2m_max,temperature_2m_min&timezone=auto`;

      const response = await fetch(url);
      if (response.ok) {
        const data = await response.json();
        const current = data.current_weather;
        const daily = data.daily;
        const temp = Math.round(current.temperature);
        const max = daily && daily.temperature_2m_max ? Math.round(daily.temperature_2m_max[0]) : null;
        const min = daily && daily.temperature_2m_min ? Math.round(daily.temperature_2m_min[0]) : null;
        const info = getWeatherInterpretation(current.weathercode);

        if (weatherIconEl) weatherIconEl.textContent = info.icon;
        let details = `${temp}°C ${info.text}`;
        if (max !== null && min !== null) {
          details += ` • ${min}° / ${max}°`;
        }
        weatherTextEl.textContent = details;
      }
    } catch (err) {
      console.warn('Não foi possível obter clima em tempo real:', err);
      if (weatherTextEl) weatherTextEl.textContent = 'Clima indisponível';
    }
  };

  if (weatherChip) {
    weatherChip.addEventListener('click', () => {
      if (weatherTextEl) weatherTextEl.textContent = 'Atualizando...';
      fetchWeatherData();
    });
  }

  // --- Módulo de Hábitos & Rotinas ---
  const dashHabitsList = document.getElementById('dashHabitsList');
  const habitsProgressText = document.getElementById('habitsProgressText');
  const habitsProgressBarFill = document.getElementById('habitsProgressBarFill');
  const btnOpenNewHabit = document.getElementById('btnOpenNewHabit');
  const habitModal = document.getElementById('habitModal');
  const habitModalBackdrop = document.getElementById('habitModalBackdrop');
  const btnCloseHabitModal = document.getElementById('btnCloseHabitModal');
  const btnCancelHabit = document.getElementById('btnCancelHabit');
  const habitForm = document.getElementById('habitForm');

  const getHabitIconEmoji = (icon) => {
    const map = {
      'droplet': 'Água',
      'activity': 'Atividade',
      'book-open': 'Leitura',
      'smile': 'Bem-estar',
      'code': 'Código',
      'heart': 'Saúde',
      'sun': 'Manhã',
      'check': 'Rotina'
    };
    return map[icon] || icon || 'Hábito';
  };

  const fetchHabitsData = async () => {
    if (!dashHabitsList || !getStoredToken()) return;
    try {
      const response = await apiFetch('/api/habitos');
      if (response.ok) {
        const habitos = await response.json();
        renderHabits(habitos);
      }
    } catch (err) {
      console.error('Erro ao buscar hábitos:', err);
    }
  };

  const renderHabits = (habitos) => {
    if (!dashHabitsList) return;

    const total = habitos.length;
    const completed = habitos.filter(h => h.concluidoHoje).length;
    const pct = total > 0 ? Math.round((completed / total) * 100) : 0;

    if (habitsProgressText) habitsProgressText.textContent = `${completed} / ${total} (${pct}%)`;
    if (habitsProgressBarFill) habitsProgressBarFill.style.width = `${pct}%`;

    if (habitos.length === 0) {
      dashHabitsList.innerHTML = '<div class="dash-empty"><strong>Sem hábitos ainda</strong>Crie um ritual diário para manter o cockpit em ritmo.</div>';
      return;
    }

    dashHabitsList.innerHTML = habitos.map(h => `
      <div class="habit-item ${h.concluidoHoje ? 'habit-completed' : ''}" data-id="${h.id}">
        <div class="habit-item-left">
          <div class="habit-icon-badge" style="background:${hexAlpha(h.cor, '18', 'var(--panel-3)')}; color:${safeColor(h.cor, 'var(--ink)')};">
            ${getHabitIconEmoji(h.icone)}
          </div>
          <div class="habit-info">
            <span class="habit-name">${escapeHtml(h.nome)}</span>
            <span class="habit-streak">${h.streakDias || h.streakAtual || 0} ${(h.streakDias || h.streakAtual || 0) === 1 ? 'dia' : 'dias'} de sequência</span>
          </div>
        </div>
        <button type="button" class="habit-check-btn ${h.concluidoHoje ? 'checked' : ''}" data-id="${h.id}" title="${h.concluidoHoje ? 'Desmarcar hábito' : 'Concluir hoje!'}">
          ${h.concluidoHoje ? '✓' : ''}
        </button>
      </div>
    `).join('');

    dashHabitsList.querySelectorAll('.habit-check-btn').forEach(btn => {
      btn.addEventListener('click', async () => {
        const habitId = btn.dataset.id;
        try {
          const res = await apiFetch(`/api/habitos/${habitId}/toggle-hoje`, { method: 'POST' });
          if (res.ok) {
            const updated = await res.json();
            showToast(updated.concluidoHoje ? 'Parabéns! Hábito concluído hoje! ' : 'Hábito desmarcado.');
            await fetchHabitsData();
          }
        } catch (err) {
          console.error('Erro ao alternar hábito:', err);
        }
      });
    });
  };

  const openHabitModal = () => {
    if (!habitModal) return;
    habitModal.classList.remove('hidden');
    const input = document.getElementById('habitNome');
    if (input) setTimeout(() => input.focus(), 100);
  };

  const closeHabitModal = () => {
    if (habitModal) habitModal.classList.add('hidden');
  };

  if (btnOpenNewHabit) btnOpenNewHabit.addEventListener('click', openHabitModal);
  if (btnCloseHabitModal) btnCloseHabitModal.addEventListener('click', closeHabitModal);
  if (btnCancelHabit) btnCancelHabit.addEventListener('click', closeHabitModal);
  if (habitModalBackdrop) habitModalBackdrop.addEventListener('click', closeHabitModal);

  if (habitForm) {
    habitForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const nome = document.getElementById('habitNome').value.trim();
      const icone = document.getElementById('habitIcone').value;
      const cor = document.getElementById('habitCor').value;

      if (!nome) return;

      try {
        const res = await apiFetch('/api/habitos', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ nome, icone, cor, diasSemana: 'TODOS' })
        });

        if (res.ok) {
          showToast(`Hábito "${nome}" criado com sucesso! `);
          habitForm.reset();
          closeHabitModal();
          await fetchHabitsData();
        } else {
          showToast('Erro ao cadastrar hábito.', 'error');
        }
      } catch (err) {
        console.error(err);
      }
    });
  }

  // --- Bloco de Notas Rápidas (Scratchpad) ---
  const scratchpadTextarea = document.getElementById('scratchpadTextarea');
  const scratchpadStatus = document.getElementById('scratchpadStatus');
  const scratchpadCharCount = document.getElementById('scratchpadCharCount');
  const btnConvertNoteToTask = document.getElementById('btnConvertNoteToTask');

  let currentScratchpadNoteId = null;
  let scratchpadDebounceTimer = null;

  const fetchScratchpadData = async () => {
    if (!scratchpadTextarea || !getStoredToken()) return;
    try {
      const response = await apiFetch('/api/notas');
      if (response.ok) {
        const notas = await response.json();
        if (notas && notas.length > 0) {
          const nota = notas[0];
          currentScratchpadNoteId = nota.id;
          scratchpadTextarea.value = nota.conteudo || '';
          if (scratchpadCharCount) {
            scratchpadCharCount.textContent = `${(nota.conteudo || '').length} caracteres`;
          }
          if (scratchpadStatus) {
            scratchpadStatus.textContent = 'Salvo';
          }
        }
      }
    } catch (err) {
      console.error('Erro ao buscar anotações rápidas:', err);
    }
  };

  if (scratchpadTextarea) {
    scratchpadTextarea.addEventListener('input', () => {
      const len = scratchpadTextarea.value.length;
      if (scratchpadCharCount) scratchpadCharCount.textContent = `${len} caracteres`;
      if (scratchpadStatus) scratchpadStatus.textContent = 'Salvando…';

      clearTimeout(scratchpadDebounceTimer);
      scratchpadDebounceTimer = setTimeout(async () => {
        if (!currentScratchpadNoteId) return;
        try {
          const res = await apiFetch(`/api/notas/${currentScratchpadNoteId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              titulo: 'Rascunho Rápido',
              conteudo: scratchpadTextarea.value
            })
          });

          if (res.ok) {
            if (scratchpadStatus) scratchpadStatus.textContent = 'Salvo';
          } else {
            if (scratchpadStatus) scratchpadStatus.textContent = 'Erro ao salvar';
          }
        } catch (err) {
          console.error(err);
          if (scratchpadStatus) scratchpadStatus.textContent = 'Erro ao salvar';
        }
      }, 500);
    });
  }

  if (btnConvertNoteToTask) {
    btnConvertNoteToTask.addEventListener('click', async () => {
      const text = scratchpadTextarea ? scratchpadTextarea.value.trim() : '';
      if (!text) {
        showToast('Escreva uma anotação antes de converter em tarefa.', 'warning');
        return;
      }

      if (!currentScratchpadNoteId) return;

      try {
        const res = await apiFetch(`/api/notas/${currentScratchpadNoteId}/converter-em-tarefa`, {
          method: 'POST'
        });

        if (res.ok) {
          showToast('Tarefa criada com sucesso a partir da anotação! ');
          await Promise.all([fetchDashboardData(), fetchSummary()]);
        } else {
          showToast('Erro ao converter anotação em tarefa.', 'error');
        }
      } catch (err) {
        console.error(err);
        showToast('Erro ao converter anotação em tarefa.', 'error');
      }
    });
  }

  // ==========================================================================
  // RADAR MULTI-ESPORTES, PREFERÊNCIAS & SINCRONIZAÇÃO COM CALENDÁRIO
  // ==========================================================================
  const dashSportsList = document.getElementById('dashSportsList');
  const sportsFilterInput = document.getElementById('sportsFilterInput');
  const sportsHighlightText = document.getElementById('sportsHighlightText');
  const sportsChip = document.getElementById('sportsChip');
  const btnOpenSportsPrefs = document.getElementById('btnOpenSportsPrefs');
  const sportsSportPills = document.getElementById('sportsSportPills');

  const sportsPreferencesModal = document.getElementById('sportsPreferencesModal');
  const sportsPreferencesModalBackdrop = document.getElementById('sportsPreferencesModalBackdrop');
  const btnCloseSportsPrefsModal = document.getElementById('btnCloseSportsPrefsModal');
  const btnCloseSportsPrefsBtn = document.getElementById('btnCloseSportsPrefsBtn');
  const activeSportsPrefsContainer = document.getElementById('activeSportsPrefsContainer');
  const addSportsPrefForm = document.getElementById('addSportsPrefForm');

  let currentSportCategory = 'all';
  let currentSportsSearch = '';
  let sportsFilterDebounce = null;
  let cachedSportsEvents = [];
  let cachedSportsPreferences = [];

  const fetchSportsData = async (esporte = currentSportCategory, busca = currentSportsSearch) => {
    if (!dashSportsList || !getStoredToken()) return;
    currentSportCategory = esporte;
    currentSportsSearch = busca;

    try {
      let queryParams = [];
      if (currentSportCategory && currentSportCategory !== 'all') {
        queryParams.push(`esporte=${encodeURIComponent(currentSportCategory)}`);
      }
      if (currentSportsSearch) {
        queryParams.push(`busca=${encodeURIComponent(currentSportsSearch)}`);
      }
      const qs = queryParams.length > 0 ? `?${queryParams.join('&')}` : '';

      const response = await apiFetch(`/api/esportes/eventos${qs}`);
      if (response.ok) {
        cachedSportsEvents = await response.json();
        renderSports(cachedSportsEvents);
      }
    } catch (err) {
      console.error('Erro ao buscar eventos esportivos:', err);
    }
  };

  const renderSports = (eventos) => {
    if (!dashSportsList) return;

    // Atualiza chip de destaque no topo (Header Ticker)
    if (sportsHighlightText && eventos && eventos.length > 0) {
      const live = eventos.find(e => e.status === 'AO VIVO');
      if (live) {
        sportsHighlightText.textContent = `AO VIVO: ${live.icone} ${live.titulo} (${live.resultado || 'Em andamento'})`;
      } else {
        const next = eventos.find(e => e.status === 'AGENDADO') || eventos[0];
        sportsHighlightText.textContent = `${next.icone} ${next.titulo} • ${next.dataHoraFormatada || ''}`;
      }
    }

    if (!eventos || eventos.length === 0) {
      dashSportsList.innerHTML = '<div class="dash-empty"><strong>Nenhum evento no radar</strong>Tente outro filtro ou recarregue — o radar busca jogos reais em tempo quase real.</div>';
      return;
    }

    // Ordena: AO VIVO > AGENDADO > ENCERRADO; depois por data
    const ordemStatus = { 'AO VIVO': 0, 'AGENDADO': 1, 'ENCERRADO': 2 };
    const lista = [...eventos].sort((a, b) => {
      const oa = ordemStatus[a.status] ?? 3;
      const ob = ordemStatus[b.status] ?? 3;
      if (oa !== ob) return oa - ob;
      return String(a.data || a.dataHoraFormatada || '').localeCompare(String(b.data || b.dataHoraFormatada || ''));
    });
    const visiveis = lista.slice(0, 12);
    const restantes = lista.length - visiveis.length;

    dashSportsList.innerHTML = visiveis.map(ev => {
      let statusClass = 'scheduled';
      let statusLabel = ev.status;
      if (ev.status === 'AO VIVO') {
        statusClass = 'live';
        statusLabel = `AO VIVO ${ev.resultado ? `(${ev.resultado})` : ''}`;
      } else if (ev.status === 'ENCERRADO') {
        statusClass = 'finished';
      }

      let sportBadgeClass = 'sports-badge-futebol';
      if (ev.esporte === 'UFC') sportBadgeClass = 'sports-badge-ufc';
      else if (ev.esporte === 'F1') sportBadgeClass = 'sports-badge-f1';
      else if (ev.esporte === 'BASQUETE') sportBadgeClass = 'sports-badge-basquete';

      const isSynced = ev.noCalendario === true;
      const calBtnHtml = isSynced
        ? `<button type="button" class="btn btn-xs btn-sync-cal synced btn-toggle-sport-cal" data-id="${ev.id}" data-synced="true" title="Remover este evento da sua agenda">✓ Na Agenda</button>`
        : `<button type="button" class="btn btn-xs btn-outline btn-sync-cal btn-toggle-sport-cal" data-id="${ev.id}" data-synced="false" title="Adicionar este evento ao seu Calendário Unificado">Salvar na agenda</button>`;

      return `
        <div class="sports-match-card" data-event-id="${ev.id}">
          <div class="sports-match-header">
            <span class="sports-status-badge ${sportBadgeClass}">${escapeHtml(ev.icone)} ${escapeHtml(ev.esporte)}</span>
            <span class="sports-status-badge ${statusClass}">${escapeHtml(statusLabel)}</span>
          </div>
          <h4 class="sports-match-title">${escapeHtml(ev.titulo)}</h4>
          <div class="sports-match-sub">${escapeHtml(ev.subtitulo || '')}</div>
          <div class="sports-match-footer">
            <div class="sports-match-meta">
              <span>${escapeHtml(ev.transmissao || 'Transmissão a confirmar')}</span>
              <span class="sports-match-date">${escapeHtml(ev.dataHoraFormatada || '')}</span>
            </div>
            ${calBtnHtml}
          </div>
        </div>
      `;
    }).join('') + (restantes > 0
      ? `<div class="sports-more-hint">+ ${restantes} evento(s) — refine a busca ou os filtros acima</div>`
      : '');

    // Attach calendar sync toggle handlers
    dashSportsList.querySelectorAll('.btn-toggle-sport-cal').forEach(btn => {
      btn.addEventListener('click', async () => {
        const eventId = btn.dataset.id;
        const isSynced = btn.dataset.synced === 'true';

        try {
          if (isSynced) {
            const res = await apiFetch(`/api/esportes/eventos/${eventId}/remover-calendario`, { method: 'DELETE' });
            if (res.ok) {
              showToast('Evento removido da sua agenda.');
              await fetchSportsData();
              if (currentAppView === 'calendar') fetchCalendarData();
            }
          } else {
            const res = await apiFetch(`/api/esportes/eventos/${eventId}/salvar-calendario`, { method: 'POST' });
            if (res.ok) {
              showToast('Evento esportivo salvo no seu Calendário! ');
              await fetchSportsData();
              if (currentAppView === 'calendar') fetchCalendarData();
            }
          }
        } catch (err) {
          console.error(err);
        }
      });
    });
  };

  // Filter Pills (Todos, UFC, F1, Futebol, Basquete)
  if (sportsSportPills) {
    sportsSportPills.querySelectorAll('.sport-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        sportsSportPills.querySelectorAll('.sport-pill').forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
        const sport = pill.dataset.sport || 'all';
        fetchSportsData(sport, currentSportsSearch);
      });
    });
  }

  // Filter search input
  if (sportsFilterInput) {
    sportsFilterInput.addEventListener('input', (e) => {
      clearTimeout(sportsFilterDebounce);
      sportsFilterDebounce = setTimeout(() => {
        fetchSportsData(currentSportCategory, e.target.value.trim());
      }, 300);
    });
  }

  // Header sports chip click: switch to dashboard & scroll to widget
  if (sportsChip) {
    sportsChip.addEventListener('click', () => {
      if (currentAppView !== 'dashboard') {
        switchAppView('dashboard');
      }
      if (dashSportsList) {
        dashSportsList.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    });
  }

  // --- Modal de Preferências Esportivas ---
  const fetchSportsPreferences = async () => {
    if (!activeSportsPrefsContainer || !getStoredToken()) return;
    try {
      const res = await apiFetch('/api/esportes/preferencias');
      if (res.ok) {
        cachedSportsPreferences = await res.json();
        renderSportsPreferences();
      }
    } catch (err) {
      console.error('Erro ao buscar preferências de esportes:', err);
    }
  };

  const renderSportsPreferences = () => {
    if (!activeSportsPrefsContainer) return;

    if (cachedSportsPreferences.length === 0) {
      activeSportsPrefsContainer.innerHTML = '<span style="font-size:0.8rem;color:var(--text-muted);">Nenhum time seguido ainda. O radar de eventos já funciona — use as sugestões abaixo para personalizar a ordem.</span>';
      return;
    }

    activeSportsPrefsContainer.innerHTML = cachedSportsPreferences.map(p => `
      <span class="sports-pref-tag" style="border-left:3px solid ${safeColor(p.cor, '#10b981')};">
        
        <span>${escapeHtml(p.nomeInteresse)}</span>
        <button type="button" class="sports-pref-remove btn-remove-pref" data-id="${p.id}" title="Deixar de seguir">&times;</button>
      </span>
    `).join('');

    activeSportsPrefsContainer.querySelectorAll('.btn-remove-pref').forEach(btn => {
      btn.addEventListener('click', async () => {
        const id = btn.dataset.id;
        try {
          const res = await apiFetch(`/api/esportes/preferencias/${id}`, { method: 'DELETE' });
          if (res.ok) {
            showToast('Interesse removido.');
            await fetchSportsPreferences();
            await fetchSportsData();
          }
        } catch (err) {
          console.error(err);
        }
      });
    });
  };

  const openSportsPrefsModal = () => {
    if (!sportsPreferencesModal) return;
    sportsPreferencesModal.classList.remove('hidden');
    fetchSportsPreferences();
  };

  const closeSportsPrefsModal = () => {
    if (sportsPreferencesModal) sportsPreferencesModal.classList.add('hidden');
  };

  if (btnOpenSportsPrefs) btnOpenSportsPrefs.addEventListener('click', openSportsPrefsModal);
  if (btnCloseSportsPrefsModal) btnCloseSportsPrefsModal.addEventListener('click', closeSportsPrefsModal);
  if (btnCloseSportsPrefsBtn) btnCloseSportsPrefsBtn.addEventListener('click', closeSportsPrefsModal);
  if (sportsPreferencesModalBackdrop) sportsPreferencesModalBackdrop.addEventListener('click', closeSportsPrefsModal);

  // Quick Preset buttons inside preferences modal
  const sportsSuggestions = document.querySelectorAll('.btn-quick-pref');
  sportsSuggestions.forEach(btn => {
    btn.addEventListener('click', async () => {
      const esporte = btn.dataset.sport;
      const nomeInteresse = btn.dataset.name;

      try {
        const res = await apiFetch('/api/esportes/preferencias', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ esporte, nomeInteresse })
        });

        if (res.ok) {
          showToast(`"${nomeInteresse}" adicionado aos seus esportes seguidos! `);
          await fetchSportsPreferences();
          await fetchSportsData();
        }
      } catch (err) {
        console.error(err);
      }
    });
  });

  // Custom preference form submit
  if (addSportsPrefForm) {
    addSportsPrefForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const esporte = document.getElementById('customPrefSport').value;
      const nomeInteresse = document.getElementById('customPrefName').value.trim();

      if (!nomeInteresse) return;

      try {
        const res = await apiFetch('/api/esportes/preferencias', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ esporte, nomeInteresse })
        });

        if (res.ok) {
          showToast(`"${nomeInteresse}" adicionado com sucesso! `);
          document.getElementById('customPrefName').value = '';
          await fetchSportsPreferences();
          await fetchSportsData();
        }
      } catch (err) {
        console.error(err);
      }
    });
  }

  // ==========================================================================
  // MÓDULO DE METAS, SONHOS & OKRs
  // ==========================================================================
  const dashGoalsList = document.getElementById('dashGoalsList');
  const goalsCardsGrid = document.getElementById('goalsCardsGrid');
  const btnOpenNewGoalCockpit = document.getElementById('btnOpenNewGoalCockpit');
  const btnGoGoalsView = document.getElementById('btnGoGoalsView');
  const btnOpenNewGoalMain = document.getElementById('btnOpenNewGoalMain');

  const goalModal = document.getElementById('goalModal');
  const goalModalBackdrop = document.getElementById('goalModalBackdrop');
  const btnCloseGoalModal = document.getElementById('btnCloseGoalModal');
  const btnCancelGoal = document.getElementById('btnCancelGoal');
  const goalForm = document.getElementById('goalForm');

  const goalAporteModal = document.getElementById('goalAporteModal');
  const goalAporteModalBackdrop = document.getElementById('goalAporteModalBackdrop');
  const btnCloseGoalAporteModal = document.getElementById('btnCloseGoalAporteModal');
  const btnCancelAporte = document.getElementById('btnCancelAporte');
  const goalAporteForm = document.getElementById('goalAporteForm');

  const kpiGoalsInProgress = document.getElementById('kpiGoalsInProgress');
  const kpiGoalsCompleted = document.getElementById('kpiGoalsCompleted');
  const kpiGoalsAvgProgress = document.getElementById('kpiGoalsAvgProgress');
  const goalsCategoryFilters = document.getElementById('goalsCategoryFilters');

  let cachedGoalsList = [];
  let currentGoalsCategoryFilter = 'all';
  let editingGoalId = null;

  const fetchGoalsData = async () => {
    if (!getStoredToken()) return;
    try {
      const response = await apiFetch('/api/metas');
      if (response.ok) {
        cachedGoalsList = await response.json();
        renderGoals();
      }
    } catch (err) {
      console.error('Erro ao buscar metas:', err);
    }
  };

  const renderGoals = () => {
    // 1. KPIs
    if (kpiGoalsInProgress || kpiGoalsCompleted || kpiGoalsAvgProgress) {
      const inProg = cachedGoalsList.filter(g => !g.concluida).length;
      const comp = cachedGoalsList.filter(g => g.concluida).length;
      const total = cachedGoalsList.length;
      const avgPct = total > 0
        ? Math.round(cachedGoalsList.reduce((acc, g) => acc + (g.percentualConcluido || 0), 0) / total)
        : 0;

      if (kpiGoalsInProgress) kpiGoalsInProgress.textContent = inProg;
      if (kpiGoalsCompleted) kpiGoalsCompleted.textContent = comp;
      if (kpiGoalsAvgProgress) kpiGoalsAvgProgress.textContent = `${avgPct}%`;
    }

    // 2. Cockpit Widget
    if (dashGoalsList) {
      if (cachedGoalsList.length === 0) {
        dashGoalsList.innerHTML = '<div class="dash-empty"><strong>Nenhuma meta ainda</strong>Defina um alvo para acompanhar o progresso no cockpit.</div>';
      } else {
        const topGoals = cachedGoalsList.slice(0, 3);
        dashGoalsList.innerHTML = topGoals.map(g => {
          const valDisplay = g.unidade === 'R$'
            ? `${formatCurrency(g.valorAtual)} / ${formatCurrency(g.valorAlvo)}`
            : `${g.valorAtual} / ${g.valorAlvo} ${escapeHtml(g.unidade || '')}`;
          const pct = Math.min(100, Math.round(g.percentualConcluido || 0));

          return `
            <div class="dash-goal-item">
              <div class="dash-goal-top">
                <span class="dash-goal-title">
                  <span style="color:${safeColor(g.cor, '#10b981')};">●</span>
                  ${escapeHtml(g.titulo)}
                </span>
                <span class="dash-goal-pct">${pct}%</span>
              </div>
              <div class="dash-goal-progress-bar">
                <div class="dash-goal-progress-fill" style="width:${pct}%;background:${safeColor(g.cor, '#10b981')};"></div>
              </div>
              <div class="dash-goal-bottom">
                <span>${valDisplay}</span>
                <button type="button" class="btn btn-xs btn-outline btn-quick-aporte" data-id="${g.id}" data-title="${escapeHtml(g.titulo)}">+ Aporte</button>
              </div>
            </div>
          `;
        }).join('');

        dashGoalsList.querySelectorAll('.btn-quick-aporte').forEach(btn => {
          btn.addEventListener('click', () => {
            openAporteModal(btn.dataset.id, btn.dataset.title);
          });
        });
      }
    }

    // 3. Full Goals Grid View
    if (goalsCardsGrid) {
      const filtered = currentGoalsCategoryFilter === 'all'
        ? cachedGoalsList
        : cachedGoalsList.filter(g => (g.categoria || '').toUpperCase() === currentGoalsCategoryFilter.toUpperCase());

      if (filtered.length === 0) {
        goalsCardsGrid.innerHTML = '<div class="dash-empty" style="grid-column: 1 / -1;">Nenhuma meta encontrada nesta categoria. </div>';
        return;
      }

      goalsCardsGrid.innerHTML = filtered.map(g => {
        const pct = Math.min(100, Math.round(g.percentualConcluido || 0));
        const valAtualStr = g.unidade === 'R$' ? formatCurrency(g.valorAtual) : `${g.valorAtual} ${escapeHtml(g.unidade || '')}`;
        const valAlvoStr = g.unidade === 'R$' ? formatCurrency(g.valorAlvo) : `${g.valorAlvo} ${escapeHtml(g.unidade || '')}`;
        const prazoStr = g.prazo ? `Prazo: ${formatDateOnly(g.prazo)}` : 'Sem prazo definido';

        return `
          <div class="goal-card ${g.concluida ? 'goal-completed' : ''}" data-id="${g.id}">
            <div class="goal-card-header">
              <span class="goal-card-badge" style="background:${hexAlpha(g.cor, '18', 'var(--panel-3)')};color:${safeColor(g.cor, 'var(--ink-2)')};">
                ${escapeHtml(g.categoria || 'GERAL')}
              </span>
              <div class="goal-card-actions">
                <button type="button" class="btn btn-xs btn-outline btn-edit-goal" data-id="${g.id}" title="Editar Meta">Editar</button>
                <button type="button" class="btn btn-xs btn-outline btn-delete-goal" data-id="${g.id}" title="Excluir Meta" style="color:var(--danger);">Excluir</button>
              </div>
            </div>
            <div>
              <h3 class="goal-card-title">${escapeHtml(g.titulo)}</h3>
              ${g.descricao ? `<p class="goal-card-desc">${escapeHtml(g.descricao)}</p>` : ''}
            </div>
            <div class="goal-card-values">
              <div>
                <span class="goal-val-current">${valAtualStr}</span>
                <span class="goal-val-target"> de ${valAlvoStr}</span>
              </div>
              <span style="font-weight:700;color:${safeColor(g.cor, 'var(--primary)')};font-size:0.95rem;">${pct}%</span>
            </div>
            <div class="goal-card-progress">
              <div class="goal-card-fill" style="width:${pct}%;background:${safeColor(g.cor, '#10b981')};"></div>
            </div>
            <div class="goal-card-footer">
              <span>${prazoStr}</span>
              ${!g.concluida ? `<button type="button" class="btn btn-xs btn-primary btn-card-aporte" data-id="${g.id}" data-title="${escapeHtml(g.titulo)}">+ Aporte</button>` : `<span style="color:var(--success);font-weight:700;">Concluída</span>`}
            </div>
          </div>
        `;
      }).join('');

      goalsCardsGrid.querySelectorAll('.btn-card-aporte').forEach(btn => {
        btn.addEventListener('click', () => {
          openAporteModal(btn.dataset.id, btn.dataset.title);
        });
      });

      goalsCardsGrid.querySelectorAll('.btn-edit-goal').forEach(btn => {
        btn.addEventListener('click', () => {
          const id = Number(btn.dataset.id);
          const goal = cachedGoalsList.find(g => g.id === id);
          if (goal) openGoalModal(goal);
        });
      });

      goalsCardsGrid.querySelectorAll('.btn-delete-goal').forEach(btn => {
        btn.addEventListener('click', async () => {
          if (!confirm('Deseja realmente excluir esta meta?')) return;
          const id = btn.dataset.id;
          try {
            const res = await apiFetch(`/api/metas/${id}`, { method: 'DELETE' });
            if (res.ok) {
              showToast('Meta excluída com sucesso.');
              await fetchGoalsData();
            }
          } catch (err) {
            console.error(err);
          }
        });
      });
    }
  };

  // Category filter clicks in full view
  if (goalsCategoryFilters) {
    goalsCategoryFilters.querySelectorAll('.fin-pill').forEach(btn => {
      btn.addEventListener('click', () => {
        goalsCategoryFilters.querySelectorAll('.fin-pill').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        currentGoalsCategoryFilter = btn.dataset.goalCategory || 'all';
        renderGoals();
      });
    });
  }

  // Goal Modals
  const openGoalModal = (goal = null) => {
    if (!goalModal) return;
    editingGoalId = goal ? goal.id : null;
    const titleEl = document.getElementById('goalModalTitle');
    if (titleEl) titleEl.textContent = goal ? 'Editar Meta ou Sonho' : 'Nova Meta ou Sonho';

    document.getElementById('goalTitulo').value = goal ? goal.titulo : '';
    document.getElementById('goalDescricao').value = goal ? (goal.descricao || '') : '';
    document.getElementById('goalCategoria').value = goal ? (goal.categoria || 'FINANCEIRA') : 'FINANCEIRA';
    document.getElementById('goalUnidade').value = goal ? (goal.unidade || 'R$') : 'R$';
    document.getElementById('goalValorAlvo').value = goal ? goal.valorAlvo : '';
    document.getElementById('goalValorAtual').value = goal ? goal.valorAtual : '0.00';
    document.getElementById('goalPrazo').value = goal && goal.prazo ? goal.prazo : '';
    document.getElementById('goalCor').value = goal && goal.cor ? goal.cor : '#10b981';

    goalModal.classList.remove('hidden');
    setTimeout(() => document.getElementById('goalTitulo').focus(), 100);
  };

  const closeGoalModal = () => {
    if (goalModal) goalModal.classList.add('hidden');
    editingGoalId = null;
  };

  if (btnOpenNewGoalCockpit) btnOpenNewGoalCockpit.addEventListener('click', () => openGoalModal());
  if (btnOpenNewGoalMain) btnOpenNewGoalMain.addEventListener('click', () => openGoalModal());
  if (btnGoGoalsView) btnGoGoalsView.addEventListener('click', () => switchAppView('goals'));
  if (btnCloseGoalModal) btnCloseGoalModal.addEventListener('click', closeGoalModal);
  if (btnCancelGoal) btnCancelGoal.addEventListener('click', closeGoalModal);
  if (goalModalBackdrop) goalModalBackdrop.addEventListener('click', closeGoalModal);

  if (goalForm) {
    goalForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const titulo = document.getElementById('goalTitulo').value.trim();
      const descricao = document.getElementById('goalDescricao').value.trim();
      const categoria = document.getElementById('goalCategoria').value;
      const unidade = document.getElementById('goalUnidade').value.trim() || 'R$';
      const valorAlvo = parseFloat(document.getElementById('goalValorAlvo').value);
      const valorAtual = parseFloat(document.getElementById('goalValorAtual').value) || 0;
      const prazo = document.getElementById('goalPrazo').value || null;
      const cor = document.getElementById('goalCor').value;

      const payload = { titulo, descricao, categoria, unidade, valorAlvo, valorAtual, prazo, cor };

      try {
        const url = editingGoalId ? `/api/metas/${editingGoalId}` : '/api/metas';
        const method = editingGoalId ? 'PUT' : 'POST';

        const res = await apiFetch(url, {
          method,
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });

        if (res.ok) {
          showToast(editingGoalId ? 'Meta atualizada com sucesso!' : 'Meta criada com sucesso! ');
          goalForm.reset();
          closeGoalModal();
          await fetchGoalsData();
        } else {
          showToast('Erro ao salvar meta.', 'error');
        }
      } catch (err) {
        console.error(err);
      }
    });
  }

  // Aporte Modal
  const openAporteModal = (id, title) => {
    if (!goalAporteModal) return;
    document.getElementById('aporteGoalId').value = id;
    const titleEl = document.getElementById('aporteGoalTitle');
    if (titleEl) titleEl.textContent = `Meta: ${title}`;
    const aporteVal = document.getElementById('aporteValor');
    if (aporteVal) aporteVal.value = '';
    goalAporteModal.classList.remove('hidden');
    setTimeout(() => { if (aporteVal) aporteVal.focus(); }, 100);
  };

  const closeAporteModal = () => {
    if (goalAporteModal) goalAporteModal.classList.add('hidden');
  };

  if (btnCloseGoalAporteModal) btnCloseGoalAporteModal.addEventListener('click', closeAporteModal);
  if (btnCancelAporte) btnCancelAporte.addEventListener('click', closeAporteModal);
  if (goalAporteModalBackdrop) goalAporteModalBackdrop.addEventListener('click', closeAporteModal);

  if (goalAporteForm) {
    goalAporteForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const id = document.getElementById('aporteGoalId').value;
      const valorAporte = parseFloat(document.getElementById('aporteValor').value);

      if (!valorAporte || valorAporte <= 0) return;

      try {
        const res = await apiFetch(`/api/metas/${id}/aporte`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ valorAporte })
        });

        if (res.ok) {
          const updated = await res.json();
          if (updated.concluida) {
            showToast('Sensacional! Meta atingida com 100% de sucesso! ');
          } else {
            showToast(`Aporte de +${formatCurrency(valorAporte)} registrado! `);
          }
          closeAporteModal();
          await fetchGoalsData();
        } else {
          showToast('Erro ao registrar aporte.', 'error');
        }
      } catch (err) {
        console.error(err);
      }
    });
  }

  // ==========================================================================
  // MÓDULO DE CALENDÁRIO UNIFICADO
  // ==========================================================================
  const calendarDaysGrid = document.getElementById('calendarDaysGrid');
  const calendarMonthTitle = document.getElementById('calendarMonthTitle');
  const btnPrevCalendarMonth = document.getElementById('btnPrevCalendarMonth');
  const btnNextCalendarMonth = document.getElementById('btnNextCalendarMonth');
  const btnCalendarToday = document.getElementById('btnCalendarToday');
  const btnOpenNewEvent = document.getElementById('btnOpenNewEvent');
  const calendarChip = document.getElementById('calendarChip');

  const eventModal = document.getElementById('eventModal');
  const eventModalBackdrop = document.getElementById('eventModalBackdrop');
  const btnCloseEventModal = document.getElementById('btnCloseEventModal');
  const btnCancelEvent = document.getElementById('btnCancelEvent');
  const eventForm = document.getElementById('eventForm');

  const dayDetailsModal = document.getElementById('dayDetailsModal');
  const dayDetailsModalBackdrop = document.getElementById('dayDetailsModalBackdrop');
  const btnCloseDayDetailsModal = document.getElementById('btnCloseDayDetailsModal');
  const btnCloseDayDetailsBtn = document.getElementById('btnCloseDayDetailsBtn');
  const btnDayAddEvent = document.getElementById('btnDayAddEvent');
  const dayDetailsList = document.getElementById('dayDetailsList');
  const dayDetailsModalTitle = document.getElementById('dayDetailsModalTitle');

  let calendarCurrentYear = new Date().getFullYear();
  let calendarCurrentMonth = new Date().getMonth() + 1; // 1-12
  let cachedCalendarItems = [];
  let selectedCalendarDateStr = null;

  const fetchCalendarData = async (ano = calendarCurrentYear, mes = calendarCurrentMonth) => {
    if (!getStoredToken()) return;
    calendarCurrentYear = ano;
    calendarCurrentMonth = mes;

    if (calendarMonthTitle) {
      const d = new Date(calendarCurrentYear, calendarCurrentMonth - 1, 1);
      const mesNome = d.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
      calendarMonthTitle.textContent = mesNome.charAt(0).toUpperCase() + mesNome.slice(1);
    }

    try {
      const response = await apiFetch(`/api/calendario?ano=${calendarCurrentYear}&mes=${calendarCurrentMonth}`);
      if (response.ok) {
        cachedCalendarItems = await response.json();
        renderCalendar();
      }
    } catch (err) {
      console.error('Erro ao buscar itens do calendário:', err);
    }
  };

  const renderCalendar = () => {
    if (!calendarDaysGrid) return;

    const today = new Date();
    const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;

    const firstDayIndex = new Date(calendarCurrentYear, calendarCurrentMonth - 1, 1).getDay();
    const daysInMonth = new Date(calendarCurrentYear, calendarCurrentMonth, 0).getDate();
    const prevMonthDays = new Date(calendarCurrentYear, calendarCurrentMonth - 1, 0).getDate();

    let cellsHtml = '';

    // Dias anteriores
    for (let x = firstDayIndex; x > 0; x--) {
      const dayNum = prevMonthDays - x + 1;
      cellsHtml += `<div class="calendar-day-cell other-month"><div class="day-cell-header"><span class="day-number">${dayNum}</span></div></div>`;
    }

    // Dias do mês atual
    for (let i = 1; i <= daysInMonth; i++) {
      const dateStr = `${calendarCurrentYear}-${String(calendarCurrentMonth).padStart(2, '0')}-${String(i).padStart(2, '0')}`;
      const isToday = dateStr === todayStr;

      const dayItems = cachedCalendarItems.filter(item => item.data === dateStr);

      const itemsHtml = dayItems.map(item => {
        let chipClass = 'chip-task';
        let icon = '';
        let label = item.titulo;

        if (item.tipo === 'DESPESA') {
          chipClass = 'chip-expense';
          icon = '';
          label = item.valor ? `-${formatCurrency(item.valor)} ${item.titulo}` : item.titulo;
        } else if (item.tipo === 'RECEITA') {
          chipClass = 'chip-income';
          icon = '';
          label = item.valor ? `+${formatCurrency(item.valor)} ${item.titulo}` : item.titulo;
        } else if (item.tipo === 'EVENTO') {
          chipClass = 'chip-event';
          icon = '';
          label = item.hora ? `${item.hora.substring(0, 5)} ${item.titulo}` : item.titulo;
        }

        const isCompleted = item.status === 'CONCLUIDA' || item.status === 'PAGO';

        return `
          <div class="day-chip ${chipClass} ${isCompleted ? 'completed-item' : ''}" title="${escapeHtml(item.titulo)}">
            <span>${icon}</span>
            <span>${escapeHtml(label)}</span>
          </div>
        `;
      }).join('');

      cellsHtml += `
        <div class="calendar-day-cell ${isToday ? 'is-today' : ''}" data-date="${dateStr}">
          <div class="day-cell-header">
            <span class="day-number">${i}</span>
            ${dayItems.length > 0 ? `<span style="font-size:0.7rem;color:var(--text-muted);font-weight:600;">${dayItems.length}</span>` : ''}
          </div>
          <div class="day-items-list">
            ${itemsHtml}
          </div>
        </div>
      `;
    }

    const totalRendered = firstDayIndex + daysInMonth;
    const nextDays = totalRendered % 7 === 0 ? 0 : 7 - (totalRendered % 7);
    for (let j = 1; j <= nextDays; j++) {
      cellsHtml += `<div class="calendar-day-cell other-month"><div class="day-cell-header"><span class="day-number">${j}</span></div></div>`;
    }

    calendarDaysGrid.innerHTML = cellsHtml;

    calendarDaysGrid.querySelectorAll('.calendar-day-cell:not(.other-month)').forEach(cell => {
      cell.addEventListener('click', () => {
        const dateStr = cell.dataset.date;
        const items = cachedCalendarItems.filter(it => it.data === dateStr);
        openDayDetails(dateStr, items);
      });
    });
  };

  const openDayDetails = (dateStr, items) => {
    selectedCalendarDateStr = dateStr;
    if (!dayDetailsModal) return;

    if (dayDetailsModalTitle) {
      const parts = dateStr.split('-');
      dayDetailsModalTitle.textContent = `Compromissos de ${parts[2]}/${parts[1]}/${parts[0]}`;
    }

    if (dayDetailsList) {
      if (items.length === 0) {
        dayDetailsList.innerHTML = '<div class="dash-empty">Nenhum compromisso ou vencimento agendado para este dia. </div>';
      } else {
        dayDetailsList.innerHTML = items.map(it => {
          let badgeColor = '#3b82f6';
          let actionBtn = '';

          if (it.tipo === 'TAREFA') {
            badgeColor = '#3b82f6';
            const isDone = it.status === 'CONCLUIDA';
            actionBtn = `<button type="button" class="btn btn-xs ${isDone ? 'btn-outline' : 'btn-primary'} btn-day-toggle-task" data-id="${it.origemId}">${isDone ? 'Concluída ✓' : 'Concluir'}</button>`;
          } else if (it.tipo === 'DESPESA' || it.tipo === 'RECEITA') {
            badgeColor = it.tipo === 'RECEITA' ? '#10b981' : '#ef4444';
            const isPaid = it.status === 'PAGO';
            actionBtn = `<button type="button" class="btn btn-xs ${isPaid ? 'btn-outline' : 'btn-primary'} btn-day-toggle-trans" data-id="${it.origemId}" data-status="${it.status}">${isPaid ? 'Pago ✓' : 'Pagar'}</button>`;
          } else if (it.tipo === 'EVENTO') {
            badgeColor = '#8b5cf6';
            actionBtn = `<button type="button" class="btn btn-xs btn-outline btn-day-delete-event" data-id="${it.origemId}" style="color:var(--danger);">Excluir</button>`;
          }

          const valText = it.valor ? ` • <strong>${formatCurrency(it.valor)}</strong>` : '';

          return `
            <div class="day-detail-item" style="border-left: 3px solid ${badgeColor};">
              <div>
                <div style="font-weight:600;font-size:0.9rem;">${escapeHtml(it.titulo)}${valText}</div>
                <div style="font-size:0.75rem;color:var(--text-muted);margin-top:2px;">
                  <span style="color:${badgeColor};font-weight:700;">${it.tipo}</span> | ${escapeHtml(it.detalhe || '')}
                  ${it.hora ? ` • ${it.hora.substring(0, 5)}` : ''}
                </div>
              </div>
              <div>${actionBtn}</div>
            </div>
          `;
        }).join('');

        dayDetailsList.querySelectorAll('.btn-day-toggle-task').forEach(btn => {
          btn.addEventListener('click', async () => {
            const id = btn.dataset.id;
            await apiFetch(`/api/tarefas/${id}/toggle`, { method: 'PATCH' });
            showToast('Status da tarefa atualizado!');
            await fetchCalendarData();
            closeDayDetailsModal();
          });
        });

        dayDetailsList.querySelectorAll('.btn-day-toggle-trans').forEach(btn => {
          btn.addEventListener('click', async () => {
            const id = btn.dataset.id;
            const newStatus = btn.dataset.status === 'PAGO' ? 'PENDENTE' : 'PAGO';
            await toggleTransStatus(id, newStatus);
            await fetchCalendarData();
            closeDayDetailsModal();
          });
        });

        dayDetailsList.querySelectorAll('.btn-day-delete-event').forEach(btn => {
          btn.addEventListener('click', async () => {
            if (!confirm('Deseja excluir este evento?')) return;
            const id = btn.dataset.id;
            await apiFetch(`/api/calendario/eventos/${id}`, { method: 'DELETE' });
            showToast('Evento excluído.');
            await fetchCalendarData();
            closeDayDetailsModal();
          });
        });
      }
    }

    dayDetailsModal.classList.remove('hidden');
  };

  const closeDayDetailsModal = () => {
    if (dayDetailsModal) dayDetailsModal.classList.add('hidden');
  };

  if (btnCloseDayDetailsModal) btnCloseDayDetailsModal.addEventListener('click', closeDayDetailsModal);
  if (btnCloseDayDetailsBtn) btnCloseDayDetailsBtn.addEventListener('click', closeDayDetailsModal);
  if (dayDetailsModalBackdrop) dayDetailsModalBackdrop.addEventListener('click', closeDayDetailsModal);

  // Month navigation buttons
  if (btnPrevCalendarMonth) {
    btnPrevCalendarMonth.addEventListener('click', () => {
      calendarCurrentMonth--;
      if (calendarCurrentMonth < 1) {
        calendarCurrentMonth = 12;
        calendarCurrentYear--;
      }
      fetchCalendarData(calendarCurrentYear, calendarCurrentMonth);
    });
  }

  if (btnNextCalendarMonth) {
    btnNextCalendarMonth.addEventListener('click', () => {
      calendarCurrentMonth++;
      if (calendarCurrentMonth > 12) {
        calendarCurrentMonth = 1;
        calendarCurrentYear++;
      }
      fetchCalendarData(calendarCurrentYear, calendarCurrentMonth);
    });
  }

  if (btnCalendarToday) {
    btnCalendarToday.addEventListener('click', () => {
      const now = new Date();
      fetchCalendarData(now.getFullYear(), now.getMonth() + 1);
    });
  }

  // Event Modal
  const openEventModal = (prefillDate = null) => {
    if (!eventModal) return;
    if (eventForm) eventForm.reset();
    const dateInput = document.getElementById('eventData');
    if (dateInput) {
      dateInput.value = prefillDate || selectedCalendarDateStr || localISODate();
    }
    eventModal.classList.remove('hidden');
    setTimeout(() => {
      const titleInput = document.getElementById('eventTitulo');
      if (titleInput) titleInput.focus();
    }, 100);
  };

  const closeEventModal = () => {
    if (eventModal) eventModal.classList.add('hidden');
  };

  if (btnOpenNewEvent) btnOpenNewEvent.addEventListener('click', () => openEventModal());
  if (btnDayAddEvent) {
    btnDayAddEvent.addEventListener('click', () => {
      closeDayDetailsModal();
      openEventModal(selectedCalendarDateStr);
    });
  }
  if (btnCloseEventModal) btnCloseEventModal.addEventListener('click', closeEventModal);
  if (btnCancelEvent) btnCancelEvent.addEventListener('click', closeEventModal);
  if (eventModalBackdrop) eventModalBackdrop.addEventListener('click', closeEventModal);

  if (eventForm) {
    eventForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const titulo = document.getElementById('eventTitulo').value.trim();
      const descricao = document.getElementById('eventDescricao').value.trim();
      const dataEvento = document.getElementById('eventData').value;
      const horaInicio = document.getElementById('eventHoraInicio').value || null;
      const horaFim = document.getElementById('eventHoraFim').value || null;
      const categoria = document.getElementById('eventCategoria').value;
      const cor = document.getElementById('eventCor').value;

      try {
        const res = await apiFetch('/api/calendario/eventos', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ titulo, descricao, dataEvento, horaInicio, horaFim, categoria, cor })
        });

        if (res.ok) {
          showToast('Compromisso agendado com sucesso! ');
          eventForm.reset();
          closeEventModal();
          await fetchCalendarData();
        } else {
          showToast('Erro ao agendar compromisso.', 'error');
        }
      } catch (err) {
        console.error(err);
      }
    });
  }

  if (calendarChip) {
    calendarChip.addEventListener('click', () => {
      switchAppView('calendar');
    });
  }


  // --- Cockpit Dashboard Data ---
  const fetchDashboardData = async () => {
    if (!getStoredToken()) return;
    try {
      const [resumoRes] = await Promise.all([
        apiFetch('/api/dashboard/resumo'),
        fetchHabitsData(),
        fetchScratchpadData(),
        fetchSportsData(),
        fetchWeatherData(),
        fetchGoalsData()
      ]);
      if (resumoRes && resumoRes.ok) {
        const data = await resumoRes.json();
        renderDashboard(data);
      }
    } catch (err) {
      console.error('Erro ao buscar dados do dashboard:', err);
    }
  };

  const renderDashboard = (data) => {
    // KPIs
    const dashKpiTarefasHoje = document.getElementById('dashKpiTarefasHoje');
    const dashKpiTarefasAtrasadas = document.getElementById('dashKpiTarefasAtrasadas');
    const dashKpiContasVencer = document.getElementById('dashKpiContasVencer');
    const dashKpiContasAtrasadas = document.getElementById('dashKpiContasAtrasadas');
    const dashKpiSaldoTotal = document.getElementById('dashKpiSaldoTotal');
    const dashKpiSaldoPrevisto = document.getElementById('dashKpiSaldoPrevisto');
    const dashKpiBalancoMes = document.getElementById('dashKpiBalancoMes');

    if (dashKpiTarefasHoje) dashKpiTarefasHoje.textContent = data.tarefasHoje || 0;
    if (dashKpiTarefasAtrasadas) dashKpiTarefasAtrasadas.textContent = `${data.tarefasAtrasadas || 0} em atraso`;
    if (dashKpiContasVencer) dashKpiContasVencer.textContent = data.contasPagarHoje || 0;
    if (dashKpiContasAtrasadas) dashKpiContasAtrasadas.textContent = `${data.contasPagarAtrasadas || 0} em atraso`;
    if (dashKpiSaldoTotal) dashKpiSaldoTotal.textContent = formatCurrency(data.saldoTotalContas);
    if (sidebarSaldoTotal) sidebarSaldoTotal.textContent = formatCurrency(data.saldoTotalContas);
    if (dashKpiSaldoPrevisto) dashKpiSaldoPrevisto.textContent = formatCurrency(data.saldoPrevistoMes);
    if (dashKpiBalancoMes) {
      dashKpiBalancoMes.textContent = `+${formatCurrency(data.totalReceitasMes)} / -${formatCurrency(data.totalDespesasMes)}`;
    }

    // Alerts Ribbon
    const cockpitAlertsBanner = document.getElementById('cockpitAlertsBanner');
    const cockpitAlertsText = document.getElementById('cockpitAlertsText');
    if (cockpitAlertsBanner && cockpitAlertsText) {
      const totalAlerts = (data.tarefasAtrasadas || 0) + (data.contasPagarAtrasadas || 0) + (data.contasPagarHoje || 0);
      if (totalAlerts > 0) {
        let msg = [];
        if (data.tarefasAtrasadas > 0) msg.push(`${data.tarefasAtrasadas} tarefa(s) atrasada(s)`);
        if (data.contasPagarAtrasadas > 0) msg.push(`${data.contasPagarAtrasadas} conta(s) em atraso`);
        if (data.contasPagarHoje > 0) msg.push(`${data.contasPagarHoje} conta(s) vencendo hoje`);
        cockpitAlertsText.textContent = `Atenção: Você tem ${msg.join(', ')}!`;
        cockpitAlertsBanner.classList.remove('hidden');
      } else {
        cockpitAlertsBanner.classList.add('hidden');
      }
    }

    // Tasks of the Day
    const dashTasksList = document.getElementById('dashTasksList');
    if (dashTasksList) {
      if (data.tarefasHojeLista && data.tarefasHojeLista.length > 0) {
        dashTasksList.innerHTML = data.tarefasHojeLista.map(t => `
          <div class="dash-item" data-task-id="${t.id}">
            <div class="dash-item-left">
              <input type="checkbox" ${t.concluida ? 'checked' : ''} class="dash-task-check" data-id="${t.id}" style="width:18px;height:18px;cursor:pointer;">
              <div>
                <div class="dash-item-title ${t.concluida ? 'completed-text' : ''}">${escapeHtml(t.titulo)}</div>
                <div class="dash-item-meta">Prioridade: <strong>${t.prioridade}</strong> | ${t.categoria}</div>
              </div>
            </div>
            <button type="button" class="btn btn-xs btn-outline btn-dash-edit-task" data-id="${t.id}">Editar</button>
          </div>
        `).join('');

        dashTasksList.querySelectorAll('.dash-task-check').forEach(chk => {
          chk.addEventListener('change', async () => {
            const taskId = chk.dataset.id;
            try {
              await apiFetch(`/api/tarefas/${taskId}/toggle`, { method: 'PATCH' });
              showToast('Status da tarefa atualizado!');
              await refreshData();
            } catch (err) {
              console.error(err);
            }
          });
        });

        dashTasksList.querySelectorAll('.btn-dash-edit-task').forEach(btn => {
          btn.addEventListener('click', () => {
            const taskId = btn.dataset.id;
            editarTarefaPorId(taskId);
          });
        });
      } else {
        dashTasksList.innerHTML = '<div class="dash-empty"><strong>Nenhuma tarefa para hoje</strong>Crie uma tarefa e comece pelo essencial.<br><button type="button" class="btn btn-xs btn-primary btn-cta-new-task">+ Criar tarefa</button></div>';
      }
    }

    // Urgent Items
    const dashUrgentList = document.getElementById('dashUrgentList');
    if (dashUrgentList) {
      let urgentItemsHtml = '';
      if (data.contasProximasVencimento) {
        const atrasadas = data.contasProximasVencimento.filter(c => c.estaAtrasada && c.tipo === 'DESPESA');
        atrasadas.forEach(c => {
          urgentItemsHtml += `
            <div class="dash-item" style="border-left: 3px solid var(--danger);">
              <div class="dash-item-left">
                
                <div>
                  <div class="dash-item-title">${escapeHtml(c.descricao)} (${formatCurrency(c.valor)})</div>
                  <div class="dash-item-meta" style="color:var(--danger);font-weight:600;">Venceu em ${formatDateOnly(c.dataVencimento)}</div>
                </div>
              </div>
              <button type="button" class="btn btn-xs btn-primary btn-pay-trans" data-id="${c.id}">Pagar</button>
            </div>
          `;
        });
      }

      if (data.tarefasUrgentesLista && data.tarefasUrgentesLista.length > 0) {
        data.tarefasUrgentesLista.slice(0, 4).forEach(t => {
          urgentItemsHtml += `
            <div class="dash-item" style="border-left: 3px solid var(--warning);">
              <div class="dash-item-left">
                
                <div>
                  <div class="dash-item-title">${escapeHtml(t.titulo)}</div>
                  <div class="dash-item-meta">Urgente | ${t.categoria}</div>
                </div>
              </div>
              <button type="button" class="btn btn-xs btn-outline btn-dash-edit-task" data-id="${t.id}">Ver</button>
            </div>
          `;
        });
      }

      if (urgentItemsHtml) {
        dashUrgentList.innerHTML = urgentItemsHtml;
        dashUrgentList.querySelectorAll('.btn-pay-trans').forEach(btn => {
          btn.addEventListener('click', async () => {
            const transId = btn.dataset.id;
            await toggleTransStatus(transId, 'PAGO');
          });
        });
        dashUrgentList.querySelectorAll('.btn-dash-edit-task').forEach(btn => {
          btn.addEventListener('click', () => {
            editarTarefaPorId(btn.dataset.id);
          });
        });
      } else {
        dashUrgentList.innerHTML = '<div class="dash-empty"><strong>Tudo em dia</strong>Nenhum item atrasado ou urgente agora.</div>';
      }
    }

    // Bills list (Next 7 days)
    const dashBillsList = document.getElementById('dashBillsList');
    if (dashBillsList) {
      if (data.contasProximasVencimento && data.contasProximasVencimento.length > 0) {
        dashBillsList.innerHTML = data.contasProximasVencimento.slice(0, 5).map(c => `
          <div class="dash-item">
            <div class="dash-item-left">
              <span class="trans-account-tag" style="background:${safeColor(c.contaCor, '#6366f1')};">${escapeHtml(c.contaNome)}</span>
              <div>
                <div class="dash-item-title">${escapeHtml(c.descricao)}</div>
                <div class="dash-item-meta">Vencimento: ${formatDateOnly(c.dataVencimento)}</div>
              </div>
            </div>
            <div style="display:flex;align-items:center;gap:10px;">
              <span style="font-weight:700;color:${c.tipo === 'RECEITA' ? 'var(--success)' : 'var(--danger)'};">
                ${c.tipo === 'RECEITA' ? '+' : '-'}${formatCurrency(c.valor)}
              </span>
              <button type="button" class="btn btn-xs ${c.status === 'PAGO' ? 'btn-outline' : 'btn-primary'} btn-quick-pay" data-id="${c.id}" data-status="${c.status}">
                ${c.status === 'PAGO' ? 'Pago' : 'Pagar'}
              </button>
            </div>
          </div>
        `).join('');

        dashBillsList.querySelectorAll('.btn-quick-pay').forEach(btn => {
          btn.addEventListener('click', async () => {
            const transId = btn.dataset.id;
            const newStatus = btn.dataset.status === 'PAGO' ? 'PENDENTE' : 'PAGO';
            await toggleTransStatus(transId, newStatus);
          });
        });
      } else {
        dashBillsList.innerHTML = '<div class="dash-empty"><strong>Sem vencimentos próximos</strong>Nenhum pagamento pendente para os próximos 7 dias.</div>';
      }
    }

    // Accounts Mini Grid
    const dashAccountsGrid = document.getElementById('dashAccountsGrid');
    if (dashAccountsGrid) {
      if (data.contas && data.contas.length > 0) {
        dashAccountsGrid.innerHTML = data.contas.map(a => `
          <div class="account-card-mini">
            <div class="acc-mini-name">${escapeHtml(a.nome)}</div>
            <div class="acc-mini-val">${formatCurrency(a.saldoAtual)}</div>
          </div>
        `).join('');
      } else {
        dashAccountsGrid.innerHTML = '<div class="dash-empty">Nenhuma conta cadastrada.</div>';
      }
    }
  };

  // Buttons in Dashboard widgets
  const btnDashGoTasks = document.getElementById('btnDashGoTasks');
  const btnDashGoFinances = document.getElementById('btnDashGoFinances');
  const btnDashAddAccount = document.getElementById('btnDashAddAccount');

  if (btnDashGoTasks) btnDashGoTasks.addEventListener('click', () => switchAppView('tasks'));
  if (btnDashGoFinances) btnDashGoFinances.addEventListener('click', () => switchAppView('finances'));
  if (btnDashAddAccount) btnDashAddAccount.addEventListener('click', () => openAccountModal());

  // --- Finances Module ---
  let currentFinancesMonth = new Date().getMonth() + 1;
  let currentFinancesYear = new Date().getFullYear();
  let currentFinFilter = 'all';
  let cachedAccountsList = [];
  let cachedCategoriesList = [];
  let cachedTransactionsList = [];

  const updateMonthLabel = () => {
    const lblFinancesMonth = document.getElementById('lblFinancesMonth');
    if (lblFinancesMonth) {
      const d = new Date(currentFinancesYear, currentFinancesMonth - 1, 1);
      const str = d.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
      lblFinancesMonth.textContent = str.charAt(0).toUpperCase() + str.slice(1);
    }
  };

  const btnPrevMonth = document.getElementById('btnPrevMonth');
  const btnNextMonth = document.getElementById('btnNextMonth');

  if (btnPrevMonth) {
    btnPrevMonth.addEventListener('click', () => {
      currentFinancesMonth--;
      if (currentFinancesMonth < 1) {
        currentFinancesMonth = 12;
        currentFinancesYear--;
      }
      updateMonthLabel();
      fetchFinancesData();
    });
  }

  if (btnNextMonth) {
    btnNextMonth.addEventListener('click', () => {
      currentFinancesMonth++;
      if (currentFinancesMonth > 12) {
        currentFinancesMonth = 1;
        currentFinancesYear++;
      }
      updateMonthLabel();
      fetchFinancesData();
    });
  }

  const fetchFinancesData = async () => {
    if (!getStoredToken()) return;
    updateMonthLabel();

    const inicio = `${currentFinancesYear}-${String(currentFinancesMonth).padStart(2, '0')}-01`;
    const lastDay = new Date(currentFinancesYear, currentFinancesMonth, 0).getDate();
    const fim = `${currentFinancesYear}-${String(currentFinancesMonth).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`;

    try {
      const [accRes, catRes, transRes] = await Promise.all([
        apiFetch('/api/financas/contas'),
        apiFetch('/api/financas/categorias'),
        apiFetch(`/api/financas/transacoes?inicio=${inicio}&fim=${fim}`)
      ]);

      if (accRes.ok && catRes.ok && transRes.ok) {
        cachedAccountsList = await accRes.json();
        cachedCategoriesList = await catRes.json();
        cachedTransactionsList = await transRes.json();

        renderFinances();
      }
    } catch (err) {
      console.error('Erro ao buscar dados financeiros:', err);
    }
  };

  const renderFinances = () => {
    // 1. Balances
    let totalSaldo = 0;
    cachedAccountsList.forEach(a => {
      totalSaldo += Number(a.saldoAtual) || 0;
    });

    let totalReceitas = 0;
    let totalDespesas = 0;
    let receitasPendentes = 0;
    let despesasPendentes = 0;

    cachedTransactionsList.forEach(t => {
      const val = Number(t.valor) || 0;
      if (t.tipo === 'RECEITA') {
        totalReceitas += val;
        if (t.status === 'PENDENTE') receitasPendentes += val;
      } else {
        totalDespesas += val;
        if (t.status === 'PENDENTE') despesasPendentes += val;
      }
    });

    const saldoPrevisto = totalSaldo + receitasPendentes - despesasPendentes;

    // Summary Cards
    const finTotalSaldo = document.getElementById('finTotalSaldo');
    const finTotalReceitas = document.getElementById('finTotalReceitas');
    const finTotalDespesas = document.getElementById('finTotalDespesas');
    const finSaldoPrevisto = document.getElementById('finSaldoPrevisto');

    if (finTotalSaldo) finTotalSaldo.textContent = formatCurrency(totalSaldo);
    if (sidebarSaldoTotal) sidebarSaldoTotal.textContent = formatCurrency(totalSaldo);
    if (finTotalReceitas) finTotalReceitas.textContent = `+ ${formatCurrency(totalReceitas)}`;
    if (finTotalDespesas) finTotalDespesas.textContent = `- ${formatCurrency(totalDespesas)}`;
    if (finSaldoPrevisto) finSaldoPrevisto.textContent = formatCurrency(saldoPrevisto);

    // 2. Render Accounts Grid
    const finAccountsList = document.getElementById('finAccountsList');
    if (finAccountsList) {
      if (cachedAccountsList.length > 0) {
        finAccountsList.innerHTML = cachedAccountsList.map(a => `
          <div class="account-full-card">
            <div class="acc-card-top">
              <span class="acc-card-name">${escapeHtml(a.nome)}</span>
              <span class="acc-card-type">${a.tipo}</span>
            </div>
            <div class="acc-card-balance">${formatCurrency(a.saldoAtual)}</div>
          </div>
        `).join('');
      } else {
        finAccountsList.innerHTML = '<div class="dash-empty">Nenhuma conta cadastrada.</div>';
      }
    }

    // Populate Account Filter Select
    const finAccountFilter = document.getElementById('finAccountFilter');
    if (finAccountFilter) {
      const currentVal = finAccountFilter.value;
      finAccountFilter.innerHTML = '<option value="">Todas as Contas</option>' +
        cachedAccountsList.map(a => `<option value="${a.id}">${escapeHtml(a.nome)}</option>`).join('');
      finAccountFilter.value = currentVal;
    }

    // 3. Render Transactions List
    renderTransactionsList();
  };

  const renderTransactionsList = () => {
    const finTransactionsList = document.getElementById('finTransactionsList');
    const finAccountFilter = document.getElementById('finAccountFilter');
    if (!finTransactionsList) return;

    const selectedAccId = finAccountFilter ? finAccountFilter.value : '';

    let list = cachedTransactionsList.filter(t => {
      if (selectedAccId && String(t.contaId) !== String(selectedAccId)) return false;
      if (currentFinFilter === 'DESPESA') return t.tipo === 'DESPESA';
      if (currentFinFilter === 'RECEITA') return t.tipo === 'RECEITA';
      if (currentFinFilter === 'PENDENTE') return t.status === 'PENDENTE';
      if (currentFinFilter === 'PAGO') return t.status === 'PAGO';
      return true;
    });

    if (list.length > 0) {
      finTransactionsList.innerHTML = list.map(t => {
        const isExp = t.tipo === 'DESPESA';
        const isPaid = t.status === 'PAGO';
        const isOverdue = t.estaAtrasada;
        const catIcon = t.categoriaIcone === 'utensils' ? 'Alimentação' :
                        t.categoriaIcone === 'home' ? 'Casa' :
                        t.categoriaIcone === 'car' ? 'Transporte' :
                        t.categoriaIcone === 'gamepad' ? 'Lazer' :
                        t.categoriaIcone === 'heartbeat' ? 'Saúde' :
                        t.categoriaIcone === 'graduation-cap' ? 'Educação' :
                        t.categoriaIcone === 'money-bill-wave' ? 'Renda' :
                        t.categoriaIcone === 'laptop-code' ? 'Trabalho' :
                        t.categoriaIcone === 'chart-line' ? 'Investimentos' : 'Outros';

        let statusClass = 'pending';
        let statusText = 'Pendente';
        if (isPaid) {
          statusClass = 'paid';
          statusText = isExp ? 'Pago' : 'Recebido';
        } else if (isOverdue) {
          statusClass = 'overdue';
          statusText = 'Atrasado';
        }

        return `
          <div class="trans-item-row" data-id="${t.id}">
            <div class="trans-item-left">
              <div class="trans-cat-badge" style="background:${hexAlpha(t.categoriaCor, '18', 'var(--panel-3)')};color:${safeColor(t.categoriaCor, 'var(--ink)')};">
                ${catIcon}
              </div>
              <div class="trans-item-center">
                <div class="trans-title">${escapeHtml(t.descricao)}</div>
                <div class="trans-meta-tags">
                  <span class="trans-account-tag" style="background:${safeColor(t.contaCor, '#6366f1')};">${escapeHtml(t.contaNome)}</span>
                  <span>Vencimento: <strong>${formatDateOnly(t.dataVencimento)}</strong></span>
                  ${t.parcelado ? `<span style="background:var(--bg-card);padding:1px 6px;border-radius:4px;border:1px solid var(--border-color);">Parcela ${t.numeroParcela}/${t.totalParcelas}</span>` : ''}
                </div>
              </div>
            </div>

            <div class="trans-item-right">
              <span class="trans-amount ${isExp ? 'expense' : 'income'}">
                ${isExp ? '-' : '+'}${formatCurrency(t.valor)}
              </span>
              <button type="button" class="trans-status-badge ${statusClass} btn-toggle-status" data-id="${t.id}" data-current="${t.status}" title="Clique para alternar status">
                ${statusText}
              </button>
              <button type="button" class="btn-icon btn-delete-trans" data-id="${t.id}" title="Excluir Transação" style="color:var(--text-muted);">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
              </button>
            </div>
          </div>
        `;
      }).join('');

      finTransactionsList.querySelectorAll('.btn-toggle-status').forEach(btn => {
        btn.addEventListener('click', async () => {
          const transId = btn.dataset.id;
          const current = btn.dataset.current;
          const novoStatus = current === 'PAGO' ? 'PENDENTE' : 'PAGO';
          await toggleTransStatus(transId, novoStatus);
        });
      });

      finTransactionsList.querySelectorAll('.btn-delete-trans').forEach(btn => {
        btn.addEventListener('click', async () => {
          const transId = btn.dataset.id;
          if (confirm('Deseja realmente excluir este lançamento financeiro?')) {
            await deleteTrans(transId);
          }
        });
      });
    } else {
      finTransactionsList.innerHTML = '<div class="dash-empty">Nenhum lançamento encontrado para os filtros selecionados.</div>';
    }
  };

  // Filter pills
  const finFilterPills = document.querySelectorAll('.fin-filter-pill');
  finFilterPills.forEach(pill => {
    pill.addEventListener('click', () => {
      finFilterPills.forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      currentFinFilter = pill.dataset.finFilter;
      renderTransactionsList();
    });
  });

  const finAccountFilter = document.getElementById('finAccountFilter');
  if (finAccountFilter) {
    finAccountFilter.addEventListener('change', () => {
      renderTransactionsList();
    });
  }

  const toggleTransStatus = async (id, status) => {
    try {
      const response = await apiFetch(`/api/financas/transacoes/${id}/status?status=${status}`, {
        method: 'PATCH'
      });
      if (response.ok) {
        showToast(`Status atualizado para ${status === 'PAGO' ? 'Pago' : 'Pendente'}!`);
        await refreshData();
      }
    } catch (err) {
      console.error(err);
    }
  };

  const deleteTrans = async (id) => {
    try {
      const response = await apiFetch(`/api/financas/transacoes/${id}`, {
        method: 'DELETE'
      });
      if (response.ok) {
        showToast('Transação excluída com sucesso!');
        await refreshData();
      }
    } catch (err) {
      console.error(err);
    }
  };

  // --- Modais de Transação & Conta ---
  const transactionModal = document.getElementById('transactionModal');
  const transModalBackdrop = document.getElementById('transModalBackdrop');
  const btnCloseTransModal = document.getElementById('btnCloseTransModal');
  const btnCancelTrans = document.getElementById('btnCancelTrans');
  const transactionForm = document.getElementById('transactionForm');
  const btnTypeExpense = document.getElementById('btnTypeExpense');
  const btnTypeIncome = document.getElementById('btnTypeIncome');
  const transTypeInput = document.getElementById('transTypeInput');
  const transConta = document.getElementById('transConta');
  const transCategoria = document.getElementById('transCategoria');
  const transParcelado = document.getElementById('transParcelado');
  const transParcelasGroup = document.getElementById('transParcelasGroup');

  const openTransactionModal = async (tipo = 'DESPESA') => {
    if (!transactionModal) return;

    if (cachedAccountsList.length === 0) {
      try {
        const accRes = await apiFetch('/api/financas/contas');
        if (accRes.ok) cachedAccountsList = await accRes.json();
      } catch {}
    }
    if (cachedCategoriesList.length === 0) {
      try {
        const catRes = await apiFetch('/api/financas/categorias');
        if (catRes.ok) cachedCategoriesList = await catRes.json();
      } catch {}
    }

    if (transConta) {
      transConta.innerHTML = cachedAccountsList.map(a => `<option value="${a.id}">${escapeHtml(a.nome)}</option>`).join('');
    }

    setTransType(tipo);

    const dateInput = document.getElementById('transDataVencimento');
    if (dateInput && !dateInput.value) {
      dateInput.value = localISODate();
    }

    transactionModal.classList.remove('hidden');
    const descInput = document.getElementById('transDescricao');
    if (descInput) setTimeout(() => descInput.focus(), 100);
  };

  const closeTransactionModal = () => {
    if (transactionModal) transactionModal.classList.add('hidden');
  };

  const setTransType = (tipo) => {
    if (!transTypeInput) return;
    transTypeInput.value = tipo;
    if (tipo === 'DESPESA') {
      btnTypeExpense.classList.add('active-expense');
      btnTypeIncome.classList.remove('active-income');
    } else {
      btnTypeIncome.classList.add('active-income');
      btnTypeExpense.classList.remove('active-expense');
    }
    updateCategoriesDropdown(tipo);
  };

  const updateCategoriesDropdown = (tipo) => {
    if (!transCategoria) return;
    const cats = cachedCategoriesList.filter(c => c.tipo === tipo);
    transCategoria.innerHTML = cats.map(c => `<option value="${c.id}">${escapeHtml(c.nome)}</option>`).join('');
  };

  if (btnTypeExpense) btnTypeExpense.addEventListener('click', () => setTransType('DESPESA'));
  if (btnTypeIncome) btnTypeIncome.addEventListener('click', () => setTransType('RECEITA'));

  if (transParcelado && transParcelasGroup) {
    transParcelado.addEventListener('change', () => {
      if (transParcelado.checked) {
        transParcelasGroup.classList.remove('hidden');
      } else {
        transParcelasGroup.classList.add('hidden');
      }
    });
  }

  if (btnCloseTransModal) btnCloseTransModal.addEventListener('click', closeTransactionModal);
  if (btnCancelTrans) btnCancelTrans.addEventListener('click', closeTransactionModal);
  if (transModalBackdrop) transModalBackdrop.addEventListener('click', closeTransactionModal);

  const btnOpenTransModal = document.getElementById('btnOpenTransModal');
  if (btnOpenTransModal) btnOpenTransModal.addEventListener('click', () => openTransactionModal('DESPESA'));

  if (transactionForm) {
    transactionForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const tipo = transTypeInput.value;
      const descricao = document.getElementById('transDescricao').value;
      const valor = parseFloat(document.getElementById('transValor').value);
      const dataVencimento = document.getElementById('transDataVencimento').value;
      const contaId = parseInt(document.getElementById('transConta').value, 10);
      const categoriaId = transCategoria.value ? parseInt(transCategoria.value, 10) : null;
      const jaPago = document.getElementById('transStatusPago').checked;
      const isParcelado = transParcelado.checked;
      const totalParcelas = isParcelado ? parseInt(document.getElementById('transTotalParcelas').value, 10) : null;
      const observacoes = document.getElementById('transObservacoes').value;

      const payload = {
        tipo,
        descricao,
        valor,
        dataVencimento,
        contaId,
        categoriaId,
        status: jaPago ? 'PAGO' : 'PENDENTE',
        parcelado: isParcelado,
        totalParcelas: isParcelado ? totalParcelas : null,
        observacoes
      };

      try {
        const response = await apiFetch('/api/financas/transacoes', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });

        if (response.ok) {
          showToast(isParcelado ? `Lançamento parcelado em ${totalParcelas}x criado!` : 'Transação registrada com sucesso!');
          transactionForm.reset();
          if (transParcelasGroup) transParcelasGroup.classList.add('hidden');
          closeTransactionModal();
          await refreshData();
        } else {
          showToast('Erro ao criar transação. Verifique os dados.', 'error');
        }
      } catch (err) {
        console.error(err);
      }
    });
  }

  // Account Modal
  const accountModal = document.getElementById('accountModal');
  const accountModalBackdrop = document.getElementById('accountModalBackdrop');
  const btnCloseAccountModal = document.getElementById('btnCloseAccountModal');
  const btnCancelAccount = document.getElementById('btnCancelAccount');
  const accountForm = document.getElementById('accountForm');
  const btnOpenAccountModal = document.getElementById('btnOpenAccountModal');

  const openAccountModal = () => {
    if (!accountModal) return;
    accountModal.classList.remove('hidden');
    const nameInput = document.getElementById('accountNome');
    if (nameInput) setTimeout(() => nameInput.focus(), 100);
  };

  const closeAccountModal = () => {
    if (accountModal) accountModal.classList.add('hidden');
  };

  if (btnOpenAccountModal) btnOpenAccountModal.addEventListener('click', openAccountModal);
  if (btnCloseAccountModal) btnCloseAccountModal.addEventListener('click', closeAccountModal);
  if (btnCancelAccount) btnCancelAccount.addEventListener('click', closeAccountModal);
  if (accountModalBackdrop) accountModalBackdrop.addEventListener('click', closeAccountModal);

  if (accountForm) {
    accountForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const nome = document.getElementById('accountNome').value;
      const tipo = document.getElementById('accountTipo').value;
      const saldoInicial = parseFloat(document.getElementById('accountSaldoInicial').value) || 0;
      const cor = document.getElementById('accountCor').value;

      try {
        const response = await apiFetch('/api/financas/contas', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ nome, tipo, saldoInicial, cor })
        });

        if (response.ok) {
          showToast(`Conta "${nome}" criada com sucesso!`);
          accountForm.reset();
          closeAccountModal();
          await refreshData();
        } else {
          showToast('Erro ao criar conta.', 'error');
        }
      } catch (err) {
        console.error(err);
      }
    });
  }

  switchAppView('dashboard');

  // ==========================================================================
  // ONBOARDING (primeira visita)
  // ==========================================================================
  const onboardingModal = document.getElementById('onboardingModal');
  const onboardingBackdrop = document.getElementById('onboardingBackdrop');
  const btnCloseOnboarding = document.getElementById('btnCloseOnboarding');
  const btnOnboardingSkip = document.getElementById('btnOnboardingSkip');
  const btnOnboardingPrev = document.getElementById('btnOnboardingPrev');
  const btnOnboardingNext = document.getElementById('btnOnboardingNext');
  let onboardingStep = 0;

  const renderOnboarding = () => {
    if (!onboardingModal) return;
    onboardingModal.querySelectorAll('.onboarding-panel').forEach(p => {
      p.classList.toggle('hidden', Number(p.dataset.panel) !== onboardingStep);
    });
    onboardingModal.querySelectorAll('.onboarding-dot').forEach(d => {
      d.classList.toggle('active', Number(d.dataset.step) === onboardingStep);
    });
    if (btnOnboardingPrev) btnOnboardingPrev.classList.toggle('hidden', onboardingStep === 0);
    if (btnOnboardingNext) {
      btnOnboardingNext.textContent = onboardingStep >= 2 ? 'Começar' : 'Próximo';
    }
  };

  const closeOnboarding = () => {
    if (onboardingModal) onboardingModal.classList.add('hidden');
    try { localStorage.setItem('lifehub_onboarding_done', '1'); } catch (_) {}
  };

  const openOnboarding = () => {
    onboardingStep = 0;
    renderOnboarding();
    if (onboardingModal) onboardingModal.classList.remove('hidden');
  };

  if (btnOnboardingNext) {
    btnOnboardingNext.addEventListener('click', () => {
      if (onboardingStep >= 2) {
        closeOnboarding();
        return;
      }
      onboardingStep++;
      renderOnboarding();
    });
  }
  if (btnOnboardingPrev) {
    btnOnboardingPrev.addEventListener('click', () => {
      onboardingStep = Math.max(0, onboardingStep - 1);
      renderOnboarding();
    });
  }
  if (btnOnboardingSkip) btnOnboardingSkip.addEventListener('click', closeOnboarding);
  if (btnCloseOnboarding) btnCloseOnboarding.addEventListener('click', closeOnboarding);
  if (onboardingBackdrop) onboardingBackdrop.addEventListener('click', closeOnboarding);

  const maybeShowOnboarding = () => {
    try {
      if (!localStorage.getItem('lifehub_onboarding_done')) {
        setTimeout(openOnboarding, 600);
      }
    } catch (_) {}
  };

  // ==========================================================================
  // EMPTY STATES COM CTA (delegação de cliques)
  // ==========================================================================
  document.addEventListener('click', (e) => {
    const t = e.target;
    if (!(t instanceof Element)) return;
    if (t.classList.contains('btn-cta-new-task') || t.closest('.btn-cta-new-task')) {
      switchAppView('tasks');
      const input = document.getElementById('taskTitle');
      if (input) input.focus();
    }
    if (t.id === 'btnOpenNewHabitEmpty' || t.closest('#btnOpenNewHabitEmpty')) {
      const btn = document.getElementById('btnOpenNewHabit');
      if (btn) btn.click();
    }
    if (t.id === 'btnOpenNewGoalEmpty' || t.closest('#btnOpenNewGoalEmpty')) {
      const btn = document.getElementById('btnOpenNewGoalCockpit');
      if (btn) btn.click();
    }
    if (t.id === 'btnDashAddAccountEmpty' || t.closest('#btnDashAddAccountEmpty')) {
      const btn = document.getElementById('btnDashAddAccount');
      if (btn) btn.click();
    }
  });

  // ==========================================================================
  // KANBAN WIP (limite em Em Andamento)
  // ==========================================================================
  const KANBAN_WIP_LIMIT = 5;
  const updateKanbanWip = () => {
    const col = document.querySelector('.kanban-column[data-status="EM_ANDAMENTO"]');
    const countEl = document.getElementById('countEmAndamento');
    if (!col || !countEl) return;
    const n = Number(countEl.textContent) || 0;
    let badge = col.querySelector('.kanban-wip-badge');
    if (!badge) {
      badge = document.createElement('span');
      badge.className = 'kanban-wip-badge';
      countEl.insertAdjacentElement('afterend', badge);
    }
    badge.textContent = n + '/' + KANBAN_WIP_LIMIT + ' WIP';
    badge.classList.toggle('wip-over', n > KANBAN_WIP_LIMIT);
    col.classList.toggle('wip-blocked', n > KANBAN_WIP_LIMIT);
  };

  // Observa contagens do kanban
  const kanbanCounts = ['countAFazer', 'countEmAndamento', 'countConcluida'];
  kanbanCounts.forEach(id => {
    const el = document.getElementById(id);
    if (el && window.MutationObserver) {
      new MutationObserver(() => updateKanbanWip()).observe(el, { childList: true, characterData: true, subtree: true });
    }
  });
  setTimeout(updateKanbanWip, 1200);

  // Hook no final do boot
  checkInitialAuth();
  maybeShowOnboarding();
});
