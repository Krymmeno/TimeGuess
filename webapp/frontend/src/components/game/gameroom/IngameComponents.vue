<template>
  <v-row>
    <v-col cols="12">
      <v-alert
        v-if="spectator"
        dense
        color="blue-grey"
        dark
        class="text-center mb-0"
      >
        {{ $t("game.spectating") }}
      </v-alert>
      <v-alert
        v-if="
          game
            ? game.currentRound.user.userId ===
              this.$store.getters['user/getUser'].userId
            : false
        "
        text
        color="primary"
        dark
        class="text-center title mb-0"
        dense
      >
        {{ $t("game.yourTurn") }}
      </v-alert>
    </v-col>
    <v-col cols="12" sm="12" md="4" class="hidden-sm-and-down">
      <TeamsWithPlayers :teams="game ? game.teams : []" />
    </v-col>
    <v-col cols="12" sm="12" md="8">
      <v-row>
        <v-col cols="12">
          <TermSheet
            :game="game"
            :time="game ? game.currentRound.time : ''"
            :round-time="roundTime"
            :round-timer="roundTimer"
            :max-time="maxTime"
            :spectator="spectator"
            @startRound="$emit('startRound')"
            @winRound="$emit('winRound')"
            @ruleViolation="$emit('ruleViolation')"
          />
        </v-col>
        <v-col cols="12">
          <TeamPoints
            :teams="game ? game.teams : []"
            :maxPoints="game ? maxPoints : 0"
          />
        </v-col>
      </v-row>
    </v-col>
  </v-row>
</template>

<script>
import TeamsWithPlayers from "@/components/game/gameroom/ingame/TeamsWithPlayers";
import TermSheet from "@/components/game/gameroom/ingame/TermSheet";
import TeamPoints from "@/components/game/gameroom/ingame/TeamPoints";
export default {
  name: "IngameComponents",
  components: { TeamPoints, TermSheet, TeamsWithPlayers },
  props: {
    game: Object,
    maxPoints: Number,
    roundTime: Number,
    roundTimer: Number,
    maxTime: Number,
    spectator: Boolean,
  },
};
</script>
