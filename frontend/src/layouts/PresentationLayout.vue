<template>
  <q-layout view="hHh Lpr lff">
    <q-page-container>
      <div class="presentation-layout-container">
        <router-view />
      </div>
    </q-page-container>
  </q-layout>
</template>

<script setup>
// Layout para páginas públicas como la presentación
</script>

<style lang="scss" scoped>
.presentation-layout-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
