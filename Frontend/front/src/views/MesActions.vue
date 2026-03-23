<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuth } from '../composables/useAuth.js'

const { authHeaders } = useAuth()

const API_BASE = 'https://api-ptut.up.railway.app'

const actions = ref([])
const loading = ref(true)

const TYPE_COLORS = {
    'SALON ÉTUDIANT': 'salon',
    'LYCÉE': 'lycee',
    'RÉSEAUX SOCIAUX': 'reseaux',
    'FORMATION': 'formation'
}

onMounted(async () => {
    try {
        const res = await fetch(`${API_BASE}/actions/mes-actions`, {
            headers: authHeaders()
        })
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        actions.value = await res.json()
    } catch (e) {
        console.log('Erreur API mes-actions:', e.message)
        // Données mockées en attendant la route
        actions.value = [
            {
                id: 1, type: 'SALON ÉTUDIANT', titre: 'Salon InfoSup – Toulouse',
                date: '24 janv.', lieu: 'Parc des Expos, Toulouse',
                statut: 'a_venir', preuve: null
            },
            {
                id: 2, type: 'FORMATION', titre: "Atelier : Pitcher l'école",
                date: '24 janv.', lieu: 'Campus principal',
                statut: 'en_cours', preuve: 'photo_atelier.jpg'
            },
            {
                id: 3, type: 'LYCÉE', titre: 'Lycée Bellevue – Albi',
                date: '10 févr.', lieu: 'Albi',
                statut: 'validee', preuve: 'attestation.pdf'
            },
            {
                id: 4, type: 'RÉSEAUX SOCIAUX', titre: 'Story Instagram – "Vie Campus"',
                date: 'Avant le 24 janv.',
                statut: 'a_venir', preuve: null
            },
        ]
    } finally {
        loading.value = false
    }
})

// ── Sections ──
const actionsAVenir = computed(() => actions.value.filter(a => a.statut === 'a_venir'))
const actionsEnCours = computed(() => actions.value.filter(a => a.statut === 'en_cours'))
const actionsValidee = computed(() => actions.value.filter(a => a.statut === 'validee'))

// ── Upload preuve ──
const dialogUpload = ref(false)
const actionSelectee = ref(null)
const fichierUpload = ref(null)
const uploadEnCours = ref(false)
const erreurUpload = ref('')

function ouvrirUpload(action) {
    actionSelectee.value = action
    fichierUpload.value = null
    erreurUpload.value = ''
    dialogUpload.value = true
}

async function soumettrePreuve() {
    if (!fichierUpload.value) return
    uploadEnCours.value = true
    erreurUpload.value = ''

    try {
        const formData = new FormData()
        formData.append('fichier', fichierUpload.value)

        const headers = authHeaders()
        // On enlève Content-Type pour laisser le browser le gérer avec le boundary
        delete headers['Content-Type']

        const res = await fetch(`${API_BASE}/actions/${actionSelectee.value.id}/preuve`, {
            method: 'POST',
            headers,
            body: formData
        })

        if (!res.ok) {
            const err = await res.json().catch(() => ({}))
            throw new Error(err.message || 'Erreur lors de l\'envoi')
        }

        // Met à jour le statut localement
        const action = actions.value.find(a => a.id === actionSelectee.value.id)
        if (action) {
            action.statut = 'en_cours'
            action.preuve = fichierUpload.value.name
        }

        dialogUpload.value = false
        actionSelectee.value = null

    } catch (e) {
        erreurUpload.value = e.message
    } finally {
        uploadEnCours.value = false
    }
}
</script>

<template>
    <v-container class="py-8" max-width="800">

        <h1 class="text-h4 font-weight-bold mb-8">Mes actions</h1>

        <!-- ── Chargement ── -->
        <div v-if="loading" class="d-flex justify-center py-12">
            <v-progress-circular indeterminate color="primary" />
        </div>

        <template v-else>

            <!-- ══════════════════════════════
                 SECTION — À venir
            ══════════════════════════════ -->
            <div class="mb-8">
                <div class="d-flex align-center ga-3 mb-4">
                    <h2 class="text-h6 font-weight-bold">À venir</h2>
                    <v-chip color="blue" variant="tonal" size="small">{{ actionsAVenir.length }}</v-chip>
                </div>

                <div v-if="actionsAVenir.length === 0" class="text-medium-emphasis text-body-2 py-2">
                    Aucune action à venir.
                </div>

                <v-card v-for="action in actionsAVenir" :key="action.id" rounded="lg" border elevation="0" class="mb-3">
                    <v-card-text class="pa-4">
                        <div class="d-flex align-center ga-3">
                            <v-chip :color="TYPE_COLORS[action.type]" variant="outlined" size="small" label
                                style="min-width: 130px; justify-content: center;">
                                {{ action.type }}
                            </v-chip>
                            <div class="flex-grow-1">
                                <div class="text-body-2 font-weight-semibold">{{ action.titre }}</div>
                                <div class="text-caption text-medium-emphasis">{{ action.date }}<span
                                        v-if="action.lieu"> · {{ action.lieu }}</span></div>
                            </div>
                            <v-btn color="bleu" variant="flat" rounded="xl" size="small" @click="ouvrirUpload(action)">
                                <v-icon class="mr-1" size="16">mdi-upload</v-icon>
                                Déposer ma preuve
                            </v-btn>
                        </div>
                    </v-card-text>
                </v-card>
            </div>

            <v-divider class="mb-8" />

            <!-- ══════════════════════════════
                 SECTION — En cours de traitement
            ══════════════════════════════ -->
            <div class="mb-8">
                <div class="d-flex align-center ga-3 mb-4">
                    <h2 class="text-h6 font-weight-bold">En cours de traitement</h2>
                    <v-chip color="warning" variant="tonal" size="small">{{ actionsEnCours.length }}</v-chip>
                </div>

                <div v-if="actionsEnCours.length === 0" class="text-medium-emphasis text-body-2 py-2">
                    Aucune action en cours de traitement.
                </div>

                <v-card v-for="action in actionsEnCours" :key="action.id" rounded="lg" border elevation="0"
                    class="mb-3">
                    <v-card-text class="pa-4">
                        <div class="d-flex align-center ga-3">
                            <v-chip :color="TYPE_COLORS[action.type]" variant="outlined" size="small" label
                                style="min-width: 130px; justify-content: center;">
                                {{ action.type }}
                            </v-chip>
                            <div class="flex-grow-1">
                                <div class="text-body-2 font-weight-semibold">{{ action.titre }}</div>
                                <div class="text-caption text-medium-emphasis">{{ action.date }}<span
                                        v-if="action.lieu"> · {{ action.lieu }}</span></div>
                                <div class="text-caption text-medium-emphasis mt-1">
                                    <v-icon size="12" class="mr-1">mdi-paperclip</v-icon>{{ action.preuve }}
                                </div>
                            </div>
                            <v-chip color="warning" variant="tonal" size="small">
                                <v-icon start size="14">mdi-clock-outline</v-icon>
                                Dossier en cours de traitement
                            </v-chip>
                        </div>
                    </v-card-text>
                </v-card>
            </div>

            <v-divider class="mb-8" />

            <!-- ══════════════════════════════
                 SECTION — Validées
            ══════════════════════════════ -->
            <div>
                <div class="d-flex align-center ga-3 mb-4">
                    <h2 class="text-h6 font-weight-bold">Actions validées</h2>
                    <v-chip color="success" variant="tonal" size="small">{{ actionsValidee.length }}</v-chip>
                </div>

                <div v-if="actionsValidee.length === 0" class="text-medium-emphasis text-body-2 py-2">
                    Aucune action validée pour l'instant.
                </div>

                <v-card v-for="action in actionsValidee" :key="action.id" rounded="lg" border elevation="0"
                    class="mb-3">
                    <v-card-text class="pa-4">
                        <div class="d-flex align-center ga-3">
                            <v-chip :color="TYPE_COLORS[action.type]" variant="outlined" size="small" label
                                style="min-width: 130px; justify-content: center;">
                                {{ action.type }}
                            </v-chip>
                            <div class="flex-grow-1">
                                <div class="text-body-2 font-weight-semibold">{{ action.titre }}</div>
                                <div class="text-caption text-medium-emphasis">{{ action.date }}<span
                                        v-if="action.lieu"> · {{ action.lieu }}</span></div>
                            </div>
                            <v-chip color="success" variant="tonal" size="small">
                                <v-icon start size="14">mdi-check-circle</v-icon>
                                Action validée
                            </v-chip>
                        </div>
                    </v-card-text>
                </v-card>
            </div>

        </template>

    </v-container>

    <!-- ── Dialog upload preuve ── -->
    <v-dialog v-model="dialogUpload" max-width="440">
        <v-card v-if="actionSelectee" rounded="xl">
            <v-card-title class="pa-5 pb-2 font-weight-bold">
                Déposer ma preuve
            </v-card-title>
            <v-card-subtitle class="px-5 pb-3 text-medium-emphasis">
                {{ actionSelectee.titre }}
            </v-card-subtitle>

            <v-card-text class="pa-5 pt-2">
                <v-alert v-if="erreurUpload" type="error" variant="tonal" density="compact" class="mb-3">
                    {{ erreurUpload }}
                </v-alert>
                <v-file-input v-model="fichierUpload" label="Sélectionner un fichier" variant="outlined" rounded="lg"
                    density="comfortable" accept="image/*,.pdf" prepend-icon="" prepend-inner-icon="mdi-paperclip"
                    hide-details />
            </v-card-text>

            <v-card-actions class="pa-5 pt-0 d-flex ga-2">
                <v-btn variant="text" rounded="xl" @click="dialogUpload = false">
                    Annuler
                </v-btn>
                <v-spacer />
                <v-btn color="bleu" variant="flat" rounded="xl" :loading="uploadEnCours" :disabled="!fichierUpload"
                    @click="soumettrePreuve">
                    Envoyer
                </v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>