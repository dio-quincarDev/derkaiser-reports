import { boot } from 'quasar/wrappers'
import axios from 'axios'

const API_CONSTANTS = {
  // URL base del backend
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  V1_ROUTE: '/v1',
  AUTH_ROUTE: '/auth',
  LOGIN_ROUTE: '/login',
  REFRESH_ROUTE: '/refresh',
  USERS_ROUTE: '/users',
}

let routerInstance;
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

const api = axios.create({
  baseURL: API_CONSTANTS.BASE_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
})

// Función para verificar si el token es válido (puedes adaptarla según tus necesidades)
function isValidToken(token) {
  // Ejemplo: verificar la expiración del token si el backend lo permite
  return !!token // Por ahora, asumimos que cualquier token no nulo es válido
}

// Interceptor para añadir el token JWT a cada solicitud
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken')
    if (token && isValidToken(token)) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// Interceptor para manejar errores globales
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Si el error es 401 y no estamos ya en un proceso de refresh
    if (error.response.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise(function(resolve, reject) {
          failedQueue.push({ resolve, reject });
        }).then(token => {
          originalRequest.headers['Authorization'] = 'Bearer ' + token;
          return axios(originalRequest);
        }).catch(err => {
          return Promise.reject(err);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const { data } = await axios.post(`${API_CONSTANTS.BASE_URL}${API_CONSTANTS.V1_ROUTE}${API_CONSTANTS.AUTH_ROUTE}${API_CONSTANTS.REFRESH_ROUTE}`, { refreshToken });

          localStorage.setItem('authToken', data.access_token || data.accessToken);
          localStorage.setItem('refreshToken', data.refresh_token || data.refreshToken);

          const newAccessToken = data.access_token || data.accessToken;
          api.defaults.headers.common['Authorization'] = 'Bearer ' + newAccessToken;
          originalRequest.headers['Authorization'] = 'Bearer ' + newAccessToken;

          processQueue(null, newAccessToken);
          return api(originalRequest);
        } catch (refreshError) {
          processQueue(refreshError, null);
          handleUnauthorized();
          return Promise.reject(refreshError);
        } finally {
          isRefreshing = false;
        }
      } else {
        handleUnauthorized();
        return Promise.reject(error);
      }
    }

    // Para otros errores, simplemente los rechazamos
    return Promise.reject(error);
  },
)

// Función para manejar redirecciones en caso de error 401
function handleUnauthorized() {
  // Limpiar tokens
  localStorage.removeItem('authToken');
  localStorage.removeItem('refreshToken');

  if (routerInstance) {
    routerInstance.push('/auth/login')
  } else {
    console.error('Router instance not initialized in interceptor.')
    window.location.href = '/auth/login' // Fallback
  }
}

export default boot(({ app, router }) => {
  app.config.globalProperties.$axios = axios
  app.config.globalProperties.$api = api
  routerInstance = router // Guarda la instancia del router
})

export { api, API_CONSTANTS }
