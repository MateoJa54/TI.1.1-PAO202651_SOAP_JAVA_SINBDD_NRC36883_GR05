# Cliente web - Conversor de Unidades Monster (GR05)

Aplicación web (Jakarta EE + Servlets + JSP) con diseño moderno y minimalista, consume el servicio SOAP WSCOMSERVIDOR. 
El diseño sigue el mismo patrón que el cliente de escritorio: una barra superior con la marca y acciones de sesión, 
y dos pestañas (Catálogo y Conversión) para acceder a las funcionalidades principales.

## Requisitos
- JDK 17+
- Maven 3.9+
- Servidor Jakarta EE 10 (Payara, Tomcat, GlassFish)
- El servidor desplegado en Payara (ver `04. SERVIDOR/README.md`)

## Cómo ejecutarlo

1. Encender el servidor SOAP (proyecto `WSCOMSERVIDOR` > Run).
2. Desplegar este proyecto en el servidor Jakarta EE:
   ```bash
   cd "04. CLIWEB/CLIWEB"
   mvn clean package
   ```
3. Copiar el WAR generado (`target/CLIWEB.war`) al directorio de despliegue del servidor.
4. Acceder a `http://localhost:8080/CLIWEB/` en el navegador.
5. Usuario y contraseña de desarrollo: `monster` / `monster2026`.

## Build y tests
```bash
cd "04. CLIWEB/CLIWEB"
mvn test
mvn package
```

## Tests
- Total: 237 tests (JUnit 5) - Modelo, utilidades, controladores, adaptador SOAP, casos borde.

## Estructura (MVC)

```
ec.edu.monster
├── servlet          LoginServlet, LogoutServlet, CatalogoServlet, ConvertirServlet
├── controlador      LoginControlador, CatalogoControlador, ConversionControlador
├── modelo           Solo datos: sesión, solicitud y respuestas del servicio
├── util             NumeroUtil (leer y mostrar números), RespuestaUtil
└── ws               Acceso al servicio SOAP (IClienteConversor, ClienteWSConversor)
```

## Páginas JSP
- `index.jsp` - Pantalla de login (similar a PanelLogin del cliente de escritorio)
- `menu.jsp` - Pantalla principal con pestañas (similar a PanelMenu del cliente de escritorio)
  - Pestaña Catálogo: muestra las categorías y unidades disponibles
  - Pestaña Conversión: formulario para realizar conversiones de unidades
- `catalogo.jsp` - Vista del catálogo (incluida en menu.jsp)
- `conversion.jsp` - Vista de conversión (incluida en menu.jsp)

## Diseño
El diseño del cliente web sigue el mismo estilo visual que el cliente de escritorio:
- Colores: fondo claro (#f4f6fa), superficies blancas, texto principal (#1f2937), secundario (#6b7280)
- Barra superior con marca y botón de cerrar sesión
- Pestañas para navegar entre Catálogo y Conversión
- Tarjetas con bordes redondeados (14px)
- Botones con estados hover y transiciones suaves
