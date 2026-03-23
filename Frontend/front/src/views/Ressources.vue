<script setup>
import { ref, onMounted } from 'vue'
import { useAuth } from '../composables/useAuth.js'

const { authHeaders, estConnecte } = useAuth()

const API_BASE = 'https://api-ptut.up.railway.app'

const fichiers = ref([])
const loading = ref(true)

function formatTaille(bytes) {
    if (!bytes) return ''
    if (bytes < 1024) return `${bytes} o`
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} Ko`
    return `${(bytes / (1024 * 1024)).toFixed(1)} Mo`
}

function typeFromContentType(contentType) {
    if (!contentType) return 'FICHIER'
    if (contentType.includes('pdf')) return 'PDF'
    if (contentType.includes('presentation') || contentType.includes('powerpoint')) return 'PPTX'
    if (contentType.includes('zip')) return 'ZIP'
    if (contentType.includes('word')) return 'DOCX'
    return 'FICHIER'
}

const ICON_COLORS = {
    'PDF': '#E53935',
    'PPTX': '#FB8C00',
    'ZIP': '#8E24AA',
    'DOCX': '#1E88E5',
    'FICHIER': '#546E7A',
}

function iconColor(type) {
    return ICON_COLORS[type] ?? '#546E7A'
}

onMounted(async () => {
    try {
        const res = await fetch(`${API_BASE}/resources`, {
            headers: estConnecte.value ? authHeaders() : {}
        })
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        const data = await res.json()
        fichiers.value = data.map(f => ({
            id: f.id,
            nom: f.fileName,
            type: typeFromContentType(f.contentType),
            taille: formatTaille(f.size),
            uploadedBy: f.uploadedBy,
            uploadedAt: f.uploadedAt
        }))
    } catch (e) {
        console.log('Erreur API resources:', e.message)
        // Données mockées si l'API échoue
        fichiers.value = [
            { id: 1, nom: "Guide de l'Ambassadeur", type: 'PDF', taille: '2.4 Mo' },
            { id: 2, nom: 'Attestation de présence', type: 'PDF', taille: '2.4 Mo' },
            { id: 3, nom: 'Présentation ISIS (Lycées)', type: 'PPTX', taille: '2.4 Mo' },
            { id: 4, nom: 'Pack Logo ISIS', type: 'ZIP', taille: '2.4 Mo' },
            { id: 5, nom: 'Flyer JPOISIS', type: 'PDF', taille: '2.4 Mo' },
        ]
    } finally {
        loading.value = false
    }
})

async function telecharger(fichier) {
    try {
        const res = await fetch(`${API_BASE}/resources/${fichier.id}/download`, {
            headers: authHeaders()
        })
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        const blob = await res.blob()
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = fichier.nom
        a.click()
        URL.revokeObjectURL(url)
    } catch (e) {
        console.log('Erreur téléchargement:', e.message)
    }
}
</script>

<template>
    <v-container class="py-8">

        <h1 class="text-h4 font-weight-bold mb-6">Bibliothèque de documents</h1>

        <!-- ── Chargement ── -->
        <div v-if="loading" class="d-flex justify-center py-12">
            <v-progress-circular indeterminate color="primary" />
        </div>

        <template v-else>
            <!-- Aucun fichier -->
            <v-alert v-if="fichiers.length === 0" type="info" variant="tonal">
                Aucun document disponible pour le moment.
            </v-alert>

            <v-row v-else>
                <v-col v-for="fichier in fichiers" :key="fichier.id" cols="12" sm="6" md="4" lg="3">
                    <v-card height="100%" rounded="lg" color="#F4F6F9" class="pa-3">
                        <div class="d-flex align-center justify-space-between">

                            <div class="d-flex align-center">
                                <v-sheet rounded="lg" width="44" height="44"
                                    class="d-flex align-center justify-center flex-shrink-0"
                                    :color="iconColor(fichier.type)">
                                    <v-icon color="white" size="22">mdi-file-document</v-icon>
                                </v-sheet>

                                <div class="ml-3">
                                    <div class="text-body-2 font-weight-medium" style="line-height: 1.2">
                                        {{ fichier.nom }}
                                    </div>
                                    <div class="text-caption text-medium-emphasis">
                                        {{ fichier.type }}<span v-if="fichier.taille"> · {{ fichier.taille }}</span>
                                    </div>
                                </div>
                            </div>

                            <v-btn icon variant="text" size="small" @click="telecharger(fichier)">
                                <v-icon size="20">mdi-download</v-icon>
                                <v-tooltip activator="parent">Télécharger</v-tooltip>
                            </v-btn>
                        </div>
                    </v-card>
                </v-col>
            </v-row>
        </template>

    </v-container>
</template>