<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuth } from '../composables/useAuth.js'

const { authHeaders } = useAuth()

const API_BASE = 'https://api-ptut.up.railway.app'

const TYPE_COLORS = {
    'SALON ÉTUDIANT': 'salon',
    'LYCÉE': 'lycee',
    'RÉSEAUX SOCIAUX': 'reseaux',
    'FORMATION': 'formation'
}

// ── Preuves en attente (mockées en attendant la route) ──
const preuves = ref([
    { id: 1, etudiant: 'Jean Dupont', action: 'Salon InfoSup – Toulouse', fichier: 'photo_salon.jpg', type: 'SALON ÉTUDIANT' },
    { id: 2, etudiant: 'Marie Martin', action: 'Story Instagram – "Vie Campus"', fichier: 'capture_insta.png', type: 'RÉSEAUX SOCIAUX' },
    { id: 3, etudiant: 'Lucas Bernard', action: 'Lycée Bellevue – Albi', fichier: 'attestation.pdf', type: 'LYCÉE' },
    { id: 4, etudiant: 'Camille Leroy', action: "Atelier : Pitcher l'école", fichier: 'photo_atelier.jpg', type: 'FORMATION' },
])

function validerPreuve(id) {
    // 🔧 Remplacer par : await fetch(`${API_BASE}/preuves/${id}/valider`, { method: 'POST', headers: authHeaders() })
    preuves.value = preuves.value.filter(p => p.id !== id)
}

// ── Étudiants depuis l'API ──
const etudiants = ref([])
const loading = ref(true)

onMounted(async () => {
    try {
        const res = await fetch(`${API_BASE}/ambassadeurs`, {
            headers: authHeaders()
        })
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        const data = await res.json()
        // On adapte le format selon ce que retourne l'API
        etudiants.value = data.map(e => ({
            id: e.id,
            prenom: e.prenom,
            nom: e.nom,
            actions: e.actions ?? []
        }))
    } catch (e) {
        // Données mockées si l'API échoue
        etudiants.value = [
            {
                id: 1, prenom: 'Jean', nom: 'Dupont',
                actions: [
                    { id: 1, type: 'SALON ÉTUDIANT', titre: 'Salon InfoSup – Toulouse', date: '24 janv.' },
                    { id: 2, type: 'FORMATION', titre: "Atelier : Pitcher l'école", date: '24 janv.' },
                ]
            },
            {
                id: 2, prenom: 'Marie', nom: 'Martin',
                actions: [
                    { id: 4, type: 'RÉSEAUX SOCIAUX', titre: 'Story Instagram – "Vie Campus"', date: 'Avant le 24 janv.' },
                ]
            },
            {
                id: 3, prenom: 'Lucas', nom: 'Bernard',
                actions: [
                    { id: 5, type: 'SALON ÉTUDIANT', titre: 'Forum des métiers – Montpellier', date: '10 mars' },
                    { id: 6, type: 'FORMATION', titre: "Atelier : Pitcher l'école", date: '24 janv.' },
                ]
            },
        ]
    } finally {
        loading.value = false
    }
})

// ── Recherche ──
const recherche = ref('')

const etudiantsFiltres = computed(() => {
    const q = recherche.value.toLowerCase().trim()
    if (!q) return etudiants.value
    return etudiants.value.filter(e =>
        e.nom.toLowerCase().includes(q) ||
        e.prenom.toLowerCase().includes(q) ||
        `${e.prenom} ${e.nom}`.toLowerCase().includes(q)
    )
})

// ── Étudiant sélectionné ──
const etudiantSelec = ref(null)

function selectionner(etudiant) {
    etudiantSelec.value = etudiantSelec.value?.id === etudiant.id ? null : etudiant
}

// ── Preview fichier ──
const previewDialog = ref(false)
const previewFichier = ref(null)

function voirFichier(preuve) {
    previewFichier.value = preuve
    previewDialog.value = true
}
</script>

<template>
    <v-container class="py-8" max-width="700">

        <!-- ══════════════════════════════════════
             SECTION 1 — Preuves à valider (mockées)
        ══════════════════════════════════════ -->
        <div class="mb-10">
            <div class="d-flex align-center mb-4 ga-3">
                <h2 class="text-h5 font-weight-bold">Preuves à valider</h2>
                <v-chip color="warning" variant="tonal" size="small">
                    {{ preuves.length }} en attente
                </v-chip>
            </div>

            <div v-if="preuves.length === 0" class="text-center text-medium-emphasis py-6">
                ✅ Aucune preuve en attente
            </div>

            <v-card v-for="preuve in preuves" :key="preuve.id" rounded="lg" border elevation="0" class="mb-3">
                <v-card-text class="pa-4">
                    <div class="d-flex align-center ga-3">
                        <v-sheet rounded="lg" width="44" height="44"
                            class="d-flex align-center justify-center flex-shrink-0" color="blue-grey-lighten-4">
                            <v-icon color="blue-grey-darken-2">mdi-file-image</v-icon>
                        </v-sheet>
                        <div class="flex-grow-1">
                            <div class="text-body-2 font-weight-semibold">{{ preuve.etudiant }}</div>
                            <div class="text-caption text-medium-emphasis">{{ preuve.action }}</div>
                            <div class="text-caption text-medium-emphasis">{{ preuve.fichier }}</div>
                        </div>
                        <v-chip :color="TYPE_COLORS[preuve.type]" variant="outlined" size="small" label
                            class="flex-shrink-0">
                            {{ preuve.type }}
                        </v-chip>
                        <div class="d-flex ga-2 flex-shrink-0">
                            <v-btn variant="text" size="small" icon @click="voirFichier(preuve)">
                                <v-icon>mdi-eye</v-icon>
                                <v-tooltip activator="parent">Voir le fichier</v-tooltip>
                            </v-btn>
                            <v-btn color="success" variant="tonal" size="small" rounded="xl"
                                @click="validerPreuve(preuve.id)">
                                <v-icon class="mr-1" size="16">mdi-check</v-icon>
                                Valider
                            </v-btn>
                        </div>
                    </div>
                </v-card-text>
            </v-card>
        </div>

        <v-divider class="mb-10" />

        <!-- ══════════════════════════════════════
             SECTION 2 — Suivi d'activités
        ══════════════════════════════════════ -->
        <h1 class="text-h4 font-weight-bold text-center mb-6">Suivi d'activités</h1>

        <!-- Chargement -->
        <div v-if="loading" class="d-flex justify-center py-12">
            <v-progress-circular indeterminate color="primary" />
        </div>

        <template v-else>
            <!-- Barre de recherche -->
            <v-text-field v-model="recherche" placeholder="Recherche" variant="outlined" rounded="xl"
                density="comfortable" prepend-inner-icon="mdi-magnify" clearable hide-details class="mb-6 mx-auto"
                style="max-width: 400px;" />

            <!-- Liste étudiants -->
            <v-list lines="one" rounded="lg" border class="mb-6">
                <template v-if="etudiantsFiltres.length > 0">
                    <v-list-item v-for="etudiant in etudiantsFiltres" :key="etudiant.id"
                        :title="`${etudiant.prenom} ${etudiant.nom}`"
                        :subtitle="`${etudiant.actions.length} action${etudiant.actions.length > 1 ? 's' : ''}`"
                        :active="etudiantSelec?.id === etudiant.id" base-color="bleu" @click="selectionner(etudiant)"
                        style="cursor: pointer;">
                        <template #prepend>
                            <v-avatar color="bleu" size="38">
                                <span class="text-caption font-weight-bold text-white">
                                    {{ etudiant.prenom[0] }}{{ etudiant.nom[0] }}
                                </span>
                            </v-avatar>
                        </template>
                        <template #append>
                            <v-icon>
                                {{ etudiantSelec?.id === etudiant.id ? 'mdi-chevron-up' : 'mdi-chevron-down' }}
                            </v-icon>
                        </template>
                    </v-list-item>
                </template>
                <v-list-item v-else>
                    <v-list-item-title class="text-center text-medium-emphasis py-4">
                        Aucun résultat pour "{{ recherche }}"
                    </v-list-item-title>
                </v-list-item>
            </v-list>

            <!-- Détail étudiant -->
            <v-expand-transition>
                <div v-if="etudiantSelec">
                    <v-card rounded="xl" border elevation="0">
                        <v-card-title class="pa-5 pb-2 d-flex align-center ga-3">
                            <v-avatar color="bleu" size="44">
                                <span class="text-body-2 font-weight-bold text-white">
                                    {{ etudiantSelec.prenom[0] }}{{ etudiantSelec.nom[0] }}
                                </span>
                            </v-avatar>
                            <div>
                                <div class="font-weight-bold text-h6">
                                    {{ etudiantSelec.prenom }} {{ etudiantSelec.nom }}
                                </div>
                                <div class="text-caption text-medium-emphasis">
                                    {{ etudiantSelec.actions.length }} action{{ etudiantSelec.actions.length > 1 ? 's' :
                                        '' }}
                                    réalisée{{ etudiantSelec.actions.length > 1 ? 's' : '' }}
                                </div>
                            </div>
                        </v-card-title>
                        <v-divider class="mx-5" />
                        <v-card-text class="pa-5">
                            <div v-if="etudiantSelec.actions.length === 0"
                                class="text-center text-medium-emphasis py-4">
                                Aucune action réalisée
                            </div>
                            <v-list v-else lines="one" rounded="lg" border>
                                <v-list-item v-for="action in etudiantSelec.actions" :key="action.id"
                                    :title="action.titre" :subtitle="action.date">
                                    <template #prepend>
                                        <v-chip :color="TYPE_COLORS[action.type]" variant="outlined" size="small" label
                                            class="mr-3" style="min-width: 130px; justify-content: center;">
                                            {{ action.type }}
                                        </v-chip>
                                    </template>
                                </v-list-item>
                            </v-list>
                        </v-card-text>
                    </v-card>
                </div>
            </v-expand-transition>
        </template>

    </v-container>

    <!-- ── Dialog preview fichier ── -->
    <v-dialog v-model="previewDialog" max-width="500">
        <v-card v-if="previewFichier" rounded="xl">
            <v-card-title class="pa-5 pb-2 font-weight-bold">
                {{ previewFichier.fichier }}
            </v-card-title>
            <v-card-text class="pa-5 pt-2 text-center">
                <v-sheet rounded="lg" color="grey-lighten-3" height="250" class="d-flex align-center justify-center">
                    <div class="text-center text-medium-emphasis">
                        <v-icon size="48" class="mb-2">mdi-file-image</v-icon>
                        <div class="text-body-2">Aperçu du fichier</div>
                        <div class="text-caption">{{ previewFichier.fichier }}</div>
                    </div>
                </v-sheet>
            </v-card-text>
            <v-card-actions class="pa-5 pt-0">
                <v-spacer />
                <v-btn color="bleu" variant="flat" rounded="xl" @click="previewDialog = false">
                    Fermer
                </v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>