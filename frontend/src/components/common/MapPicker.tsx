"use client";

import { useState, useEffect, useRef } from "react";

const DEFAULT_CENTER: [number, number] = [4.711, -74.0721];

interface MapPickerProps {
  latitud?: number | null;
  longitud?: number | null;
  direccion?: string | null;
  onSelect: (ubicacion: {
    latitud: number;
    longitud: number;
    direccion?: string | null;
  }) => void;
}

export default function MapPicker({
  latitud,
  longitud,
  direccion,
  onSelect,
}: MapPickerProps) {
  const [query, setQuery] = useState(direccion || "");
  const [coords, setCoords] = useState<[number, number] | null>(
    latitud != null && longitud != null ? ([latitud, longitud] as [number, number]) : null
  );
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  // Solo se busca cuando el usuario cambia el texto. Así, abrir una publicación
  // con ubicación o volver a renderizar el formulario no la sobrescribe.
  const ultimaBusquedaRef = useRef(query);

  const mapCenter = coords || DEFAULT_CENTER;

  const mapSrc = `https://www.openstreetmap.org/export/embed.html?bbox=${
    mapCenter[1] - 0.01
  }%2C${mapCenter[0] - 0.01}%2C${mapCenter[1] + 0.01}%2C${
    mapCenter[0] + 0.01
  }&layer=mapnik&marker=${mapCenter[0]}%2C${mapCenter[1]}`;

  const [mapKey, setMapKey] = useState(0);

  useEffect(() => {
    if (query === ultimaBusquedaRef.current) return;

    if (timerRef.current) clearTimeout(timerRef.current);

    if (!query || query.trim().length < 3) {
      return;
    }

    timerRef.current = setTimeout(async () => {
      ultimaBusquedaRef.current = query;
      try {
        const url = new URL("https://nominatim.openstreetmap.org/search");
        url.searchParams.set("format", "json");
        url.searchParams.set("q", query.trim());
        url.searchParams.set("countrycodes", "co");
        url.searchParams.set("limit", "1");

        const res = await fetch(url.toString(), {
          headers: { Accept: "application/json" },
        });
        if (!res.ok) return;

        const data = (await res.json()) as Array<{
          lat: string;
          lon: string;
          display_name: string;
        }>;

        if (data.length > 0) {
          const lat = parseFloat(data[0].lat);
          const lon = parseFloat(data[0].lon);
          setCoords([lat, lon]);
          setMapKey((k) => k + 1);
          onSelect({
            latitud: lat,
            longitud: lon,
            direccion: data[0].display_name || query,
          });
        }
      } catch {
        // noop
      } finally {
        timerRef.current = null;
      }
    }, 500);

    return () => {
      if (timerRef.current) clearTimeout(timerRef.current);
    };
  }, [query, onSelect]);

  return (
    <div className="space-y-2">
      <input
        type="text"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        className="w-full p-3 rounded-xl border border-gray-300 dark:border-slate-700 bg-transparent text-gray-900 dark:text-white text-sm outline-none focus:ring-2 focus:ring-[hsl(174_72%_40%)]"
        placeholder="Buscar dirección o lugar..."
      />

      <div className="rounded-xl overflow-hidden border border-gray-200 dark:border-slate-800">
        <iframe
          key={`${mapCenter[0]}-${mapCenter[1]}`}
          title="Mapa OpenStreetMap"
          src={mapSrc}
          className="h-64 w-full border-0"
          loading="lazy"
        />
      </div>

      {coords && direccion && (
        <p className="text-xs text-gray-600 dark:text-gray-300 truncate">
          📍 {direccion}
        </p>
      )}
    </div>
  );
}
