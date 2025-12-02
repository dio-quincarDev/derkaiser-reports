<template>
  <q-page class="q-pa-md">
    <div class="row items-center q-mb-xl">
      <div class="col">
        <h4 class="text-h4 text-weight-bold">Panel Principal</h4>
        <p class="text-subtitle1">Bienvenido al sistema de Bitácora AIP</p>
      </div>
    </div>

    <div class="row q-col-gutter-lg">
      <!-- Stats Cards -->
      <div class="col-lg-3 col-md-6 col-sm-12">
        <q-card class="bg-primary text-white">
          <q-card-section>
            <div class="text-h6">Usuarios</div>
            <div class="text-h4">1,234</div>
            <div class="text-caption">+12% desde el mes pasado</div>
          </q-card-section>
        </q-card>
      </div>

      <div class="col-lg-3 col-md-6 col-sm-12">
        <q-card class="bg-secondary text-white">
          <q-card-section>
            <div class="text-h6">Reportes</div>
            <div class="text-h4">567</div>
            <div class="text-caption">+5% desde la semana pasada</div>
          </q-card-section>
        </q-card>
      </div>

      <div class="col-lg-3 col-md-6 col-sm-12">
        <q-card class="bg-accent text-white">
          <q-card-section>
            <div class="text-h6">Tareas Pendientes</div>
            <div class="text-h4">89</div>
            <div class="text-caption">23 vencidas</div>
          </q-card-section>
        </q-card>
      </div>

      <div class="col-lg-3 col-md-6 col-sm-12">
        <q-card class="bg-info text-white">
          <q-card-section>
            <div class="text-h6">Proyectos Activos</div>
            <div class="text-h4">23</div>
            <div class="text-caption">5 nuevos esta semana</div>
          </q-card-section>
        </q-card>
      </div>
    </div>

    <div class="row q-mt-lg">
      <div class="col-12">
        <q-card>
          <q-card-section>
            <div class="text-h6">Última Actividad</div>
            <q-list separator>
              <q-item v-for="activity in recentActivities" :key="activity.id">
                <q-item-section avatar>
                  <q-avatar
                    :color="activity.type === 'login' ? 'green' : 'blue'"
                    text-color="white"
                    icon="person"
                  />
                </q-item-section>
                <q-item-section>
                  <q-item-label>{{ activity.title }}</q-item-label>
                  <q-item-label caption>{{ activity.description }}</q-item-label>
                </q-item-section>
                <q-item-section side>
                  <q-item-label caption>{{ activity.time }}</q-item-label>
                </q-item-section>
              </q-item>
            </q-list>
          </q-card-section>
        </q-card>
      </div>
    </div>
  </q-page>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const recentActivities = ref([])

onMounted(() => {
  // Simulate recent activities - in a real app this would come from an API
  recentActivities.value = [
    {
      id: 1,
      type: 'login',
      title: 'Inicio de Sesión',
      description: 'Has iniciado sesión en el sistema',
      time: 'Hace 5 minutos',
    },
    {
      id: 2,
      type: 'report',
      title: 'Nuevo Reporte Creado',
      description: 'Reporte semanal de actividades generado',
      time: 'Hace 1 hora',
    },
    {
      id: 3,
      type: 'task',
      title: 'Tarea Completada',
      description: 'Actualización de documentación finalizada',
      time: 'Hace 2 horas',
    },
    {
      id: 4,
      type: 'update',
      title: 'Sistema Actualizado',
      description: 'Nueva versión del sistema disponible',
      time: 'Ayer',
    },
  ]
})
</script>

<style scoped>
.q-card {
  border-radius: 12px;
}
</style>
