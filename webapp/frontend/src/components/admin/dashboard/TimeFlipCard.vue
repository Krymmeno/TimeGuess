<template>
  <v-col cols="12" md="4">
    <v-sheet elevation="2" rounded>
      <h3
        class="heading pa-2 primary rounded-t white--text d-flex justify-space-between align-center"
      >
        TimeFlips
        <v-btn x-small outlined color="amber" to="/timeFlip/settings">
          {{ $t("dashboard.timeFlips.manage") }}
        </v-btn>
      </h3>
      <v-alert
        dense
        text
        type="info"
        class="mt-3"
        v-if="timeFlips.length === 0"
      >
        {{ $t("dashboard.timeFlips.noTimeFlips") }}
      </v-alert>
      <div
        class="pa-3 pt-4"
        :key="index"
        v-for="(timeFlip, index) in timeFlips"
      >
        <div class="d-flex justify-space-between">
          <span class="body-1">{{ timeFlip.deviceName }}</span>
        </div>
        <v-divider></v-divider>
      </div>
    </v-sheet>
  </v-col>
</template>

<script>
export default {
  name: "TimeFlipCard",
  props: {
    timeFlips: Array,
  },
};
</script>
