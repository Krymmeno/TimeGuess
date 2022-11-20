<template>
  <v-row justify="center">
    <v-dialog v-model="dialog" persistent max-width="500px">
      <template v-slot:activator="{ on, attrs }">
        <v-btn
          x-large
          color="success"
          v-bind="attrs"
          v-on="on"
          @click="loadTopics"
        >
          {{ $t("gameLobby.createGame.createGameButton") }}
          <v-icon right> mdi-plus </v-icon>
        </v-btn>
      </template>
      <v-card>
        <v-card-title>
          <span class="headline">{{ $t("gameLobby.createGame.title") }}</span>
        </v-card-title>
        <v-divider class="mb-5"></v-divider>
        <v-card-text>
          <v-alert v-show="!validCreation" dense text type="error">
            {{ $t("gameLobby.createGame.error") }}
          </v-alert>
          <v-container>
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="createGame.roomName"
                  :label="$t('gameLobby.createGame.roomName')"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-select
                  v-model="createGame.topicId"
                  :items="availableTopics"
                  :label="$t('gameLobby.createGame.topic')"
                  required
                ></v-select>
              </v-col>
              <v-col cols="6">
                <v-select
                  v-model="createGame.maxPoints"
                  :items="points"
                  :label="$t('gameLobby.createGame.points')"
                  required
                ></v-select>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="error" text @click="dialog = false">
            {{ $t("gameLobby.createGame.cancel") }}
          </v-btn>
          <v-btn color="primary" text @click="doCreate">
            {{ $t("gameLobby.createGame.create") }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-row>
</template>

<script>
import { getAvailableTopics } from "@/services/topics.service";
import { createGameRoom } from "@/services/gameroom.service";

export default {
  name: "CreateGameButton",
  data() {
    return {
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
      availableTopics: [],
      createGame: {
        topicId: 0,
        roomName: "",
        maxPoints: "",
      },
      validCreation: true,
      dialog: false,
    };
  },
  methods: {
    doCreate() {
      createGameRoom(this.createGame)
        .then((response) => {
          this.$router.push({
            path: `/gamerooms/${response.gameRoomId}`,
          });
        })
        .catch((error) => {
          if (error.response.status === 400) {
            this.validCreation = false;
          } else {
            this.dialog = false;
          }
        });
    },
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
  },
};
</script>

<style scoped></style>
