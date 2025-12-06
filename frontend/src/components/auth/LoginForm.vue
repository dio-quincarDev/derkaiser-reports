<template>
  <q-form @submit.prevent="onFormSubmit" class="q-gutter-md">
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

    <div v-if="errorMessage" class="q-mt-md text-negative text-center">
      {{ errorMessage }}
    </div>

    <q-btn
      label="Ingresar"
      type="submit"
      color="primary"
      class="full-width q-mt-lg"
      :loading="loading"
    />
  </q-form>
</template>

<script setup>
import { ref } from 'vue';

defineProps({
  loading: Boolean,
  errorMessage: String,
});

const emit = defineEmits(['submit']);

const email = ref('');
const password = ref('');
const isPasswordVisible = ref(false);

const onFormSubmit = () => {
  emit('submit', { email: email.value, password: password.value });
};
</script>
