<script setup>
import { ref } from 'vue'
import { useAuth } from '../composables/useAuth.js'

const { utilisateur, estConnecte, estAdmin, login, fetchMe, logout } = useAuth()

const dialogProfil = ref(false)
const email = ref('')
const password = ref('')
const afficherMdp = ref(false)
const erreur = ref('')
const chargement = ref(false)

async function seConnecter() {
    erreur.value = ''
    chargement.value = true
    try {
        await login(email.value, password.value)
        await fetchMe() // récupère nom/prénom si disponible
        dialogProfil.value = false
        email.value = ''
        password.value = ''
    } catch (e) {
        erreur.value = e.message
    } finally {
        chargement.value = false
    }
}

function onProfilClick() {
    if (estConnecte.value) {
        logout()
    } else {
        dialogProfil.value = true
    }
}
</script>

<template>
    <v-app-bar flat height="64"
        style="background: linear-gradient(135deg, #6B3FA0 0%, #8B4FC8 25%, #C4507A 65%, #E8734A 100%);">
        <img src="../assets/logo-isis.png" height="100%" class="ml-3" />

        <v-app-bar-title class="text-center">
            <v-btn variant="text" color="white" :to="{ name: 'catalogue' }" class="text-body-1 font-weight-bold">
                Catalogue
            </v-btn>
            <v-btn variant="text" color="white" :to="{ name: 'ressources' }" class="text-body-1 font-weight-bold">
                Ressources
            </v-btn>
            <v-btn v-if="estConnecte && !estAdmin" variant="text" color="white" :to="{ name: 'mes-actions' }"
                class="text-body-1 font-weight-bold">
                Mes actions
            </v-btn>
            <v-btn v-if="estAdmin" variant="text" color="white" :to="{ name: 'actions' }"
                class="text-body-1 font-weight-bold">
                Actions
            </v-btn>
            <v-btn v-if="estAdmin" variant="text" color="white" :to="{ name: 'statistiques' }"
                class="text-body-1 font-weight-bold">
                Statistiques
            </v-btn>
        </v-app-bar-title>

        <v-btn variant="flat" color="bleu" rounded="xl" class="mx-3" @click="onProfilClick">
            <v-icon class="mr-2">mdi-account</v-icon>
            <span v-if="estConnecte && utilisateur">{{ utilisateur.prenom }} {{ utilisateur.nom }}</span>
            <span v-else>Mon Profil</span>
        </v-btn>
    </v-app-bar>

    <!-- ── Dialog Connexion ── -->
    <v-dialog v-model="dialogProfil" max-width="400">
        <v-card rounded="xl">
            <v-card-title class="pa-5 pb-2">
                <h3 class="font-weight-bold">Bienvenue</h3>
                <h6 class="text-medium-emphasis font-weight-regular">Connectez-vous à votre espace</h6>
            </v-card-title>

            <v-card-text class="pa-5 pt-3">
                <v-alert v-if="erreur" type="error" variant="tonal" density="compact" class="mb-4">
                    {{ erreur }}
                </v-alert>

                <v-text-field v-model="email" label="Email" type="email" variant="outlined" rounded="lg"
                    density="comfortable" class="mb-3" hide-details />

                <v-text-field v-model="password" label="Mot de passe" :type="afficherMdp ? 'text' : 'password'"
                    variant="outlined" rounded="lg" density="comfortable"
                    :append-inner-icon="afficherMdp ? 'mdi-eye-off' : 'mdi-eye'"
                    @click:append-inner="afficherMdp = !afficherMdp" hide-details @keyup.enter="seConnecter" />
            </v-card-text>

            <v-card-actions class="pa-5 pt-0 d-flex ga-2">
                <v-btn variant="text" rounded="xl" @click="dialogProfil = false">
                    Annuler
                </v-btn>
                <v-spacer />
                <v-btn color="bleu" variant="flat" rounded="xl" :loading="chargement" :disabled="!email || !password"
                    @click="seConnecter">
                    Se connecter
                </v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<style scoped>
.v-btn--active {
    color: rgb(var(--v-theme-bleu)) !important;
    text-decoration: underline;
    text-underline-offset: 4px;
}
</style>