# Cliente de consola - Conversor de Unidades Monster (GR05)

Programa de consola que consume el servicio SOAP `WSCOMSERVIDOR`.

## Requisitos

- JDK 17 o superior
- Apache NetBeans (trae Maven incluido)
- El servidor desplegado en Payara (ver `04. SERVIDOR/README.md`)

## Cómo ejecutarlo

1. Encender el servidor (proyecto `WSCOMSERVIDOR` > Run).
2. Abrir en NetBeans la carpeta `CLICON` (File > Open Project).
3. Clic derecho en el proyecto > **Run** (o F6). El programa se usa en la ventana Output.
4. Usuario y contraseña de desarrollo: `monster` / `monster2026`.

**Clean and Build** corre las 318 pruebas; no necesitan el servidor encendido.

## Cambiar la dirección del servidor

Por defecto usa `http://localhost:8080/WSCOMSERVIDOR/WSConversorUnidades`.
Para apuntar a otro equipo, cualquiera de estas opciones (en orden de prioridad):

- primer argumento del programa
- propiedad `-Dws.url=http://192.168.1.50:8080/WSCOMSERVIDOR/WSConversorUnidades`
- variable de entorno `WS_URL`

En NetBeans: clic derecho en el proyecto > Properties > Run > Arguments o VM Options.

## Estructura (MVC)

```
ec.edu.monster
├── Main            Arma los objetos y arranca el programa
├── vista           LoginVista, MenuVista, CatalogoVista, ConversionVista
├── controlador     LoginControlador, CatalogoControlador, ConversionControlador
├── modelo          Solo datos: sesión, solicitud y respuestas del servicio
├── ws              Acceso al servicio SOAP (IClienteConversor, ClienteWSConversor)
└── util            EntradaUtil (lectura por teclado), RespuestaUtil
```

El contrato del servicio está copiado en `src/main/resources/wsdl`. Si el
servidor cambia su contrato, hay que volver a copiar ahí los archivos de
`04. SERVIDOR/contrato`.

El cliente no tiene unidades ni fórmulas en su código: las categorías y las
unidades se piden al servidor, y el cálculo lo hace el servidor.
