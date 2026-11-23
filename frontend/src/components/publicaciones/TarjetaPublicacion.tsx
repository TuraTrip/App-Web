"use client";

import Image from "next/image";
import { useRouter, usePathname } from "next/navigation";
import { Publicacion } from "@/types/publicacion";
import { Rol } from "@/types/usuario";
import CabeceraAutor from "./CabeceraAutor";
import ComentariosPublicacion from "./ComentariosPublicacion";
import { getProxyImageUrl } from "@/lib/utils";

interface TarjetaPublicacionProps {
  publicacion: Publicacion;
  onExpandImage: (imagenUrl: string) => void;
  usuarioActualId?: number;
  usuarioRoles?: (string | Rol)[];
  onDeleteSuccess?: (id: number) => void;
  onEditSuccess?: (publicacion: Publicacion) => void;
  priority?: boolean;
}

export default function TarjetaPublicacion({
  publicacion,
  onExpandImage,
  usuarioActualId,
  usuarioRoles = [],
  onDeleteSuccess,
  onEditSuccess,
  priority = false,
}: TarjetaPublicacionProps) {
  const router = useRouter();
  const pathname = usePathname();

  // Normalizamos la URL de la imagen principal
  const srcProxy = getProxyImageUrl(publicacion.imagen);

  const yaEnDetalle = pathname === `/publicaciones/${publicacion.id}`;

  const esPropietario = Boolean(
    usuarioActualId && publicacion.autorId === usuarioActualId
  );

  const rolesNombres = usuarioRoles.map((r) =>
    typeof r === "string" ? r : r.nombre
  );
  const esAdmin =
    rolesNombres.includes("ROLE_ADMIN") || rolesNombres.includes("ADMIN");

  const handleCardClick = () => {
    if (!yaEnDetalle) {
      router.push(`/publicaciones/${publicacion.id}`);
    }
  };

  return (
    <article
      onClick={handleCardClick}
      className={`bg-white dark:bg-slate-900 rounded-2xl border border-gray-200 dark:border-slate-800 overflow-hidden shadow-sm transition-colors ${
        !yaEnDetalle ? "cursor-pointer hover:shadow-md" : ""
      }`}
    >
      {/* CABECERA (Redirige al detalle si se presiona en zonas neutras) */}
      <CabeceraAutor
        publicacion={publicacion}
        autorId={publicacion.autorId}
        autorNombre={publicacion.autorNombre}
        autorFotoPerfil={publicacion.autorFotoPerfil}
        fechaCreacion={publicacion.fechaCreacion}
        esPropietario={esPropietario}
        esAdmin={esAdmin}
        onDeleteSuccess={onDeleteSuccess}
        onEditSuccess={onEditSuccess}
      />

      {/* CONTENEDOR DE LA IMAGEN */}
      <div
        className="w-full relative h-[380px] sm:h-[450px] bg-slate-100 dark:bg-slate-950 overflow-hidden group cursor-pointer"
        onClick={(e) => {
          e.stopPropagation();
          onExpandImage(srcProxy || publicacion.imagen);
        }}
        title="Clic para ampliar imagen"
      >
        {srcProxy && (
          <Image
            src={srcProxy}
            alt={publicacion.titulo || "Imagen de la publicación"}
            fill
            sizes="(max-width: 640px) 100vw, (max-width: 1024px) 75vw, 600px"
            className="object-contain transition-transform duration-300 group-hover:scale-[1.01]"
            priority={priority}
          />
        )}
      </div>

      {/* CONTENIDO */}
      <div className="p-5">
        <div className="flex items-center justify-between gap-3 mb-3">
          <h2 className="text-lg font-bold text-slate-900 dark:text-white">
            {publicacion.titulo}
          </h2>
          {publicacion.categoriaNombre && (
            <span className="px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] rounded-full bg-slate-200 text-slate-700 dark:bg-slate-800 dark:text-slate-200">
              {publicacion.categoriaNombre}
            </span>
          )}
        </div>
        <p className="text-gray-600 dark:text-gray-300 mt-2 text-sm leading-relaxed">
          {publicacion.descripcion}
        </p>

        {publicacion.latitud != null && publicacion.longitud != null && (
          <a
            href={`https://www.openstreetmap.org/?mlat=${publicacion.latitud}&mlon=${publicacion.longitud}#map=16/${publicacion.latitud}/${publicacion.longitud}`}
            target="_blank"
            rel="noopener noreferrer"
            className="mt-3 inline-flex items-center gap-1 text-xs font-medium text-teal-700 dark:text-teal-300 hover:underline"
            onClick={(e) => e.stopPropagation()}
          >
            📍 {publicacion.direccion || "Ver ubicación en OpenStreetMap"}
          </a>
        )}

        <ComentariosPublicacion
          publicacionId={publicacion.id}
          usuarioRoles={usuarioRoles}
        />
      </div>
    </article>
  );
}
