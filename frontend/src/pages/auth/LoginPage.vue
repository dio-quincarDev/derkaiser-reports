<template>
  <q-card class="q-pa-md shadow-2 my-card" bordered>
    <q-card-section class="text-center">
      <div class="text-h5 text-weight-bold">Iniciar Sesión</div>
      <div class="text-subtitle1">Bienvenido a Bitácora AIP</div>
    </q-card-section>

    <q-card-section>
      <q-form @submit.prevent="handleLogin" class="q-gutter-md">
        <q-input
          v-model="email"
          label="Correo Electrónico"
          type="email"
          lazy-rules
          :rules="[val => !!val || 'El correo es requerido', val => /.+@.+\..+/.test(val) || 'Debe ser un correo válido']"
        >
          <template v-slot:prepend>
            <q-icon name="email" />
          </template>
        </q-input>

        <q-input
          v-model="password"
          label="Contraseña"
          :type="isPasswordVisible ? 'text' : 'password'"
          lazy-rules
          :rules="[val => !!val || 'La contraseña es requerida']"
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

        <div v-if="authStore.status.isError" class="q-mt-md text-negative text-center">
          {{ authStore.status.message }}
        </div>

        <q-btn
          label="Ingresar"
          type="submit"
          color="primary"
          class="full-width q-mt-lg"
          :loading="authStore.status.isloading"
        />
      </q-form>
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
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from 'stores/auth-module';

const email = ref('');
const password = ref('');
const isPasswordVisible = ref(false);

const router = useRouter();
const authStore = useAuthStore();

const handleLogin = async () => {
  const success = await authStore.login({ email: email.value, password: password.value });
  if (success) {
    router.push('/'); // Redirigir al dashboard o página principal
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
