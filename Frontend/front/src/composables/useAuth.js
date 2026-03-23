import { ref, computed } from 'vue'

const API_BASE = 'https://api-ptut.up.railway.app'

// ── État global ──
const utilisateur = ref(null)
const token = ref(null)

const estConnecte = computed(() => utilisateur.value !== null)
const estAdmin = computed(() => utilisateur.value?.role === 'ADMIN')
const estEtudiant = computed(() => utilisateur.value?.role === 'AMBASSADEUR')

// ── Connexion ──
async function login(email, password) {
    const res = await fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    })

    if (!res.ok) {
        const err = await res.json().catch(() => ({}))
        throw new Error(err.message || 'Identifiant ou mot de passe incorrect')
    }
    const data = await res.json()
    token.value = data.token
    utilisateur.value = {
        email: data.email,
        role: data.role,
        // nom/prenom à récupérer via GET /auth/me si besoin
        prenom: data.prenom ?? data.email,
        nom: data.nom ?? ''
    }

    return utilisateur.value
}

// ── Récupérer l'utilisateur connecté (GET /auth/me) ──
async function fetchMe() {
    if (!token.value) return

    const res = await fetch(`${API_BASE}/auth/me`, {
        headers: { 'Authorization': `Bearer ${token.value}` }
    })

    if (!res.ok) return

    const data = await res.json()
    utilisateur.value = {
        ...utilisateur.value,
        prenom: data.prenom ?? utilisateur.value.prenom,
        nom: data.nom ?? utilisateur.value.nom,
        email: data.email ?? utilisateur.value.email,
        role: data.role ?? utilisateur.value.role,
    }
}

// ── Déconnexion ──
function logout() {
    utilisateur.value = null
    token.value = null
}

// ── Header auth pour les requêtes protégées ──
function authHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token.value}`
    }
}

export function useAuth() {
    return {
        utilisateur,
        token,
        estConnecte,
        estAdmin,
        login,
        fetchMe,
        logout,
        authHeaders
    }
}