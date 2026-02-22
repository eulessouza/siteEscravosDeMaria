import React, { useState } from 'react'
import { useAuth } from '../context/AuthContext'

/**
 * Hook customizado para fazer requisições autenticadas com token JWT
 * Uso:
 *   const { data, loading, error, fetchData } = useFetch()
 *   fetchData('/posts')
 */
export function useFetch() {
  const { user } = useAuth()
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const fetchData = async (endpoint, options = {}) => {
    try {
      setLoading(true)
      setError(null)

      const token = localStorage.getItem('discord_token')
      const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
      }

      if (token) {
        headers.Authorization = `Bearer ${token}`
      }

      const response = await fetch(endpoint, {
        ...options,
        headers,
      })

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }

      const json = await response.json()
      setData(json)
      return json
    } catch (err) {
      setError(err.message)
      console.error(`Erro em ${endpoint}:`, err)
      throw err
    } finally {
      setLoading(false)
    }
  }

  return { data, loading, error, fetchData }
}

export default useFetch
