<template>
  <q-form @submit.prevent="onFormSubmit" class="q-gutter-md">
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

    <PasswordStrength :password="password" />

    <div v-if="errorMessage" class="q-mt-md text-negative text-center">
      {{ errorMessage }}
    </div>

    <q-btn
      label="Registrarse"
      type="submit"
      color="primary"
      class="full-width q-mt-lg"
      :loading="loading"
    />
  </q-form>
</template>

<script setup>
import { ref } from 'vue';
import PasswordStrength from 'src/common/PasswordStrength.vue';

defineProps({
  loading: Boolean,
  errorMessage: String,
});

const emit = defineEmits(['submit']);

const firstName = ref('');
const lastName = ref('');
const cargo = ref('');
const email = ref('');
const password = ref('');
const isPasswordVisible = ref(false);

const onFormSubmit = () => {
  const userData = {
    firstName: firstName.value,
    lastName: lastName.value,
    cargo: cargo.value,
    email: email.value,
    password: password.value,
  };
  emit('submit', userData);
};
</script>
