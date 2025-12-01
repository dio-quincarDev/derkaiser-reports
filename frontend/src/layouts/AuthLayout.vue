<template>
  <q-layout>
    <q-page-container>
      <q-page class="flex flex-center bg-grey-2">
        <router-view />
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script setup>
// No script logic is needed for this simple layout
</script>

<style scoped>
.q-page {
  min-height: 100vh;
}
</style>
