var API_BASE = window.location.origin;

var FETCH_TIMEOUT_MS = 15000;

function fetchWithTimeout(url, options) {
    return new Promise(function (resolve, reject) {
        var controller = new AbortController();
        var timer = setTimeout(function () {
            controller.abort();
            reject(new Error('Tiempo de espera agotado'));
        }, FETCH_TIMEOUT_MS);
        options = options || {};
        options.signal = controller.signal;
        fetch(url, options)
            .then(function (r) {
                clearTimeout(timer);
                resolve(r);
            })
            .catch(function (err) {
                clearTimeout(timer);
                if (err.name === 'AbortError') {
                    reject(new Error('Tiempo de espera agotado'));
                } else {
                    reject(err);
                }
            });
    });
}

document.addEventListener('DOMContentLoaded', function () {
    var userId = getUserId();
    if (!userId) {
        window.location.href = '/login.html';
        return;
    }
    if (!getIsAdmin()) {
        window.location.href = '/';
        return;
    }

    updateNavbar();
    cargarReportesAdmin();

    document.querySelectorAll('.admin-tab').forEach(function (tab) {
        tab.addEventListener('click', function () {
            document.querySelectorAll('.admin-tab').forEach(function (t) { t.classList.remove('active'); });
            tab.classList.add('active');
            var tabName = tab.dataset.tab;
            if (tabName === 'reports') cargarReportesAdmin();
            else if (tabName === 'banned') cargarBaneadosAdmin();
            else if (tabName === 'notifications') cargarAccionesAdmin();
        });
    });

    var searchInput = document.getElementById('adminSearchInput');
    if (searchInput) {
        searchInput.addEventListener('input', function () {
            var activeTab = document.querySelector('.admin-tab.active');
            if (activeTab && activeTab.dataset.tab === 'banned') {
                filtrarBaneados(this.value.trim().toLowerCase());
            }
        });
    }
});

var baneadosData = [];

function cargarReportesAdmin() {
    var tabContent = document.getElementById('adminTabContent');
    tabContent.innerHTML = '<p class="estado">Cargando reportes...</p>';

    fetchWithTimeout(API_BASE + '/api/admin/reports')
        .then(function (r) { return r.json(); })
        .then(function (data) {
            if (!data.ok || !data.reportes || data.reportes.length === 0) {
                tabContent.innerHTML = '<div class="admin-empty">&#128681; No hay reportes de usuarios.</div>';
                return;
            }

            var html = '<div class="admin-table-container"><table class="admin-table">' +
                '<thead><tr>' +
                '<th>ID</th>' +
                '<th>Reto</th>' +
                '<th>Reportador</th>' +
                '<th>Motivo</th>' +
                '<th>Fecha</th>' +
                '<th>Acción</th>' +
                '</tr></thead><tbody>';

            data.reportes.forEach(function (rep) {
                html += '<tr>' +
                    '<td class="cell-id">#' + rep.id + '</td>' +
                    '<td class="cell-title">' + escapeHtml(rep.challengeTitle) + '</td>' +
                    '<td>' + escapeHtml(rep.reporterName) + '<br><span class="cell-email">' + escapeHtml(rep.reporterEmail) + '</span></td>' +
                    '<td class="cell-reason">' + escapeHtml(rep.reason) + '</td>' +
                    '<td class="cell-date">' + formatDate(rep.createdAt) + '</td>' +
                    '<td class="cell-actions">' +
                    '<button class="btn btn-outline btn-sm view-challenge-btn" data-challenge-id="' + rep.challengeId + '">&#128065; Ver Reto</button>' +
                    '</td>' +
                    '</tr>';
            });

            html += '</tbody></table></div>';
            tabContent.innerHTML = html;

            tabContent.querySelectorAll('.view-challenge-btn').forEach(function (btn) {
                btn.addEventListener('click', function () {
                    var id = parseInt(btn.dataset.challengeId);
                    sessionStorage.setItem('pendingChallengeId', id);
                    window.location.href = '/';
                });
            });
        })
        .catch(function (error) {
            console.error('Error cargando reportes:', error);
            tabContent.innerHTML = '<div class="resultado error">Error al cargar reportes: ' + error.message + '</div>';
        });
}

function cargarBaneadosAdmin() {
    var tabContent = document.getElementById('adminTabContent');
    tabContent.innerHTML = '<p class="estado">Cargando usuarios baneados...</p>';

    fetchWithTimeout(API_BASE + '/api/admin/banned-users')
        .then(function (r) { return r.json(); })
        .then(function (data) {
            if (!data.ok || !data.usuarios || data.usuarios.length === 0) {
                tabContent.innerHTML = '<div class="admin-empty">&#128274; No hay usuarios baneados.</div>';
                baneadosData = [];
                return;
            }

            baneadosData = data.usuarios;
            renderBaneadosTable(baneadosData);
        })
        .catch(function (error) {
            console.error('Error cargando baneados:', error);
            tabContent.innerHTML = '<div class="resultado error">Error al cargar baneados: ' + error.message + '</div>';
        });
}

function renderBaneadosTable(usuarios) {
    var tabContent = document.getElementById('adminTabContent');

    var html = '<div class="admin-table-container"><table class="admin-table">' +
        '<thead><tr>' +
        '<th>Usuario</th>' +
        '<th>Email</th>' +
        '<th>Motivo del Baneo</th>' +
        '<th>Acción</th>' +
        '</tr></thead><tbody>';

    usuarios.forEach(function (user) {
        html += '<tr>' +
            '<td class="cell-user">' +
            '<span class="user-avatar-sm">&#128100;</span> ' + escapeHtml(user.username) +
            '</td>' +
            '<td class="cell-email">' + escapeHtml(user.email) + '</td>' +
            '<td class="cell-reason">' + escapeHtml(user.banReason || 'No especificado') + '</td>' +
            '<td class="cell-actions">' +
            '<button class="btn btn-outline btn-sm unban-btn" data-user-id="' + user.id + '" data-user-name="' + escapeHtml(user.username) + '">&#128274; Desbanear</button>' +
            '</td>' +
            '</tr>';
    });

    html += '</tbody></table></div>';
    tabContent.innerHTML = html;

    tabContent.querySelectorAll('.unban-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            if (confirm('&#191;Est&#225;s seguro de desbanear a ' + btn.dataset.userName + '?')) {
                desbanearUsuario(parseInt(btn.dataset.userId));
            }
        });
    });
}

function filtrarBaneados(query) {
    if (!query) {
        renderBaneadosTable(baneadosData);
        return;
    }
    var filtrados = baneadosData.filter(function (u) {
        return u.username.toLowerCase().indexOf(query) !== -1 ||
               u.email.toLowerCase().indexOf(query) !== -1;
    });
    renderBaneadosTable(filtrados);
}

function cargarAccionesAdmin() {
    var tabContent = document.getElementById('adminTabContent');
    tabContent.innerHTML = '<p class="estado">Cargando notificaciones...</p>';

    fetchWithTimeout(API_BASE + '/api/admin/actions')
        .then(function (r) { return r.json(); })
        .then(function (data) {
            if (!data.ok || !data.acciones || data.acciones.length === 0) {
                tabContent.innerHTML = '<div class="admin-empty">&#128231; No hay notificaciones enviadas.</div>';
                return;
            }

            var html = '<div class="admin-table-container"><table class="admin-table">' +
                '<thead><tr>' +
                '<th>Tipo</th>' +
                '<th>Email Destino</th>' +
                '<th>Motivo</th>' +
                '<th>Administrador</th>' +
                '<th>Fecha</th>' +
                '</tr></thead><tbody>';

            data.acciones.forEach(function (acc) {
                var actionLabel = acc.actionType === 'ban' ? '&#128683; Baneo' : '&#128465; Eliminaci&oacute;n';
                html += '<tr>' +
                    '<td class="cell-type">' + actionLabel + '</td>' +
                    '<td class="cell-email">' + escapeHtml(acc.targetUserEmail || 'desconocido') + '</td>' +
                    '<td class="cell-reason">' + escapeHtml(acc.reason) + '</td>' +
                    '<td>' + escapeHtml(acc.adminName) + '</td>' +
                    '<td class="cell-date">' + formatDate(acc.createdAt) + '</td>' +
                    '</tr>';
            });

            html += '</tbody></table></div>';
            tabContent.innerHTML = html;
        })
        .catch(function (error) {
            console.error('Error cargando notificaciones:', error);
            tabContent.innerHTML = '<div class="resultado error">Error al cargar notificaciones: ' + error.message + '</div>';
        });
}

function desbanearUsuario(targetUserId) {
    var adminUserId = getUserId();

    fetchWithTimeout(API_BASE + '/api/admin/unban', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ adminUserId: parseInt(adminUserId), targetUserId: targetUserId })
    })
    .then(function (response) { return response.json(); })
    .then(function (data) {
        if (data.ok) {
            alert('Usuario desbaneado correctamente');
            cargarBaneadosAdmin();
        } else {
            alert('Error: ' + data.mensaje);
        }
    })
    .catch(function (error) {
        alert('Error de conexi&oacute;n: ' + error.message);
    });
}

function escapeHtml(str) {
    if (!str) return '';
    var div = document.createElement('div');
    div.appendChild(document.createTextNode(str));
    return div.innerHTML;
}
