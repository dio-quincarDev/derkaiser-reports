<template>
  <div class="alerts">
    <BaseAlert type="success" dismissible> Email verificado correctamente </BaseAlert>

    <BaseAlert type="error"> Credenciales incorrectas </BaseAlert>

    <BaseAlert type="warning"> Tu sesión expirará en 5 minutos </BaseAlert>

    <BaseAlert type="info"> Revisa tu email para continuar </BaseAlert>
  </div>
</template>

<script>
export default {
  name: 'BaseAlertExamples',
}
</script>
