<template>
  <div class="auth-button-group">
    <q-btn
      :to="loginRoute"
      :label="loginLabel"
      class="auth-btn auth-btn-login"
      :outline="!filledButtons"
      :flat="filledButtons"
      :padding="buttonPadding"
      :ripple="false"
    />
    <q-btn
      :to="registerRoute"
      :label="registerLabel"
      class="auth-btn auth-btn-register"
      :outline="filledButtons"
      :flat="!filledButtons"
      :padding="buttonPadding"
      :ripple="false"
    />
  </div>
</template>

<script setup>
defineProps({
  loginRoute: {
    type: String,
    default: '/auth/login',
  },
  registerRoute: {
    type: String,
    default: '/auth/register',
  },
  loginLabel: {
    type: String,
    default: 'Iniciar Sesión',
  },
  registerLabel: {
    type: String,
    default: 'Registrarse',
  },
  filledButtons: {
    type: Boolean,
    default: true,
  },
  buttonPadding: {
    type: String,
    default: 'sm xl',
  },
})
</script>

<style lang="scss" scoped>
.auth-button-group {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.auth-btn {
  font-size: 1rem;
  font-weight: 500;
  text-transform: none;
  border-radius: 8px;
  min-width: 140px;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 25px -5px rgba($primary, 0.25);
  }
}

.auth-btn-login {
  border: 2px solid $primary !important;
  color: $primary !important;
  background: transparent !important;

  &:hover {
    background-color: rgba($primary, 0.1) !important;
  }
}

.auth-btn-register {
  color: white !important;
  background: $primary !important;

  &:hover {
    background-color: $primary-dark !important;
  }
}

// Responsive adjustments
@media (max-width: 768px) {
  .auth-button-group {
    flex-direction: column;
    align-items: center;
    gap: 1rem;
  }

  .auth-btn {
    width: 100%;
    max-width: 250px;
  }
}
</style>
