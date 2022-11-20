<template>
  <v-container>
    <v-row justify="space-between">
      <v-col xs="10" sm="8" md="6" lg="4">
        <v-text-field
          v-model="search"
          class="mb-4"
          append-icon="mdi-magnify"
          label="Search"
          single-line
          outlined
          hide-details
          dense
        ></v-text-field>
      </v-col>
      <v-col cols="2" class="text-right">
        <v-btn icon color="secondary" @click="refresh()">
          <v-icon>mdi-reload</v-icon>
        </v-btn>
      </v-col>
    </v-row>
    <v-data-table
      :headers="headers"
      :items="timeFlips"
      sort-by="timeFlipId"
      :search="search"
      mobile-breakpoint="860"
    >
      <template v-slot:item.status="{ item }">
        <v-icon
          align="center"
          class="pl-5"
          :color="mapStatusToColor(item.status)"
          >mdi-circle</v-icon
        >
      </template>
      <template v-slot:item.configured="{ item }">
        <v-icon
          align="center"
          class="pl-5"
          :color="mapConfigurationToColor(item.timeFlipFacetMap)"
          >mdi-circle</v-icon
        >
      </template>
      <template v-slot:item.modify="{ item }">
        <TimeFlipDialog :timeFlip="item" @dialogClosed="dialogClosed" />
      </template>
    </v-data-table>
  </v-container>
</template>

<script>
import TimeFlipDialog from "@/components/timeflip/TimeFlipDialog";
export default {
  name: "TimeFlipList",
  components: { TimeFlipDialog },
  props: {
    timeFlips: Array,
  },
  data() {
    return {
      search: "",
      headers: [
        {
          text: this.$t("timeFlip.settings.list.id"),
          align: "start",
          value: "timeFlipId",
          class: "font-weight-bold subtitle-1",
        },
        {
          text: this.$t("timeFlip.settings.list.deviceAddress"),
          align: "start",
          value: "deviceAddress",
          class: "font-weight-bold subtitle-1",
        },
        {
          text: this.$t("timeFlip.settings.list.deviceName"),
          align: "start",
          value: "deviceName",
          class: "font-weight-bold subtitle-1",
        },
        {
          text: this.$t("timeFlip.settings.list.available"),
          value: "status",
          class: "font-weight-bold subtitle-1",
        },
        {
          text: this.$t("timeFlip.settings.configured"),
          value: "configured",
          sortable: false,
          class: "font-weight-bold subtitle-1",
        },
        {
          text: this.$t("timeFlip.settings.list.batteryLevel"),
          align: "start",
          value: "batteryLevel",
          class: "font-weight-bold subtitle-1",
        },
        { text: "", value: "modify", sortable: false, align: "end" },
      ],
    };
  },
  methods: {
    mapStatusToColor(status) {
      switch (status) {
        case "ACTIVE":
          return "green";
        case "PENDING":
          return "orange";
        case "INACTIVE":
          return "red";
      }
    },
    mapConfigurationToColor(config) {
      if (Object.keys(config).length === 0) {
        return "red";
      } else {
        return "green";
      }
    },
    dialogClosed() {
      this.$emit("dialogClosed");
    },
    refresh() {
      this.$emit("refresh");
    },
  },
};
</script>

<style scoped></style>
