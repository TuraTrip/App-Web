"use client";

import Image from "next/image";
import Link from "next/link";
import BotonOpcionesPublicacion from "./BotonOpcionesPublicacion";
import { formatDate, getProxyImageUrl } from "@/lib/utils";
import { Publicacion } from "@/types/publicacion";

interface CabeceraAutorProps {
  publicacion: Publicacion;
  autorId: number;
  autorNombre?: string;
  autorFotoPerfil?: string | null;
  fechaCreacion: string;
  esPropietario: boolean;
  esAdmin: boolean;
  onDeleteSuccess?: (id: number) => void;
  onEditSuccess?: (publicacion: Publicacion) => void;
}

export default function CabeceraAutor({
  publicacion,
  autorId,
  autorNombre,
  autorFotoPerfil,
  fechaCreacion,
  esPropietario,
  esAdmin,
  onDeleteSuccess,
  onEditSuccess,
}: CabeceraAutorProps) {
  const perfilUrl = esPropietario ? "/perfil" : `/perfil/${autorId}`;

  // Normalizamos la foto de perfil del autor
  const srcProxy = getProxyImageUrl(autorFotoPerfil);

  return (
    <header className="p-4 flex items-center justify-between border-b border-gray-100 dark:border-slate-800/60">
      <div className="flex items-center gap-3">
        {/* Link Avatar: Detiene la propagación para ir solo al perfil */}
        <Link
          href={perfilUrl}
          onClick={(e) => e.stopPropagation()}
          className="w-10 h-10 rounded-full overflow-hidden bg-gray-100 dark:bg-slate-800 flex-shrink-0 border border-gray-200 dark:border-slate-700 hover:border-[hsl(174_72%_40%)] transition relative block"
          title={`Ver perfil de ${autorNombre || "Autor"}`}
        >
          {srcProxy ? (
            <Image
              src={srcProxy}
              alt={autorNombre || "Foto de perfil"}
              fill
              sizes="40px"
              className="object-cover"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-xs font-bold text-gray-500 dark:text-slate-400">
              {autorNombre ? autorNombre.charAt(0).toUpperCase() : "U"}
            </div>
          )}
        </Link>

        <div>
          {/* Link Nombre: Detiene la propagación para ir solo al perfil */}
          <Link
            href={perfilUrl}
            onClick={(e) => e.stopPropagation()}
            className="text-sm font-semibold text-slate-900 dark:text-white hover:underline hover:text-[hsl(174_72%_40%)] transition-colors block"
          >
            {autorNombre || "Usuario Anónimo"}
          </Link>
          <span className="text-[11px] text-gray-500 dark:text-gray-400 block">
            Compartido el {formatDate(fechaCreacion)}
          </span>
        </div>
      </div>

      {/* Menú de Opciones */}
      <BotonOpcionesPublicacion
        publicacion={publicacion}
        esPropietario={esPropietario}
        esAdmin={esAdmin}
        onDeleteSuccess={onDeleteSuccess}
        onEditSuccess={onEditSuccess}
      />
    </header>
  );
}
