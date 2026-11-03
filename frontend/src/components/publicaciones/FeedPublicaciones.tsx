"use client";

import { useEffect, useRef } from "react";
import { Publicacion } from "@/types/publicacion";
import TarjetaPublicacion from "./TarjetaPublicacion";
import { Rol } from "@/types/usuario";

interface FeedPublicacionesProps {
  publicaciones: Publicacion[];
  onExpandImage: (imagenUrl: string) => void;
  mensajeVacio?: string;
  usuarioActualId?: number;
  usuarioRoles?: (string | Rol)[];
  onDeleteSuccess?: (id: number) => void;
  onEditSuccess?: (publicacion: Publicacion) => void;
  hasMore?: boolean;
  loadingMore?: boolean;
  onLoadMore?: () => void;
}

export default function FeedPublicaciones({
  publicaciones,
  onExpandImage,
  mensajeVacio = "No hay publicaciones disponibles todavía. ¡Sé el primero en compartir un lugar!",
  usuarioActualId,
  usuarioRoles,
  onDeleteSuccess,
  onEditSuccess,
  hasMore = false,
  loadingMore = false,
  onLoadMore,
}: FeedPublicacionesProps) {
  const observerTargetRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const observerTarget = observerTargetRef.current;
    if (!observerTarget || !onLoadMore || !hasMore || loadingMore) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          onLoadMore();
        }
      },
      { threshold: 0.3 }
    );

    observer.observe(observerTarget);

    return () => {
      if (observerTarget) observer.unobserve(observerTarget);
    };
  }, [onLoadMore, hasMore, loadingMore]);

  if (!publicaciones || publicaciones.length === 0) {
    return (
      <div className="text-center py-12 bg-white dark:bg-slate-900 rounded-2xl border border-gray-200 dark:border-slate-800 p-8 shadow-sm">
        <p className="text-gray-500 dark:text-gray-400 text-sm">
          {mensajeVacio}
        </p>
      </div>
    );
  }

  return (
    <section className="space-y-6">
      {publicaciones.map((pub, index) => (
        <TarjetaPublicacion
          key={pub.id}
          publicacion={pub}
          priority={index === 0}
          onExpandImage={onExpandImage}
          usuarioActualId={usuarioActualId}
          usuarioRoles={usuarioRoles}
          onDeleteSuccess={onDeleteSuccess}
          onEditSuccess={onEditSuccess}
        />
      ))}

      {/* TRIGGER / LOADER INFERIOR PARA SCROLL INFINITO */}
      <div ref={observerTargetRef} className="py-6 text-center min-h-[50px]">
        {loadingMore && (
          <div className="flex items-center justify-center gap-2 text-xs font-semibold text-slate-500 dark:text-slate-400">
            <svg
              className="w-4 h-4 animate-spin text-[hsl(174_72%_40%)]"
              fill="none"
              viewBox="0 0 24 24"
            >
              <circle
                className="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                strokeWidth="4"
              />
              <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              />
            </svg>
            Cargando más lugares...
          </div>
        )}

        {!hasMore && publicaciones.length > 0 && (
          <p className="text-xs text-gray-400 dark:text-gray-600">
            ¡Has visto todas las publicaciones!
          </p>
        )}
      </div>
    </section>
  );
}
