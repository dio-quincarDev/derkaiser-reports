import { api } from 'src/boot/axios';

const AUTH_PATH = '/v1/auth';

const AuthService = {
  /**
   * Registra un nuevo usuario.
   * @param {object} userData - Datos del usuario (firstName, lastName, cargo, email, password)
   */
  register(userData) {
    return api.post(`${AUTH_PATH}/register`, userData);
  },

  /**
   * Inicia sesión en el sistema.
   * @param {object} credentials - Credenciales del usuario (email, password)
   */
  login(credentials) {
    return api.post(`${AUTH_PATH}/login`, credentials);
  },

  /**
   * Cierra la sesión del usuario.
   * @param {string} refreshToken - El token de refresco actual.
   */
  logout(refreshToken) {
    return api.post(`${AUTH_PATH}/logout`, { refreshToken });
  },

  /**
   * Obtiene la información del usuario autenticado actualmente.
   */
  fetchUser() {
    return api.get(`${AUTH_PATH}`); // Updated to include the full path
  },

  /**
   * Reenvía el email de verificación.
   * @param {string} email - El email del usuario.
   */
  resendVerification(email) {
    return api.post(`${AUTH_PATH}/resend-verification`, { email });
  },

  /**
   * Verifica el email de un usuario usando un token.
   * @param {string} token - El token de verificación.
   */
  verifyEmail(token) {
    return api.get(`${AUTH_PATH}/verify`, { params: { token } });
  },

  /**
   * Solicita el restablecimiento de contraseña.
   * @param {string} email - El email del usuario.
   */
  forgotPassword(email) {
    return api.post(`${AUTH_PATH}/forgot-password`, { email });
  },

  /**
   * Restablece la contraseña usando un token.
   * @param {object} payload - Objeto con token y nueva contraseña.
   * @param {string} payload.token - El token de restablecimiento.
   * @param {string} payload.newPassword - La nueva contraseña.
   */
  resetPassword({ token, newPassword }) {
    return api.post(`${AUTH_PATH}/reset-password`, { token, newPassword });
  },

  /**
   * Refresca el token de acceso usando el token de refresco.
   * @param {string} refreshToken - El token de refresco.
   */
  refreshToken(refreshToken) {
    return api.post(`${AUTH_PATH}/refresh`, { refreshToken });
  },
};

export default AuthService;
