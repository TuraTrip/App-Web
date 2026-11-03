"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/authStore";
import { Publicacion } from "@/types/publicacion";
import ModalPublicar from "@/components/publicaciones/ModalPublicar";
import UserAvatar from "@/components/common/UserAvatar";
import Toast from "@/components/common/Toast";

export default function Navbar() {
  const { usuario, logout } = useAuthStore();
  const router = useRouter();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [mostrarToast, setMostrarToast] = useState(false);
  const [postIdCreado, setPostIdCreado] = useState<number | null>(null);

  const handleLogout = () => {
    logout();
    router.push("/login");
  };

  const handlePublicacionExitosas = (publicacion: Publicacion) => {
    setPostIdCreado(publicacion.id);
    setMostrarToast(true);
    window.dispatchEvent(new Event("postCreado"));
  };

  return (
    <>
      <nav className="sticky top-0 z-40 border-b border-gray-200 dark:border-slate-800 px-6 py-3 flex justify-between items-center bg-white/80 dark:bg-slate-900/80 backdrop-blur-md shadow-sm">
        <Link
          href="/"
          className="font-black text-xl text-[hsl(174_72%_40%)] tracking-wider"
        >
          TuraTrip
        </Link>

        <div className="flex items-center gap-4">
          {usuario ? (
            <>
              <Link
                href="/perfil"
                className="flex items-center gap-2.5 px-3 py-1.5 rounded-full hover:bg-gray-100 dark:hover:bg-slate-800 transition"
              >
                <UserAvatar
                  key={usuario.fotoPerfil || "no-foto-nav"}
                  fotoPerfil={usuario.fotoPerfil}
                  nombre={usuario.nombre}
                  size="md"
                />
                <span className="text-sm font-semibold text-gray-800 dark:text-gray-200 hidden sm:inline">
                  {usuario.nombre}
                </span>
              </Link>

              <button
                onClick={() => setIsModalOpen(true)}
                title="Publicar un lugar"
                className="flex items-center justify-center w-9 h-9 bg-[hsl(174_72%_40%)] text-white rounded-full shadow-md hover:opacity-90 transition transform hover:scale-105 cursor-pointer"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-4 w-4"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                  strokeWidth={3}
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M12 4v16m8-8H4"
                  />
                </svg>
              </button>

              <button
                onClick={handleLogout}
                className="text-red-500 text-xs font-semibold hover:underline px-2 py-1 cursor-pointer"
              >
                Salir
              </button>
            </>
          ) : (
            <Link
              href="/login"
              className="bg-[hsl(174_72%_40%)] text-white px-4 py-2 rounded-xl text-sm font-medium hover:opacity-90 transition shadow-sm"
            >
              Iniciar sesión
            </Link>
          )}
        </div>
      </nav>

      <ModalPublicar
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={handlePublicacionExitosas}
      />

      <Toast
        mostrar={mostrarToast}
        mensaje="¡Tu publicación se envió con éxito!"
        onClose={() => setMostrarToast(false)}
        accion={{
          label: "Ver",
          onClick: () => {
            if (postIdCreado) router.push(`/publicaciones/${postIdCreado}`);
          },
        }}
      />
    </>
  );
}
