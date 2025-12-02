<!-- Botón con loading -->
<button :disabled="isLoading">
  <span v-if="!isLoading">Iniciar Sesión</span>
  <LoadingSpinner v-else />
</button>

<!-- Skeleton loaders para contenido -->
<div v-if="isLoading" class="skeleton"></div>
<div v-else>{{ content }}</div>
