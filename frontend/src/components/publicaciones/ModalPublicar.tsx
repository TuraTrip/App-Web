"use client";

import { useRef, useState } from "react";
import { useModalPublicar } from "@/hooks/useModalPublicar";
import { Publicacion } from "@/types/publicacion";
import { formatHashtags, getProxyImageUrl } from "@/lib/utils";
import CameraCapture from "@/components/common/CameraCapture";
import MapPicker from "@/components/common/MapPicker";

interface ModalPublicarProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (publicacion: Publicacion) => void;
  // Si se recibe, el modal edita esa publicación en lugar de crear una nueva
  publicacion?: Publicacion;
}

export default function ModalPublicar({
  isOpen,
  onClose,
  onSuccess,
  publicacion,
}: ModalPublicarProps) {
  const {
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
  } = useModalPublicar({ publicacion, onSuccess, onClose });

  const [cameraOpen, setCameraOpen] = useState(false);
  const [mapPickerKey, setMapPickerKey] = useState(0);
  const fileInputRef = useRef<HTMLInputElement>(null);

  if (!isOpen) return null;

  const esEdicion = Boolean(publicacion);
  const imagenVisible =
    vistaPreviaArchivo ?? getProxyImageUrl(publicacion?.imagen);

  // Limpia también el input para que pueda volver a elegirse el mismo archivo
  const handleDescartarFoto = () => {
    descartarArchivo();
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white dark:bg-slate-900 w-full max-w-lg rounded-2xl shadow-2xl border border-gray-200 dark:border-slate-800 relative animate-in fade-in zoom-in-95 duration-200">
        <div className="sticky top-0 z-10 bg-white dark:bg-slate-900 rounded-t-2xl px-6 pt-6 pb-4 flex items-start justify-between">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white pr-8">
            {esEdicion ? "Editar publicación" : "Comparte un lugar en el Mundo"}
          </h2>
          <button
            onClick={handleClose}
            className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 dark:hover:text-white p-1 rounded-full cursor-pointer transition-colors"
            aria-label="Cerrar modal"
          >
            ✕
          </button>
        </div>

        {error && (
          <div className="mx-6 mb-4 p-3 bg-red-100 text-red-700 dark:bg-red-950/50 dark:text-red-300 rounded-lg text-sm border border-red-200 dark:border-red-900 animate-in fade-in duration-150">
            {error}
          </div>
        )}

        <div className="max-h-[70vh] overflow-y-auto px-6">
          <form onSubmit={handleSubmit} className="space-y-4 pb-6">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-gray-600 dark:text-gray-400 mb-1">
                Título del lugar
              </label>
              <input
                type="text"
                value={titulo}
                onChange={(e) => setTitulo(e.target.value)}
                required
                maxLength={255}
                className="w-full p-3 rounded-xl border border-gray-300 dark:border-slate-700 bg-transparent text-gray-900 dark:text-white text-sm outline-none focus:ring-2 focus:ring-[hsl(174_72%_40%)]"
                placeholder="Ej: Playa Piangüita"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-gray-600 dark:text-gray-400 mb-1">
                Descripción
              </label>
              <textarea
                value={descripcion}
                onChange={(e) => setDescripcion(formatHashtags(e.target.value))}
                required
                maxLength={1000}
                rows={3}
                className="w-full p-3 rounded-xl border border-gray-300 dark:border-slate-700 bg-transparent text-gray-900 dark:text-white text-sm outline-none focus:ring-2 focus:ring-[hsl(174_72%_40%)] resize-none"
                placeholder="Cuéntanos qué tiene de especial..."
              />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-gray-600 dark:text-gray-400 mb-1">
                Categoría
              </label>
              <input
                type="text"
                value={categoria}
                onChange={(e) => setCategoria(e.target.value)}
                onBlur={handleCategoriaBlur}
                maxLength={50}
                className="w-full p-3 rounded-xl border border-gray-300 dark:border-slate-700 bg-transparent text-gray-900 dark:text-white text-sm outline-none focus:ring-2 focus:ring-[hsl(174_72%_40%)]"
                placeholder="#Viaje, #Relax, #Buenaventura"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-gray-600 dark:text-gray-400 mb-1">
                Lugar
              </label>
              <MapPicker
                key={mapPickerKey}
                latitud={latitud}
                longitud={longitud}
                direccion={direccion}
                onSelect={(data) => {
                  setLatitud(data.latitud);
                  setLongitud(data.longitud);
                  if (data.direccion) {
                    setDireccion(data.direccion);
                  }
                }}
              />
              {(latitud != null && longitud != null) || direccion ? (
                <button
                  type="button"
                  onClick={() => {
                    setLatitud(null);
                    setLongitud(null);
                    setDireccion("");
                    setMapPickerKey((k) => k + 1);
                  }}
                  className="mt-2 text-xs font-semibold text-red-600 hover:text-red-700 cursor-pointer"
                >
                  Quitar ubicación
                </button>
              ) : null}
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-gray-600 dark:text-gray-400 mb-1">
                {esEdicion ? "Cambiar foto" : "Foto"} (JPG, JPEG, PNG o WebP -
                Máx 5 MB)
              </label>
              {imagenVisible && (
                <div className="mb-3">
                  {/* eslint-disable-next-line @next/next/no-img-element -- la vista previa local es una URL blob */}
                  <img
                    src={imagenVisible}
                    alt={
                      vistaPreviaArchivo
                        ? "Vista previa de la foto seleccionada"
                        : "Foto actual de la publicación"
                    }
                    className="w-full max-h-56 object-contain rounded-xl bg-slate-100 dark:bg-slate-950 border border-gray-200 dark:border-slate-800"
                  />
                  <div className="mt-1 flex items-center justify-between gap-3">
                    <span className="text-xs text-gray-500 dark:text-gray-400">
                      {vistaPreviaArchivo
                        ? "Foto seleccionada"
                        : "Foto actual. Se conservará si no eliges otra."}
                    </span>
                    {vistaPreviaArchivo && (
                      <button
                        type="button"
                        onClick={handleDescartarFoto}
                        className="text-xs font-semibold text-red-600 hover:text-red-700 cursor-pointer"
                      >
                        {esEdicion ? "Conservar foto actual" : "Quitar foto"}
                      </button>
                    )}
                  </div>
                </div>
              )}
              <input
                ref={fileInputRef}
                type="file"
                accept="image/jpeg, image/png, image/webp"
                onChange={handleFileSelect}
                className="w-full text-sm text-gray-500 dark:text-gray-400 file:mr-4 file:py-2 file:px-4 file:rounded-xl file:border-0 file:text-xs file:font-semibold file:bg-teal-50 file:text-teal-700 dark:file:bg-teal-950 dark:file:text-teal-300 hover:file:bg-teal-100 cursor-pointer"
              />
              <div className="mt-2 flex items-center gap-3">
                <button
                  type="button"
                  onClick={() => setCameraOpen(true)}
                  className="px-3 py-2 text-sm bg-red-600 text-white rounded-xl hover:bg-red-500 transition"
                >
                  Usar cámara
                </button>
              </div>
            </div>
          </form>
        </div>

        <div className="sticky bottom-0 bg-white dark:bg-slate-900 p-6 pt-0">
          <div className="flex justify-end gap-3">
            <button
              type="button"
              onClick={handleClose}
              className="px-4 py-2.5 rounded-xl text-sm font-semibold text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-slate-800 transition cursor-pointer"
            >
              Cancelar
            </button>
            <button
              type="submit"
              onClick={handleSubmit as any}
              disabled={cargando}
              className="px-6 py-2.5 bg-[hsl(174_72%_40%)] text-white text-sm font-semibold rounded-xl hover:opacity-90 disabled:bg-gray-400 transition shadow-md cursor-pointer"
            >
              {esEdicion
                ? cargando
                  ? "Guardando..."
                  : "Guardar cambios"
                : cargando
                  ? "Publicando..."
                  : "Publicar"}
            </button>
          </div>
        </div>
      </div>
      {cameraOpen && (
        <CameraCapture
          onCapture={(file: File) => {
            setArchivoFromCamera(file);
            setCameraOpen(false);
          }}
          onClose={() => setCameraOpen(false)}
        />
      )}
    </div>
  );
}

// Render CameraCapture as portal/modal when requested
// (placed after component so it's easy to locate in file)
export function ModalPublicarWithCamera(props: ModalPublicarProps) {
  return <ModalPublicar {...props} />;
}
