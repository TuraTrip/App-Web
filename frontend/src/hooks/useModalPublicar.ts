"use client";

import { useEffect, useState } from "react";
import { publicacionService } from "@/services/publicacionService";
import { Publicacion } from "@/types/publicacion";
import { formatCommaSeparatedHashtags } from "@/lib/utils";

const MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

// Helper para limpiar/normalizar el string de la categoría antes de enviarlo
const normalizeCategoria = (cat: string | undefined | null): string => {
  if (!cat) return "";
  const trimmed = cat.trim();
  return trimmed.startsWith("#") ? trimmed : `#${trimmed}`;
};

interface UseModalPublicarProps {
  // Si se recibe, el formulario edita esa publicación en lugar de crear una nueva
  publicacion?: Publicacion;
  onSuccess: (publicacion: Publicacion) => void;
  onClose: () => void;
}

export function useModalPublicar({
  publicacion,
  onSuccess,
  onClose,
}: UseModalPublicarProps) {
  const categoriaInicial = normalizeCategoria(publicacion?.categoriaNombre);

  const [titulo, setTitulo] = useState(publicacion?.titulo ?? "");
  const [descripcion, setDescripcion] = useState(
    publicacion?.descripcion ?? ""
  );
  const [categoria, setCategoria] = useState<string>(categoriaInicial);
  const [archivo, setArchivo] = useState<File | null>(null);
  const [vistaPreviaArchivo, setVistaPreviaArchivo] = useState<string | null>(
    null
  );
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);
  const [latitud, setLatitud] = useState<number | null>(
    publicacion?.latitud ?? null
  );
  const [longitud, setLongitud] = useState<number | null>(
    publicacion?.longitud ?? null
  );
  const [direccion, setDireccion] = useState<string>(
    publicacion?.direccion ?? ""
  );

  useEffect(() => {
    if (!archivo) {
      setVistaPreviaArchivo(null);
      return;
    }

    const url = URL.createObjectURL(archivo);
    setVistaPreviaArchivo(url);
    return () => URL.revokeObjectURL(url);
  }, [archivo]);

  const resetForm = () => {
    setTitulo("");
    setDescripcion("");
    setCategoria("");
    setArchivo(null);
    setError("");
    setCargando(false);
    setLatitud(null);
    setLongitud(null);
    setDireccion("");
  };

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) {
      setArchivo(null);
      return;
    }

    if (file.size > MAX_SIZE_BYTES) {
      setError(
        "La imagen supera el tamaño máximo permitido (5 MB). Selecciona una más liviana."
      );
      e.target.value = "";
      setArchivo(null);
      return;
    }

    setError("");
    setArchivo(file);
  };

  const handleCategoriaBlur = () => {
    // Sin cambios no se reformatea: el formateador unificaría los espacios de
    // una categoría ya guardada (ej: "#Viaje #Relax") y la convertiría en otra.
    if (categoria === categoriaInicial) return;
    setCategoria(formatCommaSeparatedHashtags(categoria));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!publicacion && !archivo) {
      setError("La imagen es obligatoria.");
      return;
    }

    setCargando(true);
    setError("");

    const datos = {
      titulo,
      descripcion,
      categoria: normalizeCategoria(categoria) || null,
      latitud: latitud ?? undefined,
      longitud: longitud ?? undefined,
      direccion: direccion || null,
    };

    try {
      const guardada = publicacion
        ? await publicacionService.editar(publicacion.id, datos, archivo)
        : await publicacionService.crear(datos, archivo as File);
      resetForm();
      onSuccess(guardada);
      onClose();
    } catch (err: any) {
      setCargando(false);

      if (err.response?.status === 413) {
        setError(
          "La imagen es demasiado grande. El tamaño máximo permitido es de 5 MB."
        );
      } else if (err.response?.data?.error) {
        setError(err.response.data.error);
      } else if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError(
          publicacion
            ? "Ocurrió un error inesperado al guardar los cambios."
            : "Ocurrió un error inesperado al publicar."
        );
      }
    }
  };

  const handleClose = () => {
    resetForm();
    onClose();
  };

  const setArchivoFromCamera = (file: File) => {
    setError("");
    setArchivo(file);
  };

  const descartarArchivo = () => {
    setArchivo(null);
  };

  return {
    vistaPreviaArchivo,
    descartarArchivo,
    titulo,
    setTitulo,
    descripcion,
    setDescripcion,
    categoria,
    setCategoria,
    handleCategoriaBlur,
    error,
    cargando,
    handleFileSelect,
    handleSubmit,
    handleClose,
    setArchivoFromCamera,
    latitud,
    setLatitud,
    longitud,
    setLongitud,
    direccion,
    setDireccion,
  };
}
