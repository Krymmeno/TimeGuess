<template>
  <v-container>
    <v-row>
      <v-col cols="12" sm="5">
        <v-select
          v-model="topic"
          :items="availableTopics"
          :label="$t('gameRoom.pregame.topic')"
          required
          :disabled="
            gameRoom
              ? gameRoom.gameHostId !==
                this.$store.getters['user/getUser'].userId
              : true
          "
          @focus="loadTopics"
          @change="changeTopic(topic)"
        ></v-select>
      </v-col>
      <v-col cols="12" sm="4">
        <v-select
          v-model="timeFlip"
          :items="timeFlips"
          items-text="devicename"
          items-value="timeflipid"
          :label="$t('gameRoom.pregame.timeFlip')"
          required
          :disabled="
            gameRoom
              ? gameRoom.gameHostId !==
                this.$store.getters['user/getUser'].userId
              : true
          "
          @click="loadTimeFlips"
          @keydown="loadTimeFlips"
          @change="changeTimeFlip(timeFlip)"
        ></v-select>
      </v-col>
      <v-col cols="12" sm="3">
        <v-select
          v-model="maxPoints"
          :items="points"
          :label="$t('gameRoom.pregame.maxPoints')"
          required
          :disabled="
            gameRoom
              ? gameRoom.gameHostId !==
                this.$store.getters['user/getUser'].userId
              : true
          "
          @change="changeMaxPoints(maxPoints)"
        ></v-select>
      </v-col>
      <v-col v-if="spectator">
        <v-alert dense color="blue-grey" dark class="text-center">
          {{ $t("gameRoom.spectating") }}
        </v-alert>
      </v-col>
      <GameRoomActions
        v-if="!spectator"
        :game-room-id="gameRoom ? gameRoom.gameRoomId : null"
        :all-players-ready="gameRoom ? gameRoom.allPlayersReady : false"
        :time-flip="timeFlip"
        :players="gameRoom ? gameRoom.gameRoomUsers : []"
        :game-host-id="gameRoom ? gameRoom.gameHostId : null"
        :game-creation-error="gameRoom ? gameRoom.gameCreationError : null"
        @startGame="$emit('startGame')"
        @leaveRoom="$emit('leaveRoom')"
      />
    </v-row>
    <v-row>
      <v-col cols="12" sm="6" md="4">
        <v-row>
          <v-col cols="12">
            <WaitingList
              :game-room-id="gameRoom ? gameRoom.gameRoomId : null"
              :players="gameRoom ? gameRoom.gameRoomUsers : []"
              :game-host-id="gameRoom ? gameRoom.gameHostId : null"
            />
          </v-col>
          <v-col cols="12">
            <InvitedList
              :game-host-id="gameRoom ? gameRoom.gameHostId : null"
              :game-room-id="gameRoom ? gameRoom.gameRoomId : null"
              :invited-players="gameRoom ? gameRoom.invitedUsers : []"
              :game-room-users="gameRoom ? gameRoom.gameRoomUsers : []"
            />
          </v-col>
        </v-row>
      </v-col>
      <v-col cols="12" sm="6" md="8">
        <TeamList
          :game-room-id="gameRoom ? gameRoom.gameRoomId : null"
          :players="gameRoom ? gameRoom.gameRoomUsers : []"
          :teams="gameRoom ? gameRoom.availableTeamsList : []"
          :game-host-id="gameRoom ? gameRoom.gameHostId : null"
          :spectator="spectator"
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import {
  setMaxPoints,
  setTopic,
  setTimeFlip,
} from "@/services/gameroom.service";
import TeamList from "@/components/game/gameroom/pregame/TeamList";
import WaitingList from "@/components/game/gameroom/pregame/WaitingList";
import InvitedList from "@/components/game/gameroom/pregame/InvitedList";
import GameRoomActions from "@/components/game/gameroom/pregame/GameRoomActions";
import { getAvailableTimeFlips } from "@/services/timeflip.service";
import { getAvailableTopics } from "@/services/topics.service";

export default {
  name: "PregameComponents",
  components: { GameRoomActions, WaitingList, InvitedList, TeamList },
  data() {
    return {
      topicId: 0,
      points: [
        {
          text: 10,
          value: "TEN",
        },
        {
          text: 20,
          value: "TWENTY",
        },
        {
          text: 30,
          value: "THIRTY",
        },
        {
          text: 40,
          value: "FORTY",
        },
        {
          text: 50,
          value: "FIFTY",
        },
        {
          text: 60,
          value: "SIXTY",
        },
        {
          text: 70,
          value: "SEVENTY",
        },
      ],
      timeFlips: [],
    };
  },
  props: {
    gameRoom: Object,
    topic: Number,
    maxPoints: String,
    availableTopics: Array,
    timeFlip: Number,
    spectator: Boolean,
  },
  methods: {
    async loadTopics() {
      await getAvailableTopics().then((topics) => {
        this.availableTopics = [];
        topics.forEach((topic) => {
          this.availableTopics.push({
            text: topic.name,
            value: topic.topicId,
          });
        });
      });
    },
    async loadTimeFlips() {
      await getAvailableTimeFlips().then((timeFlips) => {
        let allTimeFlips = [...timeFlips];
        if (this.gameRoom?.timeFlip) {
          allTimeFlips.push(this.gameRoom.timeFlip);
        }
        allTimeFlips.sort((a, b) => a.timeFlipId - b.timeFlipId);
        this.timeFlips = allTimeFlips.map((timeFlip) => {
          return {
            text: `${timeFlip.timeFlipId} - ${timeFlip.deviceName}`,
            value: timeFlip.timeFlipId,
          };
        });
      });
    },
    changeTopic(id) {
      console.log(this.topic);
      setTopic(this.$route.path, id);
    },
    changeMaxPoints(maxPoints) {
      console.log(this.maxPoints);
      setMaxPoints(this.$route.path, maxPoints);
    },
    changeTimeFlip(timeFlip) {
      setTimeFlip(this.$route.path, timeFlip);
    },
  },
};
</script>
