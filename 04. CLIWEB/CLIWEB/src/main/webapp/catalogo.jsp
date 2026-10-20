<%@ page contentType='text/html;charset=UTF-8' language='java' %>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<!DOCTYPE html>
<html>
<head>
    <meta charset='UTF-8'>
    <title>Catálogo - Conversor de Unidades Monster</title>
    <link rel='stylesheet' href='css/app.css'>
</head>
<body style='background: transparent;'>
    <div class='panel' style='box-shadow: none; border: 0; padding: 0;'>
        <table class='table'>
            <thead>
                <tr>
                    <th>Categoría</th>
                    <th>Unidades</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var='entry' items='${datos}'>
                    <tr>
                        <td class='cat'><c:out value='${entry.key}'/></td>
                        <td><c:out value='${entry.value}'/></td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
        <c:if test='${empty datos}'>
            <div class='empty'>
                <svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'>
                    <path d='M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z'></path>
                    <line x1='12' y1='9' x2='12' y2='13'></line>
                    <line x1='12' y1='17' x2='12.01' y2='17'></line>
                </svg>
                <div>No hay categorías registradas en el servidor.</div>
            </div>
        </c:if>
    </div>
</body>
</html>
