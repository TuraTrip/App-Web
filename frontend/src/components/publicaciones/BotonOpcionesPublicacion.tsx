"use client";

import { useState, useRef, useEffect } from "react";
import { publicacionService } from "@/services/publicacionService";
import { Publicacion } from "@/types/publicacion";
import ModalPublicar from "./ModalPublicar";

interface BotonOpcionesPublicacionProps {
  publicacion: Publicacion;
  esPropietario: boolean;
  esAdmin?: boolean;
  onDeleteSuccess?: (id: number) => void;
  onEditSuccess?: (publicacion: Publicacion) => void;
}

export default function BotonOpcionesPublicacion({
  publicacion,
  esPropietario,
  esAdmin = false,
  onDeleteSuccess,
  onEditSuccess,
}: BotonOpcionesPublicacionProps) {
  const [menuAbierto, setMenuAbierto] = useState(false);
  const [mostrarModal, setMostrarModal] = useState(false);
  const [mostrarEdicion, setMostrarEdicion] = useState(false);
  const [eliminando, setEliminando] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  const publicacionId = publicacion.id;
  const puedeEliminar = esPropietario || esAdmin;

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setMenuAbierto(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape" && mostrarModal) {
        setMostrarModal(false);
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [mostrarModal]);

  if (!puedeEliminar) return null;

  const ejecutarEliminacion = async () => {
    setEliminando(true);
    try {
      await publicacionService.eliminar(publicacionId);
      if (onDeleteSuccess) {
        onDeleteSuccess(publicacionId);
      }
    } catch (error) {
      console.error("Error al eliminar la publicación:", error);
      alert("No se pudo eliminar la publicación.");
    } finally {
      setEliminando(false);
      setMostrarModal(false);
      setMenuAbierto(false);
    }
  };

  return (
    <>
      <div
        className="relative"
        ref={menuRef}
        onClick={(e) => e.stopPropagation()}
      >
        <button
          onClick={() => setMenuAbierto(!menuAbierto)}
          className="p-1.5 rounded-full text-gray-400 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition cursor-pointer"
          title="Opciones"
          aria-label="Opciones de publicación"
        >
          <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
            <path d="M10 6a2 2 0 110-4 2 2 0 010 4zM10 12a2 2 0 110-4 2 2 0 010 4zM10 18a2 2 0 110-4 2 2 0 010 4z" />
          </svg>
        </button>

        {menuAbierto && (
          <div className="absolute right-0 mt-2 w-48 bg-white dark:bg-slate-900 border border-gray-200 dark:border-slate-800 rounded-xl shadow-xl py-1 z-20 transition-all">
            {esPropietario && (
              <button
                onClick={() => {
                  setMenuAbierto(false);
                  setMostrarEdicion(true);
                }}
                className="w-full text-left px-4 py-2.5 text-xs font-semibold text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition flex items-center gap-2 cursor-pointer"
              >
                <svg
                  className="w-4 h-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                  />
                </svg>
                Editar publicación
              </button>
            )}
            <button
              onClick={() => {
                setMenuAbierto(false);
                setMostrarModal(true);
              }}
              className="w-full text-left px-4 py-2.5 text-xs font-semibold text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-500/10 transition flex items-center gap-2 cursor-pointer"
            >
              <svg
                className="w-4 h-4"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                />
              </svg>
              Eliminar publicación
            </button>
          </div>
        )}
      </div>

      {/* Se monta al abrir para que el formulario cargue los datos más recientes */}
      {mostrarEdicion && (
        <div onClick={(e) => e.stopPropagation()}>
          <ModalPublicar
            isOpen
            publicacion={publicacion}
            onClose={() => setMostrarEdicion(false)}
            onSuccess={(actualizada) => onEditSuccess?.(actualizada)}
          />
        </div>
      )}

      {mostrarModal && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 dark:bg-black/80 backdrop-blur-sm animate-fade-in"
          onClick={(e) => e.stopPropagation()}
        >
          <div className="bg-white dark:bg-slate-900 border border-gray-200 dark:border-slate-800 rounded-2xl p-6 max-w-md w-full shadow-2xl space-y-5 transition-colors">
            <div className="flex items-start gap-4">
              <div className="w-11 h-11 rounded-full bg-red-100 dark:bg-red-500/10 border border-red-200 dark:border-red-500/20 flex items-center justify-center text-red-600 dark:text-red-400 flex-shrink-0">
                <svg
                  className="w-6 h-6"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                  />
                </svg>
              </div>
              <div className="space-y-1">
                <h3 className="text-lg font-bold text-slate-900 dark:text-white">
                  ¿Eliminar publicación?
                </h3>
                <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
                  Esta acción eliminará el contenido permanentemente y no se
                  podrá deshacer.
                </p>
              </div>
            </div>

            <div className="flex items-center justify-end gap-3 pt-2 border-t border-gray-100 dark:border-slate-800/80">
              <button
                type="button"
                onClick={() => setMostrarModal(false)}
                disabled={eliminando}
                className="px-4 py-2 text-xs font-semibold text-slate-700 dark:text-slate-300 hover:text-slate-900 dark:hover:text-white bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 rounded-xl transition disabled:opacity-50 cursor-pointer"
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={ejecutarEliminacion}
                disabled={eliminando}
                className="px-4 py-2 text-xs font-semibold text-white bg-red-600 hover:bg-red-500 rounded-xl transition flex items-center gap-2 disabled:opacity-50 cursor-pointer shadow-md shadow-red-600/20"
              >
                {eliminando ? "Eliminando..." : "Sí, eliminar"}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
