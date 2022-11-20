import Vue from "vue";
import Vuetify from "vuetify/lib/framework";

Vue.use(Vuetify);

export default new Vuetify({
  theme: {
    options: { customProperties: true },
    dark: localStorage.getItem("timeGuessDarkTheme") === "true",
    themes: {
      light: {
        appTitle: "black",
      },
      dark: {
        appTitle: "white",
      },
    },
  },
});
