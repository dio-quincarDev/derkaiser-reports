<template>
  <div class="minimal-container">
    <div class="minimal-content">
      <div class="minimal-header">
        <h1 class="minimal-title">Bitácora AIP</h1>
        <p class="minimal-subtitle">Infoplazas Chiriquí</p>
      </div>

      <div class="minimal-actions">
        <BaseButton variant="outline" size="lg" :to="loginRoute">
          {{ loginLabel }}
        </BaseButton>
        <BaseButton variant="primary" size="lg" :to="registerRoute">
          {{ registerLabel }}
        </BaseButton>
      </div>
    </div>
  </div>
</template>

<script setup>
import BaseButton from 'src/common/BaseButton.vue';

const loginRoute = '/auth/login';
const registerRoute = '/auth/register';
const loginLabel = 'Iniciar Sesión';
const registerLabel = 'Registrarse';
</script>

<style lang="scss" scoped>
.minimal-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: $dark-page;
  color: white;
  font-family:
    'Inter',
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    Roboto,
    Oxygen,
    Ubuntu,
    Cantarell,
    'Open Sans',
    'Helvetica Neue',
    sans-serif;
}

.minimal-content {
  text-align: center;
  max-width: 600px;
  padding: 2rem;
  z-index: 1;
}

.minimal-header {
  margin-bottom: 3rem;
}

.minimal-title {
  font-size: 3.5rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  letter-spacing: -0.5px;
  color: $primary-light;
  line-height: 1.1;

  animation: fadeInUp 0.8s ease-out 0.2s both;
}

.minimal-subtitle {
  font-size: 1.25rem;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
  letter-spacing: 0.5px;

  animation: fadeInUp 0.8s ease-out 0.4s both;
}

.minimal-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;

  animation: fadeInUp 0.8s ease-out 0.6s both;
}

// Minimal fade-in animation
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// Responsive adjustments
@media (max-width: 768px) {
  .minimal-container {
    padding: 1rem;
  }

  .minimal-title {
    font-size: 2.5rem;
  }

  .minimal-subtitle {
    font-size: 1.1rem;
  }

  .minimal-actions {
    flex-direction: column;
    align-items: center;
    gap: 1rem;
  }

  .base-button {
    width: 100%;
    max-width: 250px;
  }
}

@media (max-width: 480px) {
  .minimal-title {
    font-size: 2rem;
  }

  .minimal-subtitle {
    font-size: 1rem;
  }

  .minimal-content {
    padding: 1rem;
  }
}
</style>
