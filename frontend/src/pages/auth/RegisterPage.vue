<template>
  <q-card class="q-pa-md shadow-2 my-card" bordered>
    <q-card-section class="text-center">
      <div class="text-h5 text-weight-bold">Crear Cuenta</div>
      <div class="text-subtitle1">Únete a Bitácora AIP</div>
    </q-card-section>

    <q-card-section>
      <RegisterForm
        v-if="!registrationSuccess"
        :loading="authStore.status.isloading"
        :error-message="authStore.status.isError ? authStore.status.message : ''"
        @submit="handleRegister"
      />
      <div v-if="registrationSuccess" class="q-mt-md text-positive text-center">
        ¡Registro exitoso! Por favor, revisa tu correo para verificar tu cuenta.
      </div>
    </q-card-section>

    <q-card-section class="text-center q-pt-none">
      <div class="text-grey-8">
        ¿Ya tienes una cuenta?
        <router-link to="/auth/login" class="text-primary text-weight-bold" style="text-decoration: none;">
          Inicia Sesión
        </router-link>
      </div>
    </q-card-section>
  </q-card>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from 'stores/auth-module';
import { useQuasar } from 'quasar';
import { useRouter } from 'vue-router';
import RegisterForm from 'components/auth/RegisterForm.vue';

const registrationSuccess = ref(false);

const authStore = useAuthStore();
const $q = useQuasar();
const router = useRouter();

const handleRegister = async (userData) => {
  try {
    await authStore.register(userData);
    registrationSuccess.value = true;
    $q.notify({
      color: 'positive',
      position: 'top',
      message: '¡Registro exitoso! Revisa tu correo para la verificación.',
      icon: 'check_circle'
    });

    setTimeout(() => {
      router.push('/auth/login');
    }, 3000);
  } catch (error) {
    $q.notify({
      color: 'negative',
      position: 'top',
      message: authStore.status.message || 'Ocurrió un error durante el registro.',
      icon: 'report_problem'
    });
  }
};
</script>

<style scoped>
.my-card {
  width: 100%;
  max-width: 450px;
  border-radius: 12px;
}
</style>
