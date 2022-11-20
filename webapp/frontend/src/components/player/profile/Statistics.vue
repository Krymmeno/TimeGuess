<template>
  <v-card>
    <v-card-title class="font-weight-light primary white--text">
      {{ $t("statistic.title") }}
    </v-card-title>
    <v-container>
      <v-row justify="space-between">
        <v-col
          v-for="[name, value] in Object.entries(statistics)"
          :key="name"
          cols="auto"
          md="4"
        >
          <Statistic :statistic="prepareStatistic(name, value)"></Statistic>
        </v-col>
      </v-row>
    </v-container>
  </v-card>
</template>

<script>
import Statistic from "@/components/player/profile/Statistic";
export default {
  name: "Statistics",
  components: { Statistic },
  props: {
    statistics: Object,
  },
  methods: {
    prepareStatistic(name, value) {
      console.log(value);
      if (name.includes("Time")) {
        value = value / 1000;
      }
      return { name, value };
    },
  },
};
</script>

<style scoped></style>
