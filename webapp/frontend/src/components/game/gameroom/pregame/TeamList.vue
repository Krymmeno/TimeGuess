<template>
  <v-sheet rounded elevation="2">
    <h3 class="heading text-center py-2">Teams</h3>
    <v-divider></v-divider>
    <v-alert dense text type="info" class="mt-3" v-if="teams.length === 0">
      {{ $t("gameRoom.pregame.teamList.noTeam") }}
    </v-alert>
    <v-container v-else>
      <v-row>
        <v-col cols="12" md="6" :key="index" v-for="(team, index) in teams">
          <Team
            :game-room-id="gameRoomId"
            :all-players="players"
            :team="team"
            :game-host-id="gameHostId"
            :spectator="spectator"
          />
        </v-col>
      </v-row>
    </v-container>
  </v-sheet>
</template>

<script>
import Team from "@/components/game/gameroom/pregame/Team";
export default {
  name: "TeamList",
  components: { Team },
  props: {
    gameRoomId: Number,
    gameHostId: Number,
    players: Array,
    teams: Array,
    spectator: Boolean,
  },
};
</script>
