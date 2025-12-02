<template>
  <q-layout view="hHh Lpr lff">
    <q-page-container>
      <div class="row justify-center items-center" style="min-height: 100vh; background-color: #f5f5f5;">
        <div class="col-xs-12 col-sm-8 col-md-6 col-lg-4">
          <router-view />
        </div>
      </div>
    </q-page-container>
  </q-layout>
</template>

<script>
export default {
  name: 'AuthLayout'
}
</script>

<style scoped>
/* Auth layout specific styles */
</style>