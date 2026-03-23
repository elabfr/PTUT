<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuth } from '../composables/useAuth.js'

const { authHeaders } = useAuth()
const API_BASE = 'https://api-ptut.up.railway.app'

const loading = ref(true)

// ── Données (mockées en attendant GET /statistiques) ──
const stats = ref({
    global: {
        totalActions: 24,
        totalAmbassadeurs: 12,
        totalPreuvesValidees: 18,
        totalPreuvesEnAttente: 4
    },
    parType: [
        { type: 'SALON ÉTUDIANT', total: 8, validees: 6, enCours: 1, aVenir: 1, couleur: '#0D47A1' },
        { type: 'LYCÉE', total: 6, validees: 4, enCours: 2, aVenir: 0, couleur: '#856404' },
        { type: 'RÉSEAUX SOCIAUX', total: 5, validees: 3, enCours: 1, aVenir: 1, couleur: '#7B1FA2' },
        { type: 'FORMATION', total: 5, validees: 5, enCours: 0, aVenir: 0, couleur: '#3CBEBE' },
    ],
    parMois: [
        { mois: 'Jan', actions: 4 },
        { mois: 'Fév', actions: 7 },
        { mois: 'Mar', actions: 3 },
        { mois: 'Avr', actions: 6 },
        { mois: 'Mai', actions: 2 },
        { mois: 'Juin', actions: 2 },
    ]
})

onMounted(async () => {
    try {
        const res = await fetch(`${API_BASE}/statistiques`, {
            headers: authHeaders()
        })
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        stats.value = await res.json()
    } catch (e) {
        console.log('Erreur API statistiques:', e.message)
        // Garde les données mockées
    } finally {
        loading.value = false
    }
})

// ── Calcul hauteur barres (max = 100%) ──
const maxMois = computed(() => Math.max(...stats.value.parMois.map(m => m.actions)))

function hauteurBarre(actions) {
    return `${(actions / maxMois.value) * 100}%`
}

// ── Calcul camembert (positions SVG) ──
const camembert = computed(() => {
    const total = stats.value.parType.reduce((s, t) => s + t.total, 0)
    let angle = 0
    return stats.value.parType.map(t => {
        const pct = t.total / total
        const start = angle
        angle += pct * 360
        const end = angle
        const r = 80
        const cx = 100
        const cy = 100
        const x1 = cx + r * Math.cos((start - 90) * Math.PI / 180)
        const y1 = cy + r * Math.sin((start - 90) * Math.PI / 180)
        const x2 = cx + r * Math.cos((end - 90) * Math.PI / 180)
        const y2 = cy + r * Math.sin((end - 90) * Math.PI / 180)
        const large = pct > 0.5 ? 1 : 0
        return {
            ...t,
            path: `M ${cx} ${cy} L ${x1} ${y1} A ${r} ${r} 0 ${large} 1 ${x2} ${y2} Z`,
            pct: Math.round(pct * 100)
        }
    })
})
</script>

<template>
    <v-container class="py-8" max-width="960">

        <h1 class="text-h4 font-weight-bold mb-8">Statistiques</h1>

        <div v-if="loading" class="d-flex justify-center py-12">
            <v-progress-circular indeterminate color="primary" />
        </div>

        <template v-else>

            <!-- ══════════════════════════════
                 SECTION 1 — Chiffres clés
            ══════════════════════════════ -->
            <v-row class="mb-8">
                <v-col cols="12" sm="6" md="3">
                    <v-card rounded="xl" border elevation="0" class="pa-5 text-center">
                        <div class="text-h3 font-weight-bold" style="color: rgb(var(--v-theme-bleu))">
                            {{ stats.global.totalActions }}
                        </div>
                        <div class="text-body-2 text-medium-emphasis mt-1">Actions au total</div>
                    </v-card>
                </v-col>
                <v-col cols="12" sm="6" md="3">
                    <v-card rounded="xl" border elevation="0" class="pa-5 text-center">
                        <div class="text-h3 font-weight-bold" style="color: rgb(var(--v-theme-vert))">
                            {{ stats.global.totalAmbassadeurs }}
                        </div>
                        <div class="text-body-2 text-medium-emphasis mt-1">Ambassadeurs</div>
                    </v-card>
                </v-col>
                <v-col cols="12" sm="6" md="3">
                    <v-card rounded="xl" border elevation="0" class="pa-5 text-center">
                        <div class="text-h3 font-weight-bold text-success">
                            {{ stats.global.totalPreuvesValidees }}
                        </div>
                        <div class="text-body-2 text-medium-emphasis mt-1">Preuves validées</div>
                    </v-card>
                </v-col>
                <v-col cols="12" sm="6" md="3">
                    <v-card rounded="xl" border elevation="0" class="pa-5 text-center">
                        <div class="text-h3 font-weight-bold text-warning">
                            {{ stats.global.totalPreuvesEnAttente }}
                        </div>
                        <div class="text-body-2 text-medium-emphasis mt-1">Preuves en attente</div>
                    </v-card>
                </v-col>
            </v-row>

            <!-- ══════════════════════════════
                 SECTION 2 — Par type + camembert
            ══════════════════════════════ -->
            <v-row class="mb-8">

                <!-- Tableau par type -->
                <v-col cols="12" md="7">
                    <v-card rounded="xl" border elevation="0" class="pa-5" height="100%">
                        <h2 class="text-h6 font-weight-bold mb-4">Répartition par type</h2>
                        <div v-for="t in stats.parType" :key="t.type" class="mb-4">
                            <div class="d-flex justify-space-between align-center mb-1">
                                <span class="text-body-2 font-weight-medium">{{ t.type }}</span>
                                <span class="text-caption text-medium-emphasis">{{ t.total }} actions</span>
                            </div>
                            <v-progress-linear :model-value="(t.validees / t.total) * 100" :color="t.couleur"
                                bg-color="grey-lighten-3" rounded height="10" />
                            <div class="d-flex ga-3 mt-1">
                                <span class="text-caption text-success">✓ {{ t.validees }} validées</span>
                                <span class="text-caption text-warning">⏳ {{ t.enCours }} en cours</span>
                                <span class="text-caption text-medium-emphasis">📅 {{ t.aVenir }} à venir</span>
                            </div>
                        </div>
                    </v-card>
                </v-col>

                <!-- Camembert SVG -->
                <v-col cols="12" md="5">
                    <v-card rounded="xl" border elevation="0" class="pa-5" height="100%">
                        <h2 class="text-h6 font-weight-bold mb-4">Répartition globale</h2>
                        <div class="d-flex flex-column align-center">
                            <svg viewBox="0 0 200 200" width="180" height="180">
                                <path v-for="slice in camembert" :key="slice.type" :d="slice.path" :fill="slice.couleur"
                                    stroke="white" stroke-width="2" />
                            </svg>
                            <div class="mt-3 w-100">
                                <div v-for="slice in camembert" :key="slice.type" class="d-flex align-center ga-2 mb-1">
                                    <div :style="`background:${slice.couleur}`"
                                        style="width:12px;height:12px;border-radius:3px;flex-shrink:0" />
                                    <span class="text-caption">{{ slice.type }}</span>
                                    <span class="text-caption text-medium-emphasis ml-auto">{{ slice.pct }}%</span>
                                </div>
                            </div>
                        </div>
                    </v-card>
                </v-col>

            </v-row>

            <!-- ══════════════════════════════
                 SECTION 3 — Progression par mois
            ══════════════════════════════ -->
            <v-card rounded="xl" border elevation="0" class="pa-5">
                <h2 class="text-h6 font-weight-bold mb-6">Progression par mois</h2>
                <div class="d-flex align-end ga-3" style="height: 160px;">
                    <div v-for="m in stats.parMois" :key="m.mois" class="d-flex flex-column align-center flex-grow-1"
                        style="height: 100%;">
                        <span class="text-caption text-medium-emphasis mb-1">{{ m.actions }}</span>
                        <div
                            :style="`height: ${hauteurBarre(m.actions)}; background: rgb(var(--v-theme-bleu)); border-radius: 6px 6px 0 0; width: 100%; transition: height 0.5s ease;`" />
                        <span class="text-caption mt-2 text-medium-emphasis">{{ m.mois }}</span>
                    </div>
                </div>
            </v-card>

        </template>

    </v-container>
</template>

<style scoped></style>