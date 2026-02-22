import React, { createContext, useContext, useState, useEffect } from 'react'
import { API_ENDPOINTS } from '../config/api'
import { ADMIN_ROLES, MODERATOR_ROLES } from '../config/roles'
import { getDiscordAuthUrl } from '../services/authService'

const AuthContext = createContext()

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth deve ser usado dentro de AuthProvider')
  }
  return context
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  // Verifica se o usuário já está autenticado (via JWT no localStorage)
  useEffect(() => {
    const checkAuth = async () => {
      try {
        const token = localStorage.getItem('discord_token')
        if (!token) {
          setLoading(false)
          return
        }

        const response = await fetch(API_ENDPOINTS.auth.me, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        })

        if (response.ok) {
          const userData = await response.json()
          setUser(userData)
        } else if (response.status === 401) {
          // Token expirado ou inválido
          localStorage.removeItem('discord_token')
          setUser(null)
        }
      } catch (err) {
        console.error('Erro ao verificar autenticação:', err)
        setError(err.message)
      } finally {
        setLoading(false)
      }
    }

    checkAuth()
  }, [])

  // Inicia o login com Discord (usa serviço que retorna a authUrl)
  const loginWithDiscord = async () => {
    try {
      setLoading(true)
      const authUrl = await getDiscordAuthUrl()
      if (authUrl) {
        window.location.href = authUrl
      } else {
        throw new Error('Não foi possível obter URL de autenticação do Discord')
      }
    } catch (err) {
      console.error('Erro ao iniciar login Discord:', err)
      setError(err.message)
      alert('Erro ao iniciar autenticação: ' + (err.message || err))
    } finally {
      setLoading(false)
    }
  }

  // Processa o callback do Discord (chamado após redirecionamento)
  const handleDiscordCallback = async (code) => {
    try {
      setLoading(true)
      const response = await fetch(`${API_ENDPOINTS.auth.discordCallback}?code=${code}`)
      const data = await response.json()

      if (data.token) {
        localStorage.setItem('discord_token', data.token)
        // try to use returned user, otherwise fetch /auth/me
        if (data.user) {
          setUser(data.user)
        } else {
          // fetch user info from /auth/me
          try {
            const token = data.token
            const meRes = await fetch(API_ENDPOINTS.auth.me, { headers: { Authorization: `Bearer ${token}` } })
            if (meRes.ok) {
              const me = await meRes.json()
              setUser(me)
            }
          } catch (e) {
            console.warn('Não foi possível buscar /auth/me após callback:', e)
          }
        }
        return true
      } else {
        throw new Error('Falha ao obter token')
      }
    } catch (err) {
      console.error('Erro ao processar callback Discord:', err)
      setError(err.message)
      return false
    } finally {
      setLoading(false)
    }
  }

  // Faz logout
  const logout = () => {
    localStorage.removeItem('discord_token')
    setUser(null)
  }

  // refresh user data from /auth/me
  const refreshUser = async () => {
    const token = localStorage.getItem('discord_token')
    if (!token) return null
    try {
      const res = await fetch(API_ENDPOINTS.auth.me, { headers: { Authorization: `Bearer ${token}` } })
      if (res.ok) {
        const u = await res.json()
        setUser(u)
        return u
      }
    } catch (err) {
      console.warn('Erro ao atualizar usuário:', err)
    }
    return null
  }

  const hasRole = (roleId) => {
    if (!user || !Array.isArray(user.roles)) return false
    return user.roles.includes(roleId)
  }

  const hasAnyRole = (roleIds = []) => {
    if (!user || !Array.isArray(user.roles)) return false
    return roleIds.some((r) => user.roles.includes(r))
  }

  const isAdmin = () => {
    return hasAnyRole(ADMIN_ROLES)
  }

  const isModerator = () => {
    return hasAnyRole(MODERATOR_ROLES) || isAdmin()
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        error,
        isAuthenticated: !!user,
        loginWithDiscord,
        handleDiscordCallback,
        logout,
        refreshUser,
        hasRole,
        hasAnyRole,
        isAdmin,
        isModerator,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export default AuthContext
