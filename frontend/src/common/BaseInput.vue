<!-- Ejemplo de uso -->
<BaseInput
  v-model="email"
  type="email"
  label="Email"
  placeholder="tu@email.com"
  :error="emailError"
  icon="mail"
  required
/>
