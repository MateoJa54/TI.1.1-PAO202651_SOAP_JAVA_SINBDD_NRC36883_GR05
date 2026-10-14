<%@ page contentType='text/html;charset=UTF-8' language='java' %>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<!DOCTYPE html>
<html>
<head>
    <meta charset='UTF-8'>
    <title>Conversión - Conversor de Unidades Monster</title>
    <style>
        * { box-sizing: border-box; font-family: 'Segoe UI', sans-serif; margin: 0; padding: 0; }
        body { background: #ffffff; color: #1f2937; }
        .card { background: #ffffff; border: 1px solid #e3e7ee; border-radius: 14px; margin: 0; padding: 20px; }
        .form-group { margin-bottom: 18px; }
        label { display: block; margin-bottom: 6px; font-size: 13px; font-weight: 500; color: #6b7280; }
        select, input[type='text'] {
            width: 100%;
            height: 40px;
            border: 1px solid #d1d5db;
            border-radius: 6px;
            padding: 0 10px;
            font-size: 14px;
            font-family: 'Segoe UI', sans-serif;
        }
        select:focus, input:focus {
            outline: none;
            border-color: #2563eb;
        }
        .units-row {
            display: grid;
            grid-template-columns: 1fr auto 1fr;
            gap: 12px;
            align-items: end;
        }
        .swap-btn {
            height: 40px;
            width: 40px;
            border: 1px solid #d1d5db;
            border-radius: 6px;
            background: #ffffff;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 18px;
        }
        .swap-btn:hover {
            background: #f9fafb;
        }
        .btn-primary {
            width: 100%;
            height: 44px;
            background: #2563eb;
            color: white;
            border: none;
            border-radius: 6px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.2s;
        }
        .btn-primary:hover {
            background: #1d4ed8;
        }
        .result-card {
            background: #f4f6fa;
            border: 1px solid #e3e7ee;
            border-radius: 14px;
            padding: 20px;
            margin-top: 22px;
            text-align: center;
        }
        .result-success {
            background: #ecfdf5;
            border-color: #a7f3d0;
        }
        .result-error {
            background: #fef2f2;
            border-color: #fecaca;
        }
        .result-text {
            font-size: 16px;
            color: #6b7280;
        }
        .result-success .result-text {
            color: #059669;
            font-size: 18px;
            font-weight: 600;
        }
        .result-error .result-text {
            color: #dc2626;
        }
    </style>
</head>
<body>
    <div class='card'>
        <form action='convertir' method='post'>
            <div class='form-group'>
                <label>Categoría</label>
                <select name='categoria' id='categoria' onchange='loadUnits()'>
                </select>
            </div>
            
            <div class='form-group'>
                <div class='units-row'>
                    <div>
                        <label>Unidad de origen</label>
                        <select name='origen' id='origen'>
                        </select>
                    </div>
                    <button type='button' class='swap-btn' onclick='swapUnits()' title='Intercambiar unidades'>⇄</button>
                    <div>
                        <label>Unidad de destino</label>
                        <select name='destino' id='destino'>
                        </select>
                    </div>
                </div>
            </div>
            
            <div class='form-group'>
                <label>Valor</label>
                <input type='text' name='valor' id='valor' placeholder='Escriba un número, por ejemplo 12.5'>
            </div>
            
            <button type='submit' class='btn-primary'>Convertir</button>
        </form>
        
        <c:if test='${not empty resultado}'>
            <div class='result-card result-success'>
                <div class='result-text'>${resultado}</div>
            </div>
        </c:if>
        
        <c:if test='${not empty error}'>
            <div class='result-card result-error'>
                <div class='result-text'>${error}</div>
            </div>
        </c:if>
    </div>
    
    <script>
        function swapUnits() {
            const origen = document.getElementById('origen');
            const destino = document.getElementById('destino');
            const temp = origen.value;
            origen.value = destino.value;
            destino.value = temp;
        }
    </script>
</body>
</html>
