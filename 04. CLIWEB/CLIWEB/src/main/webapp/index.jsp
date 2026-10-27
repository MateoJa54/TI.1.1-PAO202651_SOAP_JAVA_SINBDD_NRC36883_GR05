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
    <main class="login">
        <aside class="login-brand">
            <h1 class="brand-name">Conversor de Unidades</h1>
            <div class="brand-figure">
                <img src="img/Monster.png" alt="Monster">
            </div>
        </aside>

        <section class="login-main">
            <div class="login-col">
                <div class="login-inline-logo">
                    <img src="img/Monster.png" alt="Monster">
                    <div class="brand-name">Conversor de Unidades</div>
                </div>

                <div class="card">
                    <div class="card-head">
                        <div class="card-title">Iniciar sesión</div>
                        <div class="card-sub">Use sus credenciales para acceder al cliente web</div>
                    </div>

                    <form action="login" method="post">
                        <div class="field">
                            <label for="usuario">Usuario</label>
                            <input type="text" id="usuario" name="usuario" class="input" placeholder="Ingrese su usuario" required autocomplete="username">
                        </div>

                        <div class="field">
                            <label for="password">Contraseña</label>
                            <input type="password" id="password" name="password" class="input" placeholder="Ingrese su contraseña" required autocomplete="current-password">
                        </div>

                        <button type="submit" class="btn btn-primary btn-block">Iniciar sesión</button>
                    </form>

                    <c:if test="${not empty requestScope.error}">
                        <div class="alert alert-error">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                                <line x1="12" y1="9" x2="12" y2="13"></line>
                                <line x1="12" y1="17" x2="12.01" y2="17"></line>
                            </svg>
                            <div><c:out value="${requestScope.error}"/></div>
                        </div>
                    </c:if>
                </div>

                <div class="server-box">
                    <div class="k">Servidor SOAP</div>
                    <div class="v">http://localhost:8080/WSCOMSERVIDOR/WSConversorUnidades</div>
                </div>
            </div>
        </section>
    </main>
</body>
</html>
