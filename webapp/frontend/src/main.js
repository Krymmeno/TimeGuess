import Vue from "vue";
import App from "./App.vue";
import vuetify from "./plugins/vuetify";
import Notifications from "vue-notification";
import store from "./store";
import router from "./router";
import i18n from "./i18n";
import axios from "axios";

Vue.config.productionTip = false;

Vue.use(Notifications);

axios.interceptors.request.use((config) => {
  console.log(config);
  return config;
});

axios.interceptors.response.use(
  (response) => {
    console.log(response);
    return response;
  },
  (error) => {
    console.log(error);
    if (error.response.status === 500) {
      router.push("/errors/500");
    } else if (error.response.status === 401) {
      Vue.notify({
        title: i18n.t("notification.error") + error.response.status,
        text: i18n.t("notification.webTokenexpired"),
        type: "error",
      });
      store.dispatch("auth/logout").then(() => router.push("/login"));
    }
    return Promise.reject(error);
  }
);
new Vue({
  store,
  vuetify,
  router,
  i18n,
  render: (h) => h(App),
}).$mount("#app");
