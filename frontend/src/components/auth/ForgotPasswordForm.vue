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

    <div v-if="errorMessage" class="q-mt-md text-negative text-center">
      {{ errorMessage }}
    </div>

    <q-btn
      label="Enviar Enlace"
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

const onFormSubmit = () => {
  emit('submit', { email: email.value });
};
</script>
