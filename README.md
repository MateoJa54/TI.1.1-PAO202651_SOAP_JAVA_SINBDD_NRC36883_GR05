# Conversor de Unidades Monster — SOA con servicios web SOAP en Java (sin base de datos)

**TI.1.1 · PAO 202651 · NRC 36883 · Grupo 05**

Sistema distribuido de conversión de unidades de longitud, masa y temperatura. La lógica de
negocio reside en un servicio web SOAP (JAX-WS) desplegado en Payara Server, que es consumido
por cuatro clientes independientes: consola, escritorio, web y móvil. El sistema no utiliza
base de datos: el catálogo de unidades, la credencial y los tokens de sesión se mantienen en
memoria.

## Integrantes

- García Romero Eduardo
- Iza Barrionuevo Juan Mateo
- Jácome Noroña Brayan

## Contenido

1. [Arquitectura](#arquitectura)
2. [Estructura del repositorio](#estructura-del-repositorio)
3. [Requisitos](#requisitos)
4. [Compilación y ejecución del servidor](#compilación-y-ejecución-del-servidor)
5. [Compilación y ejecución del cliente de consola](#compilación-y-ejecución-del-cliente-de-consola)
6. [Contrato del servicio](#contrato-del-servicio)
7. [Guía para desarrollar los demás clientes](#guía-para-desarrollar-los-demás-clientes)
8. [Pruebas](#pruebas)
9. [Modelos UML](#modelos-uml)

## Arquitectura

El sistema aplica una arquitectura orientada a servicios (SOA) organizada con el patrón
arquitectónico MVC distribuido:

| Capa | Ubicación | Responsabilidad |
|---|---|---|
| Vista | Clientes (`04. CLICON`, `04. CLIESC`, `04. CLIWEB`, `04. CLIMOV`) | Interacción con el usuario |
| Controlador | Servidor, paquete `ec.edu.monster.controladores.ws` | Punto de entrada SOAP; valida el token y delega |
| Modelo | Servidor, paquetes `servicios`, `seguridad` y `modelos` | Reglas de negocio y datos |

Todos los proyectos Java utilizan el paquete base `ec.edu.monster`. Las clases de los paquetes
`modelos` (servidor) y `modelo` (cliente) contienen únicamente atributos, constructores y
métodos de acceso; la lógica se ubica en los servicios.

Patrones de diseño aplicados en el servidor: *Facade* (`WSConversorUnidades`), *Strategy*
(`IConversor`, `ConversorLineal`, `ConversorTemperatura`), *Factory* (`FabricaRespuestas`),
*Parameter Object* (`SolicitudConversion`) e inyección de dependencias por constructor.

## Estructura del repositorio

```
├── 01. UML/                    Requisitos y modelos
│   ├── 01. ERS/                Especificación de requisitos de software
│   ├── 02. ECUD/               Especificación de casos de uso
│   └── 03. DIAGRAMAS/          Modelos PowerDesigner (casos de uso, actividad,
│                               secuencia, clases y arquitectura)
├── 02. MER/                    No aplica (sistema sin base de datos)
├── 03. BDD/                    No aplica (sistema sin base de datos)
├── 04. SERVIDOR/
│   ├── WSCOMSERVIDOR/          Proyecto Maven (war) del servicio SOAP
│   └── contrato/               WSDL y XSD del servicio
├── 04. CLICON/CLICON/          Cliente de consola (Maven, jar)
├── 04. CLIESC/                 Cliente de escritorio
├── 04. CLIWEB/                 Cliente web
├── 04. CLIMOV/                 Cliente móvil
├── 05. DOCUMENTACION/img/      Diagramas exportados en PNG
└── SOAP_JAVA_SINBDD_GR05.sws   Espacio de trabajo de PowerDesigner
```

## Requisitos

| Componente | Versión | Uso |
|---|---|---|
| JDK | 17 o superior (desarrollado con Eclipse Temurin 25) | Compilación y ejecución |
| Apache Maven | 3.9 o superior (NetBeans incluye uno) | Construcción |
| Payara Server | 7 (Jakarta EE 11); verificado con 7.2026.9 | Despliegue del servidor |
| Apache NetBeans | Versión con soporte para Jakarta EE 11 | IDE recomendado |
| SAP PowerDesigner | 16.5 | Solo para abrir o editar los modelos UML |

Ambos proyectos se compilan con `maven.compiler.release=17`, por lo que el resultado es
independiente del JDK instalado, siempre que sea 17 o posterior. No se requiere ninguna
configuración local: el repositorio no contiene rutas absolutas.

## Compilación y ejecución del servidor

### Desde NetBeans

1. *File > Open Project* y seleccionar `04. SERVIDOR/WSCOMSERVIDOR`.
2. Registrar Payara Server en *Tools > Servers*, si aún no está registrado.
3. Clic derecho sobre el proyecto > *Clean and Build*. Se ejecutan las pruebas unitarias y se
   genera `target/WSCOMSERVIDOR.war`.
4. Clic derecho > *Run*, seleccionando Payara Server.

### Desde la línea de comandos

```bash
cd "04. SERVIDOR/WSCOMSERVIDOR"
mvn clean package
asadmin start-domain domain1
asadmin deploy --force=true target/WSCOMSERVIDOR.war
```

### Verificación

| Recurso | Dirección |
|---|---|
| Página de comprobación | `http://localhost:8080/WSCOMSERVIDOR/` |
| Servicio (endpoint) | `http://localhost:8080/WSCOMSERVIDOR/WSConversorUnidades` |
| WSDL | `http://localhost:8080/WSCOMSERVIDOR/WSConversorUnidades?wsdl` |

### Configuración

| Parámetro | Valor por defecto | Cómo cambiarlo |
|---|---|---|
| Usuario | `monster` | Propiedad `-Dws.usuario` o variable de entorno `WS_USUARIO` |
| Contraseña | `monster2026` | Propiedad `-Dws.password` o variable de entorno `WS_PASSWORD` |
| Orígenes CORS | `http://localhost:*`, `http://127.0.0.1:*` | Parámetro `cors.origenes.permitidos` en `WEB-INF/web.xml` |

La credencial por defecto es exclusivamente para desarrollo.

## Compilación y ejecución del cliente de consola

El servidor debe estar desplegado antes de ejecutar el cliente.

### Desde NetBeans

1. *File > Open Project* y seleccionar `04. CLICON/CLICON`.
2. Clic derecho sobre el proyecto > *Clean and Build*.
3. Clic derecho > *Run* (F6). La interacción se realiza en la ventana *Output*.

### Desde la línea de comandos

```bash
cd "04. CLICON/CLICON"
mvn clean package
mvn exec:java
```

Por defecto el cliente se conecta a `http://localhost:8080/WSCOMSERVIDOR/WSConversorUnidades`.
Para utilizar otro servidor se puede indicar la dirección, en orden de prioridad, mediante el
primer argumento del programa, la propiedad `-Dws.url` o la variable de entorno `WS_URL`.

## Contrato del servicio

- **Protocolo:** SOAP 1.1 sobre HTTP, estilo *document/literal wrapped*.
- **Espacio de nombres:** `http://ws.monster.edu.ec/`
- **Servicio / puerto:** `WSConversorUnidades` / `WSConversorUnidadesPort`
- **Contrato sin conexión:** `04. SERVIDOR/contrato/WSConversorUnidades.wsdl` y su esquema XSD.

### Operaciones

| Operación | Parámetros | Datos de la respuesta |
|---|---|---|
| `autenticar` | `usuario`, `password` | `token`, `expiracion` (ISO-8601, UTC) |
| `obtenerCategorias` | `token` | `categorias` |
| `obtenerUnidades` | `token`, `categoria` | `categoria`, `unidades` |
| `convertir` | `token`, `categoria`, `unidadOrigen`, `unidadDestino`, `valor` | `resultado`, `unidadOrigen`, `unidadDestino` |

Toda respuesta incluye además `estado` (`OK` o `ERROR`), `codigo` y `mensaje`. Los errores de
negocio se informan mediante estos campos y no mediante *SOAP Fault*.

### Códigos de respuesta

| Código | Estado | Significado |
|---|---|---|
| `200` | OK | Operación realizada correctamente |
| `AUTH-001` | ERROR | Token inválido o ausente |
| `AUTH-002` | ERROR | Sesión expirada |
| `AUTH-003` | ERROR | Usuario o contraseña incorrectos |
| `UNIT-001` | ERROR | Unidad de origen no soportada |
| `UNIT-002` | ERROR | Unidad de destino no soportada |
| `DATA-001` | ERROR | Datos incompletos o inconsistentes (incluye valor no numérico) |
| `SYS-001` | ERROR | Error interno del servidor |

### Reglas de sesión

- El token tiene una vigencia de 15 minutos desde la autenticación; su uso no la extiende.
- Las operaciones `obtenerCategorias`, `obtenerUnidades` y `convertir` exigen un token vigente.
- Ante `AUTH-001` o `AUTH-002`, el cliente debe descartar el token y solicitar una nueva
  autenticación.

### Catálogo

| Categoría | Unidades |
|---|---|
| `LONGITUD` | centímetros, metros, pies, yardas, millas |
| `MASA` | gramos, kilogramos, onzas, libras, quintales |
| `TEMPERATURA` | celsius, fahrenheit, kelvin, newton, reaumur |

Los nombres de categorías y unidades se comparan sin distinguir mayúsculas, espacios
exteriores ni tildes.

## Guía para desarrollar los demás clientes

1. Generar o escribir el cliente SOAP a partir de `04. SERVIDOR/contrato/WSConversorUnidades.wsdl`;
   no es necesario que el servidor esté encendido para ello.
2. No codificar unidades, factores ni fórmulas en el cliente: las categorías y unidades se
   obtienen con `obtenerCategorias` y `obtenerUnidades`, y el cálculo lo realiza el servidor.
3. Evaluar siempre el campo `estado` de la respuesta y mostrar al usuario `codigo` y `mensaje`
   cuando sea `ERROR`.
4. Mantener la dirección del servicio configurable, sin escribirla de forma fija en el código.
5. Organizar el proyecto con el patrón MVC y el paquete base `ec.edu.monster`.
6. Clientes que se ejecutan en el navegador: el servidor admite por defecto cualquier puerto
   de `localhost`. Otro origen debe agregarse en `cors.origenes.permitidos`.
7. Emulador de Android: el equipo anfitrión se alcanza en `10.0.2.2`, no en `localhost`.
8. Para mantener consistencia visual, el cliente web (`04. CLIWEB`) sigue el mismo diseño
   que el cliente de escritorio (`04. CLIESC`): barra superior con marca y acciones de sesión,
   y pestañas para navegar entre Catálogo y Conversión.

El cliente de consola (`04. CLICON/CLICON`) puede tomarse como implementación de referencia.
El cliente de escritorio (`04. CLIESC`) sirve como referencia de diseño visual para el cliente web.

## Pruebas

| Proyecto | Pruebas | Alcance |
|---|---|---|
| Servidor | 226 | Servicios, estrategias de conversión, seguridad, filtro CORS y controlador |
| Cliente de consola | 273 | Controladores, vistas, lectura de datos y adaptador SOAP |
| Cliente de escritorio | 253 | Controladores, modelo, utilidades, adaptador SOAP y casos borde |
| Cliente web | 237 | Modelo, utilidades, controladores, adaptador SOAP y casos borde |

Las pruebas utilizan JUnit 5 y se ejecutan con `mvn test` o con *Clean and Build* en NetBeans.
Ninguna requiere Payara: las pruebas SOAP del cliente publican un servicio de prueba dentro
del mismo proceso.

## Modelos UML

Los modelos se elaboraron en SAP PowerDesigner 16.5. Para consultarlos, abrir el espacio de
trabajo `SOAP_JAVA_SINBDD_GR05.sws`, que registra todos los modelos mediante rutas relativas.
Las imágenes exportadas se encuentran en `05. DOCUMENTACION/img`.

| Carpeta | Contenido |
|---|---|
| `01_CASOS_USO` | Diagrama general y un modelo por caso de uso |
| `02_ACTIVIDAD` | Diagramas de actividad |
| `03_SECUENCIA` | Diagramas de secuencia de los flujos principales y alternos |
| `04_CLASES` | Clases del servidor, los clientes existentes y el diseño móvil |
| `05_ARQUITECTURA` | Diagrama de arquitectura SOA / MVC |
