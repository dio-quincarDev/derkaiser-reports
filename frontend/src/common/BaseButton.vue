<template>
  <q-btn
    :to="to"
    :color="qBtnColor"
    :outline="isOutline"
    :flat="isLink"
    :size="props.size"
    :loading="loading"
    :disable="disabled"
    unelevated
    no-caps
    @click="handleClick"
  >
    <slot />
  </q-btn>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'secondary', 'outline', 'danger', 'link'].includes(value),
  },
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg'].includes(value),
  },
  loading: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  to: {
    type: String,
    default: undefined,
  },
})

const emit = defineEmits(['click'])

const isOutline = computed(() => props.variant === 'outline')
const isLink = computed(() => props.variant === 'link')

const qBtnColor = computed(() => {
  switch (props.variant) {
    case 'primary':
      return 'primary'
    case 'secondary':
      return 'secondary'
    case 'danger':
      return 'negative'
    case 'outline':
      return 'primary'
    default:
      return undefined
  }
})

const handleClick = (event) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>

<style lang="scss" scoped>
.q-btn {
  border-radius: 6px;
}
</style>
