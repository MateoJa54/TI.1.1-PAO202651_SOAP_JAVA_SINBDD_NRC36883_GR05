# Cliente de escritorio - Conversor de Unidades Monster (GR05)

Aplicación de escritorio (Swing) que consume el servicio SOAP `WSCOMSERVIDOR`.
Es la Vista de escritorio del MVC: no tiene unidades ni fórmulas, todo el
catálogo y el cálculo los entrega el servidor.

## Requisitos

- JDK 17 o superior
- Apache NetBeans (trae Maven incluido)
- El servidor desplegado en Payara (ver `04. SERVIDOR/README.md`)

## Cómo ejecutarlo

1. Encender el servidor (proyecto `WSCOMSERVIDOR` > Run).
2. Abrir en NetBeans la carpeta `CLIESC` (File > Open Project).
3. Clic derecho en el proyecto > **Run** (o F6).
4. Usuario y contraseña de desarrollo: `monster` / `monster2026`.

Desde la línea de comandos:

```bash
cd "04. CLIESC/CLIESC"
mvn clean package
mvn exec:java
```

También se puede ejecutar el jar, que deja las dependencias en `target/lib`:

```bash
java -jar target/CLIESC.jar
```

**Clean and Build** corre las 253 pruebas; no necesitan el servidor encendido.

## Cambiar la dirección del servidor

Por defecto usa `http://localhost:8080/WSCOMSERVIDOR/WSConversorUnidades`.
Para apuntar a otro equipo, cualquiera de estas opciones (en orden de prioridad):

- primer argumento del programa
- propiedad `-Dws.url=http://192.168.1.50:8080/WSCOMSERVIDOR/WSConversorUnidades`
- variable de entorno `WS_URL`

Además, la URL configurada al arrancar es la que usa la aplicación.

En NetBeans: clic derecho en el proyecto > Properties > Run > Arguments o VM Options.

## Estructura (MVC)

```
ec.edu.monster
├── Main                 Arma el adaptador SOAP y abre la ventana
├── vista                VentanaPrincipal (mediador), PanelLogin, PanelMenu,
│                        PanelCatalogo, PanelConversion, TareaAsincrona
├── controlador          LoginControlador, CatalogoControlador, ConversionControlador
├── modelo               Solo datos: sesión, solicitud y respuestas del servicio
├── util                 NumeroUtil (leer y mostrar números), RespuestaUtil
└── ws                   Acceso al servicio SOAP (IClienteConversor, ClienteWSConversor)
```

Las llamadas al servidor se hacen fuera del hilo de Swing (`TareaAsincrona`),
para que la ventana no se congele; al terminar, el resultado se vuelve a
mostrar en el hilo de la interfaz.

El contrato del servicio está copiado en `src/main/resources/wsdl`. Si el
servidor cambia su contrato, hay que volver a copiar ahí los archivos de
`04. SERVIDOR/contrato`.
