/***********************************************************/
--Disparador que valida la correcta inserción de un Empleado
/***********************************************************/

CREATE OR REPLACE TRIGGER validar_insercion_empleado
BEFORE UPDATE OR DELETE ON empleado
FOR EACH ROW
DECLARE
  v_count INTEGER;
BEGIN
  -- Verificar que el rol sea válido (solo para actualización)
  IF UPDATING AND :NEW.rol IS NOT NULL AND :NEW.rol NOT IN ('Administrador', 'Entrenador', 'Recepcionista') THEN
    RAISE_APPLICATION_ERROR(-20001, 'El rol del empleado no es válido. Los roles permitidos son Administrador, Entrenador o Recepcionista.');
  END IF;

  -- Verificar que el nombre del empleado sea único (ignorando mayúsculas y eliminados)
  IF UPDATING AND :NEW.nombre IS NOT NULL AND :NEW.nombre != :OLD.nombre THEN
    SELECT COUNT(*)
    INTO v_count
    FROM empleado
    WHERE LOWER(nombre) = LOWER(:NEW.nombre)
      AND ID_Empleado != :NEW.ID_Empleado
      AND eliminado = 0;

    IF v_count > 0 THEN
      RAISE_APPLICATION_ERROR(-20002, 'El nombre del empleado ya existe. Debe ser único.');
    END IF;
  END IF;

  -- Verificar que el correo electrónico sea único (ignorando mayúsculas y eliminados)
  IF UPDATING AND :NEW.correo_electronico IS NOT NULL AND :NEW.correo_electronico != :OLD.correo_electronico THEN
    SELECT COUNT(*)
    INTO v_count
    FROM empleado
    WHERE LOWER(correo_electronico) = LOWER(:NEW.correo_electronico)
      AND ID_Empleado != :NEW.ID_Empleado
      AND eliminado = 0;

    IF v_count > 0 THEN
      RAISE_APPLICATION_ERROR(-20003, 'El correo electrónico del empleado ya existe. Debe ser único.');
    END IF;
  END IF;

  -- Verificar que el ID no esté duplicado (solo al insertar)
  IF INSERTING AND :NEW.ID_Empleado IS NOT NULL THEN
    SELECT COUNT(*)
    INTO v_count
    FROM empleado
    WHERE ID_Empleado = :NEW.ID_Empleado;

    IF v_count > 0 THEN
      RAISE_APPLICATION_ERROR(-20004, 'El ID del empleado ya existe. Debe ser único.');
    END IF;
  END IF;

  -- Validar la actualización de 'eliminado' para evitar mutación (solo al eliminar)
  IF DELETING AND :OLD.eliminado = 0 AND :NEW.eliminado = 1 THEN
    -- Puedes agregar validación aquí si es necesario
    NULL; -- No se realiza ninguna acción en este caso
  END IF;

END;

/***********************************************************/
/* Disparador que valida la correcta inserción de una actividad */
/***********************************************************/

CREATE OR REPLACE TRIGGER validar_insercion_actividad
BEFORE INSERT OR UPDATE ON Escoge_Actividad
FOR EACH ROW
DECLARE
  v_count INTEGER;
BEGIN
  -- Validar que el ID del entrenador exista en la tabla Entrenador
  IF INSERTING OR UPDATING THEN
    SELECT COUNT(*)
    INTO v_count
    FROM Entrenador
    WHERE ID_Entrenador = :NEW.ID_Entrenador;

    IF v_count = 0 THEN
      RAISE_APPLICATION_ERROR(-20001, 'El ID del entrenador no existe en la tabla Entrenador.');
    END IF;
  END IF;

  -- Validar que las fechas sean consistentes: Fecha de inicio <= Fecha de fin
  IF INSERTING OR UPDATING THEN
    IF :NEW.Fecha_Ini > :NEW.Fecha_Fin THEN
      RAISE_APPLICATION_ERROR(-20002, 'La fecha de inicio debe ser menor o igual a la fecha de fin.');
    END IF;
  END IF;

  -- Validar que el número de plazas sea mayor a 0
  IF INSERTING OR UPDATING THEN
    IF :NEW.Plazas <= 0 THEN
      RAISE_APPLICATION_ERROR(-20003, 'El número de plazas debe ser mayor a 0.');
    END IF;
  END IF;

  -- Validar que el nombre de la actividad no esté duplicado (ignorando mayúsculas y actividades eliminadas)
  IF INSERTING OR (UPDATING AND :NEW.Nombre != :OLD.Nombre) THEN
    SELECT COUNT(*)
    INTO v_count
    FROM Escoge_Actividad
    WHERE LOWER(Nombre) = LOWER(:NEW.Nombre)
      AND Eliminado = 0
      AND ID_Actividad != :NEW.ID_Actividad;

    IF v_count > 0 THEN
      RAISE_APPLICATION_ERROR(-20004, 'El nombre de la actividad ya existe y no ha sido eliminado.');
    END IF;
  END IF;
END;
/

/***********************************************************/
/* Disparador que valida la correcta inserción de un Cliente */
/***********************************************************/
  
-- VALIDAR CLIENTE
CREATE OR REPLACE TRIGGER trg_validar_cliente
BEFORE INSERT ON ClienteIncluyeSuscripcion_Invitacion
FOR EACH ROW
DECLARE
    v_count NUMBER;
BEGIN
    -- Verificar que el código no esté duplicado
    SELECT COUNT(*)
    INTO v_count
    FROM ClienteIncluyeSuscripcion_Invitacion
    WHERE Codigo = :NEW.Codigo;

    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'El código de suscripción ya existe. Debe ser único.');
    END IF;

    -- Validar que la fecha de suscripción no sea anterior a la fecha actual
    IF :NEW.Fecha < SYSDATE THEN
        RAISE_APPLICATION_ERROR(-20002, 'La fecha de suscripción no puede ser anterior a hoy.');
    END IF;

    -- Comprobar si la suscripción ya está asignada al cliente
    SELECT COUNT(*)
    INTO v_count
    FROM ClienteIncluyeSuscripcion_Invitacion
    WHERE ID_Suscripcion = :NEW.ID_Suscripcion
      AND ID_Cliente_Invitado = :NEW.ID_Cliente_Invitado;

    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'El cliente ya está asociado a esta suscripción.');
    END IF;

END;

/***********************************************************/
/* Disparador que valida la compra de un producto */
/***********************************************************/

CREATE OR REPLACE TRIGGER validar_compra_producto
BEFORE INSERT ON Comprar
FOR EACH ROW
DECLARE
  v_stock_disponible INTEGER;
  v_cliente_existente INTEGER;
BEGIN
  -- Validar que el cliente exista en la tabla ClienteIncluyeSuscripcion_Invitacion
  SELECT COUNT(*)
  INTO v_cliente_existente
  FROM ClienteIncluyeSuscripcion_Invitacion
  WHERE ID_Cliente = :NEW.ID_Cliente;

  IF v_cliente_existente = 0 THEN
    RAISE_APPLICATION_ERROR(-20001, 'El cliente no está registrado en el sistema.');
  END IF;

  -- Verificar la disponibilidad de stock del producto
  SELECT Stock
  INTO v_stock_disponible
  FROM Producto
  WHERE ID_Producto = :NEW.ID_Producto
    AND Eliminado = 0;

  IF v_stock_disponible IS NULL THEN
    RAISE_APPLICATION_ERROR(-20002, 'El producto no existe o ha sido eliminado.');
  END IF;

  IF :NEW.Cantidad_Venta > v_stock_disponible THEN
    RAISE_APPLICATION_ERROR(-20003, 'No hay suficiente stock disponible para realizar la compra.');
  END IF;

  -- Validar que la cantidad sea positiva
  IF :NEW.Cantidad_Venta <= 0 THEN
    RAISE_APPLICATION_ERROR(-20004, 'La cantidad a comprar debe ser mayor que cero.');
  END IF;

  -- Validar que el total de la venta sea positivo
  IF :NEW.Total_Venta <= 0 THEN
    RAISE_APPLICATION_ERROR(-20005, 'El total de la venta debe ser mayor que cero.');
  END IF;

  -- Nota: El stock se actualizará mediante un procedimiento o en el método de negocio correspondiente.
END;
/

