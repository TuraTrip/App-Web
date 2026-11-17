"use client";

import { useState, useEffect, useCallback } from "react";
import { Perfil } from "@/types/usuario";
import { Publicacion, Comentario, PageResponse } from "@/types/publicacion";
import { useAuthStore } from "@/store/authStore";
import { usuarioService } from "@/services/usuarioService";
import { publicacionService } from "@/services/publicacionService";
import { comentarioService } from "@/services/comentarioService";

function determinarSiHayMas<T>(pageData: PageResponse<T>): boolean {
  if (!pageData.content || pageData.content.length === 0) {
    return false;
  }
  if (typeof pageData.last === "boolean") {
    return !pageData.last;
  }
  return pageData.number + 1 < pageData.totalPages;
}

export function usePerfilVista(usuarioIdObjetivo?: string | number) {
  const { usuario: usuarioActual, actualizarUsuario } = useAuthStore();

  const esMiPerfil: boolean = Boolean(
    !usuarioIdObjetivo ||
    (usuarioActual?.id &&
      String(usuarioActual.id) === String(usuarioIdObjetivo))
  );

  const [perfil, setPerfil] = useState<Perfil | null>(null);
  const [publicaciones, setPublicaciones] = useState<Publicacion[]>([]);
  const [comentarios, setComentarios] = useState<Comentario[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [imagenExpandida, setImagenExpandida] = useState<string | null>(null);

  // Carga inicial (Página 0)
  const cargarDatos = useCallback(async () => {
    setLoading(true);
    setError(null);
    setPage(0);

    try {
      if (esMiPerfil) {
        const [resPerfil, resPublicaciones] = await Promise.all([
          usuarioService.getPerfil(),
          publicacionService.getMisPublicaciones(0, 10),
        ]);

        setPerfil(resPerfil);
        setPublicaciones(resPublicaciones.content || []);
        setHasMore(determinarSiHayMas(resPublicaciones));

        actualizarUsuario({
          nombre: resPerfil.nombre,
          fotoPerfil: resPerfil.fotoPerfil,
        });

        comentarioService
          .listarPorUsuario(resPerfil.id, 0, 10)
          .then((res) => setComentarios(res.content || []))
          .catch((err) =>
            console.error("Error al cargar comentarios del perfil:", err)
          );
      } else if (usuarioIdObjetivo) {
        const [resPerfil, resPublicaciones] = await Promise.all([
          usuarioService.getPerfilPorId(usuarioIdObjetivo),
          publicacionService.getPublicacionesPorUsuario(
            usuarioIdObjetivo,
            0,
            10
          ),
        ]);

        setPerfil(resPerfil);
        setPublicaciones(resPublicaciones.content || []);
        setHasMore(determinarSiHayMas(resPublicaciones));

        comentarioService
          .listarPorUsuario(usuarioIdObjetivo, 0, 10)
          .then((res) => setComentarios(res.content || []))
          .catch((err) =>
            console.error("Error al cargar comentarios del perfil:", err)
          );
      }
    } catch (err: any) {
      console.error("Error al cargar perfil:", err);
      if (err.response?.status === 404) {
        setError("El perfil buscado no existe o no se encuentra disponible.");
      } else {
        setError("Error al obtener la información del perfil.");
      }
    } finally {
      setLoading(false);
    }
  }, [esMiPerfil, usuarioIdObjetivo, actualizarUsuario]);

  // Carga de las siguientes páginas para Scroll Infinito
  const fetchNextPage = useCallback(async () => {
    if (loadingMore || !hasMore) return;

    setLoadingMore(true);
    const nextPage = page + 1;

    try {
      let pageData: PageResponse<Publicacion>;

      if (esMiPerfil) {
        pageData = await publicacionService.getMisPublicaciones(nextPage, 10);
      } else if (usuarioIdObjetivo) {
        pageData = await publicacionService.getPublicacionesPorUsuario(
          usuarioIdObjetivo,
          nextPage,
          10
        );
      } else {
        return;
      }

      setPublicaciones((prev) => [...prev, ...(pageData.content || [])]);
      setPage(nextPage);
      setHasMore(determinarSiHayMas(pageData));
    } catch (err) {
      console.error("Error al cargar más publicaciones del perfil:", err);
    } finally {
      setLoadingMore(false);
    }
  }, [esMiPerfil, usuarioIdObjetivo, page, loadingMore, hasMore]);

  const revalidarPublicaciones = useCallback(async () => {
    try {
      if (esMiPerfil) {
        const resPublicaciones = await publicacionService.getMisPublicaciones(
          0,
          10
        );
        setPublicaciones(resPublicaciones.content || []);
        setPage(0);
        setHasMore(determinarSiHayMas(resPublicaciones));
      }
    } catch (err) {
      console.error("Error al revalidar publicaciones:", err);
    }
  }, [esMiPerfil]);

  useEffect(() => {
    cargarDatos();
  }, [cargarDatos]);

  useEffect(() => {
    if (esMiPerfil) {
      const handlePostCreado = () => revalidarPublicaciones();
      window.addEventListener("postCreado", handlePostCreado);
      return () => window.removeEventListener("postCreado", handlePostCreado);
    }
  }, [esMiPerfil, revalidarPublicaciones]);

  const handleDeleteSuccess = (idEliminado: number) => {
    setPublicaciones((prev) => prev.filter((p) => p.id !== idEliminado));
  };

  const handleEditSuccess = (actualizada: Publicacion) => {
    setPublicaciones((prev) =>
      prev.map((p) => (p.id === actualizada.id ? actualizada : p))
    );
  };

  const handlePerfilActualizado = (nuevoPerfil: Perfil) => {
    setPerfil(nuevoPerfil);
    actualizarUsuario({
      nombre: nuevoPerfil.nombre,
      fotoPerfil: nuevoPerfil.fotoPerfil,
    });
  };

  return {
    perfil,
    publicaciones,
    comentarios,
    loading,
    loadingMore,
    hasMore,
    error,
    esMiPerfil,
    usuarioActual,
    imagenExpandida,
    setImagenExpandida,
    handleDeleteSuccess,
    handleEditSuccess,
    handlePerfilActualizado,
    fetchNextPage,
  };
}
