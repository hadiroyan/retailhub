<script setup>
import { storeToRefs } from 'pinia';
import { ref } from 'vue'
import { useCounterStore } from '../stores/counter';

defineProps({
  msg: String,
})

const counterStore = useCounterStore();
const { count } = storeToRefs(counterStore);

</script>

<template>
  <h1>{{ msg }}</h1>

  <div class="card">
    <button type="button" @click="counterStore.addCount()">count is {{ count }}</button>
    <p class="mt-2">
      Edit
      <code>components/HelloWorld.vue</code> to test HMR
    </p>
  </div>

  <p>
    Check out
    <a href="https://vuejs.org/guide/quick-start.html#local" target="_blank">create-vue</a>, the official Vue + Vite
    starter
  </p>
  <p>
    Learn more about IDE Support for Vue in the
    <a href="https://vuejs.org/guide/scaling-up/tooling.html#ide-support" target="_blank">Vue Docs Scaling up Guide</a>.
  </p>
  <p class="read-the-docs mb-2">Click on the Vite and Vue logos to learn more</p>
</template>

<style scoped>
.read-the-docs {
  color: #888;
}
</style>
