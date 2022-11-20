<template>
  <v-col cols="12" sm="4">
    <v-dialog v-model="dialog" persistent max-width="600px">
      <template v-slot:activator="{ on, attrs }">
        <v-btn color="warning" block v-bind="attrs" v-on="on">
          <v-icon left> mdi-creation </v-icon>
          {{ $t("gameRoom.pregame.createTeam.title") }}
        </v-btn>
      </template>
      <v-card>
        <v-card-title>
          <span class="headline">{{
            $t("gameRoom.pregame.createTeam.title")
          }}</span>
        </v-card-title>
        <v-card-text>
          <v-container>
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="teamName"
                  :rules="[
                    (value) =>
                      (value || '').length <= 16 || 'Max 16 characters',
                  ]"
                  :label="$t('gameRoom.pregame.createTeam.teamName')"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="error" text @click="dialog = false">
            {{ $t("gameRoom.pregame.createTeam.cancel") }}
          </v-btn>
          <v-btn
            color="primary"
            text
            @click="
              createTeam($parent);
              dialog = false;
            "
          >
            {{ $t("gameRoom.pregame.createTeam.create") }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-col>
</template>

<script>
export default {
  name: "CreateTeamButton",
  data() {
    return {
      validTeam: true,
      dialog: false,
      teamName: "",
    };
  },
  methods: {
    createTeam(parent) {
      parent.$parent.$emit("createTeam", this.teamName);
    },
  },
};
</script>
