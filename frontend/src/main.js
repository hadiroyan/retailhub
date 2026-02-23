import { createApp } from "vue";
import "./style.css";
import App from "./App.vue";
import router from "./router";
import { createPinia } from "pinia";
import '@fortawesome/fontawesome-free/css/all.min.css';

createApp(App).use(createPinia()).use(router).mount("#app");
