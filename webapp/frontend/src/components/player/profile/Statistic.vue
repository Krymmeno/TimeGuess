<template>
  <v-card flat>
    <v-card-subtitle class="pa-0">
      {{ $t("statistic." + statistic.name) }}
    </v-card-subtitle>
    <v-card-title class="pa-0">
      {{ statistic.value }}
    </v-card-title>
  </v-card>
</template>

<script>
export default {
  name: "Statistic",
  props: {
    statistic: Object,
  },
};
</script>

<style scoped></style>
