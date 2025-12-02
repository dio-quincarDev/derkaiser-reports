<template>
  <q-card class="q-pa-md shadow-2 my-card" bordered>
    <q-card-section class="text-center">
      <div class="text-h5 text-weight-bold">Crear Cuenta</div>
      <div class="text-subtitle1">Únete a Bitácora AIP</div>
    </q-card-section>

    <q-card-section>
      <q-form @submit.prevent="handleRegister" class="q-gutter-md">
        <div class="row q-col-gutter-sm">
          <q-input
            class="col-12 col-sm-6"
            v-model="firstName"
            label="Nombre"
            lazy-rules
            :rules="[val => !!val || 'El nombre es requerido']"
          />
          <q-input
            class="col-12 col-sm-6"
            v-model="lastName"
            label="Apellido"
            lazy-rules
            :rules="[val => !!val || 'El apellido es requerido']"
          />
        </div>

        <q-input
          v-model="cargo"
          label="Cargo"
          lazy-rules
          :rules="[val => !!val || 'El cargo es requerido']"
        />

        <q-input
          v-model="email"
          label="Correo Electrónico"
          type="email"
          lazy-rules
          :rules="[val => !!val || 'El correo es requerido', val => /.+@.+\..+/.test(val) || 'Debe ser un correo válido']"
        />

        <q-input
          v-model="password"
          label="Contraseña"
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

        <div v-if="registrationSuccess" class="q-mt-md text-positive text-center">
          ¡Registro exitoso! Por favor, revisa tu correo para verificar tu cuenta.
        </div>

        <q-btn
          label="Registrarse"
          type="submit"
          color="primary"
          class="full-width q-mt-lg"
          :loading="authStore.status.isloading"
        />
      </q-form>
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

const firstName = ref('');
const lastName = ref('');
const cargo = ref('');
const email = ref('');
const password = ref('');
const isPasswordVisible = ref(false);
const registrationSuccess = ref(false);

const authStore = useAuthStore();
const $q = useQuasar();

const handleRegister = async () => {
  registrationSuccess.value = false;
  const userData = {
    firstName: firstName.value,
    lastName: lastName.value,
    cargo: cargo.value,
    email: email.value,
    password: password.value,
  };

  try {
    await authStore.register(userData);
    registrationSuccess.value = true;
    $q.notify({
      color: 'positive',
      position: 'top',
      message: '¡Registro exitoso! Revisa tu correo para la verificación.',
      icon: 'check_circle'
    });
  } catch (error) {
    // El error ya se maneja en el store, pero podemos mostrar una notificación si queremos
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
