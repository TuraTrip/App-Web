import apiClient from "@/lib/api-client";
import {
  Publicacion,
  PageResponse,
  PublicacionRequest,
} from "@/types/publicacion";

export const publicacionService = {
  getFeed: async (page = 0, size = 10) => {
    const res = await apiClient.get<PageResponse<Publicacion>>(
      `/v1/publicaciones?page=${page}&size=${size}`
    );
    return res.data;
  },

  getMisPublicaciones: async (page = 0, size = 10) => {
    const res = await apiClient.get<PageResponse<Publicacion>>(
      `/v1/publicaciones/mias?page=${page}&size=${size}`
    );
    return res.data;
  },

  getPublicacionesPorUsuario: async (
    usuarioId: number | string,
    page = 0,
    size = 10
  ) => {
    const res = await apiClient.get<PageResponse<Publicacion>>(
      `/v1/publicaciones/usuario/${usuarioId}?page=${page}&size=${size}`
    );
    return res.data;
  },

  getPorId: async (id: number | string) => {
    const res = await apiClient.get<Publicacion>(`/v1/publicaciones/${id}`);
    return res.data;
  },

  crear: async (datos: PublicacionRequest, archivo: File) => {
    const formData = new FormData();

    // Se define explícitamente el Content-Type application/json en el Blob
    const jsonBlob = new Blob([JSON.stringify(datos)], {
      type: "application/json",
    });

    formData.append("publicacion", jsonBlob);
    formData.append("archivo", archivo);

    const res = await apiClient.post<Publicacion>(
      "/v1/publicaciones",
      formData
    );
    return res.data;
  },

  // Si no se envía archivo, el backend conserva la foto actual
  editar: async (
    id: number | string,
    datos: PublicacionRequest,
    archivo?: File | null
  ) => {
    const formData = new FormData();

    const jsonBlob = new Blob([JSON.stringify(datos)], {
      type: "application/json",
    });

    formData.append("publicacion", jsonBlob);
    if (archivo) {
      formData.append("archivo", archivo);
    }

    const res = await apiClient.put<Publicacion>(
      `/v1/publicaciones/${id}`,
      formData
    );
    return res.data;
  },

  eliminar: async (id: number | string) => {
    await apiClient.delete(`/v1/publicaciones/${id}`);
  },
};

export default publicacionService;
