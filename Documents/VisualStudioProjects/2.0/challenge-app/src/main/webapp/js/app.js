const API_BASE = window.location.origin;

function getUserId() {
    return sessionStorage.getItem('userId');
}

function getUsername() {
    return sessionStorage.getItem('username');
}

function getIsAdmin() {
    return sessionStorage.getItem('isAdmin') === 'true';
}

function showError(elementId, message) {
    const el = document.getElementById(elementId);
    if (el) {
        el.innerHTML = '<div class="resultado error">' + message + '</div>';
    }
}

function showSuccess(elementId, message) {
    const el = document.getElementById(elementId);
    if (el) {
        el.innerHTML = '<div class="resultado success">' + message + '</div>';
    }
}

function checkBanned(data) {
    if (data && data.banned) {
        sessionStorage.clear();
        alert('Tu cuenta ha sido baneada. Has sido desconectado.');
        window.location.href = '/';
        return true;
    }
    return false;
}

function clearResultado(elementId) {
    const el = document.getElementById(elementId);
    if (el) {
        el.innerHTML = '';
    }
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleDateString('es-ES', { year: 'numeric', month: 'long', day: 'numeric' });
}

function formatCurrency(amount) {
    return '\u20ac' + parseFloat(amount).toFixed(2);
}

function updateNavbar() {
    var navLinks = document.getElementById('navLinks');
    if (!navLinks) return;
    var userId = getUserId();
    if (userId) {
        var adminBtn = '';
        if (getIsAdmin()) {
            adminBtn = '<button class="btn btn-danger btn-shield" id="adminPanelBtn">\uD83D\uDEE1 Admin</button>';
        }
        navLinks.innerHTML = adminBtn + '<a href="dashboard.html" class="btn btn-outline">\uD83D\uDCCA Panel</a><a href="create-challenge.html" class="btn btn-primary">\uD83D\uDE80 Nuevo Reto</a><a href="#" class="btn btn-primary" id="logoutBtn">Cerrar Sesi\u00f3n</a>';
        document.getElementById('logoutBtn')?.addEventListener('click', function (e) {
            e.preventDefault();
            sessionStorage.clear();
            window.location.href = '/';
        });
        document.getElementById('adminPanelBtn')?.addEventListener('click', function () {
            window.location.href = '/admin.html';
        });
    }
}

function abrirAdminPanel() {
    var modal = document.getElementById('adminModal');
    if (!modal) return;
    modal.classList.remove('hidden');
    document.getElementById('adminTabContent').innerHTML = '<p class="estado">Cargando...</p>';
    cargarReportesAdmin();
}

function cargarReportesAdmin() {
    var tabContent = document.getElementById('adminTabContent');
    tabContent.innerHTML = '<p class="estado">Cargando reportes...</p>';

    fetch(API_BASE + '/api/admin/reports')
        .then(function (r) { return r.json(); })
        .then(function (data) {
            if (!data.ok || !data.reportes || data.reportes.length === 0) {
                tabContent.innerHTML = '<div class="estado">No hay reportes de usuarios.</div>';
                return;
            }

            var html = '<div class="admin-list">';
            data.reportes.forEach(function (rep) {
                html += '<div class="admin-list-item">' +
                    '<div class="admin-list-header">' +
                    '<span class="admin-list-badge">\uD83D\uDEA9 Reporte #' + rep.id + '</span>' +
                    '<span class="admin-list-date">' + formatDate(rep.createdAt) + '</span>' +
                    '</div>' +
                    '<div class="admin-list-body">' +
                    '<p><strong>Reto:</strong> ' + rep.challengeTitle + ' (ID: ' + rep.challengeId + ')</p>' +
                    '<p><strong>Reportado por:</strong> ' + rep.reporterName + ' (' + rep.reporterEmail + ')</p>' +
                    '<p><strong>Motivo:</strong> ' + rep.reason + '</p>' +
                    '</div>' +
                    '<div class="admin-list-actions">' +
                    '<button class="btn btn-outline btn-sm view-challenge-btn" data-challenge-id="' + rep.challengeId + '">\uD83D\uDC41 Ver Reto</button>' +
                    '</div>' +
                    '</div>' +
                    '<div class="admin-list-separator">\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500</div>';
            });
            html += '</div>';
            tabContent.innerHTML = html;

            tabContent.querySelectorAll('.view-challenge-btn').forEach(function (btn) {
                btn.addEventListener('click', function () {
                    verRetoAdmin(parseInt(btn.dataset.challengeId));
                });
            });
        })
        .catch(function (error) {
            tabContent.innerHTML = '<div class="resultado error">Error al cargar reportes: ' + error.message + '</div>';
        });
}

function verRetoAdmin(challengeId) {
    document.getElementById('adminModal').classList.add('hidden');
    var path = window.location.pathname;
    if (path === '/' || path.endsWith('index.html') || path === '') {
        openDetail(challengeId);
    } else {
        sessionStorage.setItem('pendingChallengeId', challengeId);
        window.location.href = '/';
    }
}

function cargarBaneadosAdmin() {
    var tabContent = document.getElementById('adminTabContent');
    tabContent.innerHTML = '<p class="estado">Cargando usuarios baneados...</p>';

    fetch(API_BASE + '/api/admin/banned-users')
        .then(function (r) { return r.json(); })
        .then(function (data) {
            if (!data.ok || !data.usuarios || data.usuarios.length === 0) {
                tabContent.innerHTML = '<div class="estado">No hay usuarios baneados.</div>';
                return;
            }

            var html = '<div class="admin-list">';
            data.usuarios.forEach(function (user) {
                html += '<div class="admin-list-item">' +
                    '<div class="admin-list-header">' +
                    '<span class="admin-list-badge admin-list-badge-ban">\uD83D\uDEAB Baneado</span>' +
                    '</div>' +
                    '<div class="admin-list-body">' +
                    '<p><strong>Usuario:</strong> ' + user.username + '</p>' +
                    '<p><strong>Email:</strong> ' + user.email + '</p>' +
                    '<p><strong>Motivo:</strong> ' + (user.banReason || 'No especificado') + '</p>' +
                    '<button class="btn btn-outline btn-sm unban-btn" data-user-id="' + user.id + '" data-user-name="' + user.username + '">\uD83D\uDD13 Desbanear</button>' +
                    '</div>' +
                    '</div>';
            });
            html += '</div>';
            tabContent.innerHTML = html;

            tabContent.querySelectorAll('.unban-btn').forEach(function (btn) {
                btn.addEventListener('click', function () {
                    if (confirm('\u00bfEst\u00e1s seguro de desbanear a ' + btn.dataset.userName + '?')) {
                        desbanearUsuario(parseInt(btn.dataset.userId));
                    }
                });
            });
        })
        .catch(function (error) {
            tabContent.innerHTML = '<div class="resultado error">Error al cargar baneados: ' + error.message + '</div>';
        });
}

function cargarAccionesAdmin() {
    var tabContent = document.getElementById('adminTabContent');
    tabContent.innerHTML = '<p class="estado">Cargando notificaciones...</p>';

    fetch(API_BASE + '/api/admin/actions')
        .then(function (r) { return r.json(); })
        .then(function (data) {
            if (!data.ok || !data.acciones || data.acciones.length === 0) {
                tabContent.innerHTML = '<div class="estado">No hay notificaciones enviadas.</div>';
                return;
            }

            var html = '<div class="admin-list">';
            data.acciones.forEach(function (acc) {
                var actionIcon = acc.actionType === 'ban' ? '\uD83D\uDEAB' : '\uD83D\uDDD1';
                var actionLabel = acc.actionType === 'ban' ? 'Ban' : 'Eliminaci\u00f3n de reto';
                var email = acc.targetUserEmail || 'email desconocido';

                html += '<div class="admin-list-item admin-list-email">' +
                    '<div class="admin-list-header">' +
                    '<span class="admin-list-badge">' + actionIcon + ' ' + actionLabel + '</span>' +
                    '<span class="admin-list-date">' + formatDate(acc.createdAt) + '</span>' +
                    '</div>' +
                    '<div class="admin-list-body">' +
                    '<p>\uD83D\uDCEE <strong>Para:</strong> ' + email + '</p>' +
                    '<p>\uD83D\uDCE4 <strong>Asunto:</strong> ' + (acc.actionType === 'ban' ? 'Has sido baneado de EMpujas' : 'Tu reto ha sido eliminado de EMpujas') + '</p>' +
                    '<p>\uD83D\uDCDD <strong>Mensaje:</strong> ' + acc.reason + '</p>' +
                    '<p class="admin-list-admin">\u2014 Administrador: ' + acc.adminName + '</p>' +
                    '</div>' +
                    '</div>';
            });
            html += '</div>';
            tabContent.innerHTML = html;
        })
        .catch(function (error) {
            tabContent.innerHTML = '<div class="resultado error">Error al cargar notificaciones: ' + error.message + '</div>';
        });
}

function desbanearUsuario(targetUserId) {
    var adminUserId = getUserId();

    fetch(API_BASE + '/api/admin/unban', {
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
        alert('Error de conexi\u00f3n: ' + error.message);
    });
}

function openReasonModal(actionType, targetName) {
    return new Promise(function (resolve, reject) {
        var modal = document.getElementById('reasonModal');
        var title = document.getElementById('reasonModalTitle');
        var desc = document.getElementById('reasonModalDesc');
        var input = document.getElementById('reasonInput');
        var resultDiv = document.getElementById('reasonResultado');
        var form = document.getElementById('reasonForm');

        input.value = '';
        resultDiv.innerHTML = '';

        if (actionType === 'ban') {
            title.textContent = '\uD83D\uDEAB Banear a ' + targetName;
            desc.textContent = 'Est\u00e1s a punto de banear a "' + targetName + '". Es obligatorio explicar el motivo.';
        } else {
            title.textContent = '\uD83D\uDDD1 Eliminar reto de ' + targetName;
            desc.textContent = 'Est\u00e1s a punto de eliminar el reto de "' + targetName + '". Es obligatorio explicar el motivo.';
        }

        modal.classList.remove('hidden');
        input.focus();

        function cleanup() {
            modal.classList.add('hidden');
            form.removeEventListener('submit', onSubmit);
            document.getElementById('cancelReason').removeEventListener('click', onCancel);
        }

        function onSubmit(e) {
            e.preventDefault();
            var reason = input.value.trim();
            if (!reason) {
                showError('reasonResultado', 'Debes escribir un motivo obligatorio.');
                return;
            }
            cleanup();
            resolve(reason);
        }

        function onCancel() {
            cleanup();
            reject(new Error('Acci\u00f3n cancelada'));
        }

        form.addEventListener('submit', onSubmit);
        document.getElementById('cancelReason').addEventListener('click', onCancel);

        modal.addEventListener('click', function (e) {
            if (e.target === modal) {
                onCancel();
            }
        });
    });
}

function createChallengeCard(challenge) {
    const progress = challenge.currentAmount / challenge.goalAmount * 100;
    const isCompleted = challenge.status === 'completed';
    const isFav = challenge.favorited;
    const isAdmin = getIsAdmin();
    const userId = getUserId();
    const card = document.createElement('div');
    card.className = 'challenge-card' + (isCompleted ? ' completed' : '');
    card.dataset.id = challenge.id;

    var imageHtml = '';
    if (challenge.imageUrl) {
        imageHtml = '<div class="card-image"><img src="' + challenge.imageUrl + '" alt="' + challenge.title + '" loading="lazy"></div>';
    }

    var favBtn = '';
    if (userId) {
        favBtn = '<button class="fav-btn ' + (isFav ? 'fav-active' : '') + '" data-id="' + challenge.id + '" data-fav="' + isFav + '">' + (isFav ? '\u2764' : '\u2661') + '</button>';
    }

    var adminActions = '';
    if (isAdmin) {
        adminActions = '\n        <div class="admin-actions">\n' +
            '            <button class="btn btn-danger btn-sm delete-challenge" data-id="' + challenge.id + '" data-creator="' + challenge.creatorName + '">\uD83D\uDDD1 Eliminar Reto</button>\n' +
            '            <button class="btn btn-outline-danger btn-sm ban-creator" data-creator-id="' + challenge.creatorId + '" data-creator-name="' + challenge.creatorName + '">\uD83D\uDEAB Banear ' + challenge.creatorName + '</button>\n' +
            '        </div>';
    }

    var reportBtn = '';
    if (userId && !isAdmin) {
        reportBtn = '<button class="btn btn-sm btn-outline report-challenge" data-id="' + challenge.id + '">\uD83D\uDEA9 Reportar</button>';
    }

    var statusEmoji = isCompleted ? '\u2705' : '\uD83D\uDD25';
    var statusText = isCompleted ? 'Completado' : 'Activo';

    card.innerHTML = imageHtml + favBtn + '\n' +
        '        <h3>' + challenge.title + '</h3>\n' +
        '        <p class="challenge-creator">por ' + challenge.creatorName + ' \u00b7 ' + formatDate(challenge.createdAt) + '</p>\n' +
        '        <p class="challenge-desc">' + challenge.description + '</p>\n' +
        '        <div class="challenge-progress">\n' +
        '            <div class="progress-bar">\n' +
        '                <div class="progress-fill" style="width: ' + Math.min(100, progress) + '%"></div>\n' +
        '            </div>\n' +
        '            <div class="progress-info">\n' +
        '                <span>' + formatCurrency(challenge.currentAmount) + ' recaudados</span>\n' +
        '                <span>' + formatCurrency(challenge.goalAmount) + ' meta</span>\n' +
        '            </div>\n' +
        '        </div>\n' +
        '        <div class="challenge-stats">\n' +
        '            <span>\uD83D\uDC65 ' + challenge.supporterCount + ' apoyador(es)</span>\n' +
        '            <span class="challenge-status ' + (isCompleted ? 'status-completed' : 'status-active') + '">' + statusEmoji + ' ' + statusText + '</span>\n' +
        '        </div>\n' +
        '        <div class="challenge-actions">\n' +
        '            <button class="btn btn-outline view-detail">\uD83D\uDC41 Ver Detalles</button>\n' +
        '            ' + (isCompleted ? '<button class="btn btn-success view-completed">\uD83C\uDFC6 Ver Logro</button>' : '<button class="btn btn-primary support-btn">\uD83D\uDCB0 Apoyar</button>') + '\n' +
        '            ' + reportBtn + '\n' +
        '        </div>\n' +
        adminActions +
        '    ';

    card.querySelector('.view-detail').addEventListener('click', function () {
        openDetail(challenge.id);
    });

    var supportBtn = card.querySelector('.support-btn');
    if (supportBtn) {
        supportBtn.addEventListener('click', function () {
            openDonate(challenge.id, challenge.title);
        });
    }

    var viewCompleted = card.querySelector('.view-completed');
    if (viewCompleted) {
        viewCompleted.addEventListener('click', function () {
            openDetail(challenge.id);
        });
    }

    var favButton = card.querySelector('.fav-btn');
    if (favButton) {
        favButton.addEventListener('click', function () {
            toggleFav(challenge.id, favButton);
        });
    }

    var deleteBtn = card.querySelector('.delete-challenge');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', function () {
            openReasonModal('delete', deleteBtn.dataset.creator)
                .then(function (reason) {
                    eliminarReto(challenge.id, reason);
                })
                .catch(function () {});
        });
    }

    var banBtn = card.querySelector('.ban-creator');
    if (banBtn) {
        banBtn.addEventListener('click', function () {
            var creatorId = parseInt(banBtn.dataset.creatorId);
            var creatorName = banBtn.dataset.creatorName;
            openReasonModal('ban', creatorName)
                .then(function (reason) {
                    banearUsuario(creatorId, reason);
                })
                .catch(function () {});
        });
    }

    var reportBtnEl = card.querySelector('.report-challenge');
    if (reportBtnEl) {
        reportBtnEl.addEventListener('click', function () {
            abrirReporteModal(challenge.id, challenge.title);
        });
    }

    return card;
}

function toggleFav(challengeId, btn) {
    var userId = getUserId();
    if (!userId) {
        window.location.href = '/login.html';
        return;
    }

    fetch(API_BASE + '/api/toggle-favorite', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId: parseInt(userId), challengeId: challengeId })
    })
    .then(function (response) { return response.json(); })
    .then(function (data) {
        if (checkBanned(data)) return;
        if (data.ok) {
            var isNowFav = btn.dataset.fav === 'true';
            if (isNowFav) {
                btn.dataset.fav = 'false';
                btn.classList.remove('fav-active');
                btn.innerHTML = '\u2661';
            } else {
                btn.dataset.fav = 'true';
                btn.classList.add('fav-active');
                btn.innerHTML = '\u2764';
            }
        }
    })
    .catch(function (error) {
        console.error('Error al toggle fav:', error);
    });
}

function eliminarReto(challengeId, reason) {
    var userId = getUserId();

    fetch(API_BASE + '/api/delete-challenge', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ challengeId: challengeId, userId: parseInt(userId), reason: reason })
    })
    .then(function (response) { return response.json(); })
    .then(function (data) {
        if (data.ok) {
            if (typeof cargarRetos === 'function') cargarRetos();
            if (typeof cargarDashboard === 'function') cargarDashboard();
        } else {
            alert('Error: ' + data.mensaje);
        }
    })
    .catch(function (error) {
        alert('Error de conexi\u00f3n: ' + error.message);
    });
}

function banearUsuario(targetUserId, reason) {
    var adminUserId = getUserId();

    fetch(API_BASE + '/api/ban-user', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ adminUserId: parseInt(adminUserId), targetUserId: targetUserId, reason: reason })
    })
    .then(function (response) { return response.json(); })
    .then(function (data) {
        if (data.ok) {
            alert('Usuario baneado correctamente');
            if (typeof cargarRetos === 'function') cargarRetos();
            if (typeof cargarDashboard === 'function') cargarDashboard();
        } else {
            alert('Error: ' + data.mensaje);
        }
    })
    .catch(function (error) {
        alert('Error de conexi\u00f3n: ' + error.message);
    });
}

function abrirReporteModal(challengeId, title) {
    var modal = document.getElementById('reportModal');
    if (!modal) return;
    document.getElementById('reportChallengeId').value = challengeId;
    document.querySelector('#reportContent h3').textContent = '\uD83D\uDEA9 Reportar: ' + title;
    document.getElementById('reportReason').value = '';
    document.getElementById('reportResultado').innerHTML = '';
    modal.classList.remove('hidden');
}

function abrirCompletionModal(challengeId, currentVideoUrl, currentMessage, detailModal) {
    var modal = document.getElementById('completionModal');
    if (!modal) {
        var body = document.body;
        var div = document.createElement('div');
        div.id = 'completionModal';
        div.className = 'modal-overlay hidden';
        div.innerHTML = '<div class="modal-content">' +
            '<button class="modal-close" id="closeCompletion">&times;</button>' +
            '<h3>&#127942; Video de Logro y Agradecimiento</h3>' +
            '<p class="donate-subtitle">Comparte el video demostrando que completaste el reto y agradece a tus apoyadores.</p>' +
            '<form id="completionForm">' +
            '<div class="form-group">' +
            '<label for="completionVideoUrl">URL del Video de Logro</label>' +
            '<input type="url" id="completionVideoUrl" placeholder="https://www.youtube.com/embed/..." value="' + (currentVideoUrl || '') + '">' +
            '<span class="field-hint">Sube tu video a YouTube y pega el enlace. Solo los apoyadores podr&aacute;n verlo.</span>' +
            '</div>' +
            '<div class="form-group">' +
            '<label for="completionMessage">Mensaje de Agradecimiento</label>' +
            '<textarea id="completionMessage" rows="4" placeholder="Escribe un mensaje de agradecimiento para tus apoyadores...">' + (currentMessage || '') + '</textarea>' +
            '</div>' +
            '<div class="form-actions">' +
            '<button type="button" class="btn btn-outline" id="cancelCompletion">Cancelar</button>' +
            '<button type="submit" class="btn btn-success">&#128190; Guardar</button>' +
            '</div>' +
            '</form>' +
            '<div id="completionResultado" class="donate-resultado"></div>' +
            '</div>';
        body.appendChild(div);

        document.getElementById('closeCompletion').addEventListener('click', function () {
            modal.classList.add('hidden');
        });
        document.getElementById('cancelCompletion').addEventListener('click', function () {
            modal.classList.add('hidden');
        });
        document.getElementById('completionForm').addEventListener('submit', function (e) {
            e.preventDefault();
            guardarCompletion(challengeId, modal, detailModal);
        });
        modal.addEventListener('click', function (e) {
            if (e.target === modal) modal.classList.add('hidden');
        });
    } else {
        document.getElementById('completionVideoUrl').value = currentVideoUrl || '';
        document.getElementById('completionMessage').value = currentMessage || '';
        document.getElementById('completionResultado').innerHTML = '';
        var oldForm = document.getElementById('completionForm');
        var newForm = oldForm.cloneNode(true);
        oldForm.parentNode.replaceChild(newForm, oldForm);
        newForm.addEventListener('submit', function (e) {
            e.preventDefault();
            guardarCompletion(challengeId, modal, detailModal);
        });
    }
    modal = document.getElementById('completionModal');
    modal.classList.remove('hidden');
}

function guardarCompletion(challengeId, modal, detailModal) {
    var videoUrl = document.getElementById('completionVideoUrl').value.trim();
    var message = document.getElementById('completionMessage').value.trim();
    var resultDiv = document.getElementById('completionResultado');
    resultDiv.innerHTML = '';

    if (!videoUrl && !message) {
        resultDiv.innerHTML = '<div class="resultado error">Debes proporcionar al menos un video o un mensaje.</div>';
        return;
    }

    fetch(API_BASE + '/api/complete-challenge', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            challengeId: challengeId,
            userId: parseInt(getUserId()),
            completionVideoUrl: videoUrl,
            thankYouMessage: message
        })
    })
    .then(function (response) { return response.json(); })
    .then(function (data) {
        if (data.ok) {
            modal.classList.add('hidden');
            detailModal.classList.add('hidden');
            if (typeof cargarRetos === 'function') cargarRetos();
        } else {
            resultDiv.innerHTML = '<div class="resultado error">' + (data.mensaje || 'Error al guardar') + '</div>';
        }
    })
    .catch(function (error) {
        resultDiv.innerHTML = '<div class="resultado error">Error de conexi&oacute;n: ' + error.message + '</div>';
    });
}

function openDetail(challengeId) {
    const modal = document.getElementById('detailModal');
    const content = document.getElementById('detailContent');
    const estado = document.getElementById('estado');

    if (estado) estado.textContent = 'Cargando detalles del reto...';
    content.innerHTML = '<p class="estado">Cargando...</p>';
    modal.classList.remove('hidden');

    var userId = getUserId();
    var url = API_BASE + '/api/challenge?id=' + challengeId;
    if (userId) url += '&userId=' + userId;

    fetch(url)
        .then(function (response) {
            return response.json();
        })
        .then(function (data) {
            if (checkBanned(data)) return;
            if (!data.ok) {
                throw new Error(data.mensaje || 'Error al cargar detalles');
            }

            const c = data.challenge;
            const donations = data.donations || [];
            const progress = c.currentAmount / c.goalAmount * 100;
            const isCompleted = c.status === 'completed';
            const isAdmin = getIsAdmin();
            const isCreator = userId && parseInt(userId) === c.creatorId;
            var hasDonated = data.hasDonated === true;

            let html = '<div class="detail-layout">';
            html += '<div class="detail-info">';
            html += '<h2>' + c.title + '</h2>';
            html += '<p class="detail-meta">por ' + c.creatorName + ' \u00b7 ' + formatDate(c.createdAt) + '</p>';

            if (c.imageUrl) {
                html += '<div class="detail-image"><img src="' + c.imageUrl + '" alt="' + c.title + '"></div>';
            }

            html += '<p class="detail-desc">' + c.description + '</p>';

            if (c.videoUrl) {
                html += '<div class="video-container"><iframe src="' + c.videoUrl + '" frameborder="0" allowfullscreen></iframe></div>';
            }

            if (isCompleted && (hasDonated || isAdmin || isCreator)) {
                if (c.completionVideoUrl) {
                    html += '<h3>\uD83C\uDFC6 Video de Logro</h3>';
                    html += '<div class="video-container"><iframe src="' + c.completionVideoUrl + '" frameborder="0" allowfullscreen></iframe></div>';
                }
                if (c.thankYouMessage) {
                    html += '<div class="thank-you-message">' +
                        '<div class="thank-you-icon">\uD83D\uDE4F</div>' +
                        '<p class="thank-you-text">' + c.thankYouMessage + '</p>' +
                        '<p class="thank-you-author">\u2014 ' + c.creatorName + '</p>' +
                        '</div>';
                }
                if (!c.completionVideoUrl && !c.thankYouMessage && isCreator) {
                    html += '<div class="completion-pending">' +
                        '<p>\uD83C\uDFC6 \u00a1Tu reto se ha completado! A\u00f1ade un video de logro y un mensaje de agradecimiento.</p>' +
                        '<button class="btn btn-success" id="addCompletionBtn">\uD83C\uDFA5 A\u00f1adir Video de Logro</button>' +
                        '</div>';
                }
                if (c.completionVideoUrl || c.thankYouMessage) {
                    if (isCreator) {
                        html += '<button class="btn btn-outline btn-sm" id="editCompletionBtn" style="margin-top:0.5rem">\u270F Editar Video / Agradecimiento</button>';
                    }
                }
            } else if (isCompleted && !hasDonated && !isAdmin && !isCreator) {
                html += '<div class="completion-locked">' +
                    '<p>\uD83D\uDD12 El video de logro es exclusivo para apoyadores del reto.</p>' +
                    '</div>';
            }

            if (donations.length > 0) {
                html += '<h3>\uD83D\uDC65 \u00daltimos Apoyos</h3><ul>';
                donations.slice(0, 10).forEach(function (d) {
                    html += '<li><strong>' + d.donorName + '</strong> don\u00f3 ' + formatCurrency(d.amount) + '</li>';
                });
                html += '</ul>';
            }

            html += '</div>';
            html += '<div class="detail-panel">';
            html += '<h3>Progreso de Financiaci\u00f3n</h3>';
            html += '<div class="panel-progress">';
            html += '<div class="progress-bar"><div class="progress-fill" style="width: ' + Math.min(100, progress) + '%"></div></div>';
            html += '</div>';
            html += '<div class="panel-stat"><span>Cantidad Actual</span><strong>' + formatCurrency(c.currentAmount) + '</strong></div>';
            html += '<div class="panel-stat"><span>Meta</span><strong>' + formatCurrency(c.goalAmount) + '</strong></div>';
            html += '<div class="panel-stat"><span>Progreso</span><strong>' + Math.round(progress) + '%</strong></div>';
            html += '<div class="panel-stat"><span>\uD83D\uDC65 Apoyadores</span><strong>' + c.supporterCount + '</strong></div>';
            html += '<div class="panel-stat"><span>Estado</span><strong class="' + (isCompleted ? 'status-completed' : 'status-active') + '">' + (isCompleted ? '\u2705 Completado' : '\uD83D\uDD25 Activo') + '</strong></div>';

            if (isAdmin) {
                html += '<div class="admin-actions-panel">';
                html += '<button class="btn btn-danger btn-full" id="detailDeleteBtn">\uD83D\uDDD1 Eliminar este Reto</button>';
                html += '<button class="btn btn-outline-danger btn-full" id="detailBanBtn" style="margin-top:0.5rem">\uD83D\uDEAB Banear a ' + c.creatorName + '</button>';
                html += '</div>';
            } else if (!isCompleted) {
                html += '<button class="btn btn-primary btn-full" id="detailSupportBtn">\uD83D\uDCB0 Apoyar este Reto</button>';
            }

            if (isCompleted && c.videoUrl) {
                html += '<div class="completed-confirmation">\u2705 \u00a1Este reto se ha completado con \u00e9xito!</div>';
            }

            html += '</div></div>';

            content.innerHTML = html;

            var supportBtn = document.getElementById('detailSupportBtn');
            if (supportBtn) {
                supportBtn.addEventListener('click', function () {
                    modal.classList.add('hidden');
                    openDonate(c.id, c.title);
                });
            }

            var deleteBtn = document.getElementById('detailDeleteBtn');
            if (deleteBtn) {
                deleteBtn.addEventListener('click', function () {
                    modal.classList.add('hidden');
                    openReasonModal('delete', c.creatorName)
                        .then(function (reason) {
                            eliminarReto(c.id, reason);
                        })
                        .catch(function () {});
                });
            }

            var banBtn = document.getElementById('detailBanBtn');
            if (banBtn) {
                banBtn.addEventListener('click', function () {
                    modal.classList.add('hidden');
                    openReasonModal('ban', c.creatorName)
                        .then(function (reason) {
                            banearUsuario(c.creatorId, reason);
                        })
                        .catch(function () {});
                });
            }

            var addCompletionBtn = document.getElementById('addCompletionBtn');
            if (addCompletionBtn) {
                addCompletionBtn.addEventListener('click', function () {
                    abrirCompletionModal(c.id, c.completionVideoUrl, c.thankYouMessage, modal);
                });
            }

            var editCompletionBtn = document.getElementById('editCompletionBtn');
            if (editCompletionBtn) {
                editCompletionBtn.addEventListener('click', function () {
                    abrirCompletionModal(c.id, c.completionVideoUrl, c.thankYouMessage, modal);
                });
            }

            if (estado) estado.textContent = '';
        })
        .catch(function (error) {
            content.innerHTML = '<div class="resultado error">' + error.message + '</div>';
            if (estado) estado.textContent = '';
        });
}

function openDonate(challengeId, title) {
    const modal = document.getElementById('donateModal');
    document.getElementById('donateChallengeId').value = challengeId;
    document.querySelector('#donateContent h3').textContent = '\uD83D\uDCB0 Apoyar: ' + title;
    document.getElementById('donateResultado').innerHTML = '';
    document.getElementById('donateForm').reset();
    modal.classList.remove('hidden');
}

function cargarRetos() {
    const activeGrid = document.getElementById('activeChallengesGrid');
    const completedGrid = document.getElementById('completedChallengesGrid');
    const estado = document.getElementById('estado');

    if (!activeGrid && !completedGrid) {
        var grid = document.getElementById('challengesGrid');
        if (!grid) return;
    }
    if (activeGrid) activeGrid.innerHTML = '';
    if (completedGrid) completedGrid.innerHTML = '';
    if (estado) estado.textContent = 'Cargando retos...';

    var userId = getUserId();
    var url = API_BASE + '/api/challenges';
    if (userId) url += '?userId=' + userId;

    fetch(url)
        .then(function (response) {
            return response.json();
        })
        .then(function (data) {
            if (estado) estado.textContent = '';
            if (!data.ok) {
                throw new Error(data.mensaje || 'Error al cargar retos');
            }

            if (data.total === 0) {
                if (activeGrid) {
                    activeGrid.innerHTML = '<div class="estado">No hay retos disponibles a\u00fan. \u00a1S\u00e9 el primero en crear uno!</div>';
                }
                return;
            }

            var activeList = [];
            var completedList = [];

            data.resultados.forEach(function (challenge) {
                if (challenge.status === 'completed') {
                    completedList.push(challenge);
                } else {
                    activeList.push(challenge);
                }
            });

            if (activeGrid) {
                if (activeList.length === 0) {
                    activeGrid.innerHTML = '<div class="estado">No hay retos activos en este momento.</div>';
                } else {
                    activeList.forEach(function (ch) {
                        activeGrid.appendChild(createChallengeCard(ch));
                    });
                }
            }

            if (completedGrid) {
                var completedHeading = document.getElementById('completedHeading');
                if (completedHeading) {
                    completedHeading.style.display = completedList.length > 0 ? '' : 'none';
                }
                if (completedList.length === 0) {
                    completedGrid.innerHTML = '';
                } else {
                    completedList.forEach(function (ch) {
                        completedGrid.appendChild(createChallengeCard(ch));
                    });
                }
            }

            if (!activeGrid && !completedGrid && document.getElementById('challengesGrid')) {
                data.resultados.forEach(function (ch) {
                    document.getElementById('challengesGrid').appendChild(createChallengeCard(ch));
                });
            }

            var pendingId = sessionStorage.getItem('pendingChallengeId');
            if (pendingId) {
                sessionStorage.removeItem('pendingChallengeId');
                setTimeout(function () {
                    openDetail(parseInt(pendingId));
                }, 300);
            }
        })
        .catch(function (error) {
            if (estado) estado.textContent = '';
            if (activeGrid) activeGrid.innerHTML = '<div class="resultado error">' + error.message + '</div>';
        });
}

document.addEventListener('DOMContentLoaded', function () {

    updateNavbar();

    if (document.getElementById('challengesGrid') && !document.getElementById('dashboardGrid')) {
        cargarRetos();
    }

    var loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', function (e) {
            e.preventDefault();
            clearResultado('loginResultado');

            var email = document.getElementById('email').value.trim();
            var password = document.getElementById('password').value.trim();

            fetch(API_BASE + '/api/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: email, password: password })
            })
                .then(function (response) { return response.json(); })
                .then(function (data) {
                    if (data.ok) {
                        sessionStorage.setItem('userId', data.user.id);
                        sessionStorage.setItem('username', data.user.username);
                        sessionStorage.setItem('userEmail', data.user.email);
                        sessionStorage.setItem('isAdmin', data.isAdmin);
                        window.location.href = '/dashboard.html';
                    } else {
                        showError('loginResultado', data.mensaje || 'Credenciales inv\u00e1lidas');
                    }
                })
                .catch(function (error) {
                    showError('loginResultado', 'Error de conexi\u00f3n: ' + error.message);
                });
        });
    }

    var registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', function (e) {
            e.preventDefault();
            clearResultado('registerResultado');

            var username = document.getElementById('regUsername').value.trim();
            var email = document.getElementById('regEmail').value.trim();
            var password = document.getElementById('regPassword').value.trim();
            var confirm = document.getElementById('regConfirm').value.trim();

            if (password !== confirm) {
                showError('registerResultado', 'Las contrase\u00f1as no coinciden');
                return;
            }

            fetch(API_BASE + '/api/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: username, email: email, password: password })
            })
                .then(function (response) { return response.json(); })
                .then(function (data) {
                    if (data.ok) {
                        sessionStorage.setItem('userId', data.user.id);
                        sessionStorage.setItem('username', data.user.username);
                        sessionStorage.setItem('userEmail', data.user.email);
                        sessionStorage.setItem('isAdmin', data.isAdmin);
                        window.location.href = '/dashboard.html';
                    } else {
                        showError('registerResultado', data.mensaje || 'Error al registrarse');
                    }
                })
                .catch(function (error) {
                    showError('registerResultado', 'Error de conexi\u00f3n: ' + error.message);
                });
        });
    }

    var createForm = document.getElementById('createChallengeForm');
    if (createForm) {
        var titleInput = document.getElementById('challengeTitle');
        var descInput = document.getElementById('challengeDesc');
        var goalInput = document.getElementById('challengeGoal');
        var videoInput = document.getElementById('challengeVideo');
        var imageInput = document.getElementById('challengeImage');
        var previewSection = document.getElementById('previewSection');
        var previewTitle = document.getElementById('previewTitle');
        var previewDesc = document.getElementById('previewDesc');
        var previewGoal = document.getElementById('previewGoal');
        var previewImage = document.getElementById('previewImage');
        var previewImageContainer = document.getElementById('previewImageContainer');

        function updatePreview() {
            var t = titleInput.value.trim();
            var d = descInput.value.trim();
            var g = goalInput.value;
            var img = imageInput.value.trim();

            if (t || d || g) {
                previewSection.style.display = 'flex';
                if (t) previewTitle.textContent = t;
                if (d) previewDesc.textContent = d.substring(0, 200) + (d.length > 200 ? '...' : '');
                if (g) previewGoal.innerHTML = 'Meta: ' + formatCurrency(parseFloat(g));

                if (img) {
                    previewImage.src = img;
                    previewImageContainer.style.display = 'block';
                } else {
                    previewImageContainer.style.display = 'none';
                }
            } else {
                previewSection.style.display = 'none';
            }
        }

        titleInput.addEventListener('input', updatePreview);
        descInput.addEventListener('input', updatePreview);
        goalInput.addEventListener('input', updatePreview);
        imageInput.addEventListener('input', updatePreview);

        createForm.addEventListener('submit', function (e) {
            e.preventDefault();
            clearResultado('createResultado');

            if (!getUserId()) {
                showError('createResultado', 'Debes iniciar sesi\u00f3n para crear un reto');
                return;
            }

            var title = titleInput.value.trim();
            var description = descInput.value.trim();
            var goalAmount = parseFloat(goalInput.value);
            var videoUrl = videoInput.value.trim();
            var imageUrl = imageInput.value.trim();

            if (!title || !description || !goalAmount || goalAmount <= 0) {
                showError('createResultado', 'Todos los campos son obligatorios y la meta debe ser mayor que 0');
                return;
            }

            fetch(API_BASE + '/api/create-challenge', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    title: title,
                    description: description,
                    goalAmount: goalAmount,
                    creatorId: parseInt(getUserId()),
                    videoUrl: videoUrl || null,
                    imageUrl: imageUrl || null
                })
            })
                .then(function (response) { return response.json(); })
                .then(function (data) {
                    if (checkBanned(data)) return;
                    if (data.ok) {
                        showSuccess('createResultado', '\u00a1Reto creado con \u00e9xito! Redirigiendo...');
                        setTimeout(function () {
                            window.location.href = '/dashboard.html';
                        }, 1500);
                    } else {
                        showError('createResultado', data.mensaje || 'Error al crear el reto');
                    }
                })
                .catch(function (error) {
                    showError('createResultado', 'Error de conexi\u00f3n: ' + error.message);
                });
        });
    }

    var paymentRadios = document.querySelectorAll('input[name="payment"]');
    paymentRadios.forEach(function (radio) {
        radio.addEventListener('change', function () {
            var cardDetails = document.getElementById('cardDetails');
            if (cardDetails) {
                if (this.value === 'card') {
                    cardDetails.style.display = 'block';
                } else {
                    cardDetails.style.display = 'none';
                }
            }
        });
    });

    var reportForm = document.getElementById('reportForm');
    if (reportForm) {
        reportForm.addEventListener('submit', function (e) {
            e.preventDefault();
            clearResultado('reportResultado');

            var challengeId = parseInt(document.getElementById('reportChallengeId').value);
            var reason = document.getElementById('reportReason').value.trim();

            if (!reason) {
                showError('reportResultado', 'Por favor, describe el motivo del reporte');
                return;
            }

            fetch(API_BASE + '/api/report-challenge', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    challengeId: challengeId,
                    userId: parseInt(getUserId()),
                    reason: reason
                })
            })
                .then(function (response) { return response.json(); })
                .then(function (data) {
                    if (checkBanned(data)) return;
                    if (data.ok) {
                        showSuccess('reportResultado', 'Reporte enviado correctamente. Gracias por ayudar a mantener la comunidad segura.');
                        setTimeout(function () {
                            document.getElementById('reportModal').classList.add('hidden');
                        }, 2000);
                    } else {
                        showError('reportResultado', data.mensaje || 'Error al enviar reporte');
                    }
                })
                .catch(function (error) {
                    showError('reportResultado', 'Error de conexi\u00f3n: ' + error.message);
                });
        });
    }

    var donateForm = document.getElementById('donateForm');
    if (donateForm) {
        donateForm.addEventListener('submit', function (e) {
            e.preventDefault();
            clearResultado('donateResultado');

            var challengeId = parseInt(document.getElementById('donateChallengeId').value);
            var donorName = document.getElementById('donorName').value.trim();
            var amount = parseFloat(document.getElementById('donateAmount').value);

            if (!donorName || !amount || amount <= 0) {
                showError('donateResultado', 'Por favor, rellena todos los campos obligatorios');
                return;
            }

            var body = {
                challengeId: challengeId,
                donorName: donorName,
                amount: amount,
                userId: getUserId() ? parseInt(getUserId()) : null
            };

            fetch(API_BASE + '/api/donate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            })
                .then(function (response) { return response.json(); })
                .then(function (data) {
                    if (checkBanned(data)) return;
                    if (data.ok) {
                        showSuccess('donateResultado', '\u00a1Gracias! Tu donaci\u00f3n de ' + formatCurrency(amount) + ' ha sido procesada.');
                        setTimeout(function () {
                            document.getElementById('donateModal').classList.add('hidden');
                            if (typeof cargarRetos === 'function') cargarRetos();
                            if (typeof cargarDashboard === 'function') cargarDashboard();
                        }, 1500);
                    } else {
                        showError('donateResultado', data.mensaje || 'Error al procesar la donaci\u00f3n');
                    }
                })
                .catch(function (error) {
                    showError('donateResultado', 'Error de conexi\u00f3n: ' + error.message);
                });
        });
    }

    if (document.getElementById('dashboardGrid')) {
        cargarDashboard();
    }

    document.getElementById('closeDetail')?.addEventListener('click', function () {
        document.getElementById('detailModal').classList.add('hidden');
    });

    document.getElementById('closeDonate')?.addEventListener('click', function () {
        document.getElementById('donateModal').classList.add('hidden');
    });

    document.getElementById('cancelDonate')?.addEventListener('click', function () {
        document.getElementById('donateModal').classList.add('hidden');
    });

    document.getElementById('closeReport')?.addEventListener('click', function () {
        document.getElementById('reportModal').classList.add('hidden');
    });

    document.getElementById('cancelReport')?.addEventListener('click', function () {
        document.getElementById('reportModal').classList.add('hidden');
    });

    document.getElementById('detailModal')?.addEventListener('click', function (e) {
        if (e.target === this) this.classList.add('hidden');
    });

    document.getElementById('donateModal')?.addEventListener('click', function (e) {
        if (e.target === this) this.classList.add('hidden');
    });

    document.getElementById('reportModal')?.addEventListener('click', function (e) {
        if (e.target === this) this.classList.add('hidden');
    });
});

function cargarDashboard() {
    var userId = getUserId();
    if (!userId) {
        window.location.href = '/login.html';
        return;
    }

    document.getElementById('dashboardUsername').textContent = getUsername() || 'Usuario';
    document.getElementById('dashboardEmail').textContent = sessionStorage.getItem('userEmail') || '';

    fetch(API_BASE + '/api/profile?userId=' + userId)
        .then(function (response) { return response.json(); })
        .then(function (data) {
            if (checkBanned(data)) return;
            if (data.ok && data.resumen) {
                document.getElementById('totalCreated').textContent = data.resumen.totalCreated || 0;
                document.getElementById('totalCompleted').textContent = data.resumen.totalCompleted || 0;
                document.getElementById('totalRaised').textContent = formatCurrency(data.resumen.totalRaised || 0);
            }
        })
        .catch(function (error) {
            console.error('Error al cargar perfil:', error);
        });

    fetch(API_BASE + '/api/favorites?userId=' + userId)
        .then(function (response) { return response.json(); })
        .then(function (data) {
            if (checkBanned(data)) return;
            if (data.ok) {
                document.getElementById('totalFavorited').textContent = data.total || 0;
            }
        })
        .catch(function () {});

    var activeTab = document.querySelector('.tab.active');
    var tabName = activeTab ? activeTab.dataset.tab : 'active';
    cargarRetosDashboard(tabName, userId);

    document.querySelectorAll('.tab').forEach(function (tab) {
        tab.addEventListener('click', function () {
            document.querySelectorAll('.tab').forEach(function (t) { t.classList.remove('active'); });
            tab.classList.add('active');
            cargarRetosDashboard(tab.dataset.tab, userId);
        });
    });
}

function cargarRetosDashboard(tab, userId) {
    var grid = document.getElementById('dashboardGrid');
    var estado = document.getElementById('dashboardEstado');

    if (!grid) return;
    grid.innerHTML = '';
    estado.textContent = 'Cargando retos...';

    var url;
    if (tab === 'favorites') {
        url = API_BASE + '/api/favorites?userId=' + userId;
    } else {
        url = API_BASE + '/api/challenges?creatorId=' + userId + '&status=' + tab + '&userId=' + userId;
    }

    fetch(url)
        .then(function (response) { return response.json(); })
        .then(function (data) {
            estado.textContent = '';
            if (!data.ok) {
                throw new Error(data.mensaje || 'Error al cargar retos');
            }

            if (data.total === 0) {
                if (tab === 'active') {
                    grid.innerHTML = '<div class="estado">No tienes retos activos. <a href="create-challenge.html" class="btn btn-primary" style="display:inline-block;margin-top:0.5rem">Crea tu primer reto</a></div>';
                } else if (tab === 'completed') {
                    grid.innerHTML = '<div class="estado">A\u00fan no tienes retos completados.</div>';
                } else {
                    grid.innerHTML = '<div class="estado">A\u00fan no tienes retos favoritos. \u00a1Explora retos y marca tus favoritos!</div>';
                }
                return;
            }

            data.resultados.forEach(function (challenge) {
                grid.appendChild(createChallengeCard(challenge));
            });
        })
        .catch(function (error) {
            estado.textContent = '';
            grid.innerHTML = '<div class="resultado error">' + error.message + '</div>';
        });
}
