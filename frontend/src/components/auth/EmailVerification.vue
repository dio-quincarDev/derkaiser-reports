<template>
  <div>
    <div v-if="status === 'verifying'" class="text-center">
      <q-spinner size="50px" color="primary" />
      <div class="q-mt-md">Verificando tu correo electrónico...</div>
    </div>

    <div v-else-if="status === 'success'" class="text-positive text-center">
      <q-icon name="check_circle" size="50px" />
      <div class="q-mt-md text-h6">¡Correo verificado exitosamente!</div>
      <div class="q-mt-sm">Tu cuenta ha sido verificada correctamente.</div>
    </div>

    <div v-else-if="status === 'error'" class="text-negative text-center">
      <q-icon name="error" size="50px" />
      <div class="q-mt-md text-h6">Error en la verificación</div>
      <div class="q-mt-sm">{{ errorMessage }}</div>
    </div>

    <div v-if="status === 'error'" class="text-center q-mt-lg">
      <q-btn 
        @click="$emit('resend')" 
        color="primary" 
        :loading="isResending"
        :disable="resendSuccess"
      >
        {{ resendSuccess ? '¡Email reenviado!' : 'Reenviar correo de verificación' }}
      </q-btn>
      <div v-if="resendSuccess" class="q-mt-sm text-positive">
        ¡Email de verificación reenviado! Revisa tu bandeja de entrada.
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  status: {
    type: String,
    required: true, // 'verifying', 'success', 'error'
  },
  errorMessage: String,
  isResending: Boolean,
  resendSuccess: Boolean,
});

defineEmits(['resend']);
</script>
