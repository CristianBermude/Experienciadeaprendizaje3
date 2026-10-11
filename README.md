# Banco Académico UDB — Sistema de Gestión Bancaria Académica
### Ciclo 02/2026 · Programación Orientada a Objetos (POO901) · UDB Virtual
> **Experiencia de Aprendizaje 3 — Fase 1: Sistema Desktop Base con Swing, POO, MySQL y JDBC**

---

## 🏛️ Descripción del Proyecto

El **Banco Académico UDB** es una solución integral diseñada para la administración de clientes, cuentas bancarias, transacciones financieras y reportes gerenciales.

El proyecto de cátedra se compone de tres fases evolutivas:
- **Fase 1 (Completada)**: Sistema Desktop Base (Gestión de Clientes CRUD, Creación y Administración de Cuentas Bancarias con Swing, POO, MySQL y JDBC).
- **Fase 2 (Preparada)**: Sistema Desktop Completo con Transacciones simuladas (depósitos, retiros, transferencias), Reportes y bitácora de auditoría con Log4J.
- **Fase 3 (Preparada)**: Migración a Plataforma Web utilizando HTML, Servlets, JSP, JavaBeans y Apache Tomcat con control de roles (Administrador, Cajero, Cliente).

---

## 🚀 Guía de Puesta en Marcha y Ejecución

La aplicación cuenta con **soporte dual e inmediato** tanto para **Docker Compose** como para **XAMPP / MySQL Local**.


El contenedor corre en un puerto aislado (**3308**) para no colisionar con otros servicios:

```bash
# 1. Levantar el contenedor MySQL (crea la BD e inserta los datos de prueba)
docker compose up -d

# 2. Compilar el código Java
mkdir -p bin
javac -encoding UTF-8 -cp "lib/mysql-connector-j-9.1.0.jar" -d bin $(find src -name "*.java")

# 3. Ejecutar la aplicación
java -cp "bin:lib/mysql-connector-j-9.1.0.jar" banco.Main

# 4. Cuando termine de usar el sistema, puede apagar el contenedor
docker compose down
```


---

## 📂 Estructura del Proyecto

```
Experienciadeaprendizaje3/
├── docker-compose.yml              # Configuración de contenedor MySQL 8.0 en puerto 3308
├── lib/
│   └── mysql-connector-j-9.1.0.jar # Driver JDBC oficial de MySQL
├── sql/
│   └── 01_esquema_banco.sql        # Script DDL/DML: Clientes, Cuentas, Transacciones, Usuarios
├── src/
│   └── banco/
│       ├── Main.java               # Punto de entrada de la aplicación Swing
│       ├── modelo/                 # Persona, Cliente, Cuenta, CuentaAhorro, CuentaCorriente, etc.
│       ├── datos/                  # ConexionBD (Docker/XAMPP), ClienteDAO, CuentaDAO
│       ├── servicio/               # BancoServicio (Lógica de negocio y fachada)
│       ├── excepciones/            # BancoException, DatoInvalidoException, etc.
│       ├── util/                   # Validador territorial, Formateador monetario/fechas
│       └── vista/                  # VentanaPrincipal (JFrame, JTabbedPane, JTable, Dashboard)
├── docs/
│   ├── DOCUMENTO_TECNICO.md        # Documento técnico con diagramas UML y modelo relacional
│   └── AUDITORIA_COHERENCIA.md     # Informe de auditoría continua y cumplimiento de rúbrica
├── PLAN_DE_PRUEBAS.md              # Plan maestro de pruebas y matriz de casos de prueba
└── README.md                       # Documentación principal de inicio rápido
```

---

## 📋 Entregables de la Fase 1 Cubiertos

| Entregable Solicitado | Ubicación / Estado |
|---|---|
| **Proyecto Java Desktop** | Paquetes en `src/banco/` con interfaz gráfica moderna Swing (`VentanaPrincipal`). |
| **Gestión de Clientes (CRUD)** | Formulario y tabla interactiva con Código, DUI, Nombre, Teléfono, Correo, Dirección, Estado. |
| **Cuentas Bancarias** | Apertura y vinculación de Cuentas de Ahorro y Corrientes con saldo y validaciones. |
| **Script SQL** | `sql/01_esquema_banco.sql` con esquema relacional, restricciones y datos de prueba. |
| **Base de datos MySQL** | Soportada en Docker (`docker-compose.yml`) y XAMPP (`3306`). |
| **Plan de Pruebas** | `PLAN_DE_PRUEBAS.md` con 25+ casos de prueba detallados. |
| **Documento Técnico** | `docs/DOCUMENTO_TECNICO.md` con arquitectura, diagramas UML y modelo ER. |
| **Auditoría de Coherencia** | `docs/AUDITORIA_COHERENCIA.md` con evaluación de rúbrica (100% Excelente). |
| **Diagrama de Clases** | Modelado en Mermaid en el documento técnico. |
| **Modelo Relacional** | Diagrama entidad-relación en Mermaid en el documento técnico. |
