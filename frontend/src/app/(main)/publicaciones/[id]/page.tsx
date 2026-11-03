"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { publicacionService } from "@/services/publicacionService";
import { Publicacion } from "@/types/publicacion";
import TarjetaPublicacion from "@/components/publicaciones/TarjetaPublicacion";
import ModalImagenExpandida from "@/components/common/ModalImagenExpandida";
import { useAuthStore } from "@/store/authStore";

export default function DetallePublicacionPage() {
  const { id } = useParams();
  const router = useRouter();
  const { usuario } = useAuthStore();

  const [publicacion, setPublicacion] = useState<Publicacion | null>(null);
  const [loading, setLoading] = useState(true);
  const [imagenExpandida, setImagenExpandida] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;

    setLoading(true);
    publicacionService
      .getPorId(id as string)
      .then((data) => setPublicacion(data))
      .catch((err) => {
        console.error("Error al cargar la publicación:", err);
        setPublicacion(null);
      })
      .finally(() => setLoading(false));
  }, [id]);

  const handleDeleteSuccess = () => {
    router.back();
  };

  if (loading) {
    return (
      <div className="text-center py-12 text-gray-500 dark:text-gray-400 font-medium">
        Cargando publicación...
      </div>
    );
  }

  if (!publicacion) {
    return (
      <div className="text-center py-12 space-y-4">
        <p className="text-gray-600 dark:text-gray-400 text-sm">
          La publicación no existe o fue eliminada.
        </p>
        <Link
          href="/"
          className="text-[hsl(174_72%_40%)] font-semibold hover:underline text-sm inline-block"
        >
          Volver al feed principal
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-xl mx-auto space-y-6">
      <button
        type="button"
        onClick={() => router.back()}
        className="text-sm text-gray-600 dark:text-gray-400 hover:text-slate-900 dark:hover:text-white transition inline-flex items-center gap-1 font-medium cursor-pointer"
      >
        ← Volver
      </button>

      <TarjetaPublicacion
        publicacion={publicacion}
        onExpandImage={setImagenExpandida}
        usuarioActualId={usuario?.id}
        usuarioRoles={usuario?.roles}
        onDeleteSuccess={handleDeleteSuccess}
        onEditSuccess={setPublicacion}
      />

      <ModalImagenExpandida
        imagenUrl={imagenExpandida}
        onClose={() => setImagenExpandida(null)}
      />
    </div>
  );
}
