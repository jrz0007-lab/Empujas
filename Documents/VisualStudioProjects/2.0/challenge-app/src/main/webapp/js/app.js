const API_BASE = window.location.origin;

function getUserId() {
    return sessionStorage.getItem('userId');
}

function getUsername() {
    return sessionStorage.getItem('username');
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
    return '€' + parseFloat(amount).toFixed(2);
}

function updateNavbar() {
    var navLinks = document.getElementById('navLinks');
    if (!navLinks) return;
    var userId = getUserId();
    if (userId) {
        navLinks.innerHTML = '<a href="dashboard.html" class="btn btn-outline">Panel</a><a href="#" class="btn btn-primary" id="logoutBtn">Cerrar Sesión</a>';
        document.getElementById('logoutBtn')?.addEventListener('click', function (e) {
            e.preventDefault();
            sessionStorage.clear();
            window.location.href = '/';
        });
    }
}

function createChallengeCard(challenge) {
    const progress = challenge.currentAmount / challenge.goalAmount * 100;
    const isCompleted = challenge.status === 'completed';
    const card = document.createElement('div');
    card.className = 'challenge-card' + (isCompleted ? ' completed' : '');
    card.dataset.id = challenge.id;

    card.innerHTML = `
        <h3>${challenge.title}</h3>
        <p class="challenge-creator">por ${challenge.creatorName} &middot; ${formatDate(challenge.createdAt)}</p>
        <p class="challenge-desc">${challenge.description}</p>
        <div class="challenge-progress">
            <div class="progress-bar">
                <div class="progress-fill" style="width: ${Math.min(100, progress)}%"></div>
            </div>
            <div class="progress-info">
                <span>${formatCurrency(challenge.currentAmount)} recaudados</span>
                <span>${formatCurrency(challenge.goalAmount)} meta</span>
            </div>
        </div>
        <div class="challenge-stats">
            <span>${challenge.supporterCount} apoyador(es)</span>
            <span class="challenge-status ${isCompleted ? 'status-completed' : 'status-active'}">${isCompleted ? 'Completado' : 'Activo'}</span>
        </div>
        <div class="challenge-actions">
            <button class="btn btn-outline view-detail">Ver Detalles</button>
            ${!isCompleted ? '<button class="btn btn-primary support-btn">Apoyar Reto</button>' : '<button class="btn btn-success view-completed">Ver Reto Completado</button>'}
        </div>
    `;

    card.querySelector('.view-detail').addEventListener('click', function () {
        openDetail(challenge.id);
    });

    const supportBtn = card.querySelector('.support-btn');
    if (supportBtn) {
        supportBtn.addEventListener('click', function () {
            openDonate(challenge.id, challenge.title);
        });
    }

    const viewCompleted = card.querySelector('.view-completed');
    if (viewCompleted) {
        viewCompleted.addEventListener('click', function () {
            openDetail(challenge.id);
        });
    }

    return card;
}

function openDetail(challengeId) {
    const modal = document.getElementById('detailModal');
    const content = document.getElementById('detailContent');
    const estado = document.getElementById('estado');

    if (estado) estado.textContent = 'Cargando detalles del reto...';
    content.innerHTML = '<p class="estado">Cargando...</p>';
    modal.classList.remove('hidden');

    fetch(API_BASE + '/api/challenge?id=' + challengeId)
        .then(function (response) {
            return response.json();
        })
        .then(function (data) {
            if (!data.ok) {
                throw new Error(data.mensaje || 'Error al cargar detalles');
            }

            const c = data.challenge;
            const donations = data.donations || [];
            const progress = c.currentAmount / c.goalAmount * 100;
            const isCompleted = c.status === 'completed';

            let html = '<div class="detail-layout">';
            html += '<div class="detail-info">';

            if (isCompleted) {
                html += '<div class="completed-badge">&#9989; Completado</div>';
            }

            html += '<h2>' + c.title + '</h2>';
            html += '<p class="detail-meta">por ' + c.creatorName + ' &middot; ' + formatDate(c.createdAt) + '</p>';
            html += '<p class="detail-desc">' + c.description + '</p>';

            if (isCompleted) {
                html += '<div class="completed-confirmation">&#9989; ¡Este reto se ha completado con éxito! Mira el video de prueba a continuación.</div>';
                if (c.videoUrl) {
                    html += '<div class="video-container"><iframe src="' + c.videoUrl + '" frameborder="0" allowfullscreen></iframe></div>';
                }
            }

            if (donations.length > 0) {
                html += '<h3>Últimos Apoyos</h3><ul>';
                donations.slice(0, 10).forEach(function (d) {
                    html += '<li><strong>' + d.donorName + '</strong> donó ' + formatCurrency(d.amount) + '</li>';
                });
                html += '</ul>';
            }

            html += '</div>';
            html += '<div class="detail-panel">';
            html += '<h3>Progreso de Financiación</h3>';
            html += '<div class="panel-progress">';
            html += '<div class="progress-bar"><div class="progress-fill" style="width: ' + Math.min(100, progress) + '%"></div></div>';
            html += '</div>';
            html += '<div class="panel-stat"><span>Cantidad Actual</span><strong>' + formatCurrency(c.currentAmount) + '</strong></div>';
            html += '<div class="panel-stat"><span>Meta</span><strong>' + formatCurrency(c.goalAmount) + '</strong></div>';
            html += '<div class="panel-stat"><span>Progreso</span><strong>' + Math.round(progress) + '%</strong></div>';
            html += '<div class="panel-stat"><span>Apoyadores</span><strong>' + c.supporterCount + '</strong></div>';
            html += '<div class="panel-stat"><span>Estado</span><strong class="' + (isCompleted ? 'status-completed' : 'status-active') + '">' + (isCompleted ? 'Completado' : 'Activo') + '</strong></div>';

            if (!isCompleted) {
                html += '<button class="btn btn-primary btn-full" id="detailSupportBtn">Apoyar este Reto</button>';
            }

            if (getUserId() && parseInt(c.creatorId) === parseInt(getUserId()) && !isCompleted && c.currentAmount >= c.goalAmount) {
                html += '<div class="goal-reached">¡Meta alcanzada! Ahora puedes subir tu video de prueba.</div>';
                html += '<button class="btn btn-success btn-full" id="detailCompleteBtn" style="margin-top:0.5rem">Marcar como Completado</button>';
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

            var completeBtn = document.getElementById('detailCompleteBtn');
            if (completeBtn) {
                completeBtn.addEventListener('click', function () {
                    modal.classList.add('hidden');
                    openComplete(c.id);
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
    document.querySelector('#donateContent h3').textContent = 'Apoyar: ' + title;
    document.getElementById('donateResultado').innerHTML = '';
    document.getElementById('donateForm').reset();
    modal.classList.remove('hidden');
}

function openComplete(challengeId) {
    const modal = document.getElementById('completeModal');
    document.getElementById('completeChallengeId').value = challengeId;
    document.getElementById('completeResultado').innerHTML = '';
    document.getElementById('completeForm').reset();
    modal.classList.remove('hidden');
}

function cargarRetos() {
    const grid = document.getElementById('challengesGrid');
    const estado = document.getElementById('estado');

    if (!grid) return;
    grid.innerHTML = '';
    if (estado) estado.textContent = 'Cargando retos...';

    fetch(API_BASE + '/api/challenges')
        .then(function (response) {
            return response.json();
        })
        .then(function (data) {
            if (estado) estado.textContent = '';
            if (!data.ok) {
                throw new Error(data.mensaje || 'Error al cargar retos');
            }

            if (data.total === 0) {
                grid.innerHTML = '<div class="estado">No hay retos disponibles aún. ¡Sé el primero en crear uno!</div>';
                return;
            }

            data.resultados.forEach(function (challenge) {
                grid.appendChild(createChallengeCard(challenge));
            });
        })
        .catch(function (error) {
            if (estado) estado.textContent = '';
            grid.innerHTML = '<div class="resultado error">' + error.message + '</div>';
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
                        window.location.href = '/dashboard.html';
                    } else {
                        showError('loginResultado', data.mensaje || 'Credenciales inválidas');
                    }
                })
                .catch(function (error) {
                    showError('loginResultado', 'Error de conexión: ' + error.message);
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
                showError('registerResultado', 'Las contraseñas no coinciden');
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
                        window.location.href = '/dashboard.html';
                    } else {
                        showError('registerResultado', data.mensaje || 'Error al registrarse');
                    }
                })
                .catch(function (error) {
                    showError('registerResultado', 'Error de conexión: ' + error.message);
                });
        });
    }

    var createForm = document.getElementById('createChallengeForm');
    if (createForm) {
        createForm.addEventListener('submit', function (e) {
            e.preventDefault();
            clearResultado('createResultado');

            if (!getUserId()) {
                showError('createResultado', 'Debes iniciar sesión para crear un reto');
                return;
            }

            var title = document.getElementById('challengeTitle').value.trim();
            var description = document.getElementById('challengeDesc').value.trim();
            var goalAmount = parseFloat(document.getElementById('challengeGoal').value);

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
                    creatorId: parseInt(getUserId())
                })
            })
                .then(function (response) { return response.json(); })
                .then(function (data) {
                    if (data.ok) {
                        showSuccess('createResultado', '¡Reto creado con éxito! Redirigiendo...');
                        setTimeout(function () {
                            window.location.href = '/dashboard.html';
                        }, 1500);
                    } else {
                        showError('createResultado', data.mensaje || 'Error al crear el reto');
                    }
                })
                .catch(function (error) {
                    showError('createResultado', 'Error de conexión: ' + error.message);
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

            fetch(API_BASE + '/api/donate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    challengeId: challengeId,
                    donorName: donorName,
                    amount: amount
                })
            })
                .then(function (response) { return response.json(); })
                .then(function (data) {
                    if (data.ok) {
                        showSuccess('donateResultado', '¡Gracias! Tu donación de ' + formatCurrency(amount) + ' ha sido procesada.');
                        setTimeout(function () {
                            document.getElementById('donateModal').classList.add('hidden');
                            cargarRetos();
                        }, 1500);
                    } else {
                        showError('donateResultado', data.mensaje || 'Error al procesar la donación');
                    }
                })
                .catch(function (error) {
                    showError('donateResultado', 'Error de conexión: ' + error.message);
                });
        });
    }

    var completeForm = document.getElementById('completeForm');
    if (completeForm) {
        completeForm.addEventListener('submit', function (e) {
            e.preventDefault();
            clearResultado('completeResultado');

            var challengeId = parseInt(document.getElementById('completeChallengeId').value);
            var videoUrl = document.getElementById('videoUrl').value.trim();

            if (!videoUrl) {
                showError('completeResultado', 'Por favor, proporciona una URL de video');
                return;
            }

            fetch(API_BASE + '/api/complete-challenge', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ challengeId: challengeId, videoUrl: videoUrl })
            })
                .then(function (response) { return response.json(); })
                .then(function (data) {
                    if (data.ok) {
                        showSuccess('completeResultado', '¡Reto marcado como completado!');
                        setTimeout(function () {
                            document.getElementById('completeModal').classList.add('hidden');
                            cargarRetos();
                        }, 1500);
                    } else {
                        showError('completeResultado', data.mensaje || 'Error al completar el reto');
                    }
                })
                .catch(function (error) {
                    showError('completeResultado', 'Error de conexión: ' + error.message);
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

    document.getElementById('closeComplete')?.addEventListener('click', function () {
        document.getElementById('completeModal').classList.add('hidden');
    });

    document.getElementById('cancelComplete')?.addEventListener('click', function () {
        document.getElementById('completeModal').classList.add('hidden');
    });

    document.getElementById('detailModal')?.addEventListener('click', function (e) {
        if (e.target === this) this.classList.add('hidden');
    });

    document.getElementById('donateModal')?.addEventListener('click', function (e) {
        if (e.target === this) this.classList.add('hidden');
    });

    document.getElementById('completeModal')?.addEventListener('click', function (e) {
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
            if (data.ok && data.resumen) {
                document.getElementById('totalCreated').textContent = data.resumen.totalCreated || 0;
                document.getElementById('totalCompleted').textContent = data.resumen.totalCompleted || 0;
                document.getElementById('totalRaised').textContent = formatCurrency(data.resumen.totalRaised || 0);
            }
        })
        .catch(function (error) {
            console.error('Error al cargar perfil:', error);
        });

    cargarRetosDashboard('active');

    document.querySelectorAll('.tab').forEach(function (tab) {
        tab.addEventListener('click', function () {
            document.querySelectorAll('.tab').forEach(function (t) { t.classList.remove('active'); });
            tab.classList.add('active');
            cargarRetosDashboard(tab.dataset.tab);
        });
    });
}

function cargarRetosDashboard(status) {
    var grid = document.getElementById('dashboardGrid');
    var estado = document.getElementById('dashboardEstado');
    var userId = getUserId();

    if (!grid) return;
    grid.innerHTML = '';
    estado.textContent = 'Cargando retos...';

    fetch(API_BASE + '/api/challenges?creatorId=' + userId + '&status=' + status)
        .then(function (response) { return response.json(); })
        .then(function (data) {
            estado.textContent = '';
            if (!data.ok) {
                throw new Error(data.mensaje || 'Error al cargar retos');
            }

            if (data.total === 0) {
                if (status === 'active') {
                    grid.innerHTML = '<div class="estado">No tienes retos activos. <a href="create-challenge.html" class="btn btn-primary" style="display:inline-block;margin-top:0.5rem">Crea tu primer reto</a></div>';
                } else {
                    grid.innerHTML = '<div class="estado">Aún no tienes retos completados.</div>';
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
