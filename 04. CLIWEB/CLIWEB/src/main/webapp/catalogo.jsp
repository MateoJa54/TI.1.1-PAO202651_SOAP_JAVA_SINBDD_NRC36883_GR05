<%@ page contentType='text/html;charset=UTF-8' language='java' %>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<!DOCTYPE html>
<html>
<head>
    <meta charset='UTF-8'>
    <title>Catálogo - Conversor de Unidades Monster</title>
    <style>
        * { box-sizing: border-box; font-family: 'Segoe UI', sans-serif; margin: 0; padding: 0; }
        body { background: #ffffff; color: #1f2937; }
        .card { background: #ffffff; border: 1px solid #e3e7ee; border-radius: 14px; margin: 0; }
        table { width: 100%; border-collapse: collapse; }
        th { text-align: left; padding: 10px 16px; border-bottom: 1px solid #e3e7ee; color: #6b7280; font-weight: 500; }
        td { padding: 10px 16px; border-bottom: 1px solid #eef1f6; }
        tr:nth-child(even) td { background: #f8fafc; }
        .no-data { padding: 20px; color: #6b7280; text-align: center; }
        .header-row { background: #f8fafc; }
    </style>
</head>
<body>
    <div class='card'>
        <table>
            <thead>
                <tr class='header-row'>
                    <th>Categoría</th>
                    <th>Unidades</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var='entry' items='${datos}'>
                    <tr>
                        <td style='font-weight: 600;'>${entry.key}</td>
                        <td style='color: #6b7280;'>${entry.value}</td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
        <c:if test='${empty datos}'>
            <p class='no-data'>No hay categorías registradas en el servidor.</p>
        </c:if>
    </div>
</body>
</html>
