<template>
  <div>
    <v-card style="max-width: 608px; margin: auto">
      <v-card-title>{{ deviceName }}</v-card-title>
      <v-card-subtitle>{{ deviceAddress }}</v-card-subtitle>
      <v-card-actions>
        <v-dialog v-model="showDeviceDialog" persistent max-width="592px">
          <template v-slot:activator="{ on, attrs }">
            <v-btn color="primary" text v-bind="attrs" v-on="on">
              Edit device
            </v-btn>
          </template>
          <v-card>
            <v-card-title>
              <span class="headline">Device</span>
            </v-card-title>
            <v-card-text>
              <v-text-field v-model="newDeviceName" label="Name"></v-text-field>
              <v-text-field
                v-model="newDeviceAddress"
                label="Address"
              ></v-text-field>
            </v-card-text>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="secondary" text @click="cancelEditDevice()"
                >Cancel</v-btn
              >
              <v-btn color="primary" text @click="updateDevice()">Save</v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-card-actions>
      <v-divider class="mx-4"></v-divider>
      <v-card-text>
        <v-switch
          v-model="connected"
          label="Connected"
          color="success"
          @change="toggleConnect()"
        ></v-switch>

        <v-switch
          v-model="periodicUpdates"
          label="Periodic updates"
          @change="periodicUpdatesTimer = 0"
          id="periodicUpdatesSpinner"
        >
          <template v-slot:label>
            Periodic updates
            <v-progress-circular
              :value="periodicUpdatesTimer"
              color="pink"
              size="24"
            ></v-progress-circular>
          </template>
        </v-switch>

        <v-slider
          v-model="periodicUpdatesInterval"
          thumb-label="always"
          label="Interval (in seconds)"
          min="2"
          @change="periodicUpdatesTimer = 0"
          v-bind:disabled="!periodicUpdates"
        ></v-slider>

        <v-slider
          v-model="batteryLevel"
          thumb-label="always"
          label="Battery level"
          @change="setBatteryLevel()"
        ></v-slider>

        <v-btn-toggle v-model="facet" @change="setFacet()" id="facetToggle">
          <v-btn v-bind:key="f" v-for="f in facets">
            {{ f + 1 }}
          </v-btn>
        </v-btn-toggle>
      </v-card-text>
    </v-card>

    <div class="text-center ma-2">
      <v-snackbar v-model="snackbar" :timeout="4000">
        Could not send update to {{ updateUrl }}.
        <template v-slot:action="{ attrs }">
          <v-btn color="pink" text v-bind="attrs" @click="snackbar = false">
            Close
          </v-btn>
        </template>
      </v-snackbar>
    </div>
  </div>
</template>

<script>
import axios from "axios";
export default {
  name: "Simulator",
  props: ["updateUrl"],
  data: () => ({
    deviceName: "FakeTimeFlip",
    deviceAddress: "01:23:45:67:89:AB",
    newDeviceName: "FakeTimeFlip",
    newDeviceAddress: "01:23:45:67:89:AB",
    showDeviceDialog: false,
    connected: false,
    periodicUpdates: true,
    periodicUpdatesInterval: 10,
    periodicUpdatesTimer: 0,
    interval: {},
    batteryLevel: 100,
    facet: 0,
    facets: [...Array(12).keys()],
    snackbar: false,
  }),
  beforeDestroy() {
    clearInterval(this.interval);
  },
  mounted() {
    this.interval = setInterval(() => {
      if (this.connected && this.periodicUpdates) {
        if (this.periodicUpdatesTimer >= 100) {
          this.sendUpdate();
          return (this.periodicUpdatesTimer = 0);
        }
        this.periodicUpdatesTimer += 100 / (this.periodicUpdatesInterval - 1);
      }
    }, 1000);
  },
  methods: {
    cancelEditDevice: function () {
      this.newDeviceName = this.deviceName;
      this.newDeviceAddress = this.deviceAddress;
      this.showDeviceDialog = false;
    },
    updateDevice: function () {
      if (this.connected) {
        this.connected = false;
        this.sendUpdate();
        this.periodicUpdatesTimer = 0;
      }
      this.deviceName = this.newDeviceName;
      this.deviceAddress = this.newDeviceAddress;
      this.showDeviceDialog = false;
    },
    toggleConnect: function () {
      this.periodicUpdatesTimer = 0;
      this.sendUpdate();
    },
    setBatteryLevel: function () {
      if (this.connected) {
        this.sendUpdate();
      }
    },
    setFacet: function () {
      if (this.connected) {
        this.sendUpdate();
      }
    },
    sendUpdate: function () {
      const body = {
        deviceAddress: this.deviceAddress,
        deviceName: this.deviceName,
        connected: this.connected,
      };
      if (this.connected) {
        body.facet = this.facet + 1;
        body.batteryLevel = this.batteryLevel;
      }
      axios
        .post(
          this.updateUrl,
          body,
          {
            headers: {
              Authorization: `Bearer ${process.env.VUE_APP_JWT}`,
            },
          }
        )
        .catch(() => {
          this.snackbar = true;
        });
    },
  },
};
</script>

<style>
label[for="periodicUpdatesSpinner"] {
  justify-content: space-between;
}
#facetToggle {
  flex-wrap: wrap;
}
</style>