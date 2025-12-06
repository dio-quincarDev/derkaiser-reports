<template>
  <q-form @submit.prevent="onFormSubmit" class="q-gutter-md">
    <q-input
      v-model="token"
      label="Token de Restablecimiento"
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

    <PasswordStrength :password="newPassword" />

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

    <div v-if="errorMessage" class="q-mt-md text-negative text-center">
      {{ errorMessage }}
    </div>

    <q-btn
      label="Restablecer Contraseña"
      type="submit"
      color="primary"
      class="full-width q-mt-lg"
      :loading="loading"
    />
  </q-form>
</template>

<script setup>
import { ref, watch } from 'vue';
import PasswordStrength from 'src/common/PasswordStrength.vue';

const props = defineProps({
  token: String,
  loading: Boolean,
  errorMessage: String,
});

const emit = defineEmits(['submit']);

// Internal state for the form fields
const newPassword = ref('');
const confirmPassword = ref('');
const isPasswordVisible = ref(false);
const isConfirmPasswordVisible = ref(false);

const onFormSubmit = () => {
  if (newPassword.value !== confirmPassword.value) {
    // This check is also in the rules, but an extra check here doesn't hurt.
    return;
  }
  emit('submit', { token: props.token, newPassword: newPassword.value });
};
</script>
