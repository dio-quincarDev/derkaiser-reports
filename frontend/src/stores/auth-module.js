import { defineStore } from 'pinia';
import AuthService from 'src/service/auth-service.js';
import { api } from 'src/boot/axios';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: localStorage.getItem('authToken') || null,
    refreshToken: localStorage.getItem('refreshToken') || null,
    user: JSON.parse(localStorage.getItem('user')) || null,
    status: {
      isloading: false,
      isError: false,
      message: ''
    }
  }),

  getters: {
    isAuthenticated: (state) => !!state.accessToken,
    getUser: (state) => state.user,
    getAccessToken: (state) => state.accessToken,
    getRole: (state) => (state.user ? state.user.role : null),
  },

  actions: {
    setLoading(isLoading) {
      this.status.isloading = isLoading;
    },
    setError(message) {
      this.status.isError = true;
      this.status.message = message;
    },
    clearStatus() {
      this.status.isloading = false;
      this.status.isError = false;
      this.status.message = '';
    },

    async login(credentials) {
      this.setLoading(true);
      this.clearStatus();
      try {
        const { data } = await AuthService.login(credentials);
        this.accessToken = data.access_token;
        this.refreshToken = data.refresh_token;

        localStorage.setItem('authToken', data.access_token);
        localStorage.setItem('refreshToken', data.refresh_token);

        // Set token for subsequent requests
        api.defaults.headers.common['Authorization'] = 'Bearer ' + data.access_token;

        // Fetch user data
        await this.fetchUser();
        return true;
      } catch (error) {
        this.setError(error.response?.data?.message || 'Error en el inicio de sesión');
        this.logout(); // Limpiar estado si el login falla
        return false;
      } finally {
        this.setLoading(false);
      }
    },

    async fetchUser() {
      this.setLoading(true);
      try {
        const { data } = await AuthService.fetchUser();
        this.user = data;
        localStorage.setItem('user', JSON.stringify(data));
      } catch (error) {
        this.setError(error.response?.data?.message || 'Error al obtener datos del usuario');
        // No desloguear aquí, el interceptor de axios se encargará si es un 401
      } finally {
        this.setLoading(false);
      }
    },

    async logout() {
      this.setLoading(true);
      try {
        if (this.refreshToken) {
          await AuthService.logout(this.refreshToken);
        }
      } catch (error) {
        console.error("Error en el logout del backend, deslogueando localmente de todas formas.", error);
      } finally {
        this.accessToken = null;
        this.refreshToken = null;
        this.user = null;
        localStorage.removeItem('authToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        delete api.defaults.headers.common['Authorization'];
        this.setLoading(false);
      }
    },
    
    async register(userData) {
      this.setLoading(true);
      this.clearStatus();
      try {
        const response = await AuthService.register(userData);
        return response;
      } catch (error) {
        this.setError(error.response?.data?.message || 'Error en el registro');
        throw error;
      } finally {
        this.setLoading(false);
      }
    },
    
    async forgotPassword(email) {
      this.setLoading(true);
      this.clearStatus();
      try {
        return await AuthService.forgotPassword(email);
      } catch (error) {
        this.setError(error.response?.data?.message || 'Error al solicitar reseteo de contraseña');
        throw error;
      } finally {
        this.setLoading(false);
      }
    },

    async resetPassword(payload) {
      this.setLoading(true);
      this.clearStatus();
      try {
        return await AuthService.resetPassword(payload);
      } catch (error) {
        this.setError(error.response?.data?.message || 'Error al resetear la contraseña');
        throw error;
      } finally {
        this.setLoading(false);
      }
    },

    async verifyEmail(token) {
        this.setLoading(true);
        this.clearStatus();
        try {
            return await AuthService.verifyEmail(token);
        } catch (error) {
            this.setError(error.response?.data?.message || 'Error al verificar el email');
            throw error;
        } finally {
            this.setLoading(false);
        }
    }
  },
});
