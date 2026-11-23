"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import FeedPublicaciones from "@/components/publicaciones/FeedPublicaciones";
import TarjetaPerfilHeader from "./TarjetaPerfilHeader";
import ModalImagenExpandida from "@/components/common/ModalImagenExpandida";
import { usePerfilVista } from "@/hooks/usePerfilVista";

interface PerfilVistaProps {
  usuarioIdObjetivo?: number;
}

export default function PerfilVista({ usuarioIdObjetivo }: PerfilVistaProps) {
  const router = useRouter();
  const {
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
  } = usePerfilVista(usuarioIdObjetivo);

  if (loading) {
    return (
      <div className="text-center py-12 text-gray-500 dark:text-gray-400 font-medium animate-pulse">
        Cargando perfil...
      </div>
    );
  }

  if (error || !perfil) {
    return (
      <div className="max-w-md mx-auto text-center py-12 space-y-4">
        <p className="text-gray-600 dark:text-gray-400 text-sm">
          {error || "Usuario no encontrado"}
        </p>
        <button
          onClick={() => router.back()}
          className="text-[hsl(174_72%_40%)] font-semibold hover:underline cursor-pointer text-sm"
        >
          ← Volver atrás
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto p-4 space-y-8">
      {/* BOTÓN VOLVER (Sólo en perfil ajeno) */}
      {!esMiPerfil && (
        <button
          onClick={() => router.back()}
          className="text-sm text-gray-600 dark:text-gray-400 hover:text-slate-900 dark:hover:text-white transition inline-flex items-center gap-1 font-medium cursor-pointer"
        >
          ← Volver
        </button>
      )}

      {/* CABECERA Y FOTO DEL PERFIL */}
      <TarjetaPerfilHeader
        perfil={perfil}
        esMiPerfil={esMiPerfil}
        onPerfilActualizado={handlePerfilActualizado}
        onExpandImage={setImagenExpandida}
      />

      {/* SECCIÓN DE PUBLICACIONES */}
      <div className="bg-white dark:bg-slate-900 p-8 rounded-2xl shadow-sm border border-gray-200 dark:border-slate-800">
        <h2 className="text-xl font-bold mb-6 text-slate-900 dark:text-white">
          {esMiPerfil
            ? "Mis Publicaciones"
            : `Publicaciones de ${perfil.nombre}`}
        </h2>

        <FeedPublicaciones
          publicaciones={publicaciones}
          onExpandImage={setImagenExpandida}
          mensajeVacio={
            esMiPerfil
              ? "Aún no has compartido ningún lugar en Buenaventura. ¡Anímate a publicar uno!"
              : "Este usuario aún no ha compartido ninguna publicación en Buenaventura."
          }
          usuarioActualId={usuarioActual?.id}
          usuarioRoles={usuarioActual?.roles}
          onDeleteSuccess={handleDeleteSuccess}
          onEditSuccess={handleEditSuccess}
          hasMore={hasMore}
          loadingMore={loadingMore}
          onLoadMore={fetchNextPage}
        />
      </div>

      {/* SECCIÓN DE COMENTARIOS */}
      {comentarios.length > 0 && (
        <div className="bg-white dark:bg-slate-900 p-8 rounded-2xl shadow-sm border border-gray-200 dark:border-slate-800">
          <h2 className="text-xl font-bold mb-6 text-slate-900 dark:text-white">
            {esMiPerfil ? "Mis Comentarios" : `Comentarios de ${perfil.nombre}`}
          </h2>

          <div className="space-y-3">
            {comentarios.map((c) => (
              <Link
                key={c.id}
                href={`/publicaciones/${c.publicacionId}`}
                className="block rounded-xl border border-gray-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 px-4 py-3 hover:border-[hsl(174_72%_40%)] transition"
              >
                <p className="text-sm text-gray-800 dark:text-gray-100">
                  {c.contenido}
                </p>
                <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                  En &quot;{c.publicacionTitulo}&quot; •{" "}
                  {new Date(c.fechaCreacion).toLocaleString()}
                </p>
              </Link>
            ))}
          </div>
        </div>
      )}

      {/* MODAL AMPLIAR IMAGEN */}
      <ModalImagenExpandida
        imagenUrl={imagenExpandida}
        onClose={() => setImagenExpandida(null)}
      />
    </div>
  );
}
