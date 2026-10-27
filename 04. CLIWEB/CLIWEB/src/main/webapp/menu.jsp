<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Conversor de Unidades Monster - Cliente Web</title>
    <link rel="stylesheet" href="css/app.css">
</head>
<body>
    <header class="topbar">
        <div class="topbar-brand">
            <div class="brand-tile">
                <img src="img/marca.svg" alt="Monster" width="38" height="38">
            </div>
            <div>
                <div class="brand-title">Conversor de Unidades</div>
                <div class="brand-sub">MONSTER · Cliente web</div>
            </div>
        </div>
        <div class="topbar-tools">
            <c:if test="${not empty sessionScope.usuario}">
                <span class="chip">
                    <span class="dot"></span>
                    Conectado como <strong><c:out value="${sessionScope.usuario}"/></strong>
                </span>
            </c:if>
            <form action="logout" method="post" style="display: inline;">
                <button type="submit" class="btn btn-ghost-light">Cerrar sesión</button>
            </form>
        </div>
    </header>

    <nav class="tabs">
        <button type="button" class="tab active" id="tab-catalogo" onclick="showTab('catalogo')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <line x1="8" y1="6" x2="21" y2="6"></line>
                <line x1="8" y1="12" x2="21" y2="12"></line>
                <line x1="8" y1="18" x2="21" y2="18"></line>
                <circle cx="4" cy="6" r="1"></circle>
                <circle cx="4" cy="12" r="1"></circle>
                <circle cx="4" cy="18" r="1"></circle>
            </svg>
            Catálogo
        </button>
        <button type="button" class="tab" id="tab-conversion" onclick="showTab('conversion')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <polyline points="16 3 21 3 21 8"></polyline>
                <line x1="4" y1="20" x2="21" y2="3"></line>
                <polyline points="21 16 21 21 16 21"></polyline>
                <line x1="15" y1="15" x2="21" y2="21"></line>
                <line x1="4" y1="4" x2="9" y2="9"></line>
            </svg>
            Conversión
        </button>
    </nav>
    
    <main id="catalogo" class="content active">
        <section class="panel">
            <div class="panel-head">
                <div>
                    <div class="panel-title">Catálogo de unidades</div>
                    <div class="panel-sub">Categorías y unidades disponibles en el servidor</div>
                </div>
                <div class="panel-head-actions">
                    <button type="button" class="btn" onclick="loadCatalogo()">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                            <polyline points="23 4 23 10 17 10"></polyline>
                            <polyline points="1 20 1 14 7 14"></polyline>
                            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
                        </svg>
                        Actualizar
                    </button>
                </div>
            </div>
            <div id="catalogo-content">
                <div class="empty">
                    <div class="progress"></div>
                    <div>Cargando catálogo...</div>
                </div>
            </div>
        </section>
    </main>

    <main id="conversion" class="content">
        <section class="panel">
            <div class="panel-head">
                <div>
                    <div class="panel-title">Conversión de unidades</div>
                    <div class="panel-sub">Elija la categoría, las unidades y escriba el valor a convertir</div>
                </div>
            </div>
            <div id="conversion-content">
                <jsp:include page="conversion.jsp" />
            </div>
        </section>
    </main>
    
    <script>
        window.categoriasData = {};
        let catalogoPromise = null;
        let categoriasPromise = null;
        
        function loadUnits() {
            const categoria = document.getElementById('categoria').value;
            const origen = document.getElementById('origen');
            const destino = document.getElementById('destino');
            
            // Limpiar opciones
            origen.innerHTML = '';
            destino.innerHTML = '';
            
            if (categoria && window.categoriasData && window.categoriasData[categoria]) {
                window.categoriasData[categoria].forEach(unit => {
                    const optionOrigen = document.createElement('option');
                    optionOrigen.value = unit;
                    optionOrigen.textContent = unit;
                    origen.appendChild(optionOrigen);
                    
                    const optionDestino = document.createElement('option');
                    optionDestino.value = unit;
                    optionDestino.textContent = unit;
                    destino.appendChild(optionDestino);
                });
                
                // Seleccionar la primera unidad en origen y la segunda en destino
                if (origen.options.length > 0) {
                    origen.selectedIndex = 0;
                }
                if (destino.options.length > 1) {
                    destino.selectedIndex = 1;
                }
            }
        }
        
        function showTab(tabId) {
            document.querySelectorAll('.tab').forEach(btn => btn.classList.remove('active'));
            document.querySelectorAll('.content').forEach(content => content.classList.remove('active'));

            if (event && event.target) {
                event.target.classList.add('active');
            } else {
                const btn = document.getElementById('tab-' + tabId);
                if (btn) btn.classList.add('active');
            }
            document.getElementById(tabId).classList.add('active');

            if (tabId === 'catalogo') {
                loadCatalogo();
            } else if (tabId === 'conversion') {
                loadCategoriasForConversion();
            }
        }
        
        function obtenerCatalogo() {
            if (!catalogoPromise) {
                const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
                catalogoPromise = fetch(contextPath + '/catalogo', { credentials: 'include' })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Error al cargar catálogo: ' + response.status);
                        }
                        return response.text();
                    })
                    .catch(error => {
                        catalogoPromise = null;
                        throw error;
                    });
            }
            return catalogoPromise;
        }

        function loadCatalogo() {
            const content = document.getElementById('catalogo-content');
            obtenerCatalogo()
                .then(html => {
                    content.innerHTML = html;
                })
                .catch(error => {
                    console.error('Error al cargar el catálogo:', error);
                    content.innerHTML = '<div class="empty"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg><div>Error al cargar el catálogo</div></div>';
                });
        }
        
        // Cargar categorías para el conversor
        function loadCategoriasForConversion() {
            if (categoriasPromise) {
                return categoriasPromise;
            }

            categoriasPromise = obtenerCatalogo()
                .then(html => {
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(html, 'text/html');
                    const rows = doc.querySelectorAll('tbody tr');
                    
                    window.categoriasData = {};
                    rows.forEach(row => {
                        const cells = row.querySelectorAll('td');
                        if (cells.length >= 2) {
                            const catName = cells[0].textContent.trim();
                            const unitsText = cells[1].textContent.trim();
                            const units = unitsText.split(',').map(u => u.trim());
                            window.categoriasData[catName] = units;
                        }
                    });
                    
                    // Poblar el select de categoría
                    const categoriaSelect = document.getElementById('categoria');
                    if (categoriaSelect) {
                        categoriaSelect.innerHTML = '';
                        Object.keys(window.categoriasData).forEach(cat => {
                            const option = document.createElement('option');
                            option.value = cat;
                            option.textContent = cat;
                            categoriaSelect.appendChild(option);
                        });
                        
                        // Seleccionar automáticamente la primera categoría y cargar sus unidades
                        if (categoriaSelect.options.length > 0) {
                            categoriaSelect.selectedIndex = 0;
                            // Disparar el evento onchange manualmente
                            const event = new Event('change', { bubbles: true });
                            categoriaSelect.dispatchEvent(event);
                        }
                    }
                })
                .catch(error => {
                    categoriasPromise = null;
                    console.error('Error al cargar categorías:', error);
                });

            return categoriasPromise;
        }

        function copiarResultado(btn) {
            const card = btn.closest('.result');
            const valueEl = card ? card.querySelector('.result-value') : null;
            const texto = valueEl ? valueEl.textContent.trim() : '';
            if (!texto) return;
            const confirmar = () => {
                const original = btn.innerHTML;
                btn.innerHTML = '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>';
                setTimeout(() => { btn.innerHTML = original; }, 1200);
            };
            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(texto).then(confirmar).catch(() => {});
            } else {
                const ta = document.createElement('textarea');
                ta.value = texto;
                ta.style.position = 'fixed';
                ta.style.opacity = '0';
                document.body.appendChild(ta);
                ta.select();
                try { document.execCommand('copy'); confirmar(); } catch (e) {}
                document.body.removeChild(ta);
            }
        }
        
            // Cargar catálogo al inicio
            document.addEventListener('DOMContentLoaded', function() {
                <c:choose>
                    <c:when test='${not empty sessionScope.active_tab and sessionScope.active_tab eq "conversion"}'>
                        // defer
                    </c:when>
                    <c:otherwise>
                        loadCatalogo();
                    </c:otherwise>
                </c:choose>

                // Cambiar a la pestaña activa si existe
                <c:if test='${not empty sessionScope.active_tab}'>
                    const activeTab = '${sessionScope.active_tab}';
                    document.querySelectorAll('.tab').forEach(btn => btn.classList.remove('active'));
                    document.querySelectorAll('.content').forEach(content => content.classList.remove('active'));
                    if (activeTab === 'conversion') {
                        document.getElementById('conversion').classList.add('active');
                        const btnConv = document.getElementById('tab-conversion');
                        if (btnConv) btnConv.classList.add('active');
                        loadCategoriasForConversion();
                    } else {
                        document.getElementById('catalogo').classList.add('active');
                        const btnCat = document.getElementById('tab-catalogo');
                        if (btnCat) btnCat.classList.add('active');
                        loadCatalogo();
                    }
                    <c:remove var="active_tab" scope="session" />
                </c:if>
            
            // Restaurar valores seleccionados si existen
            <c:if test='${not empty sessionScope.conversion_categoria}'>
                setTimeout(() => {
                    const categoriaSelect = document.getElementById('categoria');
                    if (categoriaSelect) {
                        categoriaSelect.value = '${sessionScope.conversion_categoria}';
                        loadUnits();
                        
                        setTimeout(() => {
                            const origenSelect = document.getElementById('origen');
                            const destinoSelect = document.getElementById('destino');
                            const valorInput = document.getElementById('valor');
                            
                            if (origenSelect) origenSelect.value = '${sessionScope.conversion_origen}';
                            if (destinoSelect) destinoSelect.value = '${sessionScope.conversion_destino}';
                            if (valorInput) valorInput.value = '${sessionScope.conversion_valor}';
                        }, 100);
                    }
                }, 100);
                
                <c:remove var="conversion_categoria" scope="session" />
                <c:remove var="conversion_origen" scope="session" />
                <c:remove var="conversion_destino" scope="session" />
                <c:remove var="conversion_valor" scope="session" />
            </c:if>
            
            // Mostrar resultado de conversión si existe
            <c:if test='${not empty sessionScope.conversion_resultado}'>
                const resultDivOk = document.getElementById('conversion-result');
                if (resultDivOk) {
                    resultDivOk.className = 'result result--ok';
                    resultDivOk.innerHTML = '<div class="result-top">'
                        + '<div class="result-label">RESULTADO</div>'
                        + '<button type="button" class="btn btn-copy" title="Copiar resultado" aria-label="Copiar resultado" onclick="copiarResultado(this)">'
                        + '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">'
                        + '<rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>'
                        + '<path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>'
                        + '</svg>'
                        + '</button>'
                        + '</div>'
                        + '<div class="result-value"><c:out value="${sessionScope.conversion_resultado}"/></div>'
                        + '<div class="result-eq"><c:out value="${sessionScope.conversion_equiv}"/></div>';
                }
                <c:remove var="conversion_resultado" scope="session" />
                <c:remove var="conversion_equiv" scope="session" />
            </c:if>

            // Mostrar error de conversión si existe
            <c:if test='${not empty sessionScope.conversion_error}'>
                const resultDivErr = document.getElementById('conversion-result');
                if (resultDivErr) {
                    resultDivErr.className = 'result result--error';
                    resultDivErr.innerHTML = '<div class="result-label">AVISO</div>'
                        + '<div class="result-value"><c:out value="${sessionScope.conversion_error}"/></div>';
                }
                <c:remove var="conversion_error" scope="session" />
            </c:if>
        });
    </script>
</body>
</html>
