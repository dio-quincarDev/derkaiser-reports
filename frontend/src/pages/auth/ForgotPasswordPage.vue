<template>
  <q-card class="q-pa-md shadow-2 my-card" bordered>
    <q-card-section class="text-center">
      <div class="text-h5 text-weight-bold">Recuperar Contraseña</div>
      <div class="text-subtitle1">Te enviaremos un enlace para restablecer tu contraseña</div>
    </q-card-section>

    <q-card-section>
      <q-form @submit.prevent="handleForgotPassword" class="q-gutter-md">
        <q-input
          v-model="email"
          label="Correo Electrónico"
          type="email"
          lazy-rules
          :rules="[
            (val) => !!val || 'El correo es requerido',
            (val) => /.+@.+\..+/.test(val) || 'Debe ser un correo válido',
          ]"
        >
          <template v-slot:prepend>
            <q-icon name="email" />
          </template>
        </q-input>

        <div v-if="authStore.status.isError" class="q-mt-md text-negative text-center">
          {{ authStore.status.message }}
        </div>

        <div v-if="requestSuccess" class="q-mt-md text-positive text-center">
          ¡Email de recuperación enviado! Revisa tu bandeja de entrada.
        </div>

        <q-btn
          label="Enviar Enlace"
          type="submit"
          color="primary"
          class="full-width q-mt-lg"
          :loading="authStore.status.isloading"
          :disabled="requestSuccess"
        />
      </q-form>
    </q-card-section>

    <q-card-section class="text-center q-pt-none">
      <div class="text-grey-8">
        <router-link
          to="/auth/login"
          class="text-primary text-weight-bold"
          style="text-decoration: none"
        >
          Volver al Inicio de Sesión
        </router-link>
      </div>
    </q-card-section>
  </q-card>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from 'stores/auth-module'
import { useQuasar } from 'quasar'

const email = ref('')
const requestSuccess = ref(false)

const authStore = useAuthStore()
const $q = useQuasar()

const handleForgotPassword = async () => {
  try {
    await authStore.forgotPassword(email.value)
    requestSuccess.value = true
    $q.notify({
      color: 'positive',
      position: 'top',
      message: '¡Email de recuperación enviado! Revisa tu bandeja de entrada.',
      icon: 'check_circle',
    })
  } catch {
    $q.notify({
      color: 'negative',
      position: 'top',
      message:
        authStore.status.message || 'Ocurrió un error al solicitar la recuperación de contraseña.',
      icon: 'report_problem',
    })
  }
}
</script>

<style scoped>
.my-card {
  width: 100%;
  max-width: 400px;
  border-radius: 12px;
}
</style>
