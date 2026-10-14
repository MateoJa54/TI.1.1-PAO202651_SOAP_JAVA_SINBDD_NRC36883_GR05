<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Conversor de Unidades Monster - Cliente Web</title>
    <style>
        * { box-sizing: border-box; font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 0; }
        body { background: #f4f6fa; color: #1f2937; min-height: 100vh; }
        
        .app-bar {
            background: #ffffff;
            border-bottom: 1px solid #e3e7ee;
            padding: 12px 22px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .brand {
            display: flex;
            align-items: center;
            gap: 12px;
        }
        
        .brand-text {
            display: flex;
            flex-direction: column;
        }
        
        .brand-title {
            font-size: 18px;
            font-weight: 600;
            color: #1f2937;
        }
        
        .brand-subtitle {
            font-size: 12px;
            color: #6b7280;
        }
        
        .actions {
            display: flex;
            gap: 8px;
        }
        
        .btn {
            padding: 8px 16px;
            border: 1px solid #d1d5db;
            border-radius: 6px;
            background: #ffffff;
            color: #6b7280;
            cursor: pointer;
            font-size: 14px;
            transition: all 0.2s;
        }
        
        .btn:hover {
            background: #f9fafb;
            color: #1f2937;
        }
        
        .btn-primary {
            background: #2563eb;
            color: white;
            border: none;
        }
        
        .btn-primary:hover {
            background: #1d4ed8;
        }
        
        .tabs {
            background: #ffffff;
            border-bottom: 1px solid #e3e7ee;
            padding: 0 22px;
        }
        
        .tab-buttons {
            display: flex;
            gap: 0;
        }
        
        .tab-btn {
            padding: 12px 16px;
            background: none;
            border: none;
            border-bottom: 2px solid transparent;
            color: #6b7280;
            cursor: pointer;
            font-size: 14px;
            font-weight: 500;
            transition: all 0.2s;
        }
        
        .tab-btn:hover {
            color: #1f2937;
            background: #f9fafb;
        }
        
        .tab-btn.active {
            color: #2563eb;
            border-bottom-color: #2563eb;
        }
        
        .tab-content {
            padding: 20px 22px;
            display: none;
        }
        
        .tab-content.active {
            display: block;
        }
        
        .card {
            background: #ffffff;
            border: 1px solid #e3e7ee;
            border-radius: 14px;
            padding: 20px;
        }
        
        .card-header {
            margin-bottom: 20px;
        }
        
        .card-title {
            font-size: 20px;
            font-weight: 600;
            color: #1f2937;
            margin-bottom: 4px;
        }
        
        .card-subtitle {
            font-size: 13px;
            color: #6b7280;
        }
    </style>
</head>
<body>
    <div class="app-bar">
        <div class="brand">
            <div class="brand-text">
                <div class="brand-title">Conversor de Unidades</div>
                <div class="brand-subtitle">Cliente web</div>
            </div>
        </div>
        <div class="actions">
            <form action="logout" method="post" style="display: inline;">
                <button type="submit" class="btn">Cerrar sesión</button>
            </form>
        </div>
    </div>
    
    <div class="tabs">
        <div class="tab-buttons">
            <button class="tab-btn active" onclick="showTab('catalogo')">Catálogo</button>
            <button class="tab-btn" onclick="showTab('conversion')">Conversión</button>
        </div>
    </div>
    
    <div id="catalogo" class="tab-content active">
        <div class="card">
            <div class="card-header">
                <div class="card-title">Catálogo de unidades</div>
                <div class="card-subtitle">Categorías y unidades disponibles en el servidor</div>
            </div>
            <div id="catalogo-content">
                <p style="color: #6b7280; text-align: center; padding: 40px;">Cargando catálogo...</p>
            </div>
        </div>
    </div>
    
    <div id="conversion" class="tab-content">
        <div class="card">
            <div class="card-header">
                <div class="card-title">Conversión de unidades</div>
                <div class="card-subtitle">Elija la categoría, las unidades y escriba el valor a convertir</div>
            </div>
            <div id="conversion-content">
                <jsp:include page="conversion.jsp" />
            </div>
            <div id="conversion-result" style="display: none;"></div>
        </div>
    </div>
    
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
            document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
            
            event.target.classList.add('active');
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
                    content.innerHTML = '<p style="color: #dc2626; text-align: center; padding: 40px;">Error al cargar el catálogo</p>';
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
        
        // Cargar catálogo al inicio
        document.addEventListener('DOMContentLoaded', function() {
            loadCatalogo();
            
            // Cambiar a la pestaña activa si existe
            <c:if test='${not empty sessionScope.active_tab}'>
                const activeTab = '${sessionScope.active_tab}';
                const tabButtons = document.querySelectorAll('.tab-btn');
                const tabContents = document.querySelectorAll('.tab-content');
                
                tabButtons.forEach(btn => btn.classList.remove('active'));
                tabContents.forEach(content => content.classList.remove('active'));
                
                if (activeTab === 'conversion') {
                    document.getElementById('conversion').classList.add('active');
                    tabButtons[1].classList.add('active');
                    loadCategoriasForConversion();
                } else {
                    document.getElementById('catalogo').classList.add('active');
                    tabButtons[0].classList.add('active');
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
                const resultDiv = document.getElementById('conversion-result');
                resultDiv.style.display = 'block';
                resultDiv.innerHTML = '<div style="background: #ecfdf5; border: 1px solid #a7f3d0; border-radius: 14px; padding: 20px; margin-top: 22px; text-align: center;"><div style="color: #059669; font-size: 18px; font-weight: 600;">${sessionScope.conversion_resultado}</div></div>';
                <c:remove var="conversion_resultado" scope="session" />
            </c:if>
            
            // Mostrar error de conversión si existe
            <c:if test='${not empty sessionScope.conversion_error}'>
                const resultDiv = document.getElementById('conversion-result');
                resultDiv.style.display = 'block';
                resultDiv.innerHTML = '<div style="background: #fef2f2; border: 1px solid #fecaca; border-radius: 14px; padding: 20px; margin-top: 22px; text-align: center;"><div style="color: #dc2626; font-size: 16px;">${sessionScope.conversion_error}</div></div>';
                <c:remove var="conversion_error" scope="session" />
            </c:if>
        });
    </script>
</body>
</html>
