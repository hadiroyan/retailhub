import { defineStore } from "pinia";

export const useCounterStore = defineStore("counterStore", {
  state: () => ({
    counter: 0,
  }),
  getters: {
    count() {
      return this.counter;
    },
  },
  actions: {
    addCount() {
      this.counter++;
    },
  },
});
