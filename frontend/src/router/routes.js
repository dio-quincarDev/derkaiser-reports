const routes = [
  // Presentation page route (public)
  {
    path: '/',
    component: () => import('layouts/PresentationLayout.vue'),
    children: [
      {
        path: '',
        component: () => import('pages/PresentationPage.vue'),
      }
    ]
  },

  // Auth routes (public)
  {
    path: '/verify',
    component: () => import('layouts/AuthLayout.vue'),
    children: [
      {
        path: '',
        component: () => import('pages/auth/VerifyEmailPage.vue'),
        meta: { requiresGuest: true }
      }
    ]
  },
  {
    path: '/auth',
    component: () => import('layouts/AuthLayout.vue'), // Assuming we might want a different layout for auth
    children: [
      {
        path: 'login',
        component: () => import('pages/auth/LoginPage.vue'),
        meta: { requiresGuest: true }
      },
      {
        path: 'register',
        component: () => import('pages/auth/RegisterPage.vue'),
        meta: { requiresGuest: true }
      },
      {
        path: 'forgot-password',
        component: () => import('pages/auth/ForgotPasswordPage.vue'),
        meta: { requiresGuest: true }
      },
      {
        path: 'reset-password',
        component: () => import('pages/auth/ResetPasswordPage.vue'),
        meta: { requiresGuest: true }
      }
    ]
  },

  // Main application routes (protected)
  {
    path: '/app',
    component: () => import('layouts/MainLayout.vue'),
    meta: { requiresAuth: true }, // Requires authentication
    children: [
      {
        path: '',
        component: () => import('pages/IndexPage.vue')
      },
      // Add more application routes here as needed
    ]
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue'),
  },
]

export default routes
