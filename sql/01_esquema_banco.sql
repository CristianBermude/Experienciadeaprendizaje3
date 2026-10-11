-- ============================================================================
-- BANCO ACADÉMICO UDB - SISTEMA DE GESTIÓN BANCARIA ACADÉMICA
-- Asignatura: Programación Orientada a Objetos (POO901)
-- Base de Datos: MySQL 8.0 / MariaDB (Compatible con Docker y XAMPP)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS banco_academico_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_spanish_ci;

USE banco_academico_db;

-- ----------------------------------------------------------------------------
-- 1. TABLA: clientes (Fase 1 - Requerimiento Principal)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS clientes (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    codigo_cliente VARCHAR(20) NOT NULL UNIQUE,
    dui VARCHAR(10) NOT NULL UNIQUE,
    nombre_completo VARCHAR(120) NOT NULL,
    telefono VARCHAR(15) NOT NULL,
    correo_electronico VARCHAR(100) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    estado ENUM('ACTIVO', 'INACTIVO', 'BLOQUEADO') NOT NULL DEFAULT 'ACTIVO',
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_clientes_dui (dui),
    INDEX idx_clientes_codigo (codigo_cliente),
    INDEX idx_clientes_estado (estado)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 2. TABLA: cuentas (Fase 1 - Cuentas Bancarias Vinculadas)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cuentas (
    id_cuenta INT AUTO_INCREMENT PRIMARY KEY,
    numero_cuenta VARCHAR(25) NOT NULL UNIQUE,
    id_cliente INT NOT NULL,
    tipo_cuenta ENUM('AHORRO', 'CORRIENTE') NOT NULL,
    saldo DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    estado ENUM('ACTIVA', 'BLOQUEADA', 'CANCELADA') NOT NULL DEFAULT 'ACTIVA',
    fecha_apertura DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_saldo_positivo CHECK (saldo >= 0.00),
    CONSTRAINT fk_cuentas_clientes FOREIGN KEY (id_cliente)
        REFERENCES clientes (id_cliente)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    INDEX idx_cuentas_numero (numero_cuenta),
    INDEX idx_cuentas_cliente (id_cliente),
    INDEX idx_cuentas_estado (estado)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 3. TABLA: transacciones (Visión Fase 2 - Transacciones Bancarias)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transacciones (
    id_transaccion INT AUTO_INCREMENT PRIMARY KEY,
    codigo_transaccion VARCHAR(30) NOT NULL UNIQUE,
    id_cuenta_origen INT NOT NULL,
    id_cuenta_destino INT NULL,
    tipo_transaccion ENUM('DEPOSITO', 'RETIRO', 'TRANSFERENCIA') NOT NULL,
    monto DECIMAL(12, 2) NOT NULL,
    saldo_resultante DECIMAL(12, 2) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_monto_positivo CHECK (monto > 0.00),
    CONSTRAINT fk_tx_cuenta_origen FOREIGN KEY (id_cuenta_origen)
        REFERENCES cuentas (id_cuenta)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_tx_cuenta_destino FOREIGN KEY (id_cuenta_destino)
        REFERENCES cuentas (id_cuenta)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    INDEX idx_tx_origen (id_cuenta_origen),
    INDEX idx_tx_fecha (fecha_hora)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 4. TABLA: usuarios (Visión Fase 3 - Seguridad y Roles Web)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    nombre_completo VARCHAR(120) NOT NULL,
    rol ENUM('ADMINISTRADOR', 'CAJERO', 'CLIENTE') NOT NULL,
    id_cliente INT NULL,
    estado ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuarios_cliente FOREIGN KEY (id_cliente)
        REFERENCES clientes (id_cliente)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    INDEX idx_usuarios_rol (rol)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 5. DATOS DE PRUEBA INICIALES (Para validación inmediata de Fase 1)
-- ----------------------------------------------------------------------------

-- Inserción de Clientes
INSERT INTO clientes (codigo_cliente, dui, nombre_completo, telefono, correo_electronico, direccion, estado)
VALUES
('CLI-001', '02345678-9', 'Carlos Alberto Rivera Santos', '7890-1234', 'carlos.rivera@udb.edu.sv', 'Colonia Médica, Av. Dr. Emilio Álvarez #12, San Salvador', 'ACTIVO'),
('CLI-002', '03456789-0', 'María Elena Gómez Morales', '7123-4567', 'maria.gomez@udb.edu.sv', 'Residencial Santa Rosa, Senda Los Cipreses #45, Santa Tecla', 'ACTIVO'),
('CLI-003', '04567890-1', 'Juan Fernando Martínez Peña', '7987-6543', 'juan.martinez@udb.edu.sv', 'Urbanización Jardines del Volcán, Polígono E, Antiguo Cuscatlán', 'ACTIVO'),
('CLI-004', '05678901-2', 'Ana Lucía Portillo Herrera', '7456-7890', 'ana.portillo@udb.edu.sv', 'Barrio San Miguelito, 5a Av. Norte #204, San Salvador', 'BLOQUEADO'),
('CLI-005', '06789012-3', 'Roberto Enrique Ramos Cortez', '7234-5678', 'roberto.ramos@udb.edu.sv', 'Colonia Escalon, Calle Padres Aguilar #312, San Salvador', 'INACTIVO')
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo);

-- Inserción de Cuentas Bancarias
INSERT INTO cuentas (numero_cuenta, id_cliente, tipo_cuenta, saldo, estado)
VALUES
('CTA-2026-00001', 1, 'AHORRO', 1250.75, 'ACTIVA'),
('CTA-2026-00002', 1, 'CORRIENTE', 3420.00, 'ACTIVA'),
('CTA-2026-00003', 2, 'AHORRO', 850.50, 'ACTIVA'),
('CTA-2026-00004', 3, 'CORRIENTE', 5100.25, 'ACTIVA'),
('CTA-2026-00005', 4, 'AHORRO', 320.00, 'BLOQUEADA')
ON DUPLICATE KEY UPDATE saldo=VALUES(saldo);

-- Inserción de Usuarios de Prueba (Fase 3)
INSERT INTO usuarios (username, password_hash, nombre_completo, rol, id_cliente, estado)
VALUES
('admin', 'admin123', 'Administrador Principal UDB', 'ADMINISTRADOR', NULL, 'ACTIVO'),
('cajero1', 'cajero123', 'Cajero Ventanilla 01', 'CAJERO', NULL, 'ACTIVO'),
('crivera', 'cliente123', 'Carlos Alberto Rivera Santos', 'CLIENTE', 1, 'ACTIVO')
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo);
