import { useAuthStore } from 'stores/auth-module';
import { Notify } from 'quasar';

export function authGuard(to, from, next) {
  const authStore = useAuthStore();
  const isAuthenticated = authStore.isAuthenticated;

  // Regla 1: Usuario autenticado intenta ir a rutas de "invitado" (como login)
  if (to.meta.requiresGuest && isAuthenticated) {
    return next({ path: '/' }); // Redirigir a la página principal de la app
  }

  // Regla 2: Usuario no autenticado intenta ir a ruta protegida
  if (to.meta.requiresAuth && !isAuthenticated) {
    Notify.create({
      type: 'negative',
      message: 'Necesitas iniciar sesión para acceder a esta página.',
      position: 'top',
    });
    return next({ path: '/auth/login', query: { redirect: to.fullPath } });
  }

  // Regla 3: Verificación de roles para rutas que lo requieran
  if (to.meta.roles && isAuthenticated) {
    const userRole = authStore.getRole; // 'USER', 'ADMIN', etc.
    if (!userRole || !to.meta.roles.includes(userRole.toUpperCase())) {
      Notify.create({
        type: 'negative',
        message: 'No tienes los permisos necesarios para acceder a esta sección.',
        position: 'top',
      });
      // Redirigir a una página de 'acceso denegado' o a la página principal
      return next({ path: from.path !== '/auth/login' ? from.path : '/' }); 
    }
  }

  // Si no se cumple ninguna de las reglas anteriores, permitir el paso
  return next();
}
