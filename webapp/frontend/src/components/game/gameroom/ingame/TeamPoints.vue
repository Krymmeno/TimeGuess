<template>
  <v-sheet elevation="2" rounded class="pa-5">
    <h2 class="display-1 mb-5">{{ $t("game.teamPoints") }}:</h2>
    <v-row>
      <v-col cols="12" class="float-right text--secondary">
        <p class="text-right">{{ maxPoints }}</p>
      </v-col>
    </v-row>
    <v-row :key="index" v-for="(team, index) in teams">
      <v-col cols="3" class="text-truncate">
        <h3 class="heading">
          {{ team.color }} <span class="overline">({{ team.points }})</span>
        </h3>
      </v-col>
      <v-col cols="9">
        <v-progress-linear
          class="my-2"
          :color="team.color.toLowerCase()"
          rounded
          :value="Math.floor((team.points / maxPoints) * 100)"
          stream
          buffer-value="0"
        ></v-progress-linear>
      </v-col>
      <v-col class="pt-0">
        <v-divider></v-divider>
      </v-col>
    </v-row>
  </v-sheet>
</template>

<script>
export default {
  name: "TeamPoints",
  props: {
    teams: Array,
    maxPoints: Number,
  },
};
</script>
