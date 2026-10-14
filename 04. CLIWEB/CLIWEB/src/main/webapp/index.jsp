<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <title>Conversor de Unidades Monster - Cliente Web</title>
    <style>
        body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 20px; background: linear-gradient(135deg, #f4f6fa 0%, #e8eef8 100%); min-height: 100vh; }
        .card { border: 1px solid #e3e7ee; padding: 30px; margin: 60px auto; border-radius: 12px; background: white; max-width: 500px; box-shadow: 0 8px 24px rgba(15,23,42,0.08); }
        h1 { text-align: center; color: #1f2937; margin-bottom: 30px; }
        input[type=text], input[type=password] { width: 100%; padding: 10px; margin-top: 8px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box; }
        input[type=submit] { background: #2563eb; color: white; padding: 12px 20px; border: none; border-radius: 6px; cursor: pointer; width: 100%; font-weight: 600; }
        input[type=submit]:hover { background: #1d4ed8; }
        .error { color: #b91c1c; text-align: center; }
    </style>
</head>
<body>
    <div class="card">
        <h1>Conversor de Unidades</h1>
        <form action="login" method="post">
            <label>Usuario</label><br>
            <input type="text" name="usuario" placeholder="Ingrese su usuario" required><br><br>
            <label>Contraseña</label><br>
            <input type="password" name="password" placeholder="Ingrese su contraseña" required><br><br>
            <input type="submit" value="Iniciar Sesión">
        </form>
        <c:if test="${not empty requestScope.error}">
            <p class="error"><c:out value="${requestScope.error}"/></p>
        </c:if>
    </div>
</body>
</html>
