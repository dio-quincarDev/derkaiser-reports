import { useAuthStore } from 'stores/auth-module';
import { Notify } from 'quasar';

// Track if we're currently processing a route to prevent re-entrancy
let isProcessingRoute = false;

export function authGuard(to, from, next) {
  // Prevent re-entrancy to avoid potential infinite loops
  if (isProcessingRoute) {
    console.warn('Router guard re-entrancy detected, skipping guard logic');
    return next();
  }

  isProcessingRoute = true;

  try {
    const authStore = useAuthStore();
    const isAuthenticated = authStore.isAuthenticated;
    const isUserActive = authStore.isUserActive;

    const redirectToVerifyEmail = '/auth/verify-email';
    const isTargetVerifyEmail = to.path === redirectToVerifyEmail;
    const isLoginPath = to.path === '/auth/login';
    const isRegisterPath = to.path === '/auth/register';
    const isForgotPasswordPath = to.path === '/auth/forgot-password';
    const isResetPasswordPath = to.path === '/auth/reset-password';
    const isGuestRoute = to.meta.requiresGuest ||
                         isLoginPath ||
                         isRegisterPath ||
                         isForgotPasswordPath ||
                         isResetPasswordPath ||
                         isTargetVerifyEmail;

    // Handle root path '/'
    if (to.path === '/') {
      if (isAuthenticated && isUserActive) {
        return next('/app');
      }
      // If not authenticated, or not active, allow access to the presentation page.
      return next();
    }

    // Rule: Authenticated and active users trying to access guest routes are redirected to app
    if (isGuestRoute && isAuthenticated && isUserActive) {
      // Avoid redirecting if we're already trying to go to /app
      if (from.path !== '/app' && to.path !== '/app' && to.path !== '/') {
        return next({ path: '/app' });
      }
      // If somehow already at /app but hitting guest route, allow going to /
      return next('/');
    }

    // Rule: Unauthenticated users are redirected to login for protected routes
    if (to.meta.requiresAuth && !isAuthenticated) {
      Notify.create({
        type: 'negative',
        message: 'Necesitas iniciar sesión para acceder a esta página.',
        position: 'top',
      });
      // Only add redirect query if we're not already on login page
      if (to.path !== '/auth/login') {
        return next({ path: '/auth/login', query: { redirect: to.fullPath } });
      }
      return next();
    }

    // Rule: Authenticated but not active users are redirected to verify-email
    // This rule applies if they are trying to access any page EXCEPT the verify-email page itself
    if (isAuthenticated && !isUserActive && !isTargetVerifyEmail) {
      // Only redirect if not already on verify-email page
      if (to.path !== redirectToVerifyEmail) {
        Notify.create({
          type: 'warning',
          message: 'Tu cuenta no está activada. Por favor, verifica tu correo electrónico.',
          position: 'top',
        });
        return next({ path: redirectToVerifyEmail });
      }
      return next();
    }

    // Rule: Role verification (only if authenticated and active, otherwise handled above)
    if (to.meta.roles && isAuthenticated && isUserActive) {
      const userRole = authStore.getRole;
      if (!userRole || !to.meta.roles.includes(userRole.toUpperCase())) {
        Notify.create({
          type: 'negative',
          message: 'No tienes los permisos necesarios para acceder a esta sección.',
          position: 'top',
        });
        // Redirect to a safe location, avoiding infinite loops
        const fallbackPath = from.path !== '/auth/login' && from.path !== '/' ? from.path : '/app';
        return next({ path: fallbackPath });
      }
    }

    // If no redirection or blocking rule applies, allow navigation
    next();
  } finally {
    isProcessingRoute = false;
  }
}
