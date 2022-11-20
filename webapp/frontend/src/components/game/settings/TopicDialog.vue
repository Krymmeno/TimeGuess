<template>
  <v-dialog v-model="dialog" fullscreen hide-overlay>
    <template v-slot:activator="{ on, attrs }">
      <v-btn
        small
        :icon="button.isIcon"
        :fab="!button.isIcon"
        :color="button.color"
        v-bind="attrs"
        v-on="on"
      >
        <v-icon>
          {{ button.icon }}
        </v-icon>
      </v-btn>
    </template>
    <v-card>
      <v-toolbar dark color="primary">
        <v-btn icon dark @click="closeDialog">
          <v-icon>mdi-close</v-icon>
        </v-btn>
        <v-toolbar-title
          >{{ newTopic.name + " " + $t("gameSettings.topicDialog.settings") }}
        </v-toolbar-title>
      </v-toolbar>
      <v-container>
        <v-row justify="center" class="pt-7">
          <v-col cols="12" xl="8">
            <v-row justify="center">
              <v-col cols="auto" align-self="center" class="text-h5">
                {{ $t("gameSettings.topicDialog.name") }}
              </v-col>
              <v-col colsclass="ma-0" align-self="center">
                <v-form v-model="nameValid" @submit.prevent="topicChanged">
                  <v-text-field
                    v-model="name"
                    placeholder="Your Topic name"
                    :rules="namingRules"
                    single-line
                    outlined
                    hide-details
                    dense
                    class="ma-4"
                  >
                    <!--append-outer-icon="mdi-check"
                    @click:append-outer="topicChanged"-->
                    <template v-slot:append-outer>
                      <v-btn icon type="submit" :disabled="!nameValid">
                        <v-icon> mdi-check </v-icon>
                      </v-btn>
                    </template>
                  </v-text-field>
                </v-form>
              </v-col>
            </v-row>
            <div v-if="!newTopic.isNew">
              <div class="text-h5 mt-4">
                {{ $t("gameSettings.topicDialog.terms") }}
              </div>
              <TermList
                :term-list="newTopic.terms"
                @addTerm="addTerm"
                @deleteTerm="deleteTerm"
                @termChanged="termChanged"
                @importTerms="importTerms"
                @downloadTerms="downloadTerms"
              ></TermList>
            </div>
          </v-col>
        </v-row>
      </v-container>
    </v-card>
  </v-dialog>
</template>

<script>
import TermList from "@/components/game/settings/TermList";
import { addTopic, updateTopic, importTerms } from "@/services/topics.service";

import { addTerm, updateTerm, deleteTerm } from "@/services/term.service";

export default {
  name: "TopicDialog",
  components: { TermList },
  props: {
    topic: {
      type: Object,
      default: function () {
        return {
          topicId: -1,
          name: "newTopic",
          terms: [],
        };
      },
    },
    createTopic: {
      type: Boolean,
      default: false,
    },
    button: {
      type: Object,
      default: function () {
        return {
          color: "green",
          icon: "mdi-pencil",
          isIcon: true,
        };
      },
    },
  },
  data() {
    return {
      dialog: false,
      newTopic: {},
      timer: null,
      name: "",
      namingRules: [
        (value) => !!value || "gameSettings.createTopic.emptyForm",
        (value) => /^(\S\S*.)/.test(value),
      ],
      nameValid: false,
    };
  },
  methods: {
    init() {
      if (!this.createTopic) {
        this.newTopic = this.topic;
      } else {
        this.newTopic = this.getNewTopic();
      }
      this.name = this.newTopic.name;
    },
    closeDialog() {
      this.dialog = false;
      this.$emit("dialogClosed");
    },
    topicChanged() {
      if (this.nameValid) {
        if (this.newTopic.isNew) {
          addTopic(this.name)
            .then((topic) => {
              this.$notify({
                title: this.$t("notification.success"),
                text: this.$t("notification.topicSaved") + " " + topic.name,
                type: "success",
              });
              topic.terms = [];
              this.newTopic = topic;
              this.name = this.newTopic.name;
            })
            .catch((error) => {
              this.$notify({
                title: this.$t("notification.error") + error.response.status,
                text:
                  this.$t("notification.topicNotSaved") +
                  " " +
                  this.newTopic.name,
                type: "error",
              });
            });
        } else {
          updateTopic(this.newTopic.topicId, this.name)
            .then((topic) => {
              this.$notify({
                title: this.$t("notification.success"),
                text: this.$t("notification.topicChanged") + " " + topic.name,
                type: "success",
              });
              this.newTopic = topic;
              this.name = this.newTopic.name;
            })
            .catch((error) => {
              this.$notify({
                title: this.$t("notification.error") + error.response.status,
                text:
                  this.$t("notification.topicNotSaved") +
                  " " +
                  this.newTopic.name,
                type: "error",
              });
            });
        }
      }
    },
    addTerm({ term, onSuccess }) {
      addTerm(this.newTopic.topicId, term)
        .then((newTerm) => {
          this.newTopic.terms.push(newTerm);
          this.$notify({
            title: this.$t("notification.success"),
            text: this.$t("notification.termAdded") + ": " + term,
            type: "success",
          });
          onSuccess();
        })
        .catch((error) => {
          if (error.response.status === 409) {
            this.$notify({
              title: this.$t("notification.error") + error.response.status,
              text: this.$t("notification.duplicateTerm") + ": " + term,
              type: "error",
            });
          } else {
            this.$notify({
              title: this.$t("notification.error") + error.response.status,
              text: this.$t("notification.termNotSaved") + ": " + term,
              type: "error",
            });
          }
        });
    },
    getNewTopic() {
      return { topicId: -1, name: "", terms: [], isNew: true };
    },
    termChanged(term, backup) {
      updateTerm(term.termId, term.name)
        .then((changed) => {
          term.termId = changed.termId;
          term.name = changed.name;
          this.$notify({
            title: this.$t("notification.success"),
            text: this.$t("notification.termChanged") + " " + term.name,
            type: "success",
          });
        })
        .catch((error) => {
          term.termId = backup.termId;
          term.name = backup.name;
          this.$notify({
            title: this.$t("notification.error") + error.response.status,
            text: this.$t("notification.termNotSaved") + " " + term.name,
            type: "error",
          });
        });
    },
    deleteTerm(term) {
      deleteTerm(term.termId)
        .then(() => {
          let index = this.newTopic.terms.indexOf(term);
          if (index > -1) {
            this.newTopic.terms.splice(index, 1);
            this.$notify({
              title: this.$t("notification.success"),
              text: this.$t("notification.termDeleted") + ": " + term.name,
              type: "success",
            });
          }
        })
        .catch((error) => {
          this.$notify({
            title: this.$t("notification.error") + error.response.status,
            text: this.$t("notification.termNotDeleted") + ": " + term.name,
            type: "error",
          });
        });
    },
    importTerms(terms) {
      importTerms(this.newTopic.topicId, terms)
        .then((topic) => {
          this.newTopic = topic;
          this.$notify({
            title: this.$t("notification.success"),
            text: this.$t("notification.termsImported"),
            type: "success",
          });
        })
        .catch((error) => {
          this.$notify({
            title: this.$t("notification.error") + error.response.status,
            text: this.$t("notification.termsNotImported"),
            type: "error",
          });
        });
    },
    downloadTerms() {
      let fileURL = window.URL.createObjectURL(
        new Blob([
          JSON.stringify(this.convertTermsToStrings(this.newTopic.terms)),
        ])
      );
      let fileLink = document.createElement("a");

      fileLink.href = fileURL;
      fileLink.setAttribute("download", `topic_${this.newTopic.name}.json`);
      document.body.appendChild(fileLink);

      fileLink.click();
    },
    convertTermsToStrings(terms) {
      let strings = [];
      for (let term of terms) {
        console.log(term);
        console.log(terms);
        strings.push(term.name);
      }
      return strings;
    },
  },
  watch: {
    dialog(visible) {
      if (visible) {
        this.init();
      }
    },
  },
};
</script>

<style scoped></style>
