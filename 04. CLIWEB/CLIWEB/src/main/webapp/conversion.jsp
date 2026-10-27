<%@ page contentType='text/html;charset=UTF-8' language='java' %>
<form action='convertir' method='post' id='form-conversion' onsubmit='mostrarConvirtiendo()'>
    <div class='field'>
        <label for='categoria'>Categoría</label>
        <select name='categoria' id='categoria' class='select' onchange='loadUnits()'></select>
    </div>

    <div class='units-row'>
        <div class='field' style='margin-bottom: 0;'>
            <label for='origen'>De</label>
            <select name='origen' id='origen' class='select'></select>
        </div>
        <button type='button' class='btn btn-icon' onclick='swapUnits()' title='Intercambiar unidades' aria-label='Intercambiar unidades'>
            <svg width='18' height='18' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'>
                <polyline points='17 1 21 5 17 9'></polyline>
                <path d='M3 11V9a4 4 0 0 1 4-4h14'></path>
                <polyline points='7 23 3 19 7 15'></polyline>
                <path d='M21 13v2a4 4 0 0 1-4 4H3'></path>
            </svg>
        </button>
        <div class='field' style='margin-bottom: 0;'>
            <label for='destino'>A</label>
            <select name='destino' id='destino' class='select'></select>
        </div>
    </div>

    <div class='field' style='margin-top: 16px;'>
        <label for='valor'>Valor</label>
        <input type='text' name='valor' id='valor' class='input' placeholder='Escriba un número, por ejemplo 12.5'>
    </div>

    <div style='display: grid; grid-template-columns: 1fr auto; gap: 10px; align-items: center;'>
        <button type='submit' class='btn btn-primary btn-block' style='height: 44px;'>Convertir</button>
        <button type='button' class='btn' style='height: 44px; white-space: nowrap;' onclick='limpiarCampos()'>Limpiar</button>
    </div>
</form>

<div id='conversion-result' class='result result--vacio'>
    <div class='result-label'>RESULTADO</div>
    <div class='result-value result-value--vacio'>
        <svg width='18' height='18' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'>
            <polyline points='16 3 21 3 21 8'></polyline>
            <line x1='4' y1='20' x2='21' y2='3'></line>
            <polyline points='21 16 21 21 16 21'></polyline>
            <line x1='15' y1='15' x2='21' y2='21'></line>
            <line x1='4' y1='4' x2='9' y2='9'></line>
        </svg>
        <span>Ingresa un valor y pulsa Convertir</span>
    </div>
</div>

<script>
    function swapUnits() {
        const origen = document.getElementById('origen');
        const destino = document.getElementById('destino');
        if (!origen || !destino) return;
        const temp = origen.value;
        origen.value = destino.value;
        destino.value = temp;
    }

    function estadoVacio() {
        return '<div class="result-label">RESULTADO</div>'
            + '<div class="result-value result-value--vacio">'
            + '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">'
            + '<polyline points="16 3 21 3 21 8"></polyline>'
            + '<line x1="4" y1="20" x2="21" y2="3"></line>'
            + '<polyline points="21 16 21 21 16 21"></polyline>'
            + '<line x1="15" y1="15" x2="21" y2="21"></line>'
            + '<line x1="4" y1="4" x2="9" y2="9"></line>'
            + '</svg>'
            + '<span>Ingresa un valor y pulsa Convertir</span>'
            + '</div>';
    }

    function resetResultado() {
        const res = document.getElementById('conversion-result');
        if (!res) return;
        res.className = 'result result--vacio';
        res.innerHTML = estadoVacio();
    }

    function mostrarConvirtiendo() {
        const res = document.getElementById('conversion-result');
        if (!res) return;
        res.className = 'result result--cargando';
        res.innerHTML = '<div class="result-label">RESULTADO</div>'
            + '<div class="progress"></div>'
            + '<div class="result-cargando">Convirtiendo…</div>';
    }

    function limpiarCampos() {
        const valor = document.getElementById('valor');
        if (valor) {
            valor.value = '';
            valor.focus();
        }
        resetResultado();
    }
</script>
