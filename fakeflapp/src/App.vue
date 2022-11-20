<template>
  <v-app>
    <v-app-bar app color="primary" dark id="app-bar">
      <v-toolbar-title class="ml-4">FakeFlapp</v-toolbar-title>

      <v-spacer></v-spacer>

      <v-dialog v-model="showSettingsDialog" max-width="592px">
        <template v-slot:activator="{ on, attrs }">
          <v-btn text v-bind="attrs" v-on="on">
            <v-icon>mdi-cog</v-icon>
            <span class="ml-2">Settings</span>
          </v-btn>
        </template>
        <v-card>
          <v-card-title>
            <span class="headline">Settings</span>
          </v-card-title>
          <v-card-text>
            <v-text-field v-model="updateUrl" label="Update URL"></v-text-field>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="primary" text @click="showSettingsDialog = false"
              >Close</v-btn
            >
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-app-bar>

    <v-main>
      <Simulator v-bind:update-url="updateUrl" />
    </v-main>
  </v-app>
</template>

<script>
import Simulator from "./components/Simulator";

export default {
  name: "App",

  components: {
    Simulator,
  },

  data: () => ({
    updateUrl: process.env.VUE_APP_DEFAULT_UPDATE_URL,
    showSettingsDialog: false,
  }),
};
</script>

<style>
#app-bar .v-toolbar__content {
  max-width: 640px;
  margin: auto;
}
</style>