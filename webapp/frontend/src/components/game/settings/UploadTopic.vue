<template>
  <v-dialog v-model="dialog" max-width="500px">
    <template v-slot:activator="{ on, attrs }">
      <v-btn fab small color="green" v-bind="attrs" v-on="on">
        <v-icon>mdi-upload</v-icon>
      </v-btn>
    </template>
    <v-card align="center">
      <v-card-title>
        {{ $t("gameSettings.createTopic.uploadTitle") }}
        <v-card-text>
          {{ $t("gameSettings.createTopic.uploadInfo") }}
          <v-form v-model="valid" class="full-width">
            <v-container align="center">
              <v-row justify="space-around" align="center">
                <v-col cols="10">
                  <v-file-input
                    v-model="file"
                    :rules="uploadRules"
                    :loading="loading"
                    :disabled="loading"
                    accept=".json"
                    placeholder="yourTopics.json"
                  >
                    <template v-slot:append-outer>
                      <v-tooltip bottom>
                        <template v-slot:activator="{ on, attrs }">
                          <v-icon v-bind="attrs" v-on="on"
                            >mdi-help-circle
                          </v-icon>
                        </template>
                        <span class="text-center"
                          >{{ $t("gameSettings.createTopic.JSONInfo") }} <br />
                          <div>["cow", "sheep", "wolf"]</div>
                        </span>
                      </v-tooltip>
                    </template>
                  </v-file-input>
                </v-col>
              </v-row>
            </v-container>
          </v-form>
        </v-card-text>
      </v-card-title>
      <v-card-actions>
        <v-btn @click="parse" :disabled="!valid" color="primary">
          {{ $t("gameSettings.createTopic.import") }}
        </v-btn>
        <v-btn @click="dialog = false" text>
          {{ $t("gameSettings.createTopic.cancel") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  name: "CreateTopic",
  components: {},
  data() {
    return {
      uploadRules: [
        (value) => !!value || this.$t("gameSettings.createTopic.noFile"),
      ],
      dialog: false,
      valid: false,
      file: null,
      loading: false,
    };
  },
  methods: {
    parse() {
      let reader = new FileReader();
      reader.onload = () => {
        this.loading = false;
        try {
          let terms = JSON.parse(reader.result);
          this.$emit("importTerms", terms);
          this.dialog = false;
        } catch (e) {
          console.log(e);
          this.$notify({
            title: this.$t("notification.error"),
            text: this.$t("notification.parsingError"),
            type: "error",
          });
        }
      };
      reader.onerror = () => {
        this.$notify({
          title: this.$t("notification.error"),
          text: this.$t("notification.readFileError"),
          type: "error",
        });
      };
      reader.onloadstart = () => {
        this.loading = true;
      };
      reader.readAsText(this.file);
    },
  },
  watch: {
    file: function (value) {
      if (value === null) {
        this.loading = false;
        this.uploaded = false;
      }
    },
    dialog: function (value) {
      if (!value) {
        this.file = null;
      }
    },
  },
};
</script>

<style scoped></style>
