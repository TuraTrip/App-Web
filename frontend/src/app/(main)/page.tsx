// app/(main)/page.tsx
"use client";

import { useEffect, useState, useCallback } from "react";
import { publicacionService } from "@/services/publicacionService";
import { usuarioService } from "@/services/usuarioService";
import { Publicacion, PageResponse } from "@/types/publicacion";
import { Perfil } from "@/types/usuario";
import FeedPublicaciones from "@/components/publicaciones/FeedPublicaciones";
import ModalImagenExpandida from "@/components/common/ModalImagenExpandida";

/**
 * Determina si quedan páginas pendientes por cargar.
 * Prioriza la propiedad 'last' de Spring Boot y usa 'number' vs 'totalPages' como respaldo.
 */
function determinarSiHayMas<T>(pageData: PageResponse<T>): boolean {
  if (!pageData.content || pageData.content.length === 0) {
    return false;
  }
  if (typeof pageData.last === "boolean") {
    return !pageData.last;
  }
  return pageData.number + 1 < pageData.totalPages;
}

export default function MainPage() {
  const [publicaciones, setPublicaciones] = useState<Publicacion[]>([]);
  const [perfil, setPerfil] = useState<Perfil | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [imagenExpandida, setImagenExpandida] = useState<string | null>(null);

  // Carga inicial (Página 0 y Perfil)
  useEffect(() => {
    const fetchData = async () => {
      try {
        const [resPubs, resPerfil] = await Promise.allSettled([
          publicacionService.getFeed(0, 10),
          usuarioService.getPerfil(),
        ]);

        if (resPubs.status === "fulfilled") {
          const pageData = resPubs.value;
          setPublicaciones(pageData.content || []);
          setHasMore(determinarSiHayMas(pageData));
        }

        if (resPerfil.status === "fulfilled") {
          setPerfil(resPerfil.value);
        }
      } catch (err: unknown) {
        console.error("Error al cargar la página principal:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // Carga de páginas siguientes para Scroll Infinito
  const fetchNextPage = useCallback(async () => {
    if (loadingMore || !hasMore) return;

    setLoadingMore(true);
    const nextPage = page + 1;

    try {
      const pageData = await publicacionService.getFeed(nextPage, 10);

      setPublicaciones((prev) => [...prev, ...(pageData.content || [])]);
      setPage(nextPage);
      setHasMore(determinarSiHayMas(pageData));
    } catch (err) {
      console.error("Error al cargar más publicaciones:", err);
    } finally {
      setLoadingMore(false);
    }
  }, [page, loadingMore, hasMore]);

  const handleDeleteSuccess = (idEliminado: number) => {
    setPublicaciones((prev) => prev.filter((p) => p.id !== idEliminado));
  };

  const handleEditSuccess = (actualizada: Publicacion) => {
    setPublicaciones((prev) =>
      prev.map((p) => (p.id === actualizada.id ? actualizada : p))
    );
  };

  if (loading) {
    return (
      <div className="text-center py-12 text-gray-500 dark:text-gray-400 font-medium animate-pulse">
        Cargando publicaciones del Mundo...
      </div>
    );
  }

  return (
    <div className="max-w-xl mx-auto space-y-6">
      <h1 className="text-2xl font-black text-slate-900 dark:text-slate-100 tracking-tight text-center">
        Descubre el Mundo
      </h1>

      {/* FEED DE PUBLICACIONES */}
      <FeedPublicaciones
        publicaciones={publicaciones}
        onExpandImage={setImagenExpandida}
        mensajeVacio="No hay publicaciones disponibles todavía. ¡Sé el primero en compartir un lugar!"
        usuarioActualId={perfil?.id}
        usuarioRoles={perfil?.roles}
        onDeleteSuccess={handleDeleteSuccess}
        onEditSuccess={handleEditSuccess}
        hasMore={hasMore}
        loadingMore={loadingMore}
        onLoadMore={fetchNextPage}
      />

      {/* MODAL AMPLIAR IMAGEN */}
      <ModalImagenExpandida
        imagenUrl={imagenExpandida}
        onClose={() => setImagenExpandida(null)}
      />
    </div>
  );
}
