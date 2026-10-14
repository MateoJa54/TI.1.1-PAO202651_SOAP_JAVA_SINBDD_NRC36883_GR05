# Servidor SOAP - Conversor de Unidades Monster (GR05)

Servicio web SOAP (JAX-WS) sin base de datos. Lo consumen los cuatro clientes:
consola, escritorio, web y móvil.

## Requisitos

- JDK 17 o superior (desarrollado con Eclipse Temurin 25)
- Payara Server 7 (Jakarta EE 11)
- Apache NetBeans (trae Maven incluido)

## Cómo ejecutarlo

1. Abrir en NetBeans la carpeta `WSCOMSERVIDOR` (File > Open Project).
2. Clic derecho en el proyecto > **Clean and Build** (corre las 226 pruebas).
3. Clic derecho > **Run** y elegir el servidor Payara.
4. Comprobar en el navegador: <http://localhost:8080/WSCOMSERVIDOR/>

| Dato | Valor |
|---|---|
| Endpoint | `http://localhost:8080/WSCOMSERVIDOR/WSConversorUnidades` |
| WSDL | `http://localhost:8080/WSCOMSERVIDOR/WSConversorUnidades?wsdl` |
| Namespace | `http://ws.monster.edu.ec/` |
| Usuario / contraseña de desarrollo | `monster` / `monster2026` |

La credencial se puede cambiar sin tocar el código: propiedades `-Dws.usuario` y
`-Dws.password`, o variables de entorno `WS_USUARIO` y `WS_PASSWORD`.

Para generar un cliente sin tener el servidor encendido, usar los archivos de
la carpeta `contrato/` (WSDL + XSD).

## Operaciones

| Operación | Parámetros | Respuesta |
|---|---|---|
| `autenticar` | usuario, password | token y hora de expiración (15 min) |
| `obtenerCategorias` | token | LONGITUD, MASA, TEMPERATURA |
| `obtenerUnidades` | token, categoria | unidades de la categoría |
| `convertir` | token, categoria, unidadOrigen, unidadDestino, valor | resultado |

Todas las respuestas traen `estado` (OK / ERROR), `codigo` y `mensaje`.
El servicio nunca responde con un SOAP Fault por un error de negocio.

| Código | Significado |
|---|---|
| `200` | Operación correcta |
| `AUTH-001` | Token inválido o ausente |
| `AUTH-002` | Sesión expirada |
| `AUTH-003` | Usuario o contraseña incorrectos |
| `UNIT-001` | Unidad de origen no soportada |
| `UNIT-002` | Unidad de destino no soportada |
| `DATA-001` | Datos incompletos o inconsistentes (incluye valor no numérico) |
| `SYS-001` | Error interno del servidor |

## Estructura (MVC)

```
ec.edu.monster
├── controladores.ws      Controlador: WSConversorUnidades (fachada SOAP)
├── seguridad             TokenServicio, AutenticacionServicio, FiltroCors
├── servicios             CatalogoServicio, ConversionServicio, FabricaRespuestas
│   └── conversores       IConversor, ConversorLineal, ConversorTemperatura (Strategy)
├── modelos               Solo datos: atributos, constructor, get y set
└── utilidades            Textos
```

La Vista del MVC son los clientes. Las pruebas JUnit están en
`src/test/java/ec/edu/monster/pruebas`.

## Cliente web (CORS)

Un cliente que corre en el navegador necesita permiso CORS. Por defecto se
permite cualquier puerto de `http://localhost` y `http://127.0.0.1`. Para otro
origen, editar `cors.origenes.permitidos` en `src/main/webapp/WEB-INF/web.xml`.
Los clientes de consola, escritorio y móvil no lo necesitan.
