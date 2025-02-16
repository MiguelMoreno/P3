
/* TABLAS */

-- Crear tabla Producto
CREATE TABLE Producto (
    ID_Producto INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    Stock INT NOT NULL CHECK (Stock >= 0),
    Nombre VARCHAR2(100) NOT NULL,
    Eliminado INT DEFAULT 0 NOT NULL,
    Proveedor VARCHAR2(100)
);

-- Crear tabla Cliente_Invitado
CREATE TABLE Cliente_Invitado (
    ID_Cliente_Invitado INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    Eliminado INT DEFAULT 0 NOT NULL,
    Nombre VARCHAR2(100) NOT NULL
);

-- Crear tabla Empleado
CREATE TABLE Empleado (
    ID_Empleado INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    Nombre VARCHAR2(100) NOT NULL,
    Correo_Electronico VARCHAR2(100) UNIQUE NOT NULL,
    Eliminado INT DEFAULT 0 NOT NULL,
    Rol VARCHAR2(50) CHECK (Rol IN ('Administrador', 'Entrenador')) NOT NULL
);

-- Crear tabla Administrador
CREATE TABLE Administrador (
    ID_Administrador INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    ID_Empleado INT NOT NULL,
    Eliminado INT DEFAULT 0 NOT NULL,
    FOREIGN KEY (ID_Empleado) REFERENCES Empleado(ID_Empleado) ON DELETE CASCADE
);

-- Crear tabla Entrenador
CREATE TABLE Entrenador (
    ID_Entrenador INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    ID_Empleado INT NOT NULL,
    Eliminado INT DEFAULT 0 NOT NULL,
    FOREIGN KEY (ID_Empleado) REFERENCES Empleado(ID_Empleado) ON DELETE CASCADE
);

-- Crear tabla Escoge_Actividad
CREATE TABLE Escoge_Actividad (
    ID_Entrenador INT NOT NULL,
    ID_Actividad INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    Fecha_Ini DATE NOT NULL,
    Fecha_Fin DATE NOT NULL,
    Nombre VARCHAR2(100) NOT NULL,
    Descripcion VARCHAR2(500),
    Eliminado INT DEFAULT 0 NOT NULL,
    Plazas INT,
    FOREIGN KEY (ID_Entrenador) REFERENCES Entrenador(ID_Entrenador) ON DELETE CASCADE
);

-- Crear tabla ClienteIncluyeSuscripcion_Invitacion
CREATE TABLE ClienteIncluyeSuscripcion_Invitacion (
    ID_Cliente INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    ID_Suscripcion INT NOT NULL,
    ID_Cliente_Invitado INT,
    Fecha DATE NOT NULL,
    Codigo VARCHAR2(20) UNIQUE NOT NULL,
    Eliminado INT DEFAULT 0 NOT NULL,
    FOREIGN KEY (ID_Cliente_Invitado) REFERENCES Cliente_Invitado(ID_Cliente_Invitado) ON DELETE SET NULL
);

-- Crear tabla Inscribe_en_Actividad
CREATE TABLE Inscribe_en_Actividad (
    ID_Actividad INT NOT NULL,
    ID_Cliente INT NOT NULL,
    PRIMARY KEY (ID_Actividad, ID_Cliente),
    FOREIGN KEY (ID_Actividad) REFERENCES Escoge_Actividad(ID_Actividad) ON DELETE CASCADE,
    Eliminado INT DEFAULT 0 NOT NULL,
    FOREIGN KEY (ID_Cliente) REFERENCES ClienteIncluyeSuscripcion_Invitacion(ID_Cliente) ON DELETE CASCADE
);

-- Crear tabla Comprar
CREATE TABLE Comprar (
    ID_Venta INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    ID_Producto INT NOT NULL,
    ID_Cliente INT NOT NULL,
    Total_Venta NUMBER(10, 2) NOT NULL CHECK (Total_Venta > 0),
    Cantidad_Venta INT NOT NULL CHECK (Cantidad_Venta > 0),
    Fecha DATE NOT NULL,
    FOREIGN KEY (ID_Producto) REFERENCES Producto(ID_Producto) ON DELETE CASCADE,
    Eliminado INT DEFAULT 0 NOT NULL,
    FOREIGN KEY (ID_Cliente) REFERENCES ClienteIncluyeSuscripcion_Invitacion(ID_Cliente) ON DELETE CASCADE
);

-- Crear tabla Notifica_Incidencia
CREATE TABLE Notifica_Incidencia (
    ID_Incidencia INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    ID_Administrador INT NOT NULL,
    ID_Empleado INT NOT NULL,
    Fecha DATE NOT NULL,
    Descripcion VARCHAR2(500) NOT NULL,
    FOREIGN KEY (ID_Administrador) REFERENCES Administrador(ID_Administrador) ON DELETE CASCADE,
    Eliminado INT DEFAULT 0 NOT NULL,
    FOREIGN KEY (ID_Empleado) REFERENCES Empleado(ID_Empleado) ON DELETE CASCADE
);

-- Crear tabla TieneRutina
CREATE TABLE TieneRutina (
    ID_Rutina INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    ID_Cliente INT NOT NULL,
    Duracion VARCHAR2(50),
    Descripcion VARCHAR2(500),
    Eliminado INT DEFAULT 0 NOT NULL,
    FOREIGN KEY (ID_Cliente) REFERENCES ClienteIncluyeSuscripcion_Invitacion(ID_Cliente) ON DELETE CASCADE
);

-- Crear tabla Oferta_Aplicar
CREATE TABLE Oferta_Aplicar (
    ID_Cliente INT NOT NULL,
    ID_Oferta INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    Nombre VARCHAR2(100) NOT NULL,
    Descuento NUMBER(5, 2) CHECK (Descuento > 0),
    Duracion VARCHAR2(50),
    Eliminado INT DEFAULT 0 NOT NULL,
    FOREIGN KEY (ID_Cliente) REFERENCES ClienteIncluyeSuscripcion_Invitacion(ID_Cliente) ON DELETE CASCADE
);

-- Crear tabla Ejercicio
CREATE TABLE Ejercicio (
    ID_Ejercicio INT PRIMARY KEY,
    ID_Rutina INT,  -- Relacionado con la rutina
    Descripcion VARCHAR2(255),
    CONSTRAINT fk_rutina FOREIGN KEY (ID_Rutina) REFERENCES TieneRutina(ID_Rutina)
);
