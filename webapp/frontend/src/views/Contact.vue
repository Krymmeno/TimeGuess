<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" xl="8">
        <h1 class="display-3 mt-16 mb-5">{{ $t("footer.contact.title") }}</h1>
        <v-divider class="mb-16"></v-divider>
        <h2
          class="headline text-center mb-16"
          v-html="$t('footer.contact.text')"
        ></h2>
        <v-dialog v-model="dialog" width="500" persistent>
          <v-card>
            <v-card-title class="headline">
              {{ $t("footer.contact.dialog.title") }}
            </v-card-title>

            <v-card-text>
              {{ $t("footer.contact.dialog.text") }}
            </v-card-text>

            <v-divider></v-divider>

            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="primary" text @click="dialog = false">
                {{ $t("footer.contact.dialog.understand") }}
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        <v-form ref="form" v-model="validForm" lazy-validation>
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="name"
                :rules="nameRules"
                :label="$t('footer.contact.form.name')"
                required
              ></v-text-field>
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="email"
                :rules="emailRules"
                :label="$t('footer.contact.form.email')"
                required
              ></v-text-field>
            </v-col>
          </v-row>
          <v-row>
            <v-col cols="12">
              <v-textarea
                v-model="message"
                :rules="messageRules"
                :label="$t('footer.contact.form.message')"
                auto-grow
              ></v-textarea>
            </v-col>
          </v-row>
          <v-row>
            <v-col class="d-flex">
              <v-spacer></v-spacer>
              <v-btn :disabled="!validForm" color="primary" @click="submit">
                {{ $t("footer.contact.form.submit") }}
              </v-btn>
            </v-col>
          </v-row>
        </v-form>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
export default {
  name: "Contact",
  data() {
    return {
      dialog: false,
      validForm: true,
      name: "",
      email: "",
      message: "",
      nameRules: [(v) => !!v || this.$t("footer.contact.form.rules.name")],
      emailRules: [
        (v) => !!v || this.$t("footer.contact.form.rules.email"),
        (v) =>
          /.+@.+\..+/.test(v) ||
          this.$t("footer.contact.form.rules.emailValid"),
      ],
      messageRules: [
        (v) => !!v || this.$t("footer.contact.form.rules.message"),
      ],
    };
  },
  methods: {
    submit() {
      if (this.$refs.form.validate()) {
        this.dialog = true;
        this.$refs.form.reset();
      }
    },
  },
};
</script>
