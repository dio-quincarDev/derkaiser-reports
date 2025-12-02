<template>
  <q-card class="q-pa-md shadow-2 my-card" bordered>
    <q-card-section class="text-center">
      <div class="text-h5 text-weight-bold">Restablecer Contraseña</div>
      <div class="text-subtitle1">Ingresa tu nueva contraseña</div>
    </q-card-section>

    <q-card-section>
      <q-form @submit.prevent="handleResetPassword" class="q-gutter-md">
        <q-input
          v-model="token"
          label="Token de Restablecimiento"
          type="text"
          lazy-rules
          :rules="[val => !!val || 'El token es requerido']"
          readonly
        >
          <template v-slot:prepend>
            <q-icon name="vpn_key" />
          </template>
        </q-input>

        <q-input
          v-model="newPassword"
          label="Nueva Contraseña"
          :type="isPasswordVisible ? 'text' : 'password'"
          lazy-rules
          :rules="[
            val => !!val || 'La contraseña es requerida',
            val => val.length >= 8 || 'Mínimo 8 caracteres',
            val => /(?=.*[A-Z])/.test(val) || 'Debe contener una mayúscula',
            val => /(?=.*[a-z])/.test(val) || 'Debe contener una minúscula',
            val => /(?=.*\d)/.test(val) || 'Debe contener un número'
          ]"
          hint="Mínimo 8 caracteres, con mayúsculas, minúsculas y números."
        >
          <template v-slot:prepend>
            <q-icon name="lock" />
          </template>
          <template v-slot:append>
            <q-icon
              :name="isPasswordVisible ? 'visibility_off' : 'visibility'"
              class="cursor-pointer"
              @click="isPasswordVisible = !isPasswordVisible"
            />
          </template>
        </q-input>

        <q-input
          v-model="confirmPassword"
          label="Confirmar Nueva Contraseña"
          :type="isConfirmPasswordVisible ? 'text' : 'password'"
          lazy-rules
          :rules="[
            val => !!val || 'Debes confirmar la contraseña',
            val => val === newPassword || 'Las contraseñas no coinciden'
          ]"
        >
          <template v-slot:prepend>
            <q-icon name="lock_outline" />
          </template>
          <template v-slot:append>
            <q-icon
              :name="isConfirmPasswordVisible ? 'visibility_off' : 'visibility'"
              class="cursor-pointer"
              @click="isConfirmPasswordVisible = !isConfirmPasswordVisible"
            />
          </template>
        </q-input>

        <div v-if="authStore.status.isError" class="q-mt-md text-negative text-center">
          {{ authStore.status.message }}
        </div>

        <div v-if="resetSuccess" class="q-mt-md text-positive text-center">
          ¡Contraseña restablecida con éxito! Redirigiendo al inicio de sesión...
        </div>

        <q-btn
          label="Restablecer Contraseña"
          type="submit"
          color="primary"
          class="full-width q-mt-lg"
          :loading="authStore.status.isloading"
          :disabled="resetSuccess"
        />
      </q-form>
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

const route = useRoute();
const router = useRouter();
const $q = useQuasar();
const authStore = useAuthStore();

const token = ref('');
const newPassword = ref('');
const confirmPassword = ref('');
const isPasswordVisible = ref(false);
const isConfirmPasswordVisible = ref(false);
const resetSuccess = ref(false);

onMounted(() => {
  // Get the token from the URL query parameter
  const tokenFromQuery = route.query.token;
  if (tokenFromQuery) {
    token.value = tokenFromQuery;
  } else {
    // If no token provided, redirect to forgot password
    $q.notify({
      type: 'negative',
      message: 'Token de restablecimiento no proporcionado.',
      position: 'top'
    });
    router.push('/auth/forgot-password');
  }
});

const handleResetPassword = async () => {
  if (newPassword.value !== confirmPassword.value) {
    $q.notify({
      type: 'negative',
      message: 'Las contraseñas no coinciden.',
      position: 'top'
    });
    return;
  }

  try {
    await authStore.resetPassword({
      token: token.value,
      newPassword: newPassword.value
    });
    resetSuccess.value = true;
    
    $q.notify({
      color: 'positive',
      position: 'top',
      message: '¡Contraseña restablecida con éxito!',
      icon: 'check_circle'
    });

    // Redirect to login after a delay
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