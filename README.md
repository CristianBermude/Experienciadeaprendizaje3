# Banco Académico UDB — Sistema de Gestión Bancaria Académica
### Ciclo 02/2026 · Programación Orientada a Objetos (POO901) · UDB Virtual
> **Experiencia de Aprendizaje 3 — Fase 2: Sistema Desktop Completo con Transacciones, Reportes, Excepciones y Log4J**

---

## 🏛️ Descripción del Proyecto

El **Banco Académico UDB** es una solución integral diseñada para la administración de clientes, cuentas bancarias, transacciones financieras y reportes gerenciales.

El proyecto de cátedra se compone de tres fases evolutivas:
- **Fase 1 (Completada)**: Sistema Desktop Base (Gestión de Clientes CRUD, Creación y Administración de Cuentas Bancarias con Swing, POO, MySQL y JDBC).
- **Fase 2 (Completada)**: Sistema Desktop Completo con Transacciones bancarias simuladas (depósitos, retiros con saldo suficiente, transferencias atómicas ACID), Reportes consolidados y bitácora de auditoría mediante **Log4J 2**.
- **Fase 3 (Preparada)**: Migración a Plataforma Web utilizando HTML, Servlets, JSP, JavaBeans y Apache Tomcat con control de roles (Administrador, Cajero, Cliente).

---

## 🚀 Guía de Puesta en Marcha y Ejecución

La aplicación cuenta con **soporte dual e inmediato** tanto para **Docker Compose** (puerto `3308`) como para **XAMPP / MySQL Local** (puerto `3306`), con detección y conmutación automática en segundo plano.

### Compilación y Ejecución:

```bash
# 1. Levantar el contenedor MySQL (crea la BD e inserta los datos de prueba)
docker compose up -d

# 2. Compilar el código Java (incluye driver JDBC MySQL y librerías Log4J 2)
mkdir -p bin
javac -encoding UTF-8 -cp "lib/*:bin" -d bin $(find src -name "*.java")
cp src/log4j2.xml bin/

# 3. Ejecutar la aplicación gráfica Swing
java -cp "bin:lib/*" banco.Main

# 4. (Opcional) Ejecutar suite de pruebas automatizadas de Fase 2
java -cp "bin:lib/*" banco.TestIntegracionFase2

# 5. Cuando termine de usar el sistema, puede apagar el contenedor
docker compose down
```

*(En Windows use `;` como separador en lugar de `:`: `-cp "bin;lib/*"`)*

---

## 📂 Estructura del Proyecto

```
Experienciadeaprendizaje3/
├── docker-compose.yml              # Configuración de contenedor MySQL 8.0 en puerto 3308
├── lib/
│   ├── mysql-connector-j-9.1.0.jar # Driver JDBC oficial de MySQL
│   ├── log4j-api-2.24.1.jar        # API oficial de Apache Log4j 2
│   └── log4j-core-2.24.1.jar       # Motor de logging de Apache Log4j 2
├── sql/
│   └── 01_esquema_banco.sql        # Script DDL/DML: Clientes, Cuentas, Transacciones, Usuarios
├── src/
│   ├── log4j2.xml                  # Configuración de bitácora física y consola
│   └── banco/
│       ├── Main.java               # Punto de entrada de la aplicación Swing
│       ├── TestIntegracion.java    # Pruebas de integración de Fase 1
│       ├── TestIntegracionFase2.java # Suite de pruebas de transacciones y reportes Fase 2
│       ├── modelo/                 # Persona, Cliente, Cuenta, CuentaAhorro, CuentaCorriente, Transaccion, Usuario
│       ├── datos/                  # ConexionBD (Docker/XAMPP), ClienteDAO, CuentaDAO, TransaccionDAO
│       ├── servicio/               # BancoServicio (Lógica de negocio, ACID y bitácora Log4j)
│       ├── excepciones/            # BancoException, FondosInsuficientesException, TransaccionInvalidaException, etc.
│       ├── util/                   # Validador territorial, Formateador monetario/fechas
│       └── vista/                  # VentanaPrincipal (Dashboard, Clientes, Cuentas, Transacciones, Reportes)
└── README.md                       # Documentación técnica de inicio rápido
```

---

## 📋 Entregables de la Fase 2 en el Repositorio

| Entregable Solicitado | Ubicación / Estado |
|---|---|
| **Proyecto Java Desktop Completo** | Paquetes en `src/banco/` con interfaz moderna Swing (`VentanaPrincipal`). |
| **Gestión de Clientes (CRUD)** | Formulario y tabla interactiva con Código, DUI salvadoreño, Nombre, Teléfono, Correo, Dirección y Estado. |
| **Cuentas Bancarias** | Apertura y vinculación de Cuentas de Ahorro y Corrientes con saldo y validaciones. |
| **Gestión de Transacciones** | Depósito, Retiro (saldo suficiente), Transferencia atómica ACID (`commit`/`rollback`) y consulta de historial. |
| **Gestión de Reportes** | Cuentas Activas, Cuentas Bloqueadas, Clientes Registrados con saldo, Historial General, Totales Acumulados. |
| **Auditoría con Log4J 2** | `src/log4j2.xml`, registro automático de eventos INFO y errores WARN/ERROR en consola y archivo `logs/banco.log`. |
| **Script SQL y Base de Datos** | `sql/01_esquema_banco.sql` con soporte para Docker (`3308`) y XAMPP (`3306`). |
| **Pruebas de Integración** | Suite automatizada `TestIntegracionFase2.java` con 21 aserciones aprobadas al 100%. |
| **Documentación de Cátedra** | Documento Técnico, Plan de Pruebas y Auditoría gestionados para entrega de aula virtual. |
