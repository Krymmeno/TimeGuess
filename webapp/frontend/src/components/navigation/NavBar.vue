<template>
  <v-app-bar app>
    <v-container class="d-flex">
      <v-row justify="center">
        <v-col cols="12" xl="8" class="d-flex">
          <router-link
            to="/gamelobby"
            class="d-flex app-name"
            :style="{ color: this.$vuetify.theme.currentTheme.appTitle }"
          >
            <v-img
              alt="Logo"
              class="shrink mr-2"
              contain
              :src="require('@/assets/logo.png')"
              transition="scale-transition"
              width="40"
            />
            <v-toolbar-title class="title align-self-center hidden-xs-only">
              TIME<span class="font-weight-light">GUESS</span>
            </v-toolbar-title>
          </router-link>
          <v-spacer></v-spacer>
          <v-menu
            offset-y
            bottom
            left
            v-if="
              this.$store.getters['user/getUser']
                ? this.$store.getters['user/getUser'].role !== 'PLAYER'
                : false
            "
          >
            <template v-slot:activator="{ on, attrs }">
              <v-btn class="align-self-center" v-bind="attrs" v-on="on" plain>
                <v-icon> mdi-tune </v-icon>
                <span class="hidden-xs-only">Management</span>
              </v-btn>
            </template>
            <v-list dense>
              <v-list-item
                color="primary"
                v-for="(item, index) in managementItems"
                :key="index"
                link
                :to="item.href"
              >
                <v-list-item-title>
                  <v-icon left>
                    {{ item.icon }}
                  </v-icon>
                  {{ $t(`navbar.management.${item.title}`) }}
                </v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>
          <v-divider vertical class="mx-5"></v-divider>
          <span class="mr-5 align-self-center hidden-sm-and-down"
            >{{ $t("navbar.loggedInText") }}
            {{
              this.$store.getters["user/getUser"]
                ? this.$store.getters["user/getUser"].username
                : ""
            }}
          </span>
          <v-menu offset-y bottom left :close-on-content-click="false">
            <template v-slot:activator="{ on, attrs }">
              <v-btn icon color="dark" v-bind="attrs" v-on="on">
                <v-icon> mdi-account-circle-outline </v-icon>
              </v-btn>
            </template>
            <v-list dense>
              <v-list-item
                color="primary"
                v-for="(item, index) in dropdownItems"
                :key="index"
                link
                :to="item.href"
              >
                <v-list-item-title>
                  <v-icon left>
                    {{ item.icon }}
                  </v-icon>
                  {{ $t(`navbar.dropdown.${item.title}`) }}
                </v-list-item-title>
              </v-list-item>
              <v-divider></v-divider>
              <v-switch
                v-model="$vuetify.theme.dark"
                inset
                dense
                class="px-5"
                :label="$vuetify.theme.dark ? '🌙' : '☀️'"
                @click="changeTheme"
              ></v-switch>
              <v-divider></v-divider>
              <v-list-item color="error" @click="doLogout">
                <v-list-item-title class="red--text">
                  <v-icon left color="error"> mdi-logout </v-icon>
                  Logout
                </v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>
          <v-menu offset-y bottom left>
            <template v-slot:activator="{ on, attrs }">
              <v-btn icon color="dark" v-bind="attrs" v-on="on">
                <v-icon> mdi-translate </v-icon>
              </v-btn>
            </template>
            <v-list dense>
              <v-subheader>{{ $t("navbar.language.title") }}</v-subheader>
              <v-list-item-group
                v-model="selectedLanguage"
                mandatory
                color="primary"
              >
                <v-list-item
                  v-for="(language, index) in languages"
                  :key="index"
                  @click="setLocale(language.lang, index)"
                >
                  <v-list-item-content>
                    <v-list-item-title
                      class="text-uppercase"
                      v-html="
                        parsedEmoji(language.lang) +
                        $t(`navbar.language.${language.text}`)
                      "
                    >
                    </v-list-item-title>
                  </v-list-item-content>
                </v-list-item>
              </v-list-item-group>
            </v-list>
          </v-menu>
        </v-col>
      </v-row>
    </v-container>
  </v-app-bar>
</template>

<script>
import twemoji from "twemoji";
export default {
  name: "NavBar",
  data() {
    return {
      dropdownItems: [
        {
          title: "profile",
          icon: "mdi-account-outline",
          href: "/profile",
        },
        {
          title: "settings",
          icon: "mdi-account-cog-outline",
          href: "/settings",
        },
        {
          title: "users",
          icon: "mdi-account-multiple-outline",
          href: "/userManagement",
        },
      ],
      managementItems: [
        {
          title: "dashboard",
          icon: "mdi-view-dashboard-outline",
          href: "/dashboard",
        },
        {
          title: "userManagement",
          icon: "mdi-account-supervisor",
          href: "/userManagement",
        },
        {
          title: "gameSettings",
          icon: "mdi-gamepad-square-outline",
          href: "/game/settings",
        },
        {
          title: "timeFlipSettings",
          icon: "mdi-dice-d12-outline",
          href: "/timeFlip/settings",
        },
      ],
      languages: [
        {
          lang: "en",
          text: "english",
        },
        {
          lang: "de",
          text: "german",
        },
      ],
      selectedLanguage: parseInt(localStorage.getItem("timeGuessLocale")) || 0,
    };
  },
  methods: {
    doLogout() {
      this.$router.push("/login");
      setTimeout(() => {
        this.$store.dispatch("auth/logout").then(() => {
          this.$notify({
            title: this.$t("notification.success"),
            text: this.$t("notification.logoutSuccess"),
            type: "success",
          });
        });
      }, 500);
    },
    setLocale(locale, index) {
      this.$i18n.locale = locale;
      localStorage.setItem("timeGuessLocale", index);
    },
    changeTheme() {
      localStorage.setItem(
        "timeGuessDarkTheme",
        this.$vuetify.theme.dark.toString()
      );
    },
    parsedEmoji(string) {
      switch (string) {
        case "de":
          return twemoji.parse("🇦🇹");
        default:
          return twemoji.parse("🇬🇧");
      }
    },
  },
};
</script>

<style>
img.emoji {
  height: 1.2em;
  width: 1.2em;
  margin: 0 0.25em;
  vertical-align: -0.25em;
}
.app-name {
  text-decoration: none;
}
</style>
