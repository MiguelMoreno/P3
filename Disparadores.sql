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
