<template>
  <v-sheet elevation="2" rounded>
    <h3 class="heading text-center py-2">Teams</h3>
    <v-divider></v-divider>
    <v-list class="rounded-b">
      <v-list-group
        v-for="team in teams"
        :key="team.id"
        v-model="team.active"
        no-action
        sub-group
        :color="team.color.toLowerCase()"
      >
        <template v-slot:activator>
          <v-list-item-content>
            <v-list-item-title v-text="team.color"></v-list-item-title>
          </v-list-item-content>
        </template>
        <v-list-item v-for="(player, index) in team.players" :key="index">
          <v-list-item-content>
            <v-list-item-title v-text="player.username"></v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list-group>
    </v-list>
  </v-sheet>
</template>

<script>
export default {
  name: "TeamsWithPlayers",
  props: {
    teams: Array,
  },
};
</script>
