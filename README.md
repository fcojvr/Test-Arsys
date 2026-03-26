# Playwright & Jenkins CI/CD Challenge - Arsys

Este repositorio contiene una solución completa de **Infraestructura como Código (IaC)** para desplegar un entorno local de **Integración Continua**. Utiliza **Docker Compose** para orquestar un servidor **Jenkins** preconfigurado que ejecuta una suite de **Smoke Tests** con **Playwright**.

---

## 🛠️ Arquitectura y decisiones técnicas

A diferencia de una instalación manual, este entorno ha sido diseñado para ser **Plug & Play** mediante las siguientes estrategias:

- **Docker-out-of-Docker (DooD):**  
  El contenedor de Jenkins incluye el Docker CLI y accede al socket del host (`/var/run/docker.sock`). Esto permite que Jenkins levante y gestione el contenedor de Playwright de forma independiente.

- **Zero Click-Ops:**  
  Mediante **JCasC (Jenkins Configuration as Code)** y **Job DSL**, el usuario administrador, los plugins y el job de pruebas se crean automáticamente al arrancar.

---

## 🚀 Guía de inicio rápido

### 1. Requisitos

| Herramienta       | Versión mínima |
|------------------|----------------|
| Docker Engine    | 24+            |
| Docker Compose   | v2.x           |
| Puertos libres   | 8080, 50000    |

### 2. Despliegue

Para asegurar una instalación limpia, ejecuta los siguientes comandos en la raíz del proyecto:

git clone <tu-repo-url>
cd <nombre-carpeta>

# Levantar la infraestructura
docker compose up -d --build

### 3. Acceso a Jenkins

Una vez que el contenedor esté corriendo, puedes acceder a la interfaz web. Jenkins puede tardar un par de minutos en inicializarse completamente mientras instala los plugins necesarios.

| Parámetro   | Valor por defecto    |
|------------|----------------------|
| URL        | http://localhost:8080 |
| Usuario    | admin                |
| Contraseña | admin                |



## 🧪 Ejecución de pruebas (Smoke Tests)

El job **`Playwright-Smoke-Tests`** está configurado para ejecutarse en un agente especializado utilizando la imagen oficial de Playwright.

### Escenarios incluidos

- **Validación de conectividad:**  
  Tests sobre URLs públicas como "https://nodejs.org" y "https://www.typescriptlang.org/" para asegurar salida a red.

- **API Smoke Tests:**  
  Pruebas de integración contra `httpbin.org`.

- **Simulación de fallos:**  
  Escenarios de errores `400` configurados intencionalmente para validar la robustez del reporte y la captura de artefactos (`screenshots` y `traces`).

---

## 📊 Reportes y artefactos

Tras cada ejecución, Jenkins procesa los resultados automáticamente:

- **Playwright HTML Report:**  
  Visualización interactiva accesible desde el menú lateral del job.

- **Traces & Screenshots:**  
  Se capturan solo en caso de fallo para optimizar el almacenamiento, permitiendo un debugging visual rápido desde los artefactos del build.

---

## 📂 Estructura del repositorio

```plaintext
.
├── jenkins/
│   ├── Dockerfile         # Jenkins LTS + Docker CLI + Plugins
│   ├── plugins.txt        # Lista de plugins (Git, Pipeline, HTML Publisher)
│   ├── casc/              # Configuración YAML de Jenkins (JCasC)
│   └── init.groovy.d/     # Scripts Groovy para auto-creación de jobs
├── playwright-tests/
│   ├── tests/             # Specs de Playwright (TypeScript)
│   └── playwright.config.ts
└── docker-compose.yml     # Orquestador principal de servicios
```

---

## 📝 Notas de desarrollo

- **Persistencia:**  
  Para reiniciar la configuración de fábrica, es necesario usar:

```bash
docker compose down -v
```

Esto eliminará los volúmenes de Jenkins.

- **Tiempo de inicio:**  
  El sistema estará listo cuando el log muestre:

```text
Jenkins is fully up and running
```

---
