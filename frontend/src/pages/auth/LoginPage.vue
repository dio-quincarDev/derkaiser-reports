<template>
  <q-card class="q-pa-md shadow-2 my-card" bordered>
    <q-card-section class="text-center">
      <div class="text-h5 text-weight-bold">Iniciar Sesión</div>
      <div class="text-subtitle1">Bienvenido a Bitácora AIP</div>
    </q-card-section>

    <q-card-section>
      <LoginForm
        :loading="authStore.status.isloading"
        :error-message="authStore.status.isError ? authStore.status.message : ''"
        @submit="handleLogin"
      />
    </q-card-section>

    <q-card-section class="text-center q-pt-none">
      <div class="text-grey-8">
        ¿No tienes una cuenta?
        <router-link to="/auth/register" class="text-primary text-weight-bold" style="text-decoration: none;">
          Regístrate
        </router-link>
      </div>
      <div class="q-mt-sm">
        <router-link to="/auth/forgot-password" class="text-grey-6" style="text-decoration: none; font-size: 0.9em;">
          ¿Olvidaste tu contraseña?
        </router-link>
      </div>
    </q-card-section>
  </q-card>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useAuthStore } from 'stores/auth-module';
import LoginForm from 'components/auth/LoginForm.vue';

const router = useRouter();
const authStore = useAuthStore();

const handleLogin = async (credentials) => {
  const success = await authStore.login(credentials);
  if (success) {
    // Redirect to the app dashboard after successful login
    const redirectPath = router.currentRoute.value.query.redirect || '/app';
    router.push(redirectPath);
  }
};
</script>

<style scoped>
.my-card {
  width: 100%;
  max-width: 400px;
  border-radius: 12px;
}
</style>
