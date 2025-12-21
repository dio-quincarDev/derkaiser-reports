<template>
  <q-card class="q-pa-md shadow-2 my-card" bordered>
    <q-card-section class="text-center">
      <div class="text-h5 text-weight-bold">Verificar Correo Electrónico</div>
    </q-card-section>

    <q-card-section>
      <EmailVerification
        :status="verificationStatus"
        :error-message="errorMessage"
        :is-resending="isResending"
        :resend-success="resendSuccess"
        @resend="resendVerification"
      />
    </q-card-section>

    <q-card-section class="text-center q-pt-none" v-if="verificationStatus !== 'verifying'">
      <div class="text-grey-8">
        <router-link to="/auth/login" class="text-primary text-weight-bold" style="text-decoration: none;">
          Volver al Inicio de Sesión
        </router-link>
      </div>
    </q-card-section>
  </q-card>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from 'stores/auth-module';
import { useQuasar } from 'quasar';
import EmailVerification from 'components/auth/EmailVerification.vue';

const route = useRoute();
const router = useRouter();
const $q = useQuasar();
const authStore = useAuthStore();

const verificationComplete = ref(false);
const verificationSuccess = ref(false);
const errorMessage = ref('');
const isResending = ref(false);
const resendSuccess = ref(false);

const verificationStatus = computed(() => {
  if (!verificationComplete.value) {
    return 'verifying';
  }
  return verificationSuccess.value ? 'success' : 'error';
});

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

    // Redirigir al dashboard después de un corto tiempo para que el usuario vea el mensaje de éxito.
    setTimeout(() => {
      router.push('/app');
    }, 2000); // Redirigir después de 2 segundos

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