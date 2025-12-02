<template>
  <router-link
    v-if="to"
    :to="to"
    :class="[
      'base-button',
      `base-button--${variant}`,
      `base-button--${size}`,
      'router-link-btn',
      { 'base-button--disabled': disabled, 'base-button--loading': loading }
    ]"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <span v-if="!loading" class="base-button__content">
      <slot />
    </span>
    <span v-else class="base-button__loading">
      <span class="loading-spinner"></span>
    </span>
  </router-link>
  <button
    v-else
    :class="[
      'base-button',
      `base-button--${variant}`,
      `base-button--${size}`,
      { 'base-button--disabled': disabled, 'base-button--loading': loading }
    ]"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <span v-if="!loading" class="base-button__content">
      <slot />
    </span>
    <span v-else class="base-button__loading">
      <span class="loading-spinner"></span>
    </span>
  </button>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

interface Props {
  variant?: 'primary' | 'secondary' | 'outline' | 'danger' | 'link';
  size?: 'sm' | 'md' | 'lg';
  loading?: boolean;
  disabled?: boolean;
  to?: string;
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  loading: false,
  disabled: false,
  to: undefined,
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const router = useRouter()

const handleClick = (event: MouseEvent) => {
  // If it's a button with a to prop, handle navigation manually
  if (!props.to && !props.disabled && !props.loading) {
    emit('click', event)
  }
  // For router-link, navigation is handled by Vue Router automatically
  // We still emit the click event (except when disabled/loading)
  else if (props.to && !props.disabled && !props.loading) {
    emit('click', event)
  }
  // Prevent default if disabled or loading
  if (props.disabled || props.loading) {
    event.preventDefault()
  }
}
</script>

<style lang="scss">
.base-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  text-decoration: none;

  &--primary {
    background-color: #1976d2;
    color: white;

    &:hover:not(:disabled) {
      background-color: #1565c0;
    }
  }

  &--secondary {
    background-color: #6c757d;
    color: white;

    &:hover:not(:disabled) {
      background-color: #5a6268;
    }
  }

  &--outline {
    background-color: transparent;
    color: #1976d2;
    border: 1px solid #1976d2;

    &:hover:not(:disabled) {
      background-color: #1976d2;
      color: white;
    }
  }

  &--danger {
    background-color: #dc3545;
    color: white;

    &:hover:not(:disabled) {
      background-color: #c82333;
    }
  }

  &--link {
    background-color: transparent;
    color: #1976d2;
    text-decoration: underline;

    &:hover:not(:disabled) {
      color: #1565c0;
    }
  }

  &--disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  &--loading {
    pointer-events: none;
  }

  &--sm {
    padding: 0.25rem 0.5rem;
    font-size: 0.875rem;
  }

  &--md {
    padding: 0.5rem 1rem;
    font-size: 1rem;
  }

  &--lg {
    padding: 0.75rem 1.5rem;
    font-size: 1.25rem;
  }

  &__loading {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .loading-spinner {
    width: 1rem;
    height: 1rem;
    border: 2px solid transparent;
    border-top: 2px solid currentColor;
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
