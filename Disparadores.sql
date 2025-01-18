/***********************************************************/
--Disparador que valida la correcta inserción de un Empleado
/***********************************************************/

CREATE OR REPLACE TRIGGER validar_insercion_empleado
BEFORE INSERT OR UPDATE ON empleado
FOR EACH ROW
DECLARE
  v_count INTEGER; -- Variable para almacenar resultados de las consultas
BEGIN
  -- Verificar que el rol sea válido
  IF :NEW.rol NOT IN ('Administrador', 'Entrenador', 'Recepcionista') THEN
    RAISE_APPLICATION_ERROR(-20001, 'El rol del empleado no es válido. Los roles permitidos son Administrador, Entrenador o Recepcionista.');
  END IF;

  -- Verificar que el nombre del empleado sea único (ignorando mayúsculas y eliminados)
  SELECT COUNT(*)
  INTO v_count
  FROM empleado
  WHERE LOWER(nombre) = LOWER(:NEW.nombre)
    AND ID_Empleado != NVL(:NEW.ID_Empleado, -1) -- Ignorar el mismo ID si es una actualización
    AND eliminado = 0;
  
  IF v_count > 0 THEN
    RAISE_APPLICATION_ERROR(-20002, 'El nombre del empleado ya existe. Debe ser único.');
  END IF;

  -- Verificar que el correo electrónico sea único (ignorando mayúsculas y eliminados)
  SELECT COUNT(*)
  INTO v_count
  FROM empleado
  WHERE LOWER(correo_electronico) = LOWER(:NEW.correo_electronico)
    AND ID_Empleado != NVL(:NEW.ID_Empleado, -1) -- Ignorar el mismo ID si es una actualización
    AND eliminado = 0;
  
  IF v_count > 0 THEN
    RAISE_APPLICATION_ERROR(-20003, 'El correo electrónico del empleado ya existe. Debe ser único.');
  END IF;

  -- Verificar que el ID no esté duplicado (solo al insertar)
  IF INSERTING THEN
    SELECT COUNT(*)
    INTO v_count
    FROM empleado
    WHERE ID_Empleado = :NEW.ID_Empleado;
    
    IF v_count > 0 THEN
      RAISE_APPLICATION_ERROR(-20004, 'El ID del empleado ya existe. Debe ser único.');
    END IF;
  END IF;
END;
/

/***********************************************************/
--Disparador que valida la correcta inserción de un Cliente NO IMPLEMENTADO
/***********************************************************/
  
CREATE OR REPLACE TRIGGER validar_insercion_cliente
BEFORE INSERT ON cliente
FOR EACH ROW
DECLARE
  contador_id INTEGER;
  contador_email INTEGER;
BEGIN
  -- Verificar que el ID del cliente sea único
  SELECT COUNT(*) INTO contador_id
  FROM cliente
  WHERE idCliente = :NEW.idCliente;

  IF contador_id > 0 THEN
    RAISE_APPLICATION_ERROR(-20001, 'El ID del cliente ya existe. Debe ser único.');
  END IF;

  -- Verificar que el correo electrónico sea único
  SELECT COUNT(*) INTO contador_email
  FROM cliente
  WHERE email = :NEW.email;

  IF contador_email > 0 THEN
    RAISE_APPLICATION_ERROR(-20002, 'El correo electrónico ya existe. Debe ser único.');
  END IF;

  -- Verificar que el nombre tenga al menos 3 caracteres
  IF LENGTH(:NEW.nombre) < 3 THEN
    RAISE_APPLICATION_ERROR(-20003, 'El nombre del cliente debe tener al menos 3 caracteres.');
  END IF;
END;
/

/***********************************************************/
--Disparador que valida la correcta inserción de un Producto NO IMPLEMENTADO
/***********************************************************/
  
CREATE OR REPLACE TRIGGER validar_insercion_producto
BEFORE INSERT ON producto
FOR EACH ROW
DECLARE
  contador_id INTEGER;
BEGIN
  -- Verificar que el ID del producto sea único
  SELECT COUNT(*) INTO contador_id
  FROM producto
  WHERE idProducto = :NEW.idProducto;

  IF contador_id > 0 THEN
    RAISE_APPLICATION_ERROR(-20001, 'El ID del producto ya existe. Debe ser único.');
  END IF;

  -- Verificar que el stock no sea negativo
  IF :NEW.stock < 0 THEN
    RAISE_APPLICATION_ERROR(-20002, 'El stock no puede ser negativo.');
  END IF;

  -- Verificar que el precio sea mayor que 0
  IF :NEW.precio <= 0 THEN
    RAISE_APPLICATION_ERROR(-20003, 'El precio debe ser mayor que 0.');
  END IF;
END;
/
