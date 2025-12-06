<template>
  <div v-if="password.length > 0" class="password-strength-container">
    <div class="strength-bar">
      <div 
        class="strength-indicator" 
        :style="{ width: strength.percent + '%', 'background-color': strength.color }"
      ></div>
    </div>
    <div class="strength-label" :style="{ color: strength.color }">
      {{ strength.label }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  password: {
    type: String,
    required: true,
  },
});

const strength = computed(() => {
  let score = 0;
  if (!props.password) return { score, percent: 0, label: '', color: 'transparent' };

  // Criterios de fortaleza
  if (props.password.length >= 8) score++;
  if (props.password.length >= 12) score++;
  if (/[a-z]/.test(props.password)) score++;
  if (/[A-Z]/.test(props.password)) score++;
  if (/\d/.test(props.password)) score++;
  if (/[^a-zA-Z0-9]/.test(props.password)) score++;

  // Mapeo de puntaje a visualización
  switch (score) {
    case 0:
    case 1:
    case 2:
      return { score, percent: 25, label: 'Débil', color: '#EF4444' }; // Rojo
    case 3:
    case 4:
      return { score, percent: 50, label: 'Media', color: '#F59E0B' }; // Ámbar
    case 5:
      return { score, percent: 75, label: 'Fuerte', color: '#3B82F6' }; // Azul primario
    case 6:
      return { score, percent: 100, label: 'Muy Fuerte', color: '#10B981' }; // Verde
    default:
      return { score, percent: 0, label: '', color: 'transparent' };
  }
});
</script>

<style scoped>
.password-strength-container {
  margin-top: 8px;
  width: 100%;
}
.strength-bar {
  width: 100%;
  height: 6px;
  background-color: #e5e7eb;
  border-radius: 3px;
  overflow: hidden;
}
.strength-indicator {
  height: 100%;
  transition: width 0.3s ease, background-color 0.3s ease;
}
.strength-label {
  margin-top: 4px;
  font-size: 0.8em;
  font-weight: 500;
  text-align: right;
}
</style>
