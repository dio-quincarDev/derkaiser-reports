<template>
  <q-card class="q-pa-md shadow-2 my-card" bordered>
    <q-card-section class="text-center">
      <div class="text-h5 text-weight-bold">Verificar Correo Electrónico</div>
      <div v-if="!verificationComplete">
        <q-spinner size="50px" color="primary" />
        <div class="q-mt-md">Verificando tu correo electrónico...</div>
      </div>
      <div v-else-if="verificationSuccess" class="text-positive">
        <q-icon name="check_circle" size="50px" />
        <div class="q-mt-md text-h6">¡Correo verificado exitosamente!</div>
        <div class="q-mt-sm">Tu cuenta ha sido verificada correctamente.</div>
      </div>
      <div v-else class="text-negative">
        <q-icon name="error" size="50px" />
        <div class="q-mt-md text-h6">Error en la verificación</div>
        <div class="q-mt-sm">{{ errorMessage }}</div>
      </div>
    </q-card-section>

    <q-card-section v-if="verificationComplete && !verificationSuccess" class="text-center">
      <q-btn 
        @click="resendVerification" 
        color="primary" 
        :loading="isResending"
        :disable="resendSuccess"
      >
        {{ resendSuccess ? '¡Email reenviado!' : 'Reenviar correo de verificación' }}
      </q-btn>
      <div v-if="resendSuccess" class="q-mt-md text-positive">
        ¡Email de verificación reenviado! Revisa tu bandeja de entrada.
      </div>
    </q-card-section>

    <q-card-section class="text-center q-pt-none" v-if="verificationComplete">
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

const route = useRoute();
const router = useRouter();
const $q = useQuasar();
const authStore = useAuthStore();

const verificationComplete = ref(false);
const verificationSuccess = ref(false);
const errorMessage = ref('');
const isResending = ref(false);
const resendSuccess = ref(false);

onMounted(async () => {
  const token = route.query.token;
  if (!token) {
    errorMessage.value = 'Token de verificación no proporcionado.';
    verificationComplete.value = true;
    verificationSuccess.value = false;
    return;
  }

  try {
    await authStore.verifyEmail(token);
    verificationSuccess.value = true;
    $q.notify({
      color: 'positive',
      position: 'top',
      message: '¡Correo verificado exitosamente!',
      icon: 'check_circle'
    });
  } catch (error) {
    errorMessage.value = error.response?.data?.message || 'Error al verificar el correo electrónico.';
  } finally {
    verificationComplete.value = true;
  }
});

const resendVerification = async () => {
  const email = route.query.email || '';
  if (!email) {
    $q.notify({
      type: 'negative',
      message: 'Email no proporcionado para reenviar la verificación.',
      position: 'top'
    });
    return;
  }

  isResending.value = true;
  try {
    await authStore.resendVerification(email);
    resendSuccess.value = true;
    $q.notify({
      color: 'positive',
      position: 'top',
      message: '¡Email de verificación reenviado! Revisa tu bandeja de entrada.',
      icon: 'check_circle'
    });
  } catch (error) {
    $q.notify({
      color: 'negative',
      position: 'top',
      message: authStore.status.message || 'Error al reenviar el email de verificación.',
      icon: 'report_problem'
    });
  } finally {
    isResending.value = false;
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