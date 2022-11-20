<template>
  <v-dialog v-model="dialog" fullscreen hide-overlay>
    <template v-slot:activator="{ on, attrs }">
      <v-btn small icon class="mr-2" color="green" v-bind="attrs" v-on="on">
        <v-icon> mdi-pencil </v-icon>
      </v-btn>
    </template>
    <v-card>
      <v-toolbar dark color="primary">
        <v-btn icon dark @click="closeDialog">
          <v-icon>mdi-close</v-icon>
        </v-btn>
        <v-toolbar-title>TimeFlip {{ newTimeFlip.timeFlipId }}</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-toolbar-items>
          <v-btn
            dark
            text
            @click="
              newTimeFlip.timeFlipFacetMap = getConfig(true);
              updateList++;
            "
          >
            {{ $t("timeFlip.settings.dialog.fill") }}
          </v-btn>
          <v-btn dark text @click="saveTimeFlip()" :disabled="!configValid">
            {{ $t("timeFlip.settings.dialog.save") }}
          </v-btn>
        </v-toolbar-items>
      </v-toolbar>
      <v-container class="pa-6" :key="updateList">
        <v-form v-model="configValid">
          <SideSettings
            v-for="[side, configuration] of newTimeFlip.timeFlipFacetMap"
            :key="side"
            :side="side"
            :configuration="configuration"
          ></SideSettings></v-form
      ></v-container>
    </v-card>
  </v-dialog>
</template>

<script>
import SideSettings from "@/components/timeflip/SideSettings";
import { updateTimeFlip } from "@/services/timeflip.service";
export default {
  name: "TimeFlipDialog",
  components: { SideSettings },
  props: {
    timeFlip: Object,
  },
  data() {
    return {
      updateList: 0,
      dialog: false,
      configValid: false,
      newTimeFlip: {
        timeFlipId: 1,
        deviceAddress: "deviceAddress",
        deviceName: "name",
        status: "status",
        timeFlipFacetMap: this.getConfig(),
      },
    };
  },
  methods: {
    saveTimeFlip() {
      updateTimeFlip(this.newTimeFlip)
        .then((timeFlip) => {
          this.newTimeFlip = timeFlip;
          this.closeDialog();
          this.$notify({
            title: this.$t("notification.success"),
            text: this.$t("notification.timeFlipChanged"),
            type: "success",
          });
        })
        .catch((error) => {
          this.$notify({
            title: this.$t("notification.error") + error.response.status,
            text:
              this.$t("notification.timeFlipNotChanged") +
              " " +
              this.newTopic.name,
            type: "error",
          });
        });
    },
    getConfig(random) {
      let actions = ["DRAW", "RHYME", "PANTOMIME", "SPEAK"];
      let points = ["ONE", "TWO", "THREE"];
      let time = ["ONE", "TWO", "THREE"];
      let map = new Map();
      for (let i = 1; i <= 12; i++) {
        let config = {
          activity: random ? actions[Math.floor(Math.random() * 4)] : null,
          roundPoints: random ? points[Math.floor(Math.random() * 3)] : null,
          time: random ? time[Math.floor(Math.random() * 3)] : null,
        };
        map.set(String(i), config);
      }
      return map;
    },
    init() {
      this.newTimeFlip = JSON.parse(JSON.stringify(this.timeFlip));
      this.newTimeFlip.timeFlipFacetMap = new Map(
        Object.entries(this.newTimeFlip.timeFlipFacetMap)
      );
      if (this.newTimeFlip.timeFlipFacetMap.size === 0) {
        this.newTimeFlip.timeFlipFacetMap = this.getConfig();
      }
      this.updateList++;
    },
    closeDialog() {
      this.dialog = false;
      this.$emit("dialogClosed");
    },
  },
  watch: {
    dialog(visible) {
      if (visible) {
        this.init();
      }
    },
  },
};
</script>

<style scoped></style>
