<template>
  <q-card class="base-card" flat bordered>
    <q-card-section v-if="hasHeader" class="base-card__header">
      <div class="text-h6">
        <slot name="header">
          {{ title }}
        </slot>
      </div>
    </q-card-section>

    <q-card-section class="base-card__content">
      <slot />
    </q-card-section>

    <q-separator v-if="hasFooter" />

    <q-card-actions v-if="hasFooter" class="base-card__footer">
      <slot name="footer" />
    </q-card-actions>
  </q-card>
</template>

<script setup>
import { useSlots, computed } from 'vue'

const props = defineProps({
  title: {
    type: String,
    default: '',
  },
})

const slots = useSlots()

const hasHeader = computed(() => !!slots.header || !!props.title)
const hasFooter = computed(() => !!slots.footer)
</script>

<style lang="scss" scoped>
.base-card {
  border-radius: 8px;
  border-color: #e5e7eb;
}

.base-card__header {
  padding: 16px;
  font-weight: 600;
}

.base-card__content {
  padding: 16px;
}

.base-card__footer {
  padding: 16px;
}
</style>
