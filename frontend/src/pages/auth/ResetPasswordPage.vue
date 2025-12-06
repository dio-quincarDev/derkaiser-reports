<template>
  <q-card class="q-pa-md shadow-2 my-card" bordered>
    <q-card-section class="text-center">
      <div class="text-h5 text-weight-bold">Restablecer Contraseña</div>
      <div class="text-subtitle1">Ingresa tu nueva contraseña</div>
    </q-card-section>

    <q-card-section>
      <ResetPasswordForm
        v-if="!resetSuccess && token"
        :token="token"
        :loading="authStore.status.isloading"
        :error-message="authStore.status.isError ? authStore.status.message : ''"
        @submit="handleResetPassword"
      />
      <div v-if="resetSuccess" class="q-mt-md text-positive text-center">
        ¡Contraseña restablecida con éxito! Redirigiendo al inicio de sesión...
      </div>
    </q-card-section>

    <q-card-section class="text-center q-pt-none">
      <div class="text-grey-8">
        <router-link to="/auth/login" class="text-primary text-weight-bold" style="text-decoration: none;">
          Volver al Inicio de Sesión
        </router-link>
      </div>
    </q-card-section>
  </q-card>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from 'stores/auth-module';
import { useQuasar } from 'quasar';
import ResetPasswordForm from 'components/auth/ResetPasswordForm.vue';

const route = useRoute();
const router = useRouter();
const $q = useQuasar();
const authStore = useAuthStore();

const token = ref('');
const resetSuccess = ref(false);

onMounted(() => {
  const tokenFromQuery = route.query.token;
  if (tokenFromQuery) {
    token.value = tokenFromQuery;
  } else {
    $q.notify({
      type: 'negative',
      message: 'Token de restablecimiento no proporcionado.',
      position: 'top'
    });
    router.push('/auth/forgot-password');
  }
});

const handleResetPassword = async (formData) => {
  try {
    await authStore.resetPassword({
      token: formData.token,
      newPassword: formData.newPassword
    });
    resetSuccess.value = true;
    
    $q.notify({
      color: 'positive',
      position: 'top',
      message: '¡Contraseña restablecida con éxito!',
      icon: 'check_circle'
    });

    setTimeout(() => {
      router.push('/auth/login');
    }, 2000);
  } catch (error) {
    $q.notify({
      color: 'negative',
      position: 'top',
      message: authStore.status.message || 'Ocurrió un error al restablecer la contraseña.',
      icon: 'report_problem'
    });
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